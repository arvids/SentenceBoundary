public class Word {

  private final String word;
  private String       label;

  public Word(String word) {
    this.word = word;
    label = "O";
  }
  
  public Word(String word, String label) {
    this.word = word;
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return word;
  }
}
