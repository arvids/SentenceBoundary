public class Word {

  private int    start;   // start offset
  private int    end;     // end offset
  private String word; // string representation of this unit
  private String label;   // the predicted label

  public Word(int start, int end, String word) {
    this.start = start;
    this.end = end;
    this.word = word;
    this.label = "O";
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getWord() {
    return word;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
   @Override
  public String toString() {
    return word + ": " + start + "-" + end;
  }
}
