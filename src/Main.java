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
    final int numberOfSentencesPerLine = Integer.parseInt(args[2]);
    final int nGram = Integer.parseInt(args[3]);
    final int wordWindow = Integer.parseInt(args[4]);
    final double testTrainRatio = Double.parseDouble(args[5]);
    final File file = new File(filename);
    new SentenceBoundaryDetection(file, numberOfSentences, numberOfSentencesPerLine, nGram,
        wordWindow, testTrainRatio);
  }
}
