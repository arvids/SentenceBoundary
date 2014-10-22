import java.util.TreeSet;

class Symbols {

  private final TreeSet<String> symbols;
  private final TreeSet<String> isSymbols;
  private final TreeSet<String> eosSymbols;

  public Symbols() {
    symbols = new TreeSet<String>();
    isSymbols = new TreeSet<String>();
    eosSymbols = new TreeSet<String>();
    
    eosSymbols.add(".");
    eosSymbols.add("!");
    eosSymbols.add("?");
    
    isSymbols.add(":");
    isSymbols.add("]");
    isSymbols.add(")");
    isSymbols.add("\"");
    isSymbols.add("[");
    isSymbols.add("(");
    isSymbols.add("\\");
    isSymbols.add("/");
    
    symbols.addAll(eosSymbols);
    symbols.addAll(isSymbols);
  }

  public TreeSet<String> getEOSSymbols() {
    return eosSymbols;
  }

  public boolean isSymbol(String symbol) {
    return symbols.contains(symbol);
  }

  public boolean isEosSymbol(String symbol) {
    return eosSymbols.contains(symbol);
  }
  
    
  public boolean wordEndsWithIsSymbol(String word) {
    if (word.length() > 0) {
      final String lastChar = word.substring(word.length() - 1, word.length());
      if (isSymbols.contains(lastChar)) {
        return true;
      }
    }
    return false;
    
  }

  public boolean wordEndsWithEOSSymbol(String word) {
    if (word.length() > 0) {
      final String lastChar = word.substring(word.length() - 1, word.length());
      if (eosSymbols.contains(lastChar)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean wordEndsWithSymbol(String word) {
    if (word.length() > 0) {
      final String lastChar = word.substring(word.length() - 1, word.length());
      if (symbols.contains(lastChar)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean wordStartsWithSymbol(String word) {
    if (word.length() > 0) {
      final String firstChar = word.substring(0, 1);
      if (symbols.contains(firstChar)) {
        return true;
      }
    }
    return false;
  }
  
}
