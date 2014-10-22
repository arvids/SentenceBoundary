import java.util.TreeSet;

class Symbols {

  private TreeSet<String> symbols;
  private TreeSet<String> eosSymbols;

  public Symbols() {
    init();
  }

  private void init() {
    symbols = new TreeSet<String>();
    eosSymbols = new TreeSet<String>();
    eosSymbols.add(".");
    symbols.add(":");
    eosSymbols.add("!");
    eosSymbols.add("?");
    symbols.add("]");
    symbols.add(")");
    symbols.add("\"");
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

  public boolean wordEndsWithEOSSymbol(String word) {
    if (word.length() > 0) {
      final String lastChar = word.substring(word.length() - 1, word.length());
      if (symbols.contains(lastChar)) {
        return true;
      }
    }
    return false;
  }
}
