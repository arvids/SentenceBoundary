import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Text {

  private String text;
  
  public Text(ArrayList<Word> words) {
    StringBuilder sb = new StringBuilder();
    for (Word word: words) {
      if (word.getLabel().equals(SentenceBoundary.EOS)) {
        sb.append(word.getWord() + ".");
      } else {
        sb.append(word.getWord() + " ");
      }
      
    }
    String text = sb.toString();
  }
  
  public String getText() {
    return this.text;
  }
  
}
