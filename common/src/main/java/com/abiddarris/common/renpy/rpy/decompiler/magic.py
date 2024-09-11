# Copyright (c) 2015-2024 CensoredUsername
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

# This module provides tools for safely analyizing pickle files programmatically

import sys

PY3 = sys.version_info >= (3, 0)
PY2 = not PY3

import types
import pickle
import struct

try:
    # only available (and needed) from 3.4 onwards.
    from importlib.machinery import ModuleSpec
except:
    pass

if PY3:
    from io import BytesIO as StringIO
else:
    from cStringIO import StringIO

__all__ = [
    "load", "loads", "safe_load", "safe_loads", "safe_dump", "safe_dumps",
    "fake_package", "remove_fake_package",
    "FakeModule", "FakePackage", "FakePackageLoader",
    "FakeClassType", "FakeClassFactory",
    "FakeClass", "FakeStrict", "FakeWarning", "FakeIgnore",
    "FakeUnpicklingError", "FakeUnpickler", "SafeUnpickler",
    "SafePickler"
]

# Fake class implementation

class FakeWarning(FakeClass, object):
    def __new__(cls, *args, **kwargs):
        self = FakeClass.__new__(cls)
        if args or kwargs:
            print("{0} was instantiated with unexpected arguments {1}, {2}".format(cls, args, kwargs))
            self._new_args = args
        return self

    def __setstate__(self, state):
        slotstate = None

        if (isinstance(state, tuple) and len(state) == 2 and
            (state[0] is None or isinstance(state[0], dict)) and
            (state[1] is None or isinstance(state[1], dict))):
            state, slotstate = state

        if state:
            # Don't have to check for slotstate here since it's either None or a dict
            if not isinstance(state, dict):
                print("{0}.__setstate__() got unexpected arguments {1}".format(self.__class__, state))
                self._setstate_args = state
            else:
                self.__dict__.update(state)

        if slotstate:
            self.__dict__.update(slotstate)

class FakeIgnore(FakeClass, object):
    def __new__(cls, *args, **kwargs):
        self = FakeClass.__new__(cls)
        if args:
            self._new_args = args
        if kwargs:
            self._new_kwargs = kwargs
        return self

    def __setstate__(self, state):
        slotstate = None

        if (isinstance(state, tuple) and len(state) == 2 and
            (state[0] is None or isinstance(state[0], dict)) and
            (state[1] is None or isinstance(state[1], dict))):
            state, slotstate = state

        if state:
            # Don't have to check for slotstate here since it's either None or a dict
            if not isinstance(state, dict):
                self._setstate_args = state
            else:
                self.__dict__.update(state)

        if slotstate:
            self.__dict__.update(slotstate)



# Fake module implementation

class FakeModule(types.ModuleType):
    def __repr__(self):
        return "<module '{0}' (fake)>".format(self.__name__)

    def __str__(self):
        return self.__repr__()

    def __setattr__(self, name, value):
        # If a fakemodule is removed we need to remove its entry from sys.modules
        if (name in self.__dict__ and
            isinstance(self.__dict__[name], FakeModule) and not
            isinstance(value, FakeModule)):

            self.__dict__[name]._remove()
        self.__dict__[name] = value

    def __delattr__(self, name):
        if isinstance(self.__dict__[name], FakeModule):
            self.__dict__[name]._remove()
        del self.__dict__[name]

    def _remove(self):
        """
        Removes this module from :data:`sys.modules` and calls :meth:`_remove` on any
        sub-FakeModules.
        """
        for i in tuple(self.__dict__.keys()):
            if isinstance(self.__dict__[i], FakeModule):
                self.__dict__[i]._remove()
                del self.__dict__[i]
        del sys.modules[self.__name__]

    def __eq__(self, other):
        if not hasattr(other, "__name__"):
            return False
        othername = other.__name__
        if hasattr(other, "__module__"):
            othername = other.__module__ + "." + other.__name__

        return self.__name__ == othername

    def __ne__(self, other):
        return not self == other

    def __hash__(self):
        return hash(self.__name__)

    def __instancecheck__(self, instance):
        return self.__subclasscheck__(instance.__class__)

    def __subclasscheck__(self, subclass):
        return (self == subclass or
                (bool(subclass.__bases__) and
                 any(self.__subclasscheck__(base) for base in subclass.__bases__)))

