/***********************************************************************************
 * Copyright 2024 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original MIT License : 
 *
 * Copyright (c) 2012-2024 Yuri K. Schlesner, CensoredUsername, Jackmcbarn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ************************************************************************************/
package com.abiddarris.common.renpy.rpy;

import static com.abiddarris.common.files.Files.changeExtension;
import static com.abiddarris.common.files.Files.getExtension;
import static com.abiddarris.common.renpy.internal.PythonObject.*;
import static com.abiddarris.common.renpy.internal.imp.Imports.importModule;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.rpy.decompiler.RenPyCompat.pickle_detect_python2;
import static com.abiddarris.common.renpy.rpy.decompiler.RenPyCompat.pickle_safe_loads;
import static com.abiddarris.common.stream.Compresses.decompress;
import static com.abiddarris.common.stream.InputStreams.readAll;
import static com.abiddarris.common.stream.Signs.sign;
import static com.abiddarris.common.stream.Signs.unsign;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

import com.abiddarris.common.renpy.internal.Builtins;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.Struct;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Unrpyc {
    
    private static final PythonObject decompiler;
    private static PythonObject JavaPyInputStream;
    
    static {
        decompiler = importModule("decompiler");

        registerLoader("unrpyc", (unrpyc) -> {
            JavaPyInputStream = JavaPyInputStreamImpl.define(unrpyc.defineClass("JavaPyInputStream"));
        });

        importModule("unrpyc");
    }

    private static class JavaPyInputStreamImpl {

        private static PythonObject define(ClassDefiner definer) {
            definer.defineFunction("write", JavaPyInputStreamImpl::write, "self", "string");
            return definer.define();
        }

        private static void
        write(PythonObject self, PythonObject string) {
            OutputStream stream = self.getJavaAttribute("stream");
            try {
                stream.write(string.toString().getBytes());
            } catch (IOException e) {
                Builtins.Exception.call(newString(e.getMessage())).raise();
            }
        }
    }
    
    public static class Context {
        
        //list of log lines to print
        private List<String> logContents = new ArrayList<>();
      
        // any exception that occurred
        private Object error;
        
        /**
         * state of what case was encountered
         * options:
         *     error:      (default) an unexpected exception was raised
         *     ok:         the process concluded successfully
         *     bad_header: the given file cannot be parsed as a normal rpyc file
         *     skip:       the given file was skipped due to a preexisting output file
         */
        private String state = "error";
        
        //return value from the worker, if any
        private Object value;
        
        public void log(String message) {
            this.logContents.add(message);
        }
        
        public void setError(Object error) {
            this.error = error;
        }
        
        public void setResult(Object value) {
            this.value = value;
        }
        
        public void setState(String state) {
            this.state = state;
        }
    }
    
    /**
     * Exception raised when we couldn't parse the rpyc archive format
     */
    public static class BadRpycException extends RuntimeException {
        
        public BadRpycException(String message) {
            super(message);
        }
        
    }
    
    public static PythonObject read_ast_from_file(InputStream in_file, Context context) throws IOException {
        // Reads rpyc v1 or v2 file
        // v1 files are just a zlib compressed pickle blob containing some data and the ast
        // v2 files contain a basic archive structure that can be parsed to find the same blob
        int[] raw_contents = unsign(readAll(in_file));
        int[] file_start = copyOf(raw_contents, 50);
        boolean is_rpyc_v1 = false;

        int[] contents;
        if(!new String(sign(raw_contents)).startsWith("RENPY RPC2")) {
            // if the header isn't present, it should be a RPYC V1 file, which is just the blob
            contents = raw_contents;
            is_rpyc_v1 = true;
        } else {
            // parse the archive structure
            int position = 10;
            Map chunks = new HashMap();
            boolean have_errored = false;

            for(int expected_slot = 1; expected_slot < 0xFFFFFFFFL;) {
                // FIXME: Unsigned int casted to signed int
                Number[] results = Struct.unpack("III", copyOfRange(raw_contents, position, position + 12));
                int slot = results[0].intValue(), 
                    start = results[1].intValue(), 
                    length = results[2].intValue();
                
                if(slot == 0) {
                    break;
                }
                
                if(slot != expected_slot && !have_errored) {
                    have_errored = true;
                }
                
                context.log(
                    "Warning: Encountered an unexpected slot structure. It is possible the \n" +
                    "    file header structure has been changed.");

                position += 12;

                chunks.put(slot, copyOfRange(raw_contents, start, start + length));
            }
            
            if (!chunks.containsKey(1)) {
                context.setState("bad_header");
                throw new BadRpycException(
                    "Unable to find the right slot to load from the rpyc file. The file header " +
                    String.format("structure has been changed. File header: %s", file_start));
            }
            contents = (int[])chunks.get(1);
        }
        
        try {
            contents = unsign(decompress(sign(contents)));
        } catch (Exception e) {
            context.setState("bad_header");
            throw new BadRpycException(
                "Did not find a zlib compressed blob where it was expected. Either the header has been " + 
                String.format("modified or the file structure has been changed. File header: %s", file_start));
        }
            
        // add some detection of ren'py 7 files
        String version;
        if(is_rpyc_v1 || pickle_detect_python2(contents)) {
            version = is_rpyc_v1 ? "6" : "7";
            
            context.log(
                "Warning: analysis found signs that this .rpyc file was generated by ren'py \n" +
                String.format("    version %s or below, while this unrpyc version targets ren\'py \n", version) +
                "    version 8. Decompilation will still be attempted, but errors or incorrect \n" +
                "    decompilation might occur. ");
        }

        PythonObject stmts = ((PythonObject)pickle_safe_loads(contents)).getItem(newInt(1));
        return stmts;
    }
    
    /**
     * Opens the rpyc file at path in_file to load the contained AST.
     * If try_harder is True, an attempt will be made to work around obfuscation techniques.
     * Else, it is loaded as a normal rpyc file.
     */
    public static PythonObject get_ast(File src, boolean try_harder, Context context) throws IOException {
        try (var stream = new FileInputStream(src)) {
            InputStream bufStream = new BufferedInputStream(stream);
            PythonObject ast = try_harder ? null/*deobfuscate.read_ast(in_file, context)*/ :
                         read_ast_from_file(bufStream, context);
            return ast;
        } 
    }

    
    public static void decompile_rpyc(File file, Context context, boolean overwrite/*=False*/, boolean try_harder/*=False*/, boolean dump/*=False*/,
                   boolean comparable/*=False*/, boolean no_pyexpr/*=False*/, PythonObject translator/*null*/, boolean init_offset/*=False*/,
                   PythonObject sl_custom_names/*=None*/) throws IOException {
        // Output filename is input filename but with .rpy extension
        String ext = getExtension(file);
        if (dump) {
            ext = ".txt";
        } else if(ext.equals("rpyc")) {
            ext = ".rpy";
        } else if(ext.equals("rpymc")) {
            ext = ".rpym";
        } else {
            ext = null;
        }
        
        File outputFile = changeExtension(file, ext);

        if (!overwrite && outputFile.exists()) {
            context.log(String.format("Skipping %s. %s already exists.", file, outputFile.getName()));
            context.setState("skip");
            return;
        }
            
        context.log(String.format("Decompiling %s to %s ...", file, outputFile.getName()));
        PythonObject ast = get_ast(file, try_harder, context);

        try (FileOutputStream stream = new FileOutputStream(outputFile)){
            BufferedOutputStream bufferedStream = new BufferedOutputStream(stream);
            PythonObject options = decompiler.getAttribute("Options").call(new PythonArgument()
                    .addKeywordArgument("log", Builtins.None)
                    .addKeywordArgument("translator", translator)
                    .addKeywordArgument("init_offset", newBoolean(init_offset))
                    .addKeywordArgument("sl_custom_names", sl_custom_names));

            PythonObject out_file = JavaPyInputStream.call();
            out_file.setJavaAttribute("stream", stream);

            decompiler.callAttribute("pprint", out_file, ast, options);
        }
        //with out_filename.open('w', encoding='utf-8') as out_file:
            //if dump:
                //astdump.pprint(out_file, ast, comparable=comparable, no_pyexpr=no_pyexpr)
            //else:
                /* FIXME: log=context.log_contents**/

        //context.set_state('ok')
    }

}
