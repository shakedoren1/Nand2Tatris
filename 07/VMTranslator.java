import java.io.*;

public class VMTranslator {

    private static final int vm_extension_len=3;
    public static void main (String[]args) throws IOException {
        translate(args[0]);
        //translate("/Users/royhillel/Desktop/nand2tetris/projects/07/StackArithmetic/StackTest/neg.vm");
    }

    /*
     * Translates a vm file to hack assembly.
     * @param receives a path corresponding to a .vm file
     * void function - creates a cleaned vm file and a .asm file translated from the input path file received
     */
    public static void translate  (String vmPath) throws IOException{

        String cleanedVMpath = vmClean(vmPath);
        File cleanedVMFile = new File (cleanedVMpath); // open the cleaned vm file

        String asmPath = vmPath.substring(0, vmPath.length()-vm_extension_len); // new path for asm file - removes the '.vm' from the path on the file
        asmPath = String.format("%s.asm",asmPath);

        // save clean file name in @param String static_segment
        String[] pathArray = asmPath.split("/");
        String fileName = pathArray[pathArray.length-1];
        String static_segment = fileName.substring(0, fileName.length()-vm_extension_len);

        File asmFile = new File (asmPath); // create asm file with .asm extension
        FileInputStream inpStream = new FileInputStream(cleanedVMFile); // input stream from the cleaned file

        Parser parse = new Parser(inpStream);
        CodeWriter code = new CodeWriter(asmFile);
        String segment;
        // parsing through the entire file using Parser and CodeWriter classes on each line as needed
        while (parse.hasMoreLines()) {
            
            parse.advance();
            if (parse.commandType() == Parser.ComType.C_ARITHMETIC)
                code.writeArithmetic(parse.arg1());
            
            else { // push or pop command
                segment = parse.arg1();
                if (segment == "static")
                    code.staticPushPop(parse.commandType(), static_segment+"."+parse.arg2());
                else
                    code.writePushPop(parse.commandType(), segment, parse.arg2());
            }
        }
        code.close();
    }

    /*
     * Takes a given vm file, creates a new one with the same path adding 'Clean' to the name of the file.
     * Cleans all the White spaces (Empty lines, comments and indentation) coping only code to the new asm.
     * @return the new file's pathname as a string
     */
    public static String vmClean(String vmPath) throws IOException {
        File vmFile = new File (String.format("%s", vmPath)); // open input file
        // Get the file ready to read
        FileInputStream inpStream = new FileInputStream(vmFile);
        BufferedReader cleanBR = new BufferedReader(new InputStreamReader(inpStream));
        String cleanedPath = vmPath.substring(0, vmPath.length()-vm_extension_len); // removes the '.vm' from the path on the file
        cleanedPath = String.format("%sClean.vm",cleanedPath);
        File vmCleanFile = new File (cleanedPath); // create new vm file to add the clean code
        // Get the file ready to write
        FileWriter cleanFW = new FileWriter (vmCleanFile);
        BufferedWriter cleanBW = new BufferedWriter(cleanFW);
        String line = cleanBR.readLine();
        StringBuilder cleanLine = new StringBuilder();

        // going over each row and adding only code to the new asm file until the end of the input
        while (line != null) {
            int i = 0; // line counter
            // adding to the string builder anything that isn't ' ', tab, or starts with '//'
            while ((line.length() > i + 1) && (!((line.charAt(i) == '/') && (line.charAt(i+1) == '/')))) {
                if ((line.charAt(i) != '/') && (line.charAt(i) != '\t'))
                    cleanLine.append(line.charAt(i));
                else if (line.charAt(i) == '/')
                    if (line.charAt(i + 1) != '/')
                        cleanLine.append(line.charAt(i));
                i++;
            }
            // checks the last row separately
            if ((line.length() > i) && (line.charAt(i) != '/') && ((line.charAt(i) != ' ')) && (line.charAt(i) != '\t'))
                cleanLine.append(line.charAt(i));
            // adds the string builder to the new asm file if necessary
            if (cleanLine.length() != 0) {
                cleanBW.write(cleanLine.toString());
                cleanBW.newLine();
                cleanLine.setLength(0);
            }
            line = cleanBR.readLine();
        }

        cleanBR.close();
        cleanBW.close();
        return cleanedPath;
    }
}