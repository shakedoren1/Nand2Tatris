import java.util.*;

public class SymbolTable {

    private HashMap<String,Integer> symbol_table;
    
    public SymbolTable() {
        this.symbol_table = new HashMap<String,Integer>();
        putPredefined();
    }

    // put all predefined symbols in the HashMap
    public void putPredefined () {
        for (int i=0; i < 16; i++) { this.symbol_table.put(String.format("R%d", i), i); } // assign R0 - R15
        this.symbol_table.put("SCREEN", 16384);
        this.symbol_table.put("KBD", 24576);
        this.symbol_table.put("SP", 0);
        this.symbol_table.put("LCL",1);
        this.symbol_table.put("ARG",2);
        this.symbol_table.put("THIS",3);
        this.symbol_table.put("THAT",4);
    }

    // basic hashMap functions - add, contains, get
    public void addEntry (String symbol,int address) { this.symbol_table.put(symbol, address); }
    public boolean contains (String symbol) { return this.symbol_table.containsKey(symbol); }
    public int getAddress (String symbol) { return this.symbol_table.get(symbol); }

}
