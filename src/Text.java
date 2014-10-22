import java.util.ArrayList;

public class Text {

  private String text;

  public Text(ArrayList<Word> words) {
    boolean capitalizeNextWord = true;
    String wordString = "";
    final StringBuilder sb = new StringBuilder();
    for (final Word word : words) {
      wordString = word.toString();
      if (capitalizeNextWord) {
        wordString = capitalize(wordString);
      }
      if (word.getLabel().equals(SentenceBoundary.EOS)) {
        sb.append(wordString + ".");
        capitalizeNextWord = true;
      } else {
        sb.append(wordString + " ");
        capitalizeNextWord = false;
      }
    }
    this.text = sb.toString().trim();
  }

  @Override
  public String toString() {
    return this.text;
  }

  private String capitalize(String word) {
    return Character.toUpperCase(word.charAt(0)) + word.substring(1);
  }
}
