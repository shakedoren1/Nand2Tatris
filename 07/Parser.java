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
    public enum ComType {C_ARITHMETIC, C_PUSH, C_POP}

    /**
     * @return The type of the current command.
     */
    public ComType commandType() {
        String[] commands = this.command.split(" ");
        // one word ==> C_ARITHMETIC
        if (commands.length == 1)
            return ComType.C_ARITHMETIC;
        // commands[0] == 'push' ==> C_PUSH
        if (commands[0].equals("push"))
            return ComType.C_PUSH;
        // commands[0] == 'pop' ==> C_POP
        // for project 8 continue checking - if (commands[0] == "pop")
        return ComType.C_POP;
    }

    public String arg1 () {
        String[] commands = this.command.split(" ");
        if (this.commandType() != ComType.C_ARITHMETIC)
            return commands[1];
        return commands[0];
    }
    
    public int arg2 () {
        String[] commands = this.command.split(" ");
        if (this.commandType() != ComType.C_ARITHMETIC)
            return Integer.parseInt(commands[2]);
        return -1; // should never happen
    }
}
