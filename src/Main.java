import java.io.File;

/**
 * @author Arvid
 *
 */
public class Main {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    final String filename = args[0];
    final int numberOfSentences = Integer.parseInt(args[1]);
    final File file = new File(filename);
    new SentenceBoundaryDetection(file, numberOfSentences);
  }
}