class FakePackage(FakeModule):
    def __call__(self, *args, **kwargs):
        # This mainly exists to print a nicer error message when
        # someone tries to call a FakePackage instance
        raise TypeError("'{0}' FakePackage object is not callable".format(self.__name__))

    def __getattr__(self, name):
        modname = self.__name__ + "." + name
        mod = sys.modules.get(modname, None)
        if mod is None:
            try:
                __import__(modname)
            except:
                mod = FakePackage(modname)
            else:
                mod = sys.modules[modname]
        return mod

class FakePackageLoader(object):
    # the old way of loading modules. find_module returns a loader for the
    # given module. In this case, that is this object itself again.

    def find_module(self, fullname, path=None):
        if fullname == self.root or fullname.startswith(self.root + "."):
            return self
        else:
            return None

    # the new way of loading modules. It returns a ModuleSpec, that has
    # the loader attribute set to this class.

    def find_spec(self, fullname, path, target=None):
        if fullname == self.root or fullname.startswith(self.root + "."):
            return ModuleSpec(fullname, self)
        else:
            return None

    # loader methods. This loads the module.

    def load_module(self, fullname):
        return FakePackage(fullname)


# Fake unpickler implementation

class SafePickler(pickle.Pickler if PY2 else pickle._Pickler):
    """
    A pickler which can repickle object hierarchies containing objects created by SafeUnpickler.
    Due to reasons unknown, pythons pickle implementation will normally check if a given class
    actually matches with the object specified at the __module__ and __name__ of the class. Since
    this check is performed with object identity instead of object equality we cannot fake this from
    the classes themselves, and we need to override the method used for normally saving classes.
    """

    def save_global(self, obj, name=None, pack=None):
        if isinstance(obj, FakeClassType):
            if PY2:
                self.write(pickle.GLOBAL
                           + obj.__module__ + '\n' + obj.__name__ + '\n')
            elif self.proto >= 4:
                self.save(obj.__module__)
                self.save(obj.__name__)
                self.write(pickle.STACK_GLOBAL)
            else:
                self.write(pickle.GLOBAL
                           + (obj.__module__ + '\n' + obj.__name__ + '\n').decode("utf-8"))
            self.memoize(obj)
            return

        super().save_global(obj, name)

# the main API

def load(file, class_factory=None, encoding="bytes", errors="errors"):
    """
    Read a pickled object representation from the open binary :term:`file object` *file*
    and return the reconstitutded object hierarchy specified therein, generating
    any missing class definitions at runtime. This is equivalent to
    ``FakeUnpickler(file).load()``.

    The optional keyword arguments are *class_factory*, *encoding* and *errors*.
    *class_factory* can be used to control how the missing class definitions are
    created. If set to ``None``, ``FakeClassFactory({}, 'strict')`` will be used.

    In Python 3, the optional keyword arguments *encoding* and *errors* can be used
    to indicate how the unpickler should deal with pickle streams generated in python
    2, specifically how to deal with 8-bit string instances. If set to "bytes" it will
    load them as bytes objects, otherwise it will attempt to decode them into unicode
    using the given *encoding* and *errors* arguments.

    This function should only be used to unpickle trusted data.
    """
    return FakeUnpickler(file, class_factory, encoding=encoding, errors=errors).load()

def loads(string, class_factory=None, encoding="bytes", errors="errors"):
    """
    Simjilar to :func:`load`, but takes an 8-bit string (bytes in Python 3, str in Python 2)
    as its first argument instead of a binary :term:`file object`.
    """
    return FakeUnpickler(StringIO(string), class_factory,
                         encoding=encoding, errors=errors).load()

