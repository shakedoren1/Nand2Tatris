class JackTokenizer:

    def __init__(self, input_stream):
        """A Constructor - Opens the input .jack file and gets ready to tokenize it."""
        self.__all_lines = []
        self.current_line = 0
        self.current_token = 0
        self.token_value = ''
        init_lines = [line.strip() for line in input_stream]
        self.__build_all_lines(init_lines)

    # A dictionary of the way documentations and comments are represented in jack
    DOC_AND_COM = {'documentation_start': '/*', 'documentation_end': '*/', 'comment': '//'}

    def __build_all_lines(self, init_lines):
        """Adds all the lines from the file to the array __all_lines,
        Removes all the documentations and comments from the file in the process."""
        quote = False
        doc_or_com = False
        for line in init_lines:
            new_line = ''
            for index, char in enumerate(line):
                if quote:
                    if char == '"':
                        quote = False
                    new_line += char
                elif char == '"':
                    quote = True
                    new_line += char
                elif line[index:index + 2] == JackTokenizer.DOC_AND_COM['documentation_start']:
                    doc_or_com = True
                elif doc_or_com:
                    if line[index - 1:index + 1] == JackTokenizer.DOC_AND_COM['documentation_end']:
                        doc_or_com = False
                elif line[index:index + 2] == JackTokenizer.DOC_AND_COM['comment']:
                    break
                # If the char is not '"' or the start or end of a documentation or the start of a comment
                else:
                    new_line += char  # insert the char to new_line
            new_line = new_line.strip()  # Removes leading and trailing whitespaces
            if new_line:  # If after removing all the documentations and comments from the line it's not empty
                self.__all_lines.append(new_line)  # append to array __all_lines

    def has_more_tokens(self) -> bool:
        """Returns: Are there more tokens in the input?"""
        # Passed the last line
        if len(self.__all_lines) - 1 <= self.current_line:
            return False
        # The last line and the last token
        if len(self.__all_lines) - 1 == self.current_line and \
                len(self.__all_lines[self.current_line]) == self.current_token:
            return False
        # Else
        return True

    def advance(self):
        """Gets the next token from the input and makes it the current token."""
        self.token_value = ''
        self.skip_spaces()  # Skip tokens that are spaces
        # Handles all the possible token types
        if not (self.is_symbol()):
            if not (self.is_int()):
                if self.__all_lines[self.current_line][self.current_token] == '"':
                    self.string_const()
                else:
                    self.string_token()
        # End of the line
        if self.current_token >= len(self.__all_lines[self.current_line]):
            self.current_line += 1
            self.current_token = 0

    def skip_spaces(self):
        """Gets the next token from the input and makes it the current token."""
        while len(self.__all_lines[self.current_line]) - 1 > self.current_token and \
                (self.__all_lines[self.current_line][self.current_token] == ' ' or
                 self.__all_lines[self.current_line][self.current_token] == '\t'):
            self.current_token += 1

    def is_symbol(self) -> bool:
        """If the current token is a symbol, updates the token value, advances current_token and returns True.
        If the current token is not a symbol, returns False.
        """
        if self.__all_lines[self.current_line][self.current_token] not in JackTokenizer.SYMBOLS:
            return False
        self.token_value = self.__all_lines[self.current_line][self.current_token]
        self.current_token += 1
        return True

    def is_int(self) -> bool:
        """If the current token is an int, updates the token value, advances current_token and returns True.
        If the current token is not an int, returns False.
        """
        is_int = False
        while len(self.__all_lines[self.current_line]) >= self.current_token and \
                self.__all_lines[self.current_line][self.current_token].isnumeric():
            is_int = True
            self.token_value += self.__all_lines[self.current_line][self.current_token]
            self.current_token += 1
        return is_int

    def string_const(self):
        """Finds the second '"' symbol and puts all the characters between the two quotation marks into the token_value,
        advances the current_token to the end of the string_const.
        """
        self.token_value += self.__all_lines[self.current_line][self.current_token]
        self.current_token += 1
        quotes = self.__all_lines[self.current_line].find('"', self.current_token)
        self.token_value += self.__all_lines[self.current_line][self.current_token:quotes + 1]
        self.current_token = quotes + 1

    def string_token(self):
        """Puts all the characters into the token_value until the end of the line or a symbol or a space is reached,
        advances the current_token to the end of the string_token.
        """
        while len(self.__all_lines[self.current_line]) >= self.current_token and \
                self.__all_lines[self.current_line][self.current_token] not in JackTokenizer.SYMBOLS and \
                self.__all_lines[self.current_line][self.current_token] != ' ':
            self.token_value += self.__all_lines[self.current_line][self.current_token]
            self.current_token += 1

    # An array of all the keywords
    KEYWORDS = ['class', 'constructor', 'function', 'method', 'field', 'static', 'var', 'int', 'char', 'boolean',
                'void', 'true', 'false', 'null', 'this', 'let', 'do', 'if', 'else', 'while', 'return']
    # An array of all the SYMBOLS
    SYMBOLS = ['{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~']

    def token_type(self) -> str:
        """Returns: The type of the current token as a constant.
        ("KEYWORD", "SYMBOL", "IDENTIFIER", "INT_CONST", "STRING_CONST")
        """
        if self.token_value in JackTokenizer.KEYWORDS:
            return 'KEYWORD'
        if self.token_value in JackTokenizer.SYMBOLS:
            return 'SYMBOL'
        if self.token_value.isnumeric():
            return 'INT_CONST'
        if self.token_value[0] == '"':
            return 'STRING_CONST'
        return 'IDENTIFIER'

    def string_val(self) -> str:
        """Returns: The string value of the current token without the opening and closing double quotes."""
        return self.token_value[1:-1]