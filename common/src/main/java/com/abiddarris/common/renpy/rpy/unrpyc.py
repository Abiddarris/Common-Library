#!/usr/bin/env python3

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


__title__ = "Unrpyc"
__version__ = 'v2.0.2'
__url__ = "https://github.com/CensoredUsername/unrpyc"


import argparse
import glob
import struct
import sys
import traceback
import zlib
from pathlib import Path

try:
    from multiprocessing import Pool, cpu_count
except ImportError:
    # Mock required support when multiprocessing is unavailable
    def cpu_count():
        return 1

import decompiler
import deobfuscate
from decompiler import astdump, translate
from decompiler.renpycompat import (pickle_safe_loads, pickle_safe_dumps, pickle_loads,
                                    pickle_detect_python2)






# API







def worker_tl(arg_tup):
    """
    This file implements the first pass of the translation feature. It gathers TL-data from the
    given rpyc files, to be used by the common worker to translate while decompiling.
    arg_tup is (args, filename). Returns the gathered TL data in the context.
    """
    args, filename = arg_tup
    context = Context()

    try:
        context.log(f'Extracting translations from {filename}...')
        ast = get_ast(filename, args.try_harder, context)

        tl_inst = translate.Translator(args.translate, True)
        tl_inst.translate_dialogue(ast)

        # this object has to be sent back to the main process, for which it needs to be pickled.
        # the default pickler cannot pickle fake classes correctly, so manually handle that here.
        context.set_result(pickle_safe_dumps((tl_inst.dialogue, tl_inst.strings)))
        context.set_state("ok")

    except Exception as e:
        context.set_error(e)
        context.log(f'Error while extracting translations from {filename}:')
        context.log(traceback.format_exc())

    return context


def worker_common(arg_tup):
    """
    The core of unrpyc. arg_tup is (args, filename). This worker will unpack the file at filename,
    decompile it, and write the output to it's corresponding rpy file.
    """

    args, filename = arg_tup
    context = Context()

    if args.translator:
        args.translator = pickle_loads(args.translator)

    try:
        decompile_rpyc(
            filename, context, overwrite=args.clobber, try_harder=args.try_harder,
            dump=args.dump, no_pyexpr=args.no_pyexpr, comparable=args.comparable,
            init_offset=args.init_offset, sl_custom_names=args.sl_custom_names,
            translator=args.translator)

    except Exception as e:
        context.set_error(e)
        context.log(f'Error while decompiling {filename}:')
        context.log(traceback.format_exc())

    return context


def run_workers(worker, common_args, private_args, parallelism):
    """
    Runs worker in parallel using multiprocessing, with a max of `parallelism` processes.
    Workers are called as worker((common_args, private_args[i])).
    Workers should return an instance of `Context` as return value.
    """

    worker_args = ((common_args, x) for x in private_args)

    results = []
    if parallelism > 1:
        with Pool(parallelism) as pool:
            for result in pool.imap(worker, worker_args, 1):
                results.append(result)

                for line in result.log_contents:
                    print(line)

                print("")

    else:
        for result in map(worker, worker_args):
            results.append(result)

            for line in result.log_contents:
                print(line)

            print("")

    return results


def parse_sl_custom_names(unparsed_arguments):
    # parse a list of strings in the format
    # classname=name-nchildren into {classname: (name, nchildren)}
    parsed_arguments = {}
    for argument in unparsed_arguments:
        content = argument.split("=")
        if len(content) != 2:
            raise Exception(f'Bad format in custom sl displayable registration: "{argument}"')

        classname, name = content
        split = name.split("-")
        if len(split) == 1:
            amount = "many"

        elif len(split) == 2:
            name, amount = split
            if amount == "0":
                amount = 0
            elif amount == "1":
                amount = 1
            elif amount == "many":
                pass
            else:
                raise Exception(
                    f'Bad child node count in custom sl displayable registration: "{argument}"')

        else:
            raise Exception(
                f'Bad format in custom sl displayable registration: "{argument}"')

        parsed_arguments[classname] = (name, amount)

    return parsed_arguments


