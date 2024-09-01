package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonObject.*;
import static com.abiddarris.common.renpy.internal.Struct.unpack;
import static com.abiddarris.common.renpy.internal.Sys.maxsize;
import static com.abiddarris.common.stream.InputStreams.readExact;
import static com.abiddarris.common.stream.InputStreams.readLine;
import static com.abiddarris.common.stream.Signs.sign;
import static com.abiddarris.common.stream.Signs.unsign;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.CodingErrorAction.REPORT;
import static java.util.Arrays.copyOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.abiddarris.common.annotations.PrivateApi;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrivateApi
public class Pickle {
    
    //This is the highest protocol number we know how to read.
    private static final int HIGHEST_PROTOCOL = 5;
    
    private static final int MARK           = '(';   // push special markobject on stack
    private static final int STOP           = '.';   // every pickle ends with STOP
    private static final int BININT         = 'J';   // push four-byte signed int
    private static final int BININT1        = 'K';   // push 1-byte unsigned int
    private static final int BININT2        = 'M';   // push 2-byte unsigned int
    private static final int NONE           = 'N';   // push None
    private static final int SHORT_BINSTRING= 'U';   // "     "   ;    "      "       "      " < 256 bytes
    private static final int BINUNICODE     = 'X';   //   "     "       "  ; counted UTF-8 string argument
    private static final int APPEND         = 'a';   // append stack top to list below it
    private static final int BUILD          = 'b';   // call __setstate__ or __dict__.update()
    private static final int GLOBAL         = 'c';   // push self.find_class(modname, name); 2 string args   
    private static final int EMPTY_DICT     = '}';   // push empty dict
    private static final int APPENDS        = 'e';   // extend list on stack by topmost stack slice
    private static final int BINGET         = 'h';   //   "    "    "    "   "   "  ;   "    " 1-byte arg
    private static final int LONG_BINGET    = 'j';   // push item from memo on stack; index is 4-byte arg
    private static final int EMPTY_LIST     = ']';   // push empty list
    private static final int BINPUT         = 'q';   //   "     "    "   "   " ;   "    " 1-byte arg
    private static final int LONG_BINPUT    = 'r';   //   "     "    "   "   " ;   "    " 4-byte arg
    private static final int TUPLE          = 't';   // build tuple from topmost stack items
    private static final int EMPTY_TUPLE    = ')';   // push empty tuple
    private static final int SETITEMS       = 'u';   // modify dict by adding topmost key+value pairs
    
    //Protocol 2
    private static final int PROTO          = 0x80;  // identify pickle protocol
    private static final int NEWOBJ         = 0x81;  // build object by applying cls.__new__ to argtuple
    private static final int TUPLE2         = 0x86;  // build 2-tuple from two topmost stack items
    private static final int TUPLE3         = 0x87;  // build 3-tuple from three topmost stack items    
    private static final int NEWTRUE        = 0x88;  // push True
    private static final int NEWFALSE       = 0x89;  // push False
    private static final int LONG1          = 0x8a;  // push long from < 256 bytes
    
    public static Object loads(InputStream stream) {
        return loads(stream, "ASCII");
    }
    
    public static Object loads(InputStream stream, String encoding) {
        return loads(stream, true, encoding, "strict");
    }

    public static Object loads(
            InputStream stream, boolean fix_imports, String encoding, String errors /*buffers=None*/) {
        return new Unpickler(stream, fix_imports, /* buffers=buffers,*/
                      encoding, errors).load();
    }
    
    /**
     * A common base class for the other pickling exceptions.
     */
    public static class PickleError extends RuntimeException {
        
        public PickleError(String message) {
            super(message);
        }
    }
    
    /**
     * This exception is raised when there is a problem unpickling an object,
     * such as a security violation.
     *
     * <p>Note that other exceptions may also be raised during unpickling, including
     * (but not necessarily limited to) AttributeError, EOFError, ImportError,
     * and IndexError.
     */
    public static class UnpicklingError extends PickleError {
       
        public UnpicklingError(String message) {
            super(message);
        }
    }

