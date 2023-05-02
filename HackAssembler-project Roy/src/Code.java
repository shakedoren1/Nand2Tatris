

import java.util.HashMap;

public class Code {

    private HashMap<String,String> dict;

    public Code () {
        this.dict = new HashMap<>();

        // enter dest key-values
        this.dict.put("M","001");
        this.dict.put("D","010");
        this.dict.put("MD","011");
        this.dict.put("DM","011");
        this.dict.put("A","100");
        this.dict.put("AM","101");
        this.dict.put("MA","101");
        this.dict.put("AD","110");
        this.dict.put("MA","110");
        this.dict.put("AMD","111");
        this.dict.put("MDA","111");
        this.dict.put("DMA","111");
        this.dict.put("ADM","111");
        this.dict.put("MAD","111");
        this.dict.put("DAM","111");

        // enter jump key-values
        this.dict.put("JGT","001");
        this.dict.put("JEQ","010");
        this.dict.put("JGE","011");
        this.dict.put("JLT","100");
        this.dict.put("JNE","101");
        this.dict.put("JLE","110");
        this.dict.put("JMP","111");

        // enter comp key-values - except for 'A' 'D' and 'M' which keys are already taken
        this.dict.put("0","0101010");
        this.dict.put("1","0111111");
        this.dict.put("-1","0111010");
        this.dict.put("!D","0001101");
        this.dict.put("!A","0110001");
        this.dict.put("!M","1110001");
        this.dict.put("-D","0001111");
        this.dict.put("-A","0110011");
        this.dict.put("-M","1110011");
        this.dict.put("D+1","0011111");
        this.dict.put("A+1","0110111");
        this.dict.put("M+1","1110111");
        this.dict.put("D-1","0001110");
        this.dict.put("A-1","0110010");
        this.dict.put("M-1","1110010");
        this.dict.put("D+A","0000010");
        this.dict.put("D+M","1000010");
        this.dict.put("D-A","0010011");
        this.dict.put("D-M","1010011");
        this.dict.put("A-D","0000111");
        this.dict.put("M-D","1000111");
        this.dict.put("D&A","0000000");
        this.dict.put("D&M","1000000");
        this.dict.put("D|A","0110010");
        this.dict.put("D|M","1010101");
    }

    public HashMap<String,String> getDict() { return this.dict; }

    public String destOrJump (String line) {
        if (line == null)
            return "000";
        else
            return getDict().get(line);
    }

    public String comp (String line) {
        if (line.equals("D")) return "0001100";
        if (line.equals("A")) return "0110000";
        if (line.equals("M")) return "1110000";
        return getDict().get(line);
    }
}