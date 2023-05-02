import java.io.*;

public class Parser {
    
    private final BufferedReader reader; // A reader for our InputStream
    private String nextLine, command; // Strings to deal with each line in the file
    private int len; // int that will hold the length of the current command

    public Parser(FileInputStream file) {
        this.reader = new BufferedReader(new InputStreamReader(file));
    }

    /**
     * @return Are there more Lines in the input?
     */
    public Boolean hasMoreLines() throws IOException {
        this.nextLine = this.reader.readLine();
        return this.nextLine != null;
    }

    /**
     * Puts the nextLine into the current command and updates the len value accordingly.
     */
    public void advance() {
        this.command = this.nextLine;
        this.len = this.command.length() - 1;
    }

    /**
     * An enum that will help us identify the different commands.
     */
    public enum ComType {C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL}

    /**
     * @return The type of the current command.
     */
    public ComType commandType() {
        String[] commands = this.command.split(" ");
        switch (commands[0]) {
            case "push": return ComType.C_PUSH;
            case "pop": return ComType.C_POP;
            case "label": return ComType.C_LABEL;
            case "goto": return ComType.C_GOTO;
            case "if-goto": return ComType.C_IF;
            case "function": return ComType.C_FUNCTION;
            case "return": return ComType.C_RETURN;
            case "call": return ComType.C_CALL;
        }
        return ComType.C_ARITHMETIC; // if none of the above, command is arithmetic
    }

    public String arg1 () {
        String[] commands = this.command.split(" ");
        if (commands.length > 1) // command is not ARITHMETIC nor RETURN
            return commands[1];
        return commands[0];
    }
    
    public int arg2 () {
        String[] commands = this.command.split(" ");
        if (commands.length > 2) // command has int variable
            return Integer.parseInt(commands[2]);
        return -1; // should never happen
    }
}
