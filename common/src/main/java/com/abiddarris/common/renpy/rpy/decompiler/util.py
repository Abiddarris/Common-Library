# Copyright (c) 2014-2024 CensoredUsername, Jackmcbarn
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software'), to deal
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


import sys
from io import StringIO
from contextlib import contextmanager


class DecompilerBase:
    def save_state(self):
        """
        Save our current state.
        """
        state = (self.out_file,
                 self.skip_indent_until_write,
                 self.linenumber,
                 self.block_stack,
                 self.index_stack,
                 self.indent_level,
                 self.blank_line_queue)
        self.out_file = StringIO()
        return state

    def commit_state(self, state):
        """
        Commit changes since a saved state.
        """
        out_file = state[0]
        out_file.write(self.out_file.getvalue())
        self.out_file = out_file

    def rollback_state(self, state):
        """
        Roll back to a saved state.
        """
        (self.out_file,
         self.skip_indent_until_write,
         self.linenumber,
         self.block_stack,
         self.index_stack,
         self.indent_level,
         self.blank_line_queue) = state


    def do_when_blank_line(self, m):
        """
        Do something the next time we find a blank line. m should be a method that takes one
        parameter (the line we're advancing to), and returns whether or not it needs to run
        again.
        """
        self.blank_line_queue.append(m)

def reconstruct_arginfo(arginfo):
    if arginfo is None:
        return ""

    rv = ["("]
    sep = First("", ", ")

    if hasattr(arginfo, 'starred_indexes'):
        # ren'py 7.5 and above, PEP 448 compliant
        for i, (name, val) in enumerate(arginfo.arguments):
            rv.append(sep())
            if name is not None:
                rv.append(f'{name}=')
            elif i in arginfo.starred_indexes:
                rv.append('*')
            elif i in arginfo.doublestarred_indexes:
                rv.append('**')
            rv.append(val)

    else:
        # ren'py 7.4 and below, python 2 style
        for (name, val) in arginfo.arguments:
            rv.append(sep())
            if name is not None:
                rv.append(f'{name}=')
            rv.append(val)
        if arginfo.extrapos:
            rv.append(sep())
            rv.append(f'*{arginfo.extrapos}')
        if arginfo.extrakw:
            rv.append(sep())
            rv.append(f'**{arginfo.extrakw}')

    rv.append(")")

    return "".join(rv)

def string_escape(s):  # TODO see if this needs to work like encode_say_string elsewhere
    s = s.replace('\\', '\\\\')
    s = s.replace('"', '\\"')
    s = s.replace('\n', '\\n')
    s = s.replace('\t', '\\t')
    return s

# keywords used by ren'py's parser
KEYWORDS = set(['$', 'as', 'at', 'behind', 'call', 'expression', 'hide',
                'if', 'in', 'image', 'init', 'jump', 'menu', 'onlayer',
                'python', 'return', 'scene', 'set', 'show', 'with',
                'while', 'zorder', 'transform'])

word_regexp = '[a-zA-Z_\u00a0-\ufffd][0-9a-zA-Z_\u00a0-\ufffd]*'

def simple_expression_guard(s):
    # Some things we deal with are supposed to be parsed by
    # ren'py's Lexer.simple_expression but actually cannot
    # be parsed by it. figure out if this is the case
    # a slightly more naive approach would be to check
    # for spaces in it and surround it with () if necessary
    # but we're not naive
    s = s.strip()

    if Lexer(s).simple_expression():
        return s
    else:
        return f'({s})'

class Lexer:
    def eol(self):
        # eat the next whitespace and check for the end of this simple_expression
        self.re(r"(\s+|\\\n)+")
        return self.pos >= self.length

    def match(self, regexp):
        # strip whitespace and match regexp
        self.re(r"(\s+|\\\n)+")
        return self.re(regexp)

    def container(self):
        # parses something enclosed by [], () or {}'s. keyword something
        containers = {"{": "}", "[": "]", "(": ")"}
        if self.eol():
            return None

        c = self.string[self.pos]
        if c not in containers:
            return None
        self.pos += 1

        c = containers[c]

        while not self.eol():
            if c == self.string[self.pos]:
                self.pos += 1
                return True

            if self.python_string() or self.container():
                continue

            self.pos += 1

        return None

    def number(self):
        # parses a number, float or int (but not forced long)
        return self.match(r'(\+|\-)?(\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?')

    def word(self):
        # parses a word
        return self.match(word_regexp)

    def name(self):
        # parses a word unless it's in KEYWORDS.
        pos = self.pos
        word = self.word()

        if word in KEYWORDS:
            self.pos = pos
            return None

        return word

    def simple_expression(self):
        # check if there's anything in here acctually
        if self.eol():
            return False

        # parse anything which can be called or have attributes requested
        if not (self.python_string()
                or self.number()
                or self.container()
                or self.name()):
            return False

        while not self.eol():

            # if the previous was followed by a dot, there should be a word after it
            if self.match(r'\.'):
                if not self.name():
                    # ren'py errors here. I just stop caring
                    return False

                continue

            # parses slices, function calls, and postfix {}
            if self.container():
                continue

            break

            # are we at the end of the simple expression?
        return self.eol()

# Versions of Ren'Py prior to 6.17 put trailing whitespace on the end of
# simple_expressions. This class attempts to preserve the amount of
# whitespace if possible.
class WordConcatenator(object):
    def __init__(self, needs_space, reorderable=False):
        self.words = []
        self.needs_space = needs_space
        self.reorderable = reorderable

    def append(self, *args):
        self.words.extend(i for i in args if i)

    def join(self):
        if not self.words:
            return ''
        if self.reorderable and self.words[-1][-1] == ' ':
            for i in range(len(self.words) - 1, -1, -1):
                if self.words[i][-1] != ' ':
                    self.words.append(self.words.pop(i))
                    break
        last_word = self.words[-1]
        self.words = [x[:-1] if x[-1] == ' ' else x for x in self.words[:-1]]
        self.words.append(last_word)
        rv = (' ' if self.needs_space else '') + ' '.join(self.words)
        self.needs_space = rv[-1] != ' '
        return rv
