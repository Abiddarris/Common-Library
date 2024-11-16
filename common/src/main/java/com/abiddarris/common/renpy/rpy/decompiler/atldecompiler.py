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

    @dispatch(renpy.atl.RawChoice)
    def print_atl_rawchoice(self, ast):
        for chance, block in ast.choices:
            self.advance_to_block(block)
            self.indent()
            self.write("choice")
            if chance != "1.0":
                self.write(f' {chance}')
            self.write(":")
            self.print_block(block)
        if (self.index + 1 < len(self.block)
                and isinstance(self.block[self.index + 1], renpy.atl.RawChoice)):
            self.indent()
            self.write("pass")

    @dispatch(renpy.atl.RawContainsExpr)
    def print_atl_rawcontainsexpr(self, ast):
        self.indent()
        self.write(f'contains {ast.expression}')

    @dispatch(renpy.atl.RawEvent)
    def print_atl_rawevent(self, ast):
        self.indent()
        self.write(f'event {ast.name}')

    @dispatch(renpy.atl.RawFunction)
    def print_atl_rawfunction(self, ast):
        self.indent()
        self.write(f'function {ast.expr}')

    @dispatch(renpy.atl.RawOn)
    def print_atl_rawon(self, ast):
        for name, block in sorted(ast.handlers.items(),
                                  key=lambda i: i[1].loc[1]):
            self.advance_to_block(block)
            self.indent()
            self.write(f'on {name}:')
            self.print_block(block)

    @dispatch(renpy.atl.RawParallel)
    def print_atl_rawparallel(self, ast):
        for block in ast.blocks:
            self.advance_to_block(block)
            self.indent()
            self.write("parallel:")
            self.print_block(block)
        if (self.index + 1 < len(self.block)
                and isinstance(self.block[self.index + 1], renpy.atl.RawParallel)):
            self.indent()
            self.write("pass")

    @dispatch(renpy.atl.RawRepeat)
    def print_atl_rawrepeat(self, ast):
        self.indent()
        self.write("repeat")
        if ast.repeats:
            self.write(f' {ast.repeats}')  # not sure if this is even a string

    @dispatch(renpy.atl.RawTime)
    def print_atl_rawtime(self, ast):
        self.indent()
        self.write(f'time {ast.time}')
