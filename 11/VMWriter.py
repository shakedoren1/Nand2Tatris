class VMWriter:
    """ Writes VM commands into the output file : out_file"""
    def __init__(self, out_file):
        """Constructor - creates a new output .vm file and gets ready to write in it"""
        self.output_file = out_file

    def write_push(self, segment, index):
        if segment == 'var':
            segment = 'local'
        if segment == 'field':
            segment = 'this'
        self.output_file.write(f"push {segment} {index}\n")  # push segment index

    def write_pop(self, segment, index):
        if segment == 'var':
            segment = 'local'
        if segment == 'field':
            segment = 'this'
        self.output_file.write(f"pop {segment} {index}\n")  # push segment index

    def write_arithmetic(self, command):
        self.output_file.write(f"{command}\n")

    def write_label(self, label):
        self.output_file.write(f"label {label}\n")

    def write_go_to(self, label):
        self.output_file.write(f"goto {label}\n")

    def write_if(self, label):
        self.output_file.write(f"if-goto {label}\n")

    def write_call(self, name, n_args):
        self.output_file.write(f"call {name} {n_args}\n")

    def write_function(self, name, n_vars):
        self.output_file.write(f"function {name} {n_vars}\n")

    def write_return(self):
        self.output_file.write("return\n")

    def write_string(self, s):
        """Writes a string in vm: creates a new string and appends all chars one-by-one"""
        self.write_push('constant', len(s))
        self.write_call('String.new', 1)
        for ch in s:
            self.write_push('constant', ord(ch))
            self.write_call('String.appendChar', 2)
