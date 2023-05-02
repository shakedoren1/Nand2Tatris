import java.io.*;

public class VMTranslator {

    private static final int vm_extension_len=3;
    public static void main (String[]args) throws IOException {
        String path = args[0];
        File fileORdir = new File(path);
        if (path.endsWith(".vm"))  // input path is one vm file
            translate(fileORdir,false);
        
        else { // input path is a directory - may include more than one vm file to translate   
            File[] files = fileORdir.listFiles();
            for (File file : files)
                if (file.getName().endsWith(".vm"))
                    translate(file,true);
        }   
    }

    /*
     * Translates a vm file to hack assembly.
     * @param receives a path corresponding to a .vm file
     * void function - creates a cleaned vm file and a .asm file translated from the input path file received
     */
    public static void translate  (File vmFile, boolean isDir) throws IOException{

        String cleanedVMpath = vmClean(vmFile.getPath());
        File cleanedVMFile = new File (cleanedVMpath); // open the cleaned vm file
        String asmPath; // path for asm file output
       
        // create asm pathname - according to input file or dir
        if (! isDir) // single file
            asmPath = vmFile.getPath().substring(0,vmFile.getPath().length()-vm_extension_len); // removes the '.vm' from the path on the file
        else
            asmPath = cleanedVMFile.getPath().substring(0,cleanedVMFile.getPath().length()-cleanedVMFile.getName().length())+cleanedVMFile.getParentFile().getName();
        asmPath = String.format("%s.asm",asmPath); // add asm path extension
        System.out.println("ASM PATH : "+asmPath);
        // save clean file name in @param String static_segment
        String static_segment = cleanedVMFile.getName();
        
         
        FileInputStream inpStream = new FileInputStream(cleanedVMFile); // input stream from the cleaned file

        Parser parse = new Parser(inpStream);
        boolean firstFile = false;
        // create asm file with .asm extension or opens it if already exists
        File asmFile = new File (asmPath);
        if (asmFile.createNewFile()) firstFile = true;
            
        CodeWriter code = new CodeWriter(asmFile);

        if (firstFile && isDir) {
           code.writeBootstrap();
        }

        String segment; int var;
        Parser.ComType command;
        // parsing through the entire file using Parser and CodeWriter classes on each line as needed
        while (parse.hasMoreLines()) {
            
            parse.advance();
            command = parse.commandType();
            segment = parse.arg1();
            var = parse.arg2();

            switch(command) {
                case C_ARITHMETIC: code.writeArithmetic(parse.arg1());
                    break;
                case C_POP: 
                case C_PUSH: if (segment.equals("static")) code.staticPushPop(command, static_segment+"."+var);
                             else code.writePushPop(command, segment, var);
                    break;
                case C_LABEL: code.writeLabel(segment);
                    break;
                case C_CALL: code.writeCall(segment, var);
                    break;
                case C_FUNCTION: code.writeFunction(segment, var);
                    break;
                case C_GOTO: code.writeGoto(segment);
                    break;
                case C_IF: code.writeIf(segment);
                    break;
                case C_RETURN: code.writeReturn();
                    break;
            }
        }
        cleanedVMFile.delete(); // done reading. delete clean vm file
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