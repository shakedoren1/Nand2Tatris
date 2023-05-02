import java.io.*;

public class Parser {

    private final BufferedReader reader; // A reader for our InputStream
    private String nextLine, command; // Strings to deal with each line in the file
    private int len; // int that will hold the length of the current command

    /**
     * Constructor of Parser - Opens the input stream and gets ready to parse it.
     */
    public Parser(FileInputStream file) {
        this.reader = new BufferedReader(new InputStreamReader(file));
    }

    /**
     * @return Are there more commands in the input?
     */
    public Boolean hasMoreCommands() throws IOException {
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
    public enum ComType {A_COMMAND, C_COMMAND, L_COMMAND}

    /**
     * @return The type of the current command.
     */
    public ComType commandType() {
        // Starts with @ ---> A command
        if (this.command.charAt(0) == '@')
            return ComType.A_COMMAND;
        // Starts with '(' and ends with ')' ---> L command
        if ((this.command.charAt(0) == '(') && (this.command.charAt(this.len) == ')'))
            return ComType.L_COMMAND;
        // Not A of L command ---> C command
        return ComType.C_COMMAND;
    }

    /**
     * @return The symbol or decimal Xxx of the current command
     */
    public String symbol() {
        StringBuilder clean = new StringBuilder();

        // Appends to the new string the second to one before last of the characters from the current command
        for (int i = 1; i < this.len; i++)
            clean.append(this.command.charAt(i));
        // If the current command is not an L command, appends the last character
        if (this.command.charAt(this.len) != ')')
            clean.append(this.command.charAt(this.len));

        return clean.toString();
    }

    /**
     * @return The dest mnemonic in the current C-command
     */
    public String dest() {
        StringBuilder clean = new StringBuilder();
        int i = 0;
        char c = this.command.charAt(i);

        // Appends to the new string all the characters until the '=' or until the end of the command
        while ((c != '=') && (i < this.len)) {
            clean.append(c);
            i++;
            c = this.command.charAt(i);
        }
        // No dest in the command
        if (c != '=')
            return null;

        return clean.toString();
    }

    /**
     * @return The comp mnemonic in the current C-command
     */
    public String comp() {
        StringBuilder clean = new StringBuilder();
        int i = 0;
        char c = this.command.charAt(i);

        // Skips all the characters until the '=', if there is no '=' starts from the beginning
        while ((c != '=') && (i < this.len)) {
            i++;
            c = this.command.charAt(i);
        }
        if (c != '=')
            i = -1;
        i++;
        c = this.command.charAt(i);
        // Appends to the new string all the characters until the ';'
        while ((c != ';') && (i < this.len)) {
            clean.append(c);
            i++;
            c = this.command.charAt(i);
        }
        if (c != ';')
            clean.append(c);

        return clean.toString();
    }

    /**
     * @return The jump mnemonic in the current C-command
     */
    public String jump() {
        StringBuilder clean = new StringBuilder();
        int i = 0;
        char c = this.command.charAt(i);

        // Skips all the characters until the ';'
        while ((c != ';') && (i < this.len)) {
            i++;
            c = this.command.charAt(i);
        }

        // No jump in the command
        if (c != ';')
            return null;

        i++;
        c = this.command.charAt(i);
        // Appends to the new string all the characters until the end of the command
        while (i < this.len) {
            clean.append(c);
            i++;
            c = this.command.charAt(i);
        }
        clean.append(c);

        return clean.toString();
    }
}