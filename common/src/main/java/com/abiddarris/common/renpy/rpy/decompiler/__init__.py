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


from . import testcasedecompiler
from . import astdump

__all__ = ["astdump", "magic", "sl2decompiler", "testcasedecompiler", "translate", "util",
           "Options", "pprint", "Decompiler", "renpycompat"]

# Main API

# Implementation

class Decompiler(DecompilerBase):
    def rollback_state(self, state):
        self.paired_with = state[1]
        self.say_inside_menu = state[2]
        self.label_inside_menu = state[3]
        self.in_init = state[4]
        self.missing_init = state[5]
        self.most_lines_behind = state[6]
        self.last_lines_behind = state[7]
        super(Decompiler, self).rollback_state(state[0])

    @dispatch(renpy.ast.Camera)
    def print_camera(self, ast):
        self.indent()
        self.write("camera")

        if ast.layer != "master":
            self.write(f' {ast.name}')

        if ast.at_list:
            self.write(f' at {", ".join(ast.at_list)}')

        if ast.atl is not None:
            self.write(":")
            self.print_atl(ast.atl)uul

    # Translation functions

    @dispatch(renpy.ast.Translate)
    def print_translate(self, ast):
        self.indent()
        self.write(f'translate {ast.language or "None"} {ast.identifier}:')

        self.print_nodes(ast.block, 1)

    @dispatch(renpy.ast.EndTranslate)
    def print_endtranslate(self, ast):
        # an implicitly added node which does nothing...
        pass

    @dispatch(renpy.ast.TranslateBlock)
    @dispatch(renpy.ast.TranslateEarlyBlock)
    def print_translateblock(self, ast):
        self.indent()
        self.write(f'translate {ast.language or "None"} ')

        self.skip_indent_until_write = True

        in_init = self.in_init
        if (len(ast.block) == 1
                and isinstance(ast.block[0], (renpy.ast.Python, renpy.ast.Style))):
            # Ren'Py counts the TranslateBlock from "translate python" and "translate
            # style" as an Init.
            self.in_init = True
        try:
            self.print_nodes(ast.block)
        finally:
            self.in_init = in_init

    # Testcases

    @dispatch(renpy.ast.Testcase)
    def print_testcase(self, ast):
        self.require_init()
        self.indent()
        self.write(f'testcase {ast.label}:')
        self.linenumber = testcasedecompiler.pprint(
            self.out_file, ast.test.block, self.options,
            self.indent_level + 1, self.linenumber, self.skip_indent_until_write
        )
        self.skip_indent_until_write = False

    # Rpy python directives

    @dispatch(renpy.ast.RPY)
    def print_rpy_python(self, ast):
        self.indent()
        self.write(f'rpy python {ast.rest}')
