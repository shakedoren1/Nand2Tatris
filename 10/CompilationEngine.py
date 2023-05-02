class CompilationEngine:
    """Gets input from a JackTokenizer and writes parsed output in the output file."""
    OPERATORS = {'+': '+', '-': '-', '*': '*', "/": '/', '&': '&amp;', "^": '^', '#': '#', '>': '&gt;', '<': '&lt;',
                 '"': '"', '|': '|', '=': '='}

    KEYWORD_CONST = ['true', 'false', 'null', 'this']

    def __init__(self, inp_stream, out_stream):
        """A Constructor - Creates a new compilation engine with the given input stream and output file."""
        self.output_file = out_stream
        self.tokens_list = inp_stream  # list of tokens by format: <token_type> {token} <token_type>
        self.current_index = 0
        self.current_token = self.tokens_list[0]  # initiate current token with first token

    def compile_class(self):
        """Compiles a complete class."""
        self.output_file.write('<class>\n')
        self.process()  # class
        self.process()  # identifier - class name
        self.process()  # {
        #  compile var decs
        while self.current_token == '<keyword> static </keyword>\n' or \
                self.current_token == '<keyword> field </keyword>\n':
            self.output_file.write('<classVarDec>\n')
            self.compile_class_var_dec()
            self.output_file.write('</classVarDec>\n')
        # compile subroutines
        while self.current_token == '<keyword> function </keyword>\n' or \
                self.current_token == '<keyword> method </keyword>\n' or \
                self.current_token == '<keyword> constructor </keyword>\n':
            self.compile_subroutine()
        self.process()  # }
        self.output_file.write('</class>\n')

    def compile_class_var_dec(self):
        """Compiles a static declaration or a field declaration."""
        self.process()  # static or field
        self.process()  # type
        self.process()  # varName
        # list of variables: x,y,z,...
        while self.current_token != '<symbol> ; </symbol>\n':
            self.process()  # , or varname
        self.process()  # ;

    def compile_subroutine(self):
        """Compiles a complete method, function, or constructor."""
        self.output_file.write('<subroutineDec>\n')
        self.process()  # method/function/constructor
        self.process()  # type or void
        self.process()  # name
        self.process()  # (
        self.output_file.write('<parameterList>\n')
        self.compile_parameter_list()
        self.output_file.write('</parameterList>\n')
        self.process()  # )
        self.output_file.write('<subroutineBody>\n')
        self.compile_subroutine_body()
        self.output_file.write('</subroutineBody>\n')
        self.output_file.write('</subroutineDec>\n')

    def compile_parameter_list(self):
        """Compiles a (possibly empty) parameter list, not including the enclosing "()"."""
        while self.current_token != '<symbol> ) </symbol>\n':
            self.process()

    def compile_subroutine_body(self):
        """Compiles a subroutine's body."""
        self.process()  # {
        # compile vars in the top of subroutine
        while self.current_token == '<keyword> var </keyword>\n':
            self.output_file.write('<varDec>\n')
            self.compile_var_dec()
            self.output_file.write('</varDec>\n')
        self.compile_statements()
        self.process()  # }

    def compile_var_dec(self):
        """Compiles a var declaration."""
        self.process()  # var
        self.process()  # type
        # list of variables: x,y,z,...
        while self.current_token != '<symbol> ; </symbol>\n':
            self.process()  # varname
        self.process()  # ;

    def compile_statements(self):
        """Compiles a sequence of statements, not including the enclosing 
        "{}".
        """
        self.output_file.write('<statements>\n')
        while self.current_token != '<symbol> } </symbol>\n':
            if self.current_token == '<keyword> do </keyword>\n':
                self.output_file.write('<doStatement>\n')
                self.compile_do()
                self.output_file.write('</doStatement>\n')
            elif self.current_token == '<keyword> let </keyword>\n':
                self.output_file.write('<letStatement>\n')
                self.compile_let()
                self.output_file.write('</letStatement>\n')
            elif self.current_token == '<keyword> while </keyword>\n':
                self.output_file.write('<whileStatement>\n')
                self.compile_while()
                self.output_file.write('</whileStatement>\n')
            elif self.current_token == '<keyword> return </keyword>\n':
                self.output_file.write('<returnStatement>\n')
                self.compile_return()
                self.output_file.write('</returnStatement>\n')
            elif self.current_token == '<keyword> if </keyword>\n':
                self.output_file.write('<ifStatement>\n')
                self.compile_if()
                self.output_file.write('</ifStatement>\n')
        self.output_file.write('</statements>\n')

    def compile_let(self):
        """Compiles a let statement."""
        self.process()  # let
        self.process()  # varname
        if self.current_token == "<symbol> [ </symbol>\n":
            self.process()  # [
            self.compile_expression()
            self.process()  # ]
        self.process()  # =
        self.compile_expression()
        self.process()  # ;

    def compile_if(self):
        """Compiles an if statement, possibly with a trailing else clause."""
        self.process()  # if
        self.process()  # (
        self.compile_expression()
        self.process()  # )
        self.process()  # {
        self.compile_statements()
        self.process()  # }
        if self.current_token == '<keyword> else </keyword>\n':
            self.process()  # else
            self.process()  # {
            self.compile_statements()
            self.process()  # }

    def compile_while(self):
        """Compiles a while statement."""
        self.process()  # while
        self.process()  # (
        self.compile_expression()
        self.process()  # )
        self.process()  # {
        self.compile_statements()
        self.process()  # }

    def compile_do(self):
        """Compiles a do statement."""
        self.process()  # do
        self.subroutine_call()  # no tag needed
        self.process()  # ;

    def subroutine_call(self):
        """Compiles subroutine call - serves both compileDo and compileTerm, no tag needed"""
        self.process()  # either subroutinename or class/var name
        if self.current_token == '<symbol> . </symbol>\n':
            self.process()  # .
            self.process()  # sub name
        self.process()  # (
        self.compile_expression_list()  # <expressionList> tag written inside compile_expression_list
        self.process()  # )

    def compile_return(self):
        """Compiles a return statement."""
        self.process()  # return
        if self.current_token != '<symbol> ; </symbol>\n':  # compile a possible expression
            self.compile_expression()
        self.process()  # ;

    def compile_expression(self):
        """Compiles an expression."""
        self.output_file.write('<expression>\n')
        not_first = False  # flag for first expression term in the expression
        # while condition - expression will only end at ) or ; or ] or ,
        while self.current_token != '<symbol> ) </symbol>\n' and self.current_token \
                != '<symbol> ; </symbol>\n' and self.current_token \
                != '<symbol> ] </symbol>\n' and self.current_token != '<symbol> , </symbol>\n':
            token = self.current_token.split()[1]  # get clean token
            if token in CompilationEngine.OPERATORS:  # if token is an operator
                if token != '-':
                    self.output_file.write('<symbol> ' + CompilationEngine.OPERATORS[token] + ' </symbol>\n')
                    self.inc_current_index()
                elif not_first:  # '-' only as not first term in expression is an op and not unaryOp
                    self.output_file.write('<symbol> ' + CompilationEngine.OPERATORS[token] + ' </symbol>\n')
                    self.inc_current_index()
            self.output_file.write('<term>\n')
            self.compile_term()
            self.output_file.write('</term>\n')
            not_first = True
        self.output_file.write('</expression>\n')

    def compile_term(self) -> None:
        """Compiles a term."""
        current_type = self.current_token.split()[0]  #
        if current_type == '<integerConstant>' or current_type == '<stringConstant>' or current_type == '<keyword>':
            self.process()  # simple term
        elif current_type == '<identifier>':
            next_token = self.tokens_list[self.current_index + 1]  # certainly exists
            if next_token == '<symbol> . </symbol>\n' or next_token == '<symbol> ( </symbol>\n':
                self.subroutine_call()
            elif next_token == '<symbol> [ </symbol>\n':
                self.process()  # varName
                self.process()  # [
                self.compile_expression()  # expression
                self.process()  # ]
            else:  # next token is ';'
                self.process()  # varName
        elif self.current_token == '<symbol> ( </symbol>\n':  # term is '('expression')' kind
            self.process()  # (
            self.compile_expression()
            self.process()  # )
        elif self.current_token == '<symbol> - </symbol>\n' or self.current_token == '<symbol> ~ </symbol>\n':
            #  term is unaryOp term
            self.process()  # unaryOp
            self.output_file.write('<term>\n')
            self.compile_term()  # term
            self.output_file.write('</term>\n')

    def compile_expression_list(self) -> None:
        """Compiles a (possibly empty) comma-separated list of expressions."""
        self.output_file.write('<expressionList>\n')
        while self.current_token != '<symbol> ) </symbol>\n':
            self.compile_expression()
            if self.current_token == '<symbol> , </symbol>\n':
                self.process()  # ,
        self.output_file.write('</expressionList>\n')

    def process(self):
        """ Processes current token: write it and increment index. """
        self.output_file.write(self.current_token)
        self.inc_current_index()

    def inc_current_index(self):
        """Increment index"""
        if self.current_index < len(self.tokens_list) - 1:  # next token exists
            self.current_index += 1
            self.current_token = self.tokens_list[self.current_index]
