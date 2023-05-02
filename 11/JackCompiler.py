import sys
import os

from JackTokenizer import JackTokenizer
from CompilationEngine import CompilationEngine


def compile_file(in_file, out_file):
    """Compiles a jack file to vm using CompilationEngine.
    Void function - receives input to read from and output file to write to"""
    tokens = []  # list of tokens
    tokenizer = JackTokenizer(in_file)
    while tokenizer.has_more_tokens():
        tokenizer.advance()
        if tokenizer.token_type() == 'STRING_CONST':
            tokens.append((tokenizer.token_type(), tokenizer.string_val()))
        else:
            tokens.append((tokenizer.token_type(), tokenizer.token_value))
    compilation = CompilationEngine(tokens, out_file)
    compilation.compile_class()


if "__main__" == __name__:
    """Parses the input file/directory and calls compile_file on each input file"""
    path = sys.argv[1]
    if os.path.isdir(path):
        files_to_compile = [os.path.join(path, filename) for filename in os.listdir(path)]
    else:
        files_to_compile = [path]
    for file_path in files_to_compile:
        filename, ext = os.path.splitext(file_path)
        if ext == ".jack":
            output_path = filename + ".vm"
            with open(file_path, 'r') as input_file, open(output_path, 'w') as output_file:
                compile_file(input_file, output_file)