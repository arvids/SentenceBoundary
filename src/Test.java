import java.io.File;
import java.util.ArrayList;

import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.InstanceList;

public class Test {

  public static void main(String[] args) {
    // testUkWacRead();
    testMakeTrainingData();
  }

  private static void testMakeTrainingData() {
    final long start = System.currentTimeMillis();
    final File file = new File("/Users/Arvid/Dropbox/ukWac/UKWAC-25.xml");
    System.out.println(file.getAbsolutePath());
    final InstanceList test = SentenceBoundary.createTrainingData(file, true);
    final FeatureVectorSequence testData = (FeatureVectorSequence) test.get(0).getData();
    System.out.println("tid: ");
    System.out.println((System.currentTimeMillis() - start));
  }

  private static void testUkWacRead() {
    final File file = new File("test/ukwac");
    System.out.println(file.getAbsolutePath());
    final ArrayList<String> sentences = SentenceBoundary.readUkWac(file);
    for (final String sentence : sentences) {
      System.out.println(sentence);
    }
  }
}
