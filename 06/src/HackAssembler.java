import java.io.*;

public class HackAssembler {

    public static void main (String[]args) throws IOException {
        try {
            assemble(args[0]); // Calling assemble with the path received
            //assemble("C:/Users/shake/nand2tetris/06/p06/src/EasyTest.asm"); // <--------For local Testing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A helper function that assemble the asm code using Parser and Code classes.
     */
    public static void assemble (String asmName) throws IOException {
        String cleanedFilePath = asmClean(asmName); // Cleans the asm file

        //handling symbols
        String symboledFilePath = asmSymbols(cleanedFilePath);

        String hackPath = asmName.substring(0, asmName.length()-4); // removes the '.asm' from the path on the file
        File asmFile = new File (symboledFilePath); // open the cleaned and symboled asm file
        File hackFile = new File (String.format("%s.hack", hackPath)); // create hack file
        FileInputStream inpStream = new FileInputStream(asmFile); // create input stream to send to Parser
        // getting ready to write
        FileWriter hackFW = new FileWriter (hackFile);
        BufferedWriter hackBW = new BufferedWriter(hackFW);

        Parser parse = new Parser(inpStream);
        Code code = new Code();

        // parsing through the entire file using Parser and Code classes on each line as needed
        StringBuilder lineToWrite = new StringBuilder();
        String destStr, compStr, jumpStr, symbolStr;
        while (parse.hasMoreCommands()) {
            parse.advance();
            // Converting a C command to a 16 digit binary code
            if (parse.commandType() == Parser.ComType.C_COMMAND) {
                lineToWrite.append("111");
                compStr = parse.comp();
                destStr = parse.dest();
                jumpStr = parse.jump();
                lineToWrite.append(code.comp(compStr));
                lineToWrite.append(code.destOrJump(destStr));
                lineToWrite.append(code.destOrJump(jumpStr));
            }
            // Converting an A command to a 16 digit binary code
            else if ((parse.commandType() == Parser.ComType.A_COMMAND) || (parse.commandType() == Parser.ComType.L_COMMAND)) {
                symbolStr = parse.symbol();
                symbolStr = toBinary(symbolStr);
                lineToWrite.append(symbolStr);
            }
            // writes the converted line to the new hack file and gets ready for the next line
            hackBW.write(lineToWrite.toString());
            hackBW.newLine();
            lineToWrite.setLength(0);
        }

        hackBW.close();
    }

    /**
     * @return 16 digit binary number of the int received as a string.
     */
    // NO SYMBOLS <-----------------
    public static String toBinary(String str) {
        int num = Integer.parseInt(str);
        String binaryNum = String.format("%16s", Integer.toBinaryString(num)).replace(' ', '0');
        return binaryNum;
    }

    /**
     * Takes a given asm code, creates a new one with the same path adding 'Clean' to the name of the file.
     * Cleans all the White spaces (Empty lines, comments and indentation) coping only code to the new asm.
     * @return the new file's pathname as a string
     */
    public static String asmClean(String asmName) throws IOException {
        File asmFile = new File (String.format("%s", asmName)); // open input file
        // Gets the file ready to read
        FileInputStream inpStream = new FileInputStream(asmFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inpStream));
        String newPath = asmName.substring(0, asmName.length()-4); // removes the '.asm' from the path on the file
        newPath = String.format("%sClean.asm",newPath);
        File asmFileClean = new File (newPath); // create new asm file to add the clean code
        // Gets the file ready to write
        FileWriter hackFW = new FileWriter (asmFileClean);
        BufferedWriter hackBW = new BufferedWriter(hackFW);
        String line = reader.readLine();
        StringBuilder clean = new StringBuilder();

        // going over each row and adding only code to the new asm file until the end of the input
        while (line != null) {
            int i = 0; // line counter
            // adding to the string builder anything that isn't ' ', tab, or start with '//'
            while ((line.length() > i + 1) && (!((line.charAt(i) == '/') && (line.charAt(i+1) == '/')))) {
                if ((line.charAt(i) != '/') && ((line.charAt(i) != ' ')) && (line.charAt(i) != '\t'))
                    clean.append(line.charAt(i));
                else if (line.charAt(i) == '/')
                    if (line.charAt(i + 1) != '/')
                        clean.append(line.charAt(i));
                i++;
            }
            // checks the last row separately
            if ((line.length() > i) && (line.charAt(i) != '/') && ((line.charAt(i) != ' ')) && (line.charAt(i) != '\t'))
                clean.append(line.charAt(i));
            // adds the string builder to the new asm file if necessary
            if (clean.length() != 0) {
                hackBW.write(clean.toString());
                hackBW.newLine();
                clean.setLength(0);
            }
            line = reader.readLine();
        }

        hackBW.close();
        return newPath;
    }

    /*
     * Handle symbols
     * @param cleaned file pathname
     * @return symboled file pathname
     */
    public static String asmSymbols (String asmCleanedPath) throws IOException {
        // Prepare to read and write to new file
        File asmCleaned = new File (String.format("%s", asmCleanedPath)); // open input file -- cleaned asm file
        // Gets the file ready to read
        FileInputStream inpStream = new FileInputStream(asmCleaned);
        BufferedReader asmCleanedBR = new BufferedReader(new InputStreamReader(inpStream));

        String fpPath = asmCleanedPath.substring(0, asmCleanedPath.length()-9); // removes the 'Clean.asm' from the path on the file
        fpPath = String.format("%sSymboledFP.asm", fpPath);
        File asmSymboledFileFP = new File (fpPath); // create new asm file to add the clean and symboled code - FP = first pass
        // Gets the file ready to write
        FileWriter symboledWriter = new FileWriter (asmSymboledFileFP);
        BufferedWriter symboledBW = new BufferedWriter(symboledWriter);
        String line = asmCleanedBR.readLine();

        SymbolTable sb = new SymbolTable(); // create the symbol table and assign predefined symbols
        int row=0;

        // FIRST PASS
        while (line != null) {

            // if A/C command - write line to new file and increament row counter
            if (line.charAt(0) != '(') { 
                row++; 
                symboledBW.write(line);
                symboledBW.newLine();
            }
            
            // else - L command, save entry to symbol table (xxx,row), do not write line and do not increment row
            else {
                String str = line.substring(1, line.length()-1); // get xxx
                sb.addEntry(str, row);
            }
            
            line = asmCleanedBR.readLine();
        }
        asmCleanedBR.close();
        symboledBW.close(); // First pass writing ended

        // Restart rw - reading from symboledFP (first pass) and writing to new file symboledSP (second pass)
        InputStream inpStreamSP = new FileInputStream(asmSymboledFileFP);
        BufferedReader asmSymboledBR = new BufferedReader(new InputStreamReader(inpStreamSP));
        String spPath = asmCleanedPath.substring(0, asmCleanedPath.length()-9); // leave only name of file
        spPath = String.format("%sSymboledSP.asm", spPath); // add 'SymboledSP.asm'
        File asmSymboledSP = new File(spPath); // create symboled second pass file
        FileWriter symboledWriterSP = new FileWriter (asmSymboledSP);
        BufferedWriter symboledBWSP = new BufferedWriter(symboledWriterSP);
        line = asmSymboledBR.readLine();
        int ram = 16, variableValue;
        String variableKey;

        // SECOND PASS
        while (line != null) {
            if (line.charAt(0) == '@' && !Character.isDigit(line.charAt(1))) { // A-command and not a number
                variableKey = line.substring(1);
                if (! sb.contains(variableKey)) { // new variable - add to symbolTable
                    sb.addEntry(variableKey, ram);
                    ram++;
               }
               variableValue = sb.getAddress(variableKey);// value existed before or has been just added
               line = "@" + String.format("%d", variableValue); // change symbol to corresponding int from the symbolTable
            }
            symboledBWSP.write(line);
            symboledBWSP.newLine();
            line = asmSymboledBR.readLine();
        }
        asmSymboledBR.close();
        symboledBWSP.close(); // Second pass writing ended

        return spPath;
    }
}
