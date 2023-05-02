class SymbolTable:

    def __init__(self):
        """A Constructor - Creates a new Symbol table."""
        # Dictionaries that will represent the tables in the format: {"name":["type", "kind", count]}
        self.class_level = dict()
        self.subroutine_level = dict()
        self.count = {'static': 0, 'field': 0, 'argument': 0, 'local': 0, 'var': 0}

    def subroutine(self):
        """initializes the values for a subroutine-level symbol table"""
        self.subroutine_level.clear()
        self.count['argument'] = 0
        self.count['local'] = 0
        self.count['var'] = 0

    def define(self, name, type, kind):
        """Adds to the table a new variable of a given name, type, and kind.
        assigns to it the index value of that kind, and adds 1 to the index
        """
        if kind == 'static' or kind == 'field':
            self.class_level[name] = [type, kind, self.count[kind]]
            self.count[kind] += 1
        else:
            self.subroutine_level[name] = [type, kind, self.count[kind]]
            self.count[kind] += 1

    def var_count(self, kind) -> int:
        """Returns: The number of variables of a given kind already defined in the table."""
        return self.count[kind]

    def kind_of(self, name) -> str:
        """Returns: The kind of variables of the named identifier.
        If the identifier is not found, returns NONE"""
        if name in self.subroutine_level:
            return self.subroutine_level[name][1]
        elif name in self.class_level:
            return self.class_level[name][1]

    def type_of(self, name) -> str:
        """Returns: The type of the named variable."""
        if name in self.subroutine_level:
            return self.subroutine_level[name][0]
        elif name in self.class_level:
            return self.class_level[name][0]

    def index_of(self, name) -> int:
        """Returns: The index of the named variable."""
        if name in self.subroutine_level:
            return self.subroutine_level[name][2]
        elif name in self.class_level:
            return self.class_level[name][2]
