import sys
import os

from JackTokenizer import JackTokenizer
from CompilationEngine import CompilationEngine

TOKEN_TYPES = {"KEYWORD": '<keyword> {} </keyword>\n',
               "SYMBOL": '<symbol> {} </symbol>\n',
               "IDENTIFIER": '<identifier> {} </identifier>\n',
               "INT_CONST": '<integerConstant> {} </integerConstant>\n',
               "STRING_CONST": '<stringConstant> {} </stringConstant>\n'}


def compile_file(in_file, out_file):
    """Compiles a jack file to xml using CompilationEngine.
    Void function - receives input to read from and output file to write to"""
    tokens = []  # list of tokens
    tokenizer = JackTokenizer(in_file)
    while tokenizer.has_more_tokens():
        tokenizer.advance()
        if tokenizer.token_type() == 'STRING_CONST':
            tokens.append(TOKEN_TYPES[tokenizer.token_type()].format(tokenizer.string_val()))
        else:
            tokens.append(TOKEN_TYPES[tokenizer.token_type()].format(tokenizer.token_value))
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
            output_path = filename + ".xml"
            with open(file_path, 'r') as input_file, open(output_path, 'w') as output_file:
                compile_file(input_file, output_file)
