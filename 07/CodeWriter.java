import java.io.*;

public class CodeWriter {

    private final BufferedWriter writer; // A reader for our InputStream
    private int function_index;
    private final String tempIndex = "5";

    public CodeWriter(File file) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(file));
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
                    "A=M\n" +                 //A=M
                    "M=D+M\n");             // M=D+M
        } else if (segment.equals("sub")) {
            writer.write("@SP\n" +              // @SP
                    "A=M\n" +                 //A=M
                    "M=M-D\n");             // M=M-D
        } else if (segment.equals("neg")) {
            writer.write("@SP\n" +              // @SP
                    "A=M\n" +                 //A=M
                    "M=-M\n");              // M=-M
        } else if (segment.equals("eq")) {
            writer.write("A=M\n"+               // A=M
                    "M=M-D\n"+              // M=M-D
                    "D=M\n"+                // D=M
                    String.format("@TRUE%d\n", this.function_index)+// @TRUEi
                    "D;JEQ\n"+              // D;JEQ
                    String.format("@FALSE%d\n", this.function_index)+// @FALSEi
                    "D;JNE\n");             // D;JNE
            writeTF(function_index); // write functions (TRUEi),(FALSEi),(CONTINUEi)
            this.function_index++;
        } else if (segment.equals("gt")) {
            writer.write("A=M\n"+               // A=M
                    "M=M-D\n"+              // M=M-D
                    "D=M\n"+                // D=M
                    String.format("@TRUE%d\n", this.function_index)+// @TRUEi
                    "D;JGT\n"+              // D;JGT
                    String.format("@FALSE%d\n", this.function_index)+// @FALSEi
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
            writer.write("A=M\n"+           // A=M
                    "M=D&M\n");         // M=D&M
        } else if (segment.equals("or")) {
            writer.write("A=M\n"+           // A=M
                    "M=D|M\n");         // M=D|M
        } else {   // segment = "not"
            writer.write("A=M\n"+           // A=M
                    "M=!M\n");          // M=!M
        }
        SPPlus();
    }

    /**
     * Writes to the output file the assembly code that implements the given push or pop command.
     */
    public void writePushPop(Parser.ComType command, String segment, int index) throws IOException {
        if (command == Parser.ComType.C_PUSH) {
            if (segment.equals("constant")) {
                RAM_SP(index);
            } else {
                if (segment.equals("pointer")) {
                    if (index == 0) {
                        RAM_str1_str2("THIS", "SP", 1);
                    } else {
                        RAM_str1_str2("THAT", "SP", 1);
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
                    } else {  // segment = "TEMP"
                        addr(tempIndex, index);
                    }
                    RAM_str1_str2("ADDR", "SP", 0);
                }
            }
            SPPlus();
        } else {
            if (segment.equals("pointer")) {
                SPMinus();
                if (index == 0) {
                    RAM_str1_str2("SP", "THIS", 2);
                } else {
                    RAM_str1_str2("SP", "THAT", 2);
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
                RAM_str1_str2("SP", "ADDR", 0);
            }
        }
    }

    /**
     * Writes to the output file the assembly code that implements the given static command.
     */
    public void staticPushPop(Parser.ComType command, String str) throws IOException {
        if (command == Parser.ComType.C_PUSH) {
            RAM_str1_str2(str, "SP", 0);
            SPPlus();
        } else {
            SPMinus();
            RAM_str1_str2("SP", str, 0);
        }
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
            writer.write("D=A\n");  // @D=A
        else
            writer.write("D=M\n");  // @D=M
        writer.write("@" + index + "\n" +    // @index
                        "D=D+A\n" +             // D=D+M
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
    public void RAM_SP(int index) throws IOException {
        writer.write("@" + index + "\n" +   // @i
                "D=A\n" +               // D=A
                "@SP\n" +               // @SP
                "A=M\n" +               // A=M
                "M=D\n");               // M=D
    }

    /**
     * Writes to the file in HACK: RAM[str1] <- RAM[str2]
     * one exception for the case of push/pop pointer that needs to update the inside of a cell and not a location in memory
     */
    public void RAM_str1_str2(String str1, String str2, int popPointer) throws IOException {
        writer.write("@" + str1 + "\n");   // @str1
        if (popPointer != 1) // if not pushPointer
            writer.write("A=M\n");          // A=M
        writer.write("D=M\n" +              // D=M
                        "@" + str2 + "\n");     // @str2
        if (popPointer != 2) // if not popPointer
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
     * closes the output file.
     */
    public void close() throws IOException {
        writer.write("(END)\n"+             // (END)
                "@END\n"+      // @END
                "0;JMP\n");    // 0;JMP
        writer.close();
    }
}
