public class Word {

  private int    start;   // start offset
  private int    end;     // end offset
  private String sentence; // string representation of this unit
  private String label;   // the predicted label
  private String word;

  public Word(int start, int end, String sentence) {
    this.start = start;
    this.end = end;
    this.sentence = sentence;
    this.word = sentence.substring(start, end);
    this.label = "O";
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getSentence() {
    return sentence;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
  public String getWord() {
    return this.word;
  }
  

  @Override
  public String toString() {
    return sentence + ": " + start + "-" + end;
  }
}