def safe_load(file, class_factory=None, safe_modules=(), use_copyreg=False,
              encoding="bytes", errors="errors"):
    """
    Read a pickled object representation from the open binary :term:`file object` *file*
    and return the reconstitutded object hierarchy specified therein, substituting any
    class definitions by fake classes, ensuring safety in the unpickling process.
    This is equivalent to ``SafeUnpickler(file).load()``.

    The optional keyword arguments are *class_factory*, *safe_modules*, *use_copyreg*,
    *encoding* and *errors*. *class_factory* can be used to control how the missing class
    definitions are created. If set to ``None``, ``FakeClassFactory({}, 'strict')`` will be
    used. *safe_modules* can be set to a set of strings of module names, which will be
    regarded as safe by the unpickling process, meaning that it will import objects
    from that module instead of generating fake classes (this does not apply to objects
    in submodules). *use_copyreg* is a boolean value indicating if it's allowed to
    use extensions from the pickle extension registry (documented in the :mod:`copyreg`
    module).

    In Python 3, the optional keyword arguments *encoding* and *errors* can be used
    to indicate how the unpickler should deal with pickle streams generated in python
    2, specifically how to deal with 8-bit string instances. If set to "bytes" it will
    load them as bytes objects, otherwise it will attempt to decode them into unicode
    using the given *encoding* and *errors* arguments.

    This function can be used to unpickle untrusted data safely with the default
    class_factory when *safe_modules* is empty and *use_copyreg* is False.
    """
    return SafeUnpickler(file, class_factory, safe_modules, use_copyreg,
                         encoding=encoding, errors=errors).load()


def safe_dump(obj, file, protocol=pickle.HIGHEST_PROTOCOL):
    """
    A convenience function wrapping SafePickler. It functions similarly to pickle.dump
    """
    SafePickler(file, protocol).dump(obj)

def safe_dumps(obj, protocol=pickle.HIGHEST_PROTOCOL):
    """
    A convenience function wrapping SafePickler. It functions similarly to pickle.dumps
    """
    file = StringIO()
    SafePickler(file, protocol).dump(obj)
    return file.getvalue()

def fake_package(name):
    """
    Mounts a fake package tree with the name *name*. This causes any attempt to import
    module *name*, attributes of the module or submodules will return a :class:`FakePackage`
    instance which implements the same behaviour. These :class:`FakePackage` instances compare
    properly with :class:`FakeClassType` instances allowing you to code using FakePackages as
    if the modules and their attributes actually existed.

    This is implemented by creating a :class:`FakePackageLoader` instance with root *name*
    and inserting it in the first spot in :data:`sys.meta_path`. This ensures that importing the
    module and submodules will work properly. Further the :class:`FakePackage` instances take
    care of generating submodules as attributes on request.

    If a fake package tree with the same *name* is already registered, no new fake package
    tree will be mounted.

    This returns the :class:`FakePackage` instance *name*.
    """
    if name in sys.modules and isinstance(sys.modules[name], FakePackage):
        return sys.modules[name]
    else:
        loader = FakePackageLoader(name)
        sys.meta_path.insert(0, loader)
        return __import__(name)

def remove_fake_package(name):
    """
    Removes the fake package tree mounted at *name*.

    This works by first looking for any FakePackageLoaders in :data:`sys.path`
    with their root set to *name* and removing them from sys.path. Next it will
    find the top-level :class:`FakePackage` instance *name* and from this point
    traverse the tree of created submodules, removing them from :data:`sys.path`
    and removing their attributes. After this the modules are not registered
    anymore and if they are not referenced from user code anymore they will be
    garbage collected.

    If no fake package tree *name* exists a :exc:`ValueError` will be raised.
    """

    # Get the package entry via its entry in sys.modules
    package = sys.modules.get(name, None)
    if package is None:
        raise ValueError("No fake package with the name {0} found".format(name))

    if not isinstance(package, FakePackage):
        raise ValueError("The module {0} is not a fake package".format(name))

    # Attempt to remove the loader from sys.meta_path

    loaders = [i for i in sys.meta_path if isinstance(i, FakePackageLoader) and i.root == name]
    for loader in loaders:
        sys.meta_path.remove(loader)

    # Remove all module and submodule entries from sys.modules
    package._remove()

    # It is impossible to kill references to the modules, but all traces
    # of it have been removed from the import machinery and the submodule
    # tree structure has been broken up.