def plural_s(n, unit):
    """Correctly uses the plural form of 'unit' when 'n' is not one"""
    return f"1 {unit}" if n == 1 else f"{n} {unit}s"


def main():
    if not sys.version_info[:2] >= (3, 9):
        raise Exception(
            f"'{__title__} {__version__}' must be executed with Python 3.9 or later.\n"
            f"You are running {sys.version}")

    # argparse usage: python3 unrpyc.py [-c] [--try-harder] [-d] [-p] file [file ...]
    cc_num = cpu_count()
    ap = argparse.ArgumentParser(description="Decompile .rpyc/.rpymc files")

    ap.add_argument(
        'file',
        type=str,
        nargs='+',
        help="The filenames to decompile. "
        "All .rpyc files in any sub-/directories passed will also be decompiled.")

    ap.add_argument(
        '-c',
        '--clobber',
        dest='clobber',
        action='store_true',
        help="Overwrites output files if they already exist.")

    ap.add_argument(
        '--try-harder',
        dest="try_harder",
        action="store_true",
        help="Tries some workarounds against common obfuscation methods. This is a lot slower.")

    ap.add_argument(
        '-p',
        '--processes',
        dest='processes',
        action='store',
        type=int,
        choices=list(range(1, cc_num)),
        default=cc_num - 1 if cc_num > 2 else 1,
        help="Use the specified number or processes to decompile. "
        "Defaults to the amount of hw threads available minus one, disabled when muliprocessing "
        "unavailable is.")

    astdump = ap.add_argument_group('astdump options', 'All unrpyc options related to ast-dumping.')
    astdump.add_argument(
        '-d',
        '--dump',
        dest='dump',
        action='store_true',
        help="Instead of decompiling, pretty print the ast to a file")

    astdump.add_argument(
        '--comparable',
        dest='comparable',
        action='store_true',
        help="Only for dumping, remove several false differences when comparing dumps. "
        "This suppresses attributes that are different even when the code is identical, such as "
        "file modification times. ")

    astdump.add_argument(
        '--no-pyexpr',
        dest='no_pyexpr',
        action='store_true',
        help="Only for dumping, disable special handling of PyExpr objects, instead printing them "
        "as strings. This is useful when comparing dumps from different versions of Ren'Py. It "
        "should only be used if necessary, since it will cause loss of information such as line "
        "numbers.")

    ap.add_argument(
        '--no-init-offset',
        dest='init_offset',
        action='store_false',
        help="By default, unrpyc attempt to guess when init offset statements were used and insert "
        "them. This is always safe to do for ren'py 8, but as it is based on a heuristic it can be "
        "disabled. The generated code is exactly equivalent, only slightly more cluttered.")

    ap.add_argument(
        '--register-sl-displayable',
        dest="sl_custom_names",
        type=str,
        nargs='+',
        help="Accepts mapping separated by '=', "
        "where the first argument is the name of the user-defined displayable object, "
        "and the second argument is a string containing the name of the displayable, "
        "potentially followed by a '-', and the amount of children the displayable takes"
        "(valid options are '0', '1' or 'many', with 'many' being the default)")

    ap.add_argument(
        '-t',
        '--translate',
        dest='translate',
        type=str,
        action='store',
        help="Changes the dialogue language in the decompiled script files, using a translation "
        "already present in the tl dir.")

    ap.add_argument(
        '--version',
        action='version',
        version=f"{__title__} {__version__}")

    args = ap.parse_args()

    # Catch impossible arg combinations so they don't produce strange errors or fail silently
    if (args.no_pyexpr or args.comparable) and not args.dump:
        ap.error("Options '--comparable' and '--no_pyexpr' require '--dump'.")

    if args.dump and args.translate:
        ap.error("Options '--translate' and '--dump' cannot be used together.")

    if args.sl_custom_names is not None:
        try:
            args.sl_custom_names = parse_sl_custom_names(args.sl_custom_names)
        except Exception as e:
            print("\n".join(e.args))
            return

    def glob_or_complain(inpath):
        """Expands wildcards and casts output to pathlike state."""
        retval = [Path(elem).resolve(strict=True) for elem in glob.glob(inpath, recursive=True)]
        if not retval:
            print(f'Input path not found: {inpath}')
        return retval

    def traverse(inpath):
        """
        Filters from input path for rpyc/rpymc files and returns them. Recurses into all given
        directories by calling itself.
        """
        if inpath.is_file() and inpath.suffix in ['.rpyc', '.rpymc']:
            yield inpath
        elif inpath.is_dir():
            for item in inpath.iterdir():
                yield from traverse(item)

    # Check paths from argparse through globing and pathlib. Constructs a tasklist with all
    # `Ren'Py compiled files` the app was assigned to process.
    worklist = []
    for entry in args.file:
        for globitem in glob_or_complain(entry):
            for elem in traverse(globitem):
                worklist.append(elem)

    # Check if we actually have files. Don't worry about no parameters passed,
    # since ArgumentParser catches that
    if not worklist:
        print("Found no script files to decompile.")
        return

    if args.processes > len(worklist):
        args.processes = len(worklist)

    print(f"Found {plural_s(len(worklist), 'file')} to process. "
          f"Performing decompilation using {plural_s(args.processes, 'worker')}.")

    # If a big file starts near the end, there could be a long time with only one thread running,
    # which is inefficient. Avoid this by starting big files first.
    worklist.sort(key=lambda x: x.stat().st_size, reverse=True)

    translation_errors = 0
    args.translator = None
    if args.translate:
        # For translation, we first need to analyse all files for translation data.
        # We then collect all of these back into the main process, and build a
        # datastructure of all of them. This datastructure is then passed to
        # all decompiling processes.
        # Note: because this data contains some FakeClasses, Multiprocessing cannot
        # pass it between processes (it pickles them, and pickle will complain about
        # these). Therefore, we need to manually pickle and unpickle it.

        print("Step 1: analysing files for translations.")
        results = run_workers(worker_tl, args, worklist, args.processes)

        print('Compiling extracted translations.')
        tl_dialogue = {}
        tl_strings = {}
        for entry in results:
            if entry.state != "ok":
                translation_errors += 1

            if entry.value:
                new_dialogue, new_strings = pickle_loads(entry.value)
                tl_dialogue.update(new_dialogue)
                tl_strings.update(new_strings)

        translator = translate.Translator(None)
        translator.dialogue = tl_dialogue
        translator.strings = tl_strings
        args.translator = pickle_safe_dumps(translator)

        print("Step 2: decompiling.")

    results = run_workers(worker_common, args, worklist, args.processes)

    success = sum(result.state == "ok" for result in results)
    skipped = sum(result.state == "skip" for result in results)
    failed = sum(result.state == "error" for result in results)
    broken = sum(result.state == "bad_header" for result in results)

    print("")
    print(f"{55 * '-'}")
    print(f"{__title__} {__version__} results summary:")
    print(f"{55 * '-'}")
    print(f"Processed {plural_s(len(results), 'file')}.")

    print(f"> {plural_s(success, 'file')} were successfully decompiled.")

    if broken:
        print(f"> {plural_s(broken, 'file')} did not have the correct header, "
              "these were ignored.")

    if failed:
        print(f"> {plural_s(failed, 'file')} failed to decompile due to errors.")

    if skipped:
        print(f"> {plural_s(skipped, 'file')} were skipped as the output file already existed.")

    if translation_errors:
        print(f"> {plural_s(translation_errors, 'file')} failed translation extraction.")


    if skipped:
        print("")
        print("To overwrite existing files instead of skipping them, use the --clobber flag.")

    if broken:
        print("")
        print("To attempt to bypass modifications to the file header, use the --try-harder flag.")

    if failed:
        print("")
        print("Errors were encountered during decompilation. Check the log for more information.")
        print("When making a bug report, please include this entire log.")

if __name__ == '__main__':
    main()