    /**
     * Decode a long from a two's complement little-endian binary string.
     *
     * <p>>>> decode_long(b'') 0 
     * <p>>>> decode_long(b"\xff\x00") 255 
     * <p>>>> decode_long(b"\xff\x7f") 32767
     * <p>>>> decode_long(b"\x00\xff") -256 
     * <p>>>> decode_long(b"\x00\x80") -32768 
     * <p>>>> decode_long(b"\x80") -128
     * <p>>>> decode_long(b"\x7f") 127
     */
    private static int decode_long(int[] data) {
        if(data.length > 4) {
            throw new IllegalArgumentException("Not gonna bother with this " + Arrays.toString(data));
        }
        ByteBuffer buffer = ByteBuffer.wrap(sign(data));
        buffer.order(LITTLE_ENDIAN);
        
        return buffer.getInt();
    }
   
    public static class Unpickler {

        private InputStream stream;
        private String encoding;
        private String errors;
        
        private List<Object> metastack = new ArrayList<>();
        private List<Object> stack = new ArrayList<>();
        private Map<Object, Object> memo = new HashMap<>();
        private int proto;
        private Unframer unframer;
        private Map<Integer, Runnable> dispatch = new HashMap<>();

        /**
         * This takes a binary file for reading a pickle data stream.
         *
         * <p>The protocol version of the pickle is detected automatically, so no proto argument is
         * needed.
         *
         * <p>The argument *file* must have two methods, a read() method that takes an integer
         * argument, and a readline() method that requires no arguments. Both methods should return
         * bytes. Thus *file* can be a binary file object opened for reading, an io.BytesIO object,
         * or any other custom object that meets this interface.
         *
         * <p>The file-like object must have two methods, a read() method that takes an integer
         * argument, and a readline() method that requires no arguments. Both methods should return
         * bytes. Thus file-like object can be a binary file object opened for reading, a BytesIO
         * object, or any other custom object that meets this interface.
         *
         * <p>If *buffers* is not None, it should be an iterable of buffer-enabled objects that is
         * consumed each time the pickle stream references an out-of-band buffer view. Such buffers
         * have been given in order to the *buffer_callback* of a Pickler object.
         *
         * <p>If *buffers* is None (the default), then the buffers are taken from the pickle stream,
         * assuming they are serialized there. It is an error for *buffers* to be None if the pickle
         * stream was produced with a non-None *buffer_callback*.
         *
         * <p>Other optional arguments are *fix_imports*, *encoding* and errors*, which are used to
         * control compatibility support for pickle stream generated by Python 2. If *fix_imports*
         * is True, pickle will try to map the old Python 2 names to the new names used in Python 3.
         * The *encoding* and *errors* tell pickle how to decode 8-bit string instances pickled by
         * Python 2; these default to 'ASCII' and 'strict', respectively. *encoding* can be 'bytes'
         * to read these 8-bit string instances as bytes objects.
         */
        public Unpickler(InputStream stream, boolean fix_imports,
                 String encoding, String errors/*, buffers=None*/) {
            this.stream = stream;
            this.encoding = encoding;
            this.errors = errors;
                 
            dispatch.put(PROTO, this::load_proto);
            dispatch.put(EMPTY_DICT, this::load_empty_dictionary);
            dispatch.put(BINPUT, this::load_binput);
            dispatch.put(MARK, this::load_mark);
            dispatch.put(BINUNICODE, this::load_binunicode);
            dispatch.put(EMPTY_LIST, this::load_empty_list);
            dispatch.put(LONG1, this::load_long1);
            dispatch.put(LONG_BINGET, this::load_long_binget);
            dispatch.put(BININT, this::load_binint);
            dispatch.put(SHORT_BINSTRING, this::load_short_binstring);
            dispatch.put(TUPLE3, this::load_tuple3);
            dispatch.put(APPEND, this::load_append);
            dispatch.put(STOP, this::load_stop);
            dispatch.put(SETITEMS, this::load_setitems);
            dispatch.put(GLOBAL, this::load_global);
            dispatch.put(EMPTY_TUPLE, this::load_empty_tuple);
            dispatch.put(NEWOBJ, this::load_newobj);
            dispatch.put(NONE, this::load_none);
            dispatch.put(BININT1, this::load_binint1);
            dispatch.put(BINGET, this::load_binget);
            dispatch.put(TUPLE2, this::load_tuple2);
            dispatch.put(BUILD, this::load_build);
            dispatch.put(TUPLE, this::load_tuple);
            dispatch.put(NEWFALSE, this::load_false);
            dispatch.put(NEWTRUE, this::load_true);
            dispatch.put(APPENDS, this::load_appends);
            dispatch.put(LONG_BINPUT, this::load_long_binput);
            dispatch.put(BININT2, this::load_binint2);

            /*self._buffers = iter(buffers) if buffers is not None else None
            self.memo = {}
            
            self.proto = 0
            self.fix_imports = fix_imports*/
        }
        
