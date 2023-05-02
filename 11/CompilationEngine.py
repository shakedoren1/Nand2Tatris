from SymbolTable import SymbolTable
from VMWriter import VMWriter


class CompilationEngine:

    KEYWORD_CONST = ['true', 'false', 'null', 'this']
    OPERATORS = {'+': 'add', '-': 'sub', '*': 'call Math.multiply 2', "/": 'call Math.divide 2', '&': 'and',
                 "^": 'shiftleft', '#': 'shiftright', '>': 'gt', '<': 'lt', '"': '"', '|': 'or', '=': 'eq'}
    UNARY_OPERATORS = {'-': 'neg', '~': 'not', '^': 'shiftleft', '#': 'shiftright'}

    def __init__(self, tokens, out_file):
        """A Constructor - Creates a new compilation engine with the given input stream and output file."""
        self.output_file = out_file
        self.tokens_list = tokens  # list of tokens by format: <token_type> {token} <token_type>
        self.current_index = 0
        self.current_token = self.tokens_list[0]  # initiate current token with first token
        self.symbol_table = SymbolTable()
        self.vm_writer = VMWriter(out_file)
        self.name_of_class = ''
        self.if_count = 0
        self.while_count = 0

    def compile_class(self):
        """Compiles a complete class."""
        self.inc_current_index()  # skip class
        self.name_of_class = self.current_token[1]
        self.inc_current_index()  # skip identifier - class name
        self.inc_current_index()  # skipped {
        while self.current_token[1] == 'static' or self.current_token[1] == 'field':
            self.compile_class_var_dec()
        while self.current_token[1] == 'function' or self.current_token[1] == 'method' or \
                self.current_token[1] == 'constructor':
            self.compile_subroutine()
        self.inc_current_index()  # skip }

    def compile_class_var_dec(self):
        """Compiles a static declaration or a field declaration.
        Also, handels a var declaration the same way.
        """
        kind = self.current_token[1]  # static or field or var
        self.inc_current_index()
        type = self.current_token[1]  # type
        self.inc_current_index()
        name = self.current_token[1]  # varName
        self.inc_current_index()
        self.symbol_table.define(name, type, kind)
        while self.current_token[1] != ';':
            self.inc_current_index()  # skip ,
            name = self.current_token[1]  # varName
            self.inc_current_index()
            self.symbol_table.define(name, type, kind)
        self.inc_current_index()  # skip ;

    def compile_subroutine(self):
        """Compiles a complete method, function or constructor."""
        self.symbol_table.subroutine()
        subroutine_kind = self.current_token[1]  # method, function or constructor
        self.inc_current_index()
        self.inc_current_index()    # skip type or void
        subroutine_name = self.current_token[1]
        self.inc_current_index()  # skip func name
        self.compile_parameter_list(subroutine_kind == 'method')
        self.compile_subroutine_body(subroutine_name, subroutine_kind)

    def compile_parameter_list(self, is_method):
        """Compiles a (possibly empty) parameter list, not including the enclosing "()"."""
        self.inc_current_index()  # skipped (
        if is_method:
            self.symbol_table.define('this', self.name_of_class, 'argument')
        while self.current_token[1] != ')':
            argument_type = self.current_token[1]  # type
            self.inc_current_index()
            argument_name = self.current_token[1]  # varName
            self.inc_current_index()
            self.symbol_table.define(argument_name, argument_type, 'argument')
            if self.current_token[1] == ',':
                self.inc_current_index()  # skip ,
        self.inc_current_index()  # skip )

    def compile_subroutine_body(self, subroutine_name, subroutine_kind):
        """Compiles a subroutine's body."""
        self.inc_current_index()  # skip {
        while self.current_token[1] == 'var':
            self.compile_var_dec()
        function_name = self.name_of_class + "." + subroutine_name
        var_count = self.symbol_table.var_count('var')
        self.vm_writer.write_function(function_name, var_count)
        if subroutine_kind == 'method':
            self.vm_writer.write_push('argument', 0)
            self.vm_writer.write_pop('pointer', 0)
        elif subroutine_kind == 'constructor':
            self.vm_writer.write_push('constant', self.symbol_table.count['field'])
            self.vm_writer.write_call('Memory.alloc', 1)
            self.vm_writer.write_pop('pointer', 0)
        self.compile_statements()
        self.inc_current_index()  # skip }

    def compile_var_dec(self):
        """Compiles a var declaration."""
        self.compile_class_var_dec()

    def compile_statements(self):
        """Compiles a sequence of statements, not including the enclosing
        "{}".
        """
        while self.current_token[1] != '}':  # change order?
            if self.current_token[1] == 'let':
                self.compile_let()
            elif self.current_token[1] == 'if':
                self.compile_if()
            elif self.current_token[1] == 'while':
                self.compile_while()
            elif self.current_token[1] == 'do':
                self.compile_do()
            elif self.current_token[1] == 'return':
                self.compile_return()

    def compile_let(self):
        """Compiles a let statement."""
        self.inc_current_index()  # skip let
        var_name = self.current_token[1]  # varName
        self.inc_current_index()  # skip var
        if self.current_token[1] == '[':
            self.vm_writer.write_push(self.symbol_table.kind_of(var_name), self.symbol_table.index_of(var_name))
            self.inc_current_index()  # skip [
            self.compile_expression()
            self.inc_current_index()  # skip ]
            self.vm_writer.write_arithmetic('add')
            self.inc_current_index()  # skip =
            self.compile_expression()
            self.vm_writer.write_pop('temp', 0)
            self.vm_writer.write_pop('pointer', 1)
            self.vm_writer.write_push('temp', 0)
            self.vm_writer.write_pop('that', 0)
        else:
            self.inc_current_index()  # skip =
            self.compile_expression()
            self.vm_writer.write_pop(self.symbol_table.kind_of(var_name), self.symbol_table.index_of(var_name))
        self.inc_current_index()  # skip ;

    def compile_if(self):
        """Compiles an if statement, possibly with a trailing else clause."""
        self.inc_current_index()  # skip if
        self.inc_current_index()  # skip (
        self.compile_expression()
        self.vm_writer.write_arithmetic('not')
        self.inc_current_index()  # skip )
        label_else = "ELSE_" + str(self.if_count)
        label_end_if = "END_IF_" + str(self.if_count)
        self.if_count += 1  # keeps the labels unique
        self.vm_writer.write_if(label_else)
        self.inc_current_index()  # skip {
        self.compile_statements()  # what to do if true
        self.vm_writer.write_go_to(label_end_if)
        self.vm_writer.write_label(label_else)
        self.inc_current_index()  # skip }
        if self.current_token[1] == 'else':
            self.inc_current_index()  # skip else
            self.inc_current_index()  # skip {
            self.compile_statements()  # what to do if false
            self.inc_current_index()  # skip }
        self.vm_writer.write_label(label_end_if)

    def compile_while(self):
        """Compiles a while statement."""
        self.inc_current_index()  # skip while
        label_while = "WHILE_" + str(self.while_count)
        self.vm_writer.write_label(label_while)
        self.inc_current_index()  # skip (
        self.compile_expression()  # the while condition
        self.inc_current_index()  # skip )
        self.vm_writer.write_arithmetic('not')
        label_end_while = "END_WHILE_" + str(self.while_count)
        self.while_count += 1  # keeps the labels unique
        self.vm_writer.write_if(label_end_while)
        self.inc_current_index()  # skip {
        self.compile_statements()  # what to do while true
        self.vm_writer.write_go_to(label_while)
        self.vm_writer.write_label(label_end_while)
        self.inc_current_index()  # skip }

    def compile_do(self):
        """Compiles a do statement."""
        self.inc_current_index()  # skip do
        self.compile_expression()
        self.vm_writer.write_pop('temp', 0)
        self.inc_current_index()  # skip ;

    def subroutine_call(self):
        """Compiles subroutine call - serves both compileDo and compileTerm, no tag needed"""
        subroutine_name = ''
        while self.current_token[1] != '(':
            subroutine_name += self.current_token[1]
            self.inc_current_index()
        arguments = 0
        sub_subroutine_name_1 = subroutine_name[:subroutine_name.find('.')]  # the name before the first .
        sub_subroutine_name_2 = subroutine_name[subroutine_name.find('.'):]  # the name after the first .
        if '.' not in subroutine_name:
            subroutine_name = self.name_of_class + '.' + subroutine_name
            self.vm_writer.write_push('pointer', 0)
            arguments += 1
        elif self.symbol_table.kind_of(sub_subroutine_name_1) is not None:
            self.vm_writer.write_push(
                self.symbol_table.kind_of(sub_subroutine_name_1),  self.symbol_table.index_of(sub_subroutine_name_1))
            arguments += 1
            subroutine_name = str(self.symbol_table.type_of(sub_subroutine_name_1)) + sub_subroutine_name_2
        self.inc_current_index()  # skipped (
        arguments += self.compile_expression_list()
        self.inc_current_index()  # skipped )
        self.vm_writer.write_call(subroutine_name, arguments)

    def compile_return(self):
        """Compiles a return statement."""
        self.inc_current_index()  # skip return
        if self.current_token[1] == ';':
            self.vm_writer.write_push('constant', 0)
        else:
            self.compile_expression()
        self.vm_writer.write_return()
        self.inc_current_index()  # skip ;

    def compile_expression(self):
        """Compiles an expression."""
        first_term = True  # flag for first term in the expression
        op = ''
        # while condition - expression will only end at ) or ; or ] or ,
        while self.current_token[1] != ')' and self.current_token[1] \
                != ';' and self.current_token[1] \
                != ']' and self.current_token[1] != ',':
            token = self.current_token[1]  # get token value
            if token in CompilationEngine.OPERATORS:  # if token is an operator
                if first_term:  # unaryOp term
                    self.compile_term()
                else:
                    if op != '':
                        self.vm_writer.write_arithmetic(CompilationEngine.OPERATORS[op])
                    op = self.current_token[1]
                    self.inc_current_index()
            else:  # token is not an operator
                self.compile_term()
            first_term = False
        if op != '':
            self.vm_writer.write_arithmetic(CompilationEngine.OPERATORS[op])

    def compile_term(self):
        """Compiles a term."""
        if self.current_token[0] == 'INT_CONST':  # int
            self.vm_writer.write_push('constant', self.current_token[1])
            self.inc_current_index()
        elif self.current_token[0] == 'KEYWORD':  # this,true,false or null
            if self.current_token[1] == 'this':
                self.vm_writer.write_push('pointer', 0)
            else:
                self.vm_writer.write_push('constant', 0)
                if self.current_token[1] == 'true':
                    self.vm_writer.write_arithmetic('not')
            self.inc_current_index()
        elif self.current_token[0] == 'STRING_CONST':  # string
            self.vm_writer.write_string(self.current_token[1])
            self.inc_current_index()
        elif self.current_token[1] == '(':  # '('expression')'
            self.inc_current_index()  # skip (
            self.compile_expression()
            self.inc_current_index()  # skip )
        elif self.current_token[1] in CompilationEngine.UNARY_OPERATORS:  # unaryOp(term)
            op = CompilationEngine.UNARY_OPERATORS[self.current_token[1]]
            self.inc_current_index()
            self.compile_term()
            self.vm_writer.write_arithmetic(op)
        elif self.symbol_table.kind_of(self.current_token[1]) is not None:  # identifier
            if self.tokens_list[self.current_index + 1][1] == '.':  # class
                self.subroutine_call()
            else:
                self.vm_writer.write_push(
                    self.symbol_table.kind_of(self.current_token[1]),
                    self.symbol_table.index_of(self.current_token[1]))
                self.inc_current_index()
                if self.tokens_list[self.current_index][1] == '[':  # array
                    self.inc_current_index()  # skip [
                    self.compile_expression()  # index
                    self.vm_writer.write_arithmetic('add')  # make 'that' point to array+index
                    self.vm_writer.write_pop('pointer', 1)
                    self.vm_writer.write_push('that', 0)
                    self.inc_current_index()  # skip ]
        else:
            self.subroutine_call()

    def compile_expression_list(self) -> int:
        """Compiles a (possibly empty) comma-separated list of expressions."""
        count = 0  # counts num of expressions in the list
        while self.current_token[1] != ')':
            if self.current_token[1] == ',':
                self.inc_current_index()  # skip ,
            count += 1
            self.compile_expression()
        return count

    def inc_current_index(self):
        """Increment index"""
        if self.current_index < len(self.tokens_list) - 1:  # next token exists
            self.current_index += 1
            self.current_token = self.tokens_list[self.current_index]