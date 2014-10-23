import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;

public class Test {

  private static final Pattern splitPattern = Pattern.compile("[^\\s]+");

  public static void main(String[] args) {
    //testPartOfUkWacRead();
    SentenceBoundary test = testSentenceBoundary();
    System.out.println(test.getCRF().toString());
    test.writeCRF("crf3");
    try {
      test.readCRF("crf3.gz");
      System.out.println(test.getCRF().toString());
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (ClassNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    SentenceBoundary test2 = new SentenceBoundary();
    try {
      test2.readCRF("crf3.gz");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println(test2.getCRF().toString());
  }
  
  private static SentenceBoundary testSentenceBoundary() {
    final File file = new File("/Users/Arvid/Dropbox/ukWac/UKWAC-25.xml");
    System.out.println(file.getAbsolutePath());
    return new SentenceBoundary(file, 100);
  }
  
  

  private static void testMakeTrainingData() {
    final long start = System.currentTimeMillis();
    final File file = new File("/Users/Arvid/Dropbox/ukWac/UKWAC-25.xml");
    System.out.println(file.getAbsolutePath());
    
    final SentenceBoundary  sentenceBoundary = new SentenceBoundary();
    sentenceBoundary.createTrainningDataFromSentences(SentenceBoundary.chunks(SentenceBoundary.readUkWac(file), 20));
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
  
  private static void testPartOfUkWacRead() {
    final File file = new File("/Users/Arvid/DropBox/ukWac/UKWAC-25.xml");
    System.out.println(file.getAbsolutePath());
    final ArrayList<String> sentences = SentenceBoundary.readUkWac(file, 10);
    for (final String sentence : sentences) {
      System.out.println(sentence);
    }
    
  }
}
