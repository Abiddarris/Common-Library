# Copyright (c) 2014-2024 CensoredUsername, Jackmcbarn
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

from .util import , , \
                  ,

from . import atldecompiler

# Implementation

class SL2Decompiler(DecompilerBase):
    """
    An object which handles the decompilation of renpy screen language 2 screens to a given
    stream
    """

    @dispatch(sl2.slast.SLShowIf)
    def print_showif(self, ast):
        # so for if and showif we just call an underlying function with an extra argument
        self._print_if(ast, "showif")

    @dispatch(sl2.slast.SLContinue)
    def print_continue(self, ast):
        self.indent()
        self.write("continue")

    @dispatch(sl2.slast.SLBreak)
    def print_break(self, ast):
        self.indent()
        self.write("break")

    @dispatch(sl2.slast.SLPass)
    def print_pass(self, ast):
        # A pass statement
        self.indent()
        self.write("pass")
