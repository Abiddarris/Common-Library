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
                  reconstruct_arginfo, split_logical_lines

from . import atldecompiler

from renpy import ui
from renpy.ast import PyExpr
from renpy.text import text
from renpy.sl2 import sldisplayables as sld
from renpy.display import layout, behavior, im, motion, dragdrop, transform

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

    def print_block(self, ast, immediate_block=False):
        # represents an SLBlock node, which is a container of keyword arguments and children
        #
        # block is a child of showif, if, use, user-defined displayables.
        # for showif, if and use, no keyword properties on the same line are allowed
        # for custom displayables, they are allowed.
        #
        # immediate_block: boolean, indicates that no keyword properties are before the :, and
        # that a block is required
        first_line, other_lines = self.sort_keywords_and_children(
            ast, immediate_block=immediate_block)

        has_block = immediate_block or bool(other_lines)

        self.print_keyword_or_child(first_line, first_line=True, has_block=has_block)

        if other_lines:
            with self.increase_indent():
                for line in other_lines:
                    self.print_keyword_or_child(line)

            # special case, a block is forced, while there is no content
        elif immediate_block:
            with self.increase_indent():
                self.indent()
                self.write("pass")

    @dispatch(sl2.slast.SLFor)
    def print_for(self, ast):
        # Since tuple unpickling is hard, renpy just gives up and inserts a
        # $ a,b,c = _sl2_i after the for statement if any tuple unpacking was
        # attempted in the for statement. Detect this and ignore this slast.SLPython entry
        if ast.variable == "_sl2_i":
            variable = ast.children[0].code.source[:-9]
            children = ast.children[1:]
        else:
            variable = ast.variable.strip() + " "
            children = ast.children

        self.indent()
        if hasattr(ast, "index_expression") and ast.index_expression is not None:
            self.write(f'for {variable}index {ast.index_expression} in {ast.expression}:')

        else:
            self.write(f'for {variable}in {ast.expression}:')

        # for doesn't contain a block, but just a list of child nodes
        self.print_nodes(children, 1)

    @dispatch(sl2.slast.SLContinue)
    def print_continue(self, ast):
        self.indent()
        self.write("continue")

    @dispatch(sl2.slast.SLBreak)
    def print_break(self, ast):
        self.indent()
        self.write("break")

    @dispatch(sl2.slast.SLPython)
    def print_python(self, ast):
        self.indent()

        # Extract the source code from the slast.SLPython object. If it starts with a
        # newline, print it as a python block, else, print it as a $ statement
        code = ast.code.source
        if code.startswith("\n"):
            code = code[1:]
            self.write("python:")
            with self.increase_indent():
                self.write_lines(split_logical_lines(code))
        else:
            self.write(f'$ {code}')

    @dispatch(sl2.slast.SLPass)
    def print_pass(self, ast):
        # A pass statement
        self.indent()
        self.write("pass")

    @dispatch(sl2.slast.SLUse)
    def print_use(self, ast):
        # A use statement requires reconstructing the arguments it wants to pass
        self.indent()
        self.write("use ")
        args = reconstruct_arginfo(ast.args)
        if isinstance(ast.target, PyExpr):
            self.write(f'expression {ast.target}')
            if args:
                self.write(" pass ")
        else:
            self.write(f'{ast.target}')

        self.write(f'{args}')
        if hasattr(ast, 'id') and ast.id is not None:
            self.write(f' id {ast.id}')

        if hasattr(ast, "block") and ast.block:
            self.print_block(ast.block)

    @dispatch(sl2.slast.SLTransclude)
    def print_transclude(self, ast):
        self.indent()
        self.write("transclude")

    @dispatch(sl2.slast.SLDefault)
    def print_default(self, ast):
        # A default statement
        self.indent()
        self.write(f'default {ast.variable} = {ast.expression}')

    @dispatch(sl2.slast.SLDisplayable)
    def print_displayable(self, ast, has_block=False):
        # slast.SLDisplayable represents a variety of statements. We can figure out
        # what statement it represents by analyzing the called displayable and style
        # attributes.
        key = (ast.displayable, ast.style)
        nameAndChildren = self.displayable_names.get(key)

        if nameAndChildren is None and self.options.sl_custom_names:
            # check if we have a name registered for this displayable
            nameAndChildren = self.options.sl_custom_names.get(ast.displayable.__name__)
            self.print_debug(
                f'Substituted "{nameAndChildren[0]}" as the name for displayable {ast.displayable}')

        if nameAndChildren is None:
            # This is a (user-defined) displayable we don't know about.
            # fallback: assume the name of the displayable matches the given style
            # this is rather often the case. However, as it may be wrong we have to
            # print a debug message
            nameAndChildren = (ast.style, 'many')
            self.print_debug(
    f'''Warning: Encountered a user-defined displayable of type "{ast.displayable}".
    Unfortunately, the name of user-defined displayables is not recorded in the compiled file.
    For now the style name "{ast.style}" will be substituted.
    To check if this is correct, find the corresponding renpy.register_sl_displayable call.''')  # noqa

        (name, children) = nameAndChildren
        self.indent()
        self.write(name)
        if ast.positional:
            self.write(" " + " ".join(ast.positional))

        atl_transform = getattr(ast, 'atl_transform', None)
        # The AST contains no indication of whether or not "has" blocks
        # were used. We'll use one any time it's possible (except for
        # directly nesting them, or if they wouldn't contain any children),
        # since it results in cleaner code.

        # if we're not already in a has block, and have a single child that's a displayable,
        # which itself has children, and the line number of this child is after any atl
        # transform or keyword we can safely use a has statement
        if (not has_block
                and children == 1
                and len(ast.children) == 1
                and isinstance(ast.children[0], sl2.slast.SLDisplayable)
                and ast.children[0].children
                and (not ast.keyword
                     or ast.children[0].location[1] > ast.keyword[-1][1].linenumber)
                and (atl_transform is None
                     or ast.children[0].location[1] > atl_transform.loc[1])):

            first_line, other_lines = self.sort_keywords_and_children(ast, ignore_children=True)
            self.print_keyword_or_child(first_line, first_line=True, has_block=True)

            with self.increase_indent():
                for line in other_lines:
                    self.print_keyword_or_child(line)

                self.advance_to_line(ast.children[0].location[1])
                self.indent()
                self.write("has ")

                self.skip_indent_until_write = True
                self.print_displayable(ast.children[0], True)

        elif has_block:
            # has block: for now, assume no block of any kind present
            first_line, other_lines = self.sort_keywords_and_children(ast)
            self.print_keyword_or_child(first_line, first_line=True, has_block=False)
            for line in other_lines:
                self.print_keyword_or_child(line)

        else:
            first_line, other_lines = self.sort_keywords_and_children(ast)
            self.print_keyword_or_child(first_line, first_line=True, has_block=bool(other_lines))

            with self.increase_indent():
                for line in other_lines:
                    self.print_keyword_or_child(line)

    displayable_names = {
        (behavior.AreaPicker, "default"):       ("areapicker", 1),
        (behavior.Button, "button"):            ("button", 1),
        (behavior.DismissBehavior, "default"):  ("dismiss", 0),
        (behavior.Input, "input"):              ("input", 0),
        (behavior.MouseArea, 0):                ("mousearea", 0),
        (behavior.MouseArea, None):             ("mousearea", 0),
        (behavior.OnEvent, 0):                  ("on", 0),
        (behavior.OnEvent, None):               ("on", 0),
        (behavior.Timer, "default"):            ("timer", 0),
        (dragdrop.Drag, "drag"):                ("drag", 1),
        (dragdrop.Drag, None):                  ("drag", 1),
        (dragdrop.DragGroup, None):             ("draggroup", 'many'),
        (im.image, "default"):                  ("image", 0),
        (layout.Grid, "grid"):                  ("grid", 'many'),
        (layout.MultiBox, "fixed"):             ("fixed", 'many'),
        (layout.MultiBox, "hbox"):              ("hbox", 'many'),
        (layout.MultiBox, "vbox"):              ("vbox", 'many'),
        (layout.NearRect, "default"):           ("nearrect", 1),
        (layout.Null, "default"):               ("null", 0),
        (layout.Side, "side"):                  ("side", 'many'),
        (layout.Window, "frame"):               ("frame", 1),
        (layout.Window, "window"):              ("window", 1),
        (motion.Transform, "transform"):        ("transform", 1),
        (sld.sl2add, None):                     ("add", 0),
        (sld.sl2bar, None):                     ("bar", 0),
        (sld.sl2vbar, None):                    ("vbar", 0),
        (sld.sl2viewport, "viewport"):          ("viewport", 1),
        (sld.sl2vpgrid, "vpgrid"):              ("vpgrid", 'many'),
        (text.Text, "text"):                    ("text", 0),
        (transform.Transform, "transform"):     ("transform", 1),
        (ui._add, None):                        ("add", 0),
        (ui._hotbar, "hotbar"):                 ("hotbar", 0),
        (ui._hotspot, "hotspot"):               ("hotspot", 1),
        (ui._imagebutton, "image_button"):      ("imagebutton", 0),
        (ui._imagemap, "imagemap"):             ("imagemap", 'many'),
        (ui._key, None):                        ("key", 0),
        (ui._label, "label"):                   ("label", 0),
        (ui._textbutton, "button"):             ("textbutton", 0),
        (ui._textbutton, 0):                    ("textbutton", 0),
    }