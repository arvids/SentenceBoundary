public class SubSentence {

  private int    start;   // start offset
  private int    end;     // end offset
  private String sentence; // string representation of this unit
  private String label;   // the predicted label

  public SubSentence(int start, int end, String sentence) {
    this.start = start;
    this.end = end;
    this.sentence = sentence;
    label = "O";
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public String getSentence() {
    return sentence;
  }

  public void setSentence(String sentence) {
    this.sentence = sentence;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return sentence + ": " + start + "-" + end;
  }
}
