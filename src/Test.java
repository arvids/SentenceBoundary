import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.InstanceList;

public class Test {

  private static final Pattern splitPattern = Pattern.compile("[^\\s]+");

  public static void main(String[] args) {
    final File file = new File("/Users/Arvid/Dropbox/ukWac/UKWAC-25.xml");
    System.out.println(file.getAbsolutePath());
    new SentenceBoundary(file);
  }

  private static void testMakeTrainingData() {
    final long start = System.currentTimeMillis();
    final File file = new File("/Users/Arvid/Dropbox/ukWac/UKWAC-25.xml");
    System.out.println(file.getAbsolutePath());
    final InstanceList test =
        SentenceBoundary.createTrainingData(SentenceBoundary.chunks(
            SentenceBoundary.readUkWac(file), 20));
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

  private static ArrayList<Word> getWords(String line) {
    final Matcher matcher = splitPattern.matcher(line);
    final ArrayList<Word> words = new ArrayList<Word>();
    while (matcher.find()) {
      System.out.println("FIND");
      final int start = matcher.start();
      System.out.println("Start: " + start);
      final int end = matcher.end();
      System.out.println("End: " + end);
      final String word = matcher.group();
      System.out.println("Rep: " + word);
      System.out.println("testarN: " + line.substring(start, end));
      words.add(new Word(word));
    }
    return words;
  }
}
