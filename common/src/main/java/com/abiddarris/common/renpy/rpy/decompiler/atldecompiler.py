# Copyright (c) 2012-2024 Yuri K. Schlesner, CensoredUsername, Jackmcbarn
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

class ATLDecompiler(DecompilerBase):
    @dispatch(renpy.atl.RawChild)
    def print_atl_rawchild(self, ast):
        for child in ast.children:
            self.advance_to_block(child)
            self.indent()
            self.write("contains:")
            self.print_block(child)

    @dispatch(renpy.atl.RawContainsExpr)
    def print_atl_rawcontainsexpr(self, ast):
        self.indent()
        self.write(f'contains {ast.expression}')

    @dispatch(renpy.atl.RawEvent)
    def print_atl_rawevent(self, ast):
        self.indent()
        self.write(f'event {ast.name}')

    @dispatch(renpy.atl.RawTime)
    def print_atl_rawtime(self, ast):
        self.indent()
        self.write(f'time {ast.time}')
