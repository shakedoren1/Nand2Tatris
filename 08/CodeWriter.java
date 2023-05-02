import java.io.*;

public class CodeWriter {

    private final BufferedWriter writer; // A reader for our InputStream
    private int function_index;
    private final String tempIndex = "5";


    public CodeWriter(File file) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(file,true));
        this.function_index = 1;
    }

    /**
     * Writes to the output file the assembly code that implements the given arithmetic-logic command.
     */
    public void writeArithmetic(String segment) throws IOException {
        SPMinus(); // necessary for all options
        if (!(segment.equals("not") || segment.equals("neg"))) { // necessary for 2 arguments commands - all but not and neg
            D_SP();
            SPMinus();
        }
        if (segment.equals("add")) {
            writer.write("@SP\n" +              // @SP
                    "A=M\n" +               //A=M
                    "M=D+M\n");             // M=D+M
        } else if (segment.equals("sub")) {
            writer.write("@SP\n" +              // @SP
                    "A=M\n" +               //A=M
                    "M=M-D\n");             // M=M-D
        } else if (segment.equals("neg")) {
            writer.write("@SP\n" +              // @SP
                    "A=M\n" +               //A=M
                    "M=-M\n");              // M=-M
        } else if (segment.equals("eq")) {
            writer.write("A=M\n"+               // A=M
                    "M=M-D\n"+              // M=M-D
                    "D=M\n"+                // D=M
                    String.format("@TRUE%d\n", this.function_index)+    // @TRUEi
                    "D;JEQ\n"+              // D;JEQ
                    String.format("@FALSE%d\n", this.function_index)+   // @FALSEi
                    "D;JNE\n");             // D;JNE
            writeTF(function_index); // write functions (TRUEi),(FALSEi),(CONTINUEi)
            this.function_index++;
        } else if (segment.equals("gt")) {
            writer.write("A=M\n"+               // A=M
                    "M=M-D\n"+              // M=M-D
                    "D=M\n"+                // D=M
                    String.format("@TRUE%d\n", this.function_index)+    // @TRUEi
                    "D;JGT\n"+              // D;JGT
                    String.format("@FALSE%d\n", this.function_index)+   // @FALSEi
                    "D;JLE\n");             // D;JNE
            writeTF(function_index);
            this.function_index++;
        } else if (segment.equals("lt")) {
            writer.write("A=M\n"+               // A=M
                    "M=M-D\n"+              // M=M-D
                    "D=M\n"+                // D=M
                    String.format("@TRUE%d\n", this.function_index)+// @TRUEi
                    "D;JLT\n"+              // D;JLT
                    String.format("@FALSE%d\n", this.function_index)+// @FALSEi
                    "D;JGE\n");             // D;JGE
            writeTF(function_index);
            this.function_index++;
        } else if (segment.equals("and")) {
            writer.write("A=M\n"+               // A=M
                    "M=D&M\n");             // M=D&M
        } else if (segment.equals("or")) {
            writer.write("A=M\n"+               // A=M
                    "M=D|M\n");             // M=D|M
        } else {   // segment = "not"
            writer.write("A=M\n"+               // A=M
                    "M=!M\n");              // M=!M
        }
        SPPlus();
    }

    /**
     * Writes to the output file the assembly code that implements the given push or pop command.
     */
    public void writePushPop(Parser.ComType command, String segment, int index) throws IOException {
        // if the command is a push command, uses the necessary help functions to write the matching hack code for each segment
        if (command == Parser.ComType.C_PUSH) {
            // for constant - only loads the index to the stack
            if (segment.equals("constant")) {
                RAM_SP(Integer.toString(index));
            } else {
                // for pointer - special case explained in RAM_str1_str2 function
                if (segment.equals("pointer")) {
                    if (index == 0) {
                        RAM_from_to("THIS", "SP", 1);
                    } else {
                        RAM_from_to("THAT", "SP", 1);
                    }
                    // for every other segment sets the address to be segment + index and calls RAM_str1_str2 function with the necessary parameters
                } else {
                    if (segment.equals("local")) {
                        addr("LCL", index);
                    } else if (segment.equals("argument")) {
                        addr("ARG", index);
                    } else if (segment.equals("this")) {
                        addr("THIS", index);
                    } else if (segment.equals("that")) {
                        addr("THAT", index);
                    } else {  // segment = "TEMP"
                        addr(tempIndex, index);
                    }
                    RAM_from_to("ADDR", "SP", 0);
                }
            }
            // advances the stack pointer to the next index
            SPPlus();
            // same logic applies to push commands
        } else {
            if (segment.equals("pointer")) {
                SPMinus();
                if (index == 0) {
                    RAM_from_to("SP", "THIS", 2);
                } else {
                    RAM_from_to("SP", "THAT", 2);
                }
            } else {
                if (segment.equals("local")) {
                    addr("LCL", index);
                } else if (segment.equals("argument")) {
                    addr("ARG", index);
                } else if (segment.equals("this")) {
                    addr("THIS", index);
                } else if (segment.equals("that")) {
                    addr("THAT", index);
                } else {   // segment = "TEMP"
                    addr(tempIndex, index);
                }
                SPMinus();
                RAM_from_to("SP", "ADDR", 0);
            }
        }
    }

    /**
     * Writes to the output file the assembly code that implements the given static command.
     */
    public void staticPushPop(Parser.ComType command, String str) throws IOException {
        if (command == Parser.ComType.C_PUSH) {
            RAM_from_to(str, "SP", 1);
            SPPlus();
        } else { // POP STATIC.X
            SPMinus();
            RAM_from_to("SP", str, 2);
        }
    }

    /**
     * Writes to the output file the assembly code that implements the label command.
     */
    public void writeLabel(String label) throws IOException {
        // adds a label
        writer.write("(" + label + ")\n");       // (label)
    }

    /**
     * Writes to the output file the assembly code that implements the goto command.
     */
    public void writeGoto(String label) throws IOException {
        // jumps to the label
        writer.write("@" + label+ "\n" +        // @label
                "0;JMP\n");                 // 0;JMP
    }

    /**
     * Writes to the output file the assembly code that implements the if-goto command.
     */
    public void writeIf(String label) throws IOException {
        SPMinus(); // point to the last variable in the stack
        D_SP(); // puts the value of the last variable of the stack in D
        // jumps to the label if D is not 0
        writer.write("@" + label +"\n" +         // @label
                "D;JNE\n");                 // 0;JMP
    }

    /**
     * Writes to the output file the assembly code that implements the Bootstrap.
     */
    public void writeBootstrap() throws IOException {
        String SP_init = "256";
        writer.write("@" + SP_init +"\n" +      // @SP_init
                        "D=A\n" +                   // D=A
                        "@SP\n" +                   // @SP
                        "M=D\n");                   // M=D
        writeCall("Sys.init", 0);
    }

    /**
     * Writes to the output file the assembly code that implements the function command.
     */
    public void writeFunction(String functionName, int nVars) throws IOException {
        writeLabel(functionName);   // function's entry point (label)
        // initializes nVars local variables to 0
        for (int i = 0; i < nVars; i++) {
            writePushPop(Parser.ComType.C_PUSH, "constant", 0);
        }
    }

    /**
     * Writes to the output file the assembly code that implements the call command.
     */
    public void writeCall(String functionName, int nArgs) throws IOException {
        String uniqueLabel = functionName + function_index;  // creating a unique label for the return address
        function_index += 1;    // advancing the function index to keep the labels unique
        RAM_SP(uniqueLabel);    // inserting the return address to the stack
        SPPlus();   // advancing the stack pointer
        String [] pointers = {"LCL", "ARG", "THIS", "THAT"};
        // inserting the segment pointers to the stack
        for (String segment : pointers) {
            RAM_from_to(segment, "SP", 1);
            SPPlus();
        }
        // Reposition ARG: ARG = SP - 5 - nArgs
        String repositionARG = "5";
        writer.write("@SP\n" +              // @SP
                "D=M\n" +               // D=M
                "@" + repositionARG + "\n" +    // @repositionARG
                "D=D-A\n" +             // D=D-A
                "@" + nArgs + "\n" +    // @nArgs
                "D=D-A\n" +             // D=D-A
                "@ARG\n" +              // @ARG
                "M=D\n");               // M=D
        // Reposition LCL: LCL = SP
        writer.write("@SP\n" +              // @SP
                "D=M\n" +               // D=M
                "@LCL\n" +              // @LCL
                "M=D\n");               // M=D
        writeGoto(functionName);    // Transfer control to the callee
        writeLabel(uniqueLabel);    // Injects this label into the code
    }

    /**
     * Writes to the output file the assembly code that implements the return command.
     */
    public void writeReturn() throws IOException {
        // gets the address at the frame's end: endFrame = LCL
        writer.write("@LCL\n" +              // @LCL
                "D=M\n" +               // D=M
                "@endFrame\n" +         // @endFrame
                "M=D\n");               // M=D
        // gets the return address: retAddr = *(endFrame - returnAddress)
        String returnAddress = "5";
        writer.write("@" + returnAddress + "\n" +   // @returnAddress
                "D=D-A\n" +             // D=D-A
                "A=D\n" +               // A=D
                "D=M\n" +               // D=M
                "@retAddr\n" +          // @retAddr
                "M=D\n");               // M=D
        // puts the return value for the caller: *ARG = pop()
        writePushPop(Parser.ComType.C_POP, "argument", 0);
        // reposition SP: SP = ARG + 1
        writer.write("@ARG\n" +             // @ARG
                "D=M\n" +               // D=M
                "D=D+1\n" +             // D=D+1
                "@SP\n" +               // @SP
                "M=D\n");               // M=D
        String [] pointers = {"THAT", "THIS", "ARG", "LCL"};
        // restores segments
        for (String segment : pointers) {
            restore(segment);
        }
        // jumps to the return address
        writer.write("@retAddr\n" +        // @retAddr
                        "A=M\n" +           // A=M
                        "0;JMP\n");         // 0;JMP
    }

    /**
     * Writes to the file in HACK: segment = *(endFrame - 1)
     */
    public void restore(String segment) throws IOException {
        writer.write("@endFrame\n" +        // @endFrame
                "M=M-1\n" +             // M=M-1
                "A=M\n" +               // A=M
                "D=M\n" +               // D=M
                "@" + segment + "\n" +  // @segment
                "M=D\n");               // M=D
    }

    /**
     * Writes to the file in HACK: SP--
     */
    public void SPMinus() throws IOException {
        writer.write("@SP\n" +              // @SP
                "M=M-1\n");             // M=M-1
    }

    /**
     * Writes to the file in HACK: SP++
     */
    public void SPPlus() throws IOException {
        writer.write("@SP\n" +              // @SP
                "M=M+1\n");             // M=M-1
    }

    /**
     * Writes to the file in HACK: addr = segment + index
     * one exception for temp case because it's an int and not a cell in memory
     */
    public void addr(String segment, int index) throws IOException {
        writer.write("@" + segment + "\n");  // @segment
        if (segment.equals(tempIndex)) // if temp
            writer.write("D=A\n");          // @D=A
        else
            writer.write("D=M\n");          // @D=M
        writer.write("@" + index + "\n" +    // @index
                "D=D+A\n" +             // D=D+A
                "@ADDR\n" +             // @ADDR
                "M=D\n");               // M=D
    }

    /**
     * Writes to the file in HACK: D = RAM[SP]
     */
    public void D_SP() throws IOException {
        writer.write("@SP\n" +              // @SP
                "A=M\n" +               // A=M
                "D=M\n");               // D=M
    }

    /**
     * Writes to the file in HACK: RAM[SP] <- index
     */
    public void RAM_SP(String index) throws IOException {
        writer.write("@" + index + "\n" +   // @i
                "D=A\n" +               // D=A
                "@SP\n" +               // @SP
                "A=M\n" +               // A=M
                "M=D\n");               // M=D
    }

    /**
     * Writes to the file in HACK: RAM[to] <- RAM[from]
     * one exception for the case of push/pop pointer that needs to update the inside of a cell and not a location in memory
     */
    public void RAM_from_to(String from, String to, int pointer) throws IOException {
        writer.write("@" + from + "\n");   // @str2
        if (pointer != 1) // if not pushPointer
            writer.write("A=M\n");          // A=M
        writer.write("D=M\n" +              // D=M
                "@" + to + "\n");     // @str1
        if (pointer != 2) // if not popPointer
            writer.write("A=M\n");          // A=M
        writer.write("M=D\n");              // M=D
    }

    /**
     * Writes TRUE/FALSE functions in hack file: TRUE puts 1 in SP, FALSE puts 0.
     * index recieved to create a unique name for each function
     */
    public void writeTF(int i) throws IOException {
        writer.write(String.format("(TRUE%d)\n", i)+ // (TRUEi)
                "@SP\n"+                   // @SP
                "A=M\n"+                   // A=M
                "M=-1\n"+                   // M=-1
                String.format("@CONTINUE%d\n", i)+ //@CONTINUEi
                "0;JMP\n"+                 // 0;JMP
                String.format("(FALSE%d)\n", i)+ // (FALSEi)
                "@SP\n"+                    // @SP
                "A=M\n"+                    // A=M
                "M=0\n"+                    // M=0
                String.format("@CONTINUE%d\n", i)+// @CONTINUEi
                "0;JMP\n"+                  // 0;JMP
                String.format("(CONTINUE%d)\n", i));            //(CONTINUEi)
    }

    /**
     * adds an infinite loop at the end of the code and closes the output file.
     */
    public void close() throws IOException {
        writer.write("(END)\n"+             // (END)
                "@END\n"+               // @END
                "0;JMP\n");             // 0;JMP
        writer.close();
    }

}