        private int[] _file_read(int n) {
            try {
                return unsign(readExact(stream, n));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        
        private int[] _file_readline() {
            try {
                byte[] b = readLine(stream);
                b = copyOf(b, b.length + 1);
                b[b.length - 1] = '\n';
                return unsign(b);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        
        private CodingErrorAction getError(String error) {
            if(error.equals("strict")) {
                return REPORT;
            }
            throw new IllegalStateException("unsupported error handling : " + error);
        }

        private int[] read(int n) {
            return unframer.read(n);
        }

        private int[] readline() {
            return unframer.readline();
        }
        
        private void append(Object obj) {
            stack.add(obj);
        }

        /*
         * Read a pickled object representation from the open file.
         *
         * Return the reconstituted object hierarchy specified in the file.
         */
        public Object load() {
            unframer = new Unframer(this);
            /*
            self.readinto = self._unframer.readinto
            
            dispatch = self.dispatch*/
            try {
                while (true) {
                    int[] key = read(1);
                    if (key.length == 0)
                        throw new UncheckedIOException(new EOFException());
                    
                    Runnable runnable = dispatch.get(key[0]);
                    if(runnable == null) {
                        throw new IllegalArgumentException(key[0] + " not supported");
                    }
                    runnable.run();
                }
            } catch (_Stop stopinst) {
                return stopinst.value;
            }
        }

        /**
         * Return a list of items pushed in the stack after last MARK instruction.
         */
        private List<Object> pop_mark() {
            List<Object> items = this.stack;
            this.stack = (List)this.metastack.remove(this.metastack.size() - 1);
            
            return items;
        }
        
        /*
        def persistent_load(self, pid):
            raise UnpicklingError("unsupported persistent id encountered")
        */

        private void load_proto() {
            int proto = this.read(1)[0];
            if(!(0 <= proto && proto <= HIGHEST_PROTOCOL)) {
                throw new IllegalArgumentException(
                    String.format("unsupported pickle protocol: %d", proto));
            }
                
            this.proto = proto;
        }
        

        /*def load_frame(self):
            frame_size, = unpack('<Q', self.read(8))
            if frame_size > sys.maxsize:
                raise ValueError("frame size > sys.maxsize: %d" % frame_size)
            self._unframer.load_frame(frame_size)
        dispatch[FRAME[0]] = load_frame

        def load_persid(self):
            try:
                pid = self.readline()[:-1].decode("ascii")
            except UnicodeDecodeError:
                raise UnpicklingError(
                    "persistent IDs in protocol 0 must be ASCII strings")
            self.append(self.persistent_load(pid))
        dispatch[PERSID[0]] = load_persid

        def load_binpersid(self):
            pid = self.stack.pop()
            self.append(self.persistent_load(pid))
        dispatch[BINPERSID[0]] = load_binpersid
        */

        protected void load_none() {
            this.append(None);
        }
        
        protected void load_false() {
            this.append(False);
        }
        
        protected void load_true() {
            this.append(True);
        }
        /*
        def load_int(self):
            data = self.readline()
            if data == FALSE[1:]:
                val = False
            elif data == TRUE[1:]:
                val = True
            else:
                val = int(data, 0)
            self.append(val)
        dispatch[INT[0]] = load_int
        */
        
        private void load_binint() {
            // FIXME: Integer overflow lol
            this.append(newInt(unpack("<i", this.read(4))[0].intValue()));
        }
        
        protected void load_binint1() {
            this.append(newInt(this.read(1)[0]));
        }
        
        protected void load_binint2() {
            this.append(newInt(
                    unpack("<H", this.read(2))[0].intValue()));
        }
        
        /*
        def load_long(self):
            val = self.readline()[:-1]
            if val and val[-1] == b'L'[0]:
                val = val[:-1]
            self.append(int(val, 0))
        dispatch[LONG[0]] = load_long
        */
        
        private void load_long1() {
            int n = this.read(1)[0];
            int[] data = this.read(n);
            this.append(decode_long(data));
        }
        
        /*
        def load_long4(self):
            n, = unpack('<i', self.read(4))
            if n < 0:
                # Corrupt or hostile pickle -- we never write one like this
                raise UnpicklingError("LONG pickle has negative byte count")
            data = self.read(n)
            self.append(decode_long(data))
        dispatch[LONG4[0]] = load_long4

        def load_float(self):
            self.append(float(self.readline()[:-1]))
        dispatch[FLOAT[0]] = load_float

        def load_binfloat(self):
            self.append(unpack('>d', self.read(8))[0])
        dispatch[BINFLOAT[0]] = load_binfloat
        */

        private Object _decode_string(int[] value) {
            // Used to allow strings from Python 2 to be decoded either as
            // bytes or Unicode strings.  This should be used only with the
            // STRING, BINSTRING and SHORT_BINSTRING opcodes.
            if (this.encoding.equals("bytes"))
                return value;
            
            CharsetDecoder decoder = Charset.forName(this.encoding)
                .newDecoder();
            decoder.onMalformedInput(getError(this.errors));
                
            try {
                return newString(decoder.decode(
                    ByteBuffer.wrap(sign(value))
                ).toString());
            } catch (CharacterCodingException e) {
                throw new RuntimeException(e);
            }
        }
       /*
        def load_string(self):
            data = self.readline()[:-1]
            # Strip outermost quotes
            if len(data) >= 2 and data[0] == data[-1] and data[0] in b'"\'':
                data = data[1:-1]
            else:
                raise UnpicklingError("the STRING opcode argument must be quoted")
            self.append(self._decode_string(codecs.escape_decode(data)[0]))
        dispatch[STRING[0]] = load_string

        def load_binstring(self):
            # Deprecated BINSTRING uses signed 32-bit length
            len, = unpack('<i', self.read(4))
            if len < 0:
                raise UnpicklingError("BINSTRING pickle has negative byte count")
            data = self.read(len)
            self.append(self._decode_string(data))
        dispatch[BINSTRING[0]] = load_binstring

        def load_binbytes(self):
            len, = unpack('<I', self.read(4))
            if len > maxsize:
                raise UnpicklingError("BINBYTES exceeds system's maximum size "
                                      "of %d bytes" % maxsize)
            self.append(self.read(len))
        dispatch[BINBYTES[0]] = load_binbytes

        def load_unicode(self):
            self.append(str(self.readline()[:-1], 'raw-unicode-escape'))
        dispatch[UNICODE[0]] = load_unicode
        */
        
        private void load_binunicode() {
            int len = unpack("<I", this.read(4))[0].intValue();
            if (len > maxsize)
                throw new UnpicklingError(
                    String.format("BINUNICODE exceeds system's maximum size " +
                                      "of %d bytes", maxsize));
            this.append(newString(
                new String(
                    sign(this.read(len)),
                    StandardCharsets.UTF_8
                )
            ));
        }
        
        /*def load_binunicode8(self):
            len, = unpack('<Q', self.read(8))
            if len > maxsize:
                raise UnpicklingError("BINUNICODE8 exceeds system's maximum size "
                                      "of %d bytes" % maxsize)
            self.append(str(self.read(len), 'utf-8', 'surrogatepass'))
        dispatch[BINUNICODE8[0]] = load_binunicode8

        def load_binbytes8(self):
            len, = unpack('<Q', self.read(8))
            if len > maxsize:
                raise UnpicklingError("BINBYTES8 exceeds system's maximum size "
                                      "of %d bytes" % maxsize)
            self.append(self.read(len))
        dispatch[BINBYTES8[0]] = load_binbytes8

        def load_bytearray8(self):
            len, = unpack('<Q', self.read(8))
            if len > maxsize:
                raise UnpicklingError("BYTEARRAY8 exceeds system's maximum size "
                                      "of %d bytes" % maxsize)
            b = bytearray(len)
            self.readinto(b)
            self.append(b)
        dispatch[BYTEARRAY8[0]] = load_bytearray8

        def load_next_buffer(self):
            if self._buffers is None:
                raise UnpicklingError("pickle stream refers to out-of-band data "
                                      "but no *buffers* argument was given")
            try:
                buf = next(self._buffers)
            except StopIteration:
                raise UnpicklingError("not enough out-of-band buffers")
            self.append(buf)
        dispatch[NEXT_BUFFER[0]] = load_next_buffer

        def load_readonly_buffer(self):
            buf = self.stack[-1]
            with memoryview(buf) as m:
                if not m.readonly:
                    self.stack[-1] = m.toreadonly()
        dispatch[READONLY_BUFFER[0]] = load_readonly_buffer
        */
        private void load_short_binstring() {
            int len = this.read(1)[0];
            int[] data = this.read(len);
            this.append(this._decode_string(data));
        }
        
        /*
        def load_short_binbytes(self):
            len = self.read(1)[0]
            self.append(self.read(len))
        dispatch[SHORT_BINBYTES[0]] = load_short_binbytes

        def load_short_binunicode(self):
            len = self.read(1)[0]
            self.append(str(self.read(len), 'utf-8', 'surrogatepass'))
        dispatch[SHORT_BINUNICODE[0]] = load_short_binunicode
        */
        
        private void load_tuple() {
            List items = this.pop_mark();
            
            this.append(newTuple((PythonObject[])items.toArray(PythonObject[]::new)));
        }
        
        public void load_empty_tuple() {
            this.append(newTuple());
        }
        
        /*
        def load_tuple1(self):
            self.stack[-1] = (self.stack[-1],)
        dispatch[TUPLE1[0]] = load_tuple1
        */
        
        private void load_tuple2() {
            int stackSize = this.stack.size();
         
            PythonObject o2 = (PythonObject)this.stack.remove(stackSize - 1);
            PythonObject o = (PythonObject)this.stack.remove(stackSize - 2);
            
            this.stack.add(newTuple(o, o2));
        }
        
        private void load_tuple3() {
            int stackSize = this.stack.size();
         
            PythonObject o3 = (PythonObject)this.stack.remove(stackSize - 1);
            PythonObject o2 = (PythonObject)this.stack.remove(stackSize - 2);
            PythonObject o1 = (PythonObject)this.stack.remove(stackSize - 3);
            
            PythonObject tuple3 = newTuple(o1, o2, o3);
            this.stack.add(tuple3);
        }
        
        
        private void load_empty_list() {
            this.append(newList());
        }
        
        private void load_empty_dictionary() {
            this.append(newDict());
        }
            
        /*def load_empty_set(self):
            self.append(set())
        dispatch[EMPTY_SET[0]] = load_empty_set

        def load_frozenset(self):
            items = self.pop_mark()
            self.append(frozenset(items))
        dispatch[FROZENSET[0]] = load_frozenset

        def load_list(self):
            items = self.pop_mark()
            self.append(items)
        dispatch[LIST[0]] = load_list

        def load_dict(self):
            items = self.pop_mark()
            d = {items[i]: items[i+1]
                 for i in range(0, len(items), 2)}
            self.append(d)
        dispatch[DICT[0]] = load_dict

        # INST and OBJ differ only in how they get a class object.  It's not
        # only sensible to do the rest in a common routine, the two routines
        # previously diverged and grew different bugs.
        # klass is the class to instantiate, and k points to the topmost mark
        # object, following which are the arguments for klass.__init__.
        def _instantiate(self, klass, args):
            if (args or not isinstance(klass, type) or
                hasattr(klass, "__getinitargs__")):
                try:
                    value = klass(*args)
                except TypeError as err:
                    raise TypeError("in constructor for %s: %s" %
                                    (klass.__name__, str(err)), sys.exc_info()[2])
            else:
                value = klass.__new__(klass)
            self.append(value)

        def load_inst(self):
            module = self.readline()[:-1].decode("ascii")
            name = self.readline()[:-1].decode("ascii")
            klass = self.find_class(module, name)
            self._instantiate(klass, self.pop_mark())
        dispatch[INST[0]] = load_inst

        def load_obj(self):
            # Stack is ... markobject classobject arg1 arg2 ...
            args = self.pop_mark()
            cls = args.pop(0)
            self._instantiate(cls, args)
        dispatch[OBJ[0]] = load_obj
        */
        
        public void load_newobj() {
            PythonObject args = (PythonObject) this.stack.remove(this.stack.size() - 1);
            PythonObject cls = (PythonObject)this.stack.remove(this.stack.size() - 1);
            
            PythonObject obj = cls.callAttribute("__new__", new PythonArgument(cls)
                .addPositionalArguments(args));
            this.append(obj);
        }
        
        /*
        def load_newobj_ex(self):
            kwargs = self.stack.pop()
            args = self.stack.pop()
            cls = self.stack.pop()
            obj = cls.__new__(cls, *args, **kwargs)
            self.append(obj)
        dispatch[NEWOBJ_EX[0]] = load_newobj_ex
        */
        protected void load_global() {
            Charset charset = Charset.forName("UTF-8");
            
            int[] moduleBytes = this.readline();
            moduleBytes = copyOf(moduleBytes, moduleBytes.length - 1);
            
            int[] nameBytes = this.readline();
            nameBytes = copyOf(nameBytes, nameBytes.length - 1);
            
            String module = new String(sign(moduleBytes), charset);
            String name = new String(sign(nameBytes), charset);
            PythonObject klass = this.find_class(module, name);
            this.append(klass);
        }
        
        /*
        def load_stack_global(self):
            name = self.stack.pop()
            module = self.stack.pop()
            if type(name) is not str or type(module) is not str:
                raise UnpicklingError("STACK_GLOBAL requires str")
            self.append(self.find_class(module, name))
        dispatch[STACK_GLOBAL[0]] = load_stack_global

        def load_ext1(self):
            code = self.read(1)[0]
            self.get_extension(code)
        dispatch[EXT1[0]] = load_ext1

        def load_ext2(self):
            code, = unpack('<H', self.read(2))
            self.get_extension(code)
        dispatch[EXT2[0]] = load_ext2

        def load_ext4(self):
            code, = unpack('<i', self.read(4))
            self.get_extension(code)
        dispatch[EXT4[0]] = load_ext4

        def get_extension(self, code):
            nil = []
            obj = _extension_cache.get(code, nil)
            if obj is not nil:
                self.append(obj)
                return
            key = _inverted_registry.get(code)
            if not key:
                if code <= 0: # note that 0 is forbidden
                    # Corrupt or hostile pickle.
                    raise UnpicklingError("EXT specifies code <= 0")
                raise ValueError("unregistered extension code %d" % code)
            obj = self.find_class(*key)
            _extension_cache[code] = obj
            self.append(obj)
        */

        protected PythonObject find_class(String module, String name) {
            throw new UnsupportedOperationException();
            /*# Subclasses may override this.
            sys.audit('pickle.find_class', module, name)
            if self.proto < 3 and self.fix_imports:
                if (module, name) in _compat_pickle.NAME_MAPPING:
                    module, name = _compat_pickle.NAME_MAPPING[(module, name)]
                elif module in _compat_pickle.IMPORT_MAPPING:
                    module = _compat_pickle.IMPORT_MAPPING[module]
            __import__(module, level=0)
            if self.proto >= 4:
                return _getattribute(sys.modules[module], name)[0]
            else:
                return getattr(sys.modules[module], name)*/
        }
        /*
        def load_reduce(self):
            stack = self.stack
            args = stack.pop()
            func = stack[-1]
            stack[-1] = func(*args)
        dispatch[REDUCE[0]] = load_reduce

        def load_pop(self):
            if self.stack:
                del self.stack[-1]
            else:
                self.pop_mark()
        dispatch[POP[0]] = load_pop

        def load_pop_mark(self):
            self.pop_mark()
        dispatch[POP_MARK[0]] = load_pop_mark

        def load_dup(self):
            self.append(self.stack[-1])
        dispatch[DUP[0]] = load_dup

        def load_get(self):
            i = int(self.readline()[:-1])
            try:
                self.append(self.memo[i])
            except KeyError:
                msg = f'Memo value not found at index {i}'
                raise UnpicklingError(msg) from None
        dispatch[GET[0]] = load_get
        */

        protected void load_binget() {
            int i = this.read(1)[0];
            if(!this.memo.containsKey(i)) {
                throw new UnpicklingError(
                    String.format(
                        "Memo value not found at index {i}",
                        i
                    )
                );
            }
            this.append(this.memo.get(i));
        }
        
        private void load_long_binget() {
            long i = unpack("<I", this.read(4))[0].longValue();
            Object obj = this.memo.get(i);
            if(obj == null) {
                throw new UnpicklingError("Memo value not found at index " + i); //from None
            }
            
            this.append(obj);
        }
        
        /*def load_put(self):
            i = int(self.readline()[:-1])
            if i < 0:
                raise ValueError("negative PUT argument")
            self.memo[i] = self.stack[-1]
        dispatch[PUT[0]] = load_put
        */

        private void load_binput() {
            int i = this.read(1)[0];
            if (i < 0)
                throw new IllegalArgumentException("negative BINPUT argument");
            this.memo.put(i, this.stack.get(this.stack.size() - 1));
        }

        protected void load_long_binput() {
            long i = unpack("<I", this.read(4))[0].longValue();
            if (i > maxsize) {
                ValueError.call(newString("negative LONG_BINPUT argument")).raise();
            }
            this.memo.put(i, this.stack.get(this.stack.size() - 1));
        }/*
        def load_memoize(self):
            memo = self.memo
            memo[len(memo)] = self.stack[-1]
        dispatch[MEMOIZE[0]] = load_memoize
        */
        
        private void load_append() {
            List<Object> stack = this.stack;
            PythonObject value = (PythonObject) stack.remove(stack.size() - 1);
            PythonObject list = (PythonObject)stack.get(stack.size() - 1);
            list.callAttribute("append", value);
        }
        
        protected void load_appends() {
            List items = this.pop_mark();
            PythonObject list_obj = (PythonObject)this.stack.get(this.stack.size() - 1);
            
            // FIXME: Unsupported extend
            /*try:
                extend = list_obj.extend
            except AttributeError:
                pass
            else:
                extend(items)
                return*/
            // Even if the PEP 307 requires extend() and append() methods,
            // fall back on append() if the object has no extend() method
            // for backward compatibility.
            PythonObject append = list_obj.getAttribute("append");
            for (Object item : items) {
                append.call((PythonObject)item);
            }
        }
        
        /*
        def load_setitem(self):
            stack = self.stack
            value = stack.pop()
            key = stack.pop()
            dict = stack[-1]
            dict[key] = value
        dispatch[SETITEM[0]] = load_setitem
        */
        
        private void load_setitems() {
            List<Object> items = this.pop_mark();
            PythonObject dict = (PythonObject)this.stack.get(this.stack.size() -1);
            for(int i = 0; i < items.size(); i+= 2) {
                dict.setItem((PythonObject)items.get(i), (PythonObject) items.get(i + 1));
            }
        }
        
        /*
        def load_additems(self):
            items = self.pop_mark()
            set_obj = self.stack[-1]
            if isinstance(set_obj, set):
                set_obj.update(items)
            else:
                add = set_obj.add
                for item in items:
                    add(item)
        dispatch[ADDITEMS[0]] = load_additems
        */
        
        
        protected void load_build() {
            List stack = this.stack;
            PythonObject state = (PythonObject)stack.remove(stack.size() - 1);
            PythonObject inst = (PythonObject)stack.get(stack.size() - 1);
            
            PythonObject setstate = inst.getAttribute("__setstate__", None);
            
            if (setstate != None) {
                setstate.call(state);
                return;
            }
            
            PythonObject slotstate = None;
            if (isinstance.call(state, tuple).toBoolean() && len.call(state).toInt() == 2) {
                slotstate = state.getItem(newInt(1));
                state = state.getItem(newInt(0));
            }
                
            if (state.toBoolean()) {
                // FIXME: sys.intern not supported
                
                PythonObject inst_dict = inst.getAttribute("__dict__");
                //intern = sys.intern
                for (PythonObject k : state) {
                    //if type(k) is str:
                    //    inst_dict[intern(k)] = v
                    //else:
                        inst_dict.setItem(k, state.getItem(k));
                }
            }
            
            if (slotstate.toBoolean()) {
                // FIXME: It should use slotstate.items();
                
                for (PythonObject k : slotstate) {
                    // FIXME: k shouldn't be converted to string
                    inst.setAttribute(k.toString(), slotstate.getItem(k));
                }
            }
        }
        
        private void load_mark() {
            this.metastack.add(this.stack);
            this.stack = new ArrayList<>();
        }
        
        
        private void load_stop() {
            Object value = this.stack.get(this.stack.size() - 1);
            throw new _Stop(value);
        }
        
    }

    private static class Unframer {

        private Object current_frame;
        private Unpickler pickler;

        private Unframer(Unpickler pickler/*file_tell=None*/) {
            this.pickler = pickler;
        }
        
        private int[] file_read(int n) {
            return pickler._file_read(n);
        }
        
        private int[] file_readline() {
            return pickler._file_readline();
        }
        
        private int[] read(int n) {
            //if (current_frame != null) {
                /*var data = current_frame.read(n);
                
                if not data and n != 0:
                    self.current_frame = None
                    return self.file_read(n)
                if len(data) < n:
                    raise UnpicklingError(
                        "pickle exhausted before end of frame")
                return data*/
            //} else {
                return file_read(n);
            //}
        }

        /*
        def readinto(self, buf):
            if self.current_frame:
                n = self.current_frame.readinto(buf)
                if n == 0 and len(buf) != 0:
                    self.current_frame = None
                    n = len(buf)
                    buf[:] = self.file_read(n)
                    return n
                if n < len(buf):
                    raise UnpicklingError(
                        "pickle exhausted before end of frame")
                return n
            else:
                n = len(buf)
                buf[:] = self.file_read(n)
                return n
        */
        
        
        public int[] readline() {
            /*if (this.current_frame) {
                data = self.current_frame.readline()
                if not data:
                    self.current_frame = None
                    return self.file_readline()
                if data[-1] != b'\n'[0]:
                    raise UnpicklingError(
                        "pickle exhausted before end of frame")
                return data
            else:*/
                return this.file_readline();
        }
        /*
        def load_frame(self, frame_size):
            if self.current_frame and self.current_frame.read() != b'':
                raise UnpicklingError(
                    "beginning of a new frame before end of current frame")
            self.current_frame = io.BytesIO(self.file_read(frame_size))

        */
    }

    /**
     * An instance of _Stop is raised by Unpickler.load_stop() in response to
     * the STOP opcode, passing the object that is the result of unpickling.
     */
    private static class _Stop extends RuntimeException {
        private Object value;
        
        private _Stop(Object value) {
            this.value = value;
        }
    }
}