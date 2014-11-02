import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Sequence;

public class SentenceBoundaryDetection {

  private int                 numberOfSentences;
  private static int          numberOfWords;
  private static final int    numberOfChunks       = 20;
  private static int          chunkSize;
  private static File         file;
  public static final String  EOS                  = "EOS";
  public static final String  IS                   = "IS";
  private CRF                 crf;
  private static ArrayList<String> trainData;
  private static ArrayList<String> testData;
  

  public SentenceBoundaryDetection() {
    file = new File("FAKE");
  }

  public SentenceBoundaryDetection(File file) {
    this(file, Integer.MAX_VALUE);
  }

  public SentenceBoundaryDetection(File file, int numberOfSentences) {
    this.numberOfSentences = numberOfSentences;
    SentenceBoundaryDetection.file = file;
    numberOfWords = 0;
    long start = System.currentTimeMillis();
    //setData9010(file, numberOfSentences);
    setData3SentencesAtATime9010(file, numberOfSentences);
    this.numberOfSentences = trainData.size();
    System.out.println("Converted " + numberOfWords + " words to " + this.numberOfSentences
        + " sentences in " + ((System.currentTimeMillis() - start) / 1000) + " s");
    chunkSize = this.numberOfSentences / numberOfChunks;
    System.out.println("ChunkSize: " + chunkSize);
    start = System.currentTimeMillis();
    final ArrayList<ArrayList<String>> trainDataChunks = chunks(trainData, chunkSize);
    System.out.println("Split " + this.numberOfSentences + " in to " + trainDataChunks.size()
        + " chunks in " + (System.currentTimeMillis() - start) + " ms");
    final InstanceList instanceList = createTrainningDataFromSentences(trainDataChunks);
    train(instanceList);
    writeCRF("crftest");
    evaluate();
    
  }

  public CRF getCRF() {
    return crf;
  }

  public void setCRF(CRF crf) {
    this.crf = crf;
  }

  public InstanceList createTrainingDataFromSentences(ArrayList<String> sentences) {
    final LabelAlphabet labelAlphabet = new LabelAlphabet();
    labelAlphabet.lookupLabel(SentenceBoundaryDetection.EOS, true);
    labelAlphabet.lookupLabel(SentenceBoundaryDetection.IS, true);
    final Pipe pipe =
        new SerialPipes(new Pipe[] {new Sentence2Pipe(),
            new TokenSequence2FeatureVectorSequence(true, true)});
    final InstanceList instanceList = new InstanceList(pipe);
    final long start = System.currentTimeMillis();
    instanceList.addThruPipe(new Instance(sentences, "", "", file.getName()));
    System.out.println("Completed: " + sentences.size() + "sentences in "
        + (System.currentTimeMillis() - start) + " ms");
    return instanceList;
  }

  public InstanceList createTrainningDataFromSentences(ArrayList<ArrayList<String>> sentencesChunks) {
    final LabelAlphabet labelAlphabet = new LabelAlphabet();
    labelAlphabet.lookupLabel(SentenceBoundary.EOS, true);
    labelAlphabet.lookupLabel(SentenceBoundary.IS, true);
    final Pipe pipe =
        new SerialPipes(new Pipe[] {new Sentence2Pipe(),
            new TokenSequence2FeatureVectorSequence(true, true)});
    final InstanceList instanceList = new InstanceList(pipe);
    double chunkPercentage = (chunkSize * 100.0) / numberOfSentences;
    double total = chunkPercentage;
    double i = 0.0;
    final long totalStart = System.currentTimeMillis();
    for (final ArrayList<String> chunk : sentencesChunks) {
      final long start = System.currentTimeMillis();
      instanceList.addThruPipe(new Instance(chunk, "", "", file.getName() + i));
      i += 1;
      total = i * chunkPercentage;
      if (total > 100) {
        chunkPercentage = 100.0 - ((i - 1) * chunkPercentage);
        total = 100.0;
      }
      System.out.println("Completed: " + chunkPercentage + "% in "
          + (System.currentTimeMillis() - start) + " ms");
      System.out.println((total) + "% of Total completed in "
          + ((System.currentTimeMillis() - totalStart) / 1000) + " s");
    }
    return instanceList;
  }
  
  public InstanceList createTestDataFromSentences(ArrayList<String> sentences) {
    InstanceList testData = new InstanceList(crf.getInputPipe());
    testData.add(crf.getInputPipe().instanceFrom(new Instance(sentences, "", "", "")));
    return testData;
    
  }

  public static ArrayList<String> readUkWac(File file) {
    final ArrayList<String> sentences = new ArrayList<String>();
    final Symbols symbols = new Symbols();
    final StringBuilder sb = new StringBuilder();;
    try {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      for (String line; (line = br.readLine()) != null;) {
        if (line.startsWith("<text")) {
          continue;
        } else if (line.startsWith("</text")) {
          continue;
        } else if (line.equals("<s>")) {
          sb.setLength(0);
          continue;
        } else if (line.equals("</s>")) {
          sentences.add(sb.toString().toLowerCase());
          continue;
        } else {
          final String s = line.split("\\s")[0];
          numberOfWords++;
          if (symbols.isEosSymbol(s)) {
            sb.append(s);
          } else {
            sb.append(" " + s);
          }
        }
      }
      br.close();
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return sentences;
  }

  public static ArrayList<String> readUkWac(File file, int numberOfSentences) {
    final ArrayList<String> sentences = new ArrayList<String>();
    final Symbols symbols = new Symbols();
    final StringBuilder sb = new StringBuilder();;
    int i = 0;
    try {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      for (String line; (line = br.readLine()) != null;) {
        if (i == numberOfSentences) {
          break;
        }
        if (line.startsWith("<text")) {
          continue;
        } else if (line.startsWith("</text")) {
          continue;
        } else if (line.equals("<s>")) {
          sb.setLength(0);
          continue;
        } else if (line.equals("</s>")) {
          sentences.add(sb.toString());
          i++;
          continue;
        } else {
          final String s = line.split("\\s")[0];
          numberOfWords++;
          if (symbols.isEosSymbol(s)) {
            sb.append(s);
          } else {
            sb.append(" " + s);
          }
        }
      }
      br.close();
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return sentences;
  }
  
  private void setData3SentencesAtATime9010(File file, int numberOfSentences) {
    trainData = new ArrayList<String>();
    testData = new ArrayList<String>();
    Random random = new Random();
    final Symbols symbols = new Symbols();
    final StringBuilder sb = new StringBuilder();;
    int i = 0;
    int j = 0;
    try {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      for (String line; (line = br.readLine()) != null;) {
        if (i >= numberOfSentences && j == 0) {
          break;
        }
        if (line.startsWith("<text")) {
          continue;
        } else if (line.startsWith("</text")) {
          continue;
        } else if (line.equals("<s>")) {
          continue;
        } else if (line.equals("</s>")) {
          sb.append(" EOS");
          j++;
          if (j == 3) {
          if (random.nextInt(10) == 9) {
            testData.add(sb.toString());
          } else {
            trainData.add(sb.toString());
            i += j;
          }
          sb.setLength(0);
          j = 0;
          }
          continue;
        } else {
          final String s = line.split("\\s")[0];
          numberOfWords++;
          if (symbols.isSymbol(s)) {
            sb.append(s);
          } else {
            sb.append(" " + s);
          }
        }
      }
      br.close();
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    System.out.println("Train: " + trainData.size());
    System.out.println("Test: " + testData.size());
  }
  
  private void setData9010(File file, int numberOfSentences) {
    trainData = new ArrayList<String>();
    testData = new ArrayList<String>();
    Random random = new Random();
    final Symbols symbols = new Symbols();
    final StringBuilder sb = new StringBuilder();;
    int i = 0;
    try {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      for (String line; (line = br.readLine()) != null;) {
        if (i == numberOfSentences) {
          break;
        }
        if (line.startsWith("<text")) {
          continue;
        } else if (line.equals("<s>")) {
          sb.setLength(0);
          continue;
        } else if (line.equals("</s>")) {
          if (random.nextInt(10) == 9) {
            testData.add(sb.toString());
          } else {
            trainData.add(sb.toString());
            i++;
          }
          continue;
        } else {
          final String s = line.split("\\s")[0];
          numberOfWords++;
          if (symbols.isEosSymbol(s)) {
            sb.append(s);
          } else {
            sb.append(" " + s);
          }
        }
      }
      br.close();
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    System.out.println("Train: " + trainData.size());
    System.out.println("Test: " + testData.size());
  }

  public String predict(ArrayList<String> sentences) {
    final long start = System.currentTimeMillis();
    final Instance instance = crf.getInputPipe().instanceFrom(new Instance(sentences, "", "", ""));
    System.out.println("Predict complete");
    System.out.println("Time: " + (System.currentTimeMillis() - start));
    return predict(instance);
  }

  public String predict(Instance instance) {
    final Sequence input = (Sequence) instance.getData();
    @SuppressWarnings("unchecked")
    final ArrayList<Word> words = (ArrayList<Word>) instance.getName();
    final ArrayList<String> labels = new ArrayList<String>();
    final Sequence<String> labelresults = crf.transduce(input);
    for (int i = 0; i < labelresults.size(); i++) {
      words.get(i).setLabel(labelresults.get(i));
    }
    return new Text(words).toString();
  }

  public void train(InstanceList instanceList) {
    final long start = System.currentTimeMillis();
    crf = new CRF(instanceList.getPipe(), (Pipe) null);
    crf.addStatesForLabelsConnectedAsIn(instanceList);
    final CRFTrainerByLabelLikelihood crfTrainer = new CRFTrainerByLabelLikelihood(crf);
    crfTrainer.trainIncremental(instanceList);
    crf.getInputPipe().getDataAlphabet().stopGrowth();
    System.out.println("Training complete");
    System.out.println("Time: " + (System.currentTimeMillis() - start));
  }

  
  
  public void evaluate() {
    if (crf == null) {
      new IllegalStateException("CRF not initialized");
    }
    final Symbols symbols = new Symbols();
    int truePositive = 0;
    int trueNegative = 0;
    int falsePositive = 0;
    int falseNegative = 0;
    
    InstanceList instanceList = createTestDataFromSentences(testData);
    for (int i = 0; i < instanceList.size(); i++) {
      Instance instance = instanceList.get(i);
      String abStractName = (String) instance.getSource();
      
      //predict
      Sequence input = (Sequence) instance.getData();
      ArrayList<Word> words = (ArrayList<Word>) instance.getName();
      ArrayList<String> labels = new ArrayList<String>();
      
      
      Sequence<String> output = crf.transduce(input);
      for (int j = 0; j < output.size(); j++) {
        labels.add(output.get(j));
      }
      
      for (int k = 0; k < labels.size(); k++) {
        words.get(k).setLabel(labels.get(k));
      }    
      
      ArrayList<String> orgLabels = labelSequenceToStringArrayList((LabelSequence) instance.getTarget());

      for (int l = 0; l < words.size(); l++) {
        String word = words.get(l).toString();
        String prediction = words.get(l).getLabel();
        String original = orgLabels.get(l);

          if (prediction.equals(original)) {
            if (symbols.wordEndsWithEOSSymbol(word)) {
              truePositive++;
            } else {
              trueNegative++;
            }
          }
          else {
            if (prediction.equals("EOS") && original.equals("IS"))
              falsePositive++;
            else if (prediction.equals("IS") && original.equals("EOS"))
              falseNegative++;
          }
        
      }
    }
     
    double precision = truePositive / (double)(truePositive + falsePositive);
    double recall = truePositive / (double)(truePositive + falseNegative);
    double accuracy = (truePositive + trueNegative) / (double)(truePositive + trueNegative + falsePositive + falseNegative);
    double f1 = 2 * precision * recall / (precision + recall);
    
    
    System.out.println("Total tests: " + (truePositive + trueNegative + falsePositive + falseNegative));
    System.out.println("True positive: " + truePositive);
    System.out.println("True negative: " + trueNegative);
    System.out.println("False positive: " + falsePositive);
    System.out.println("False negative: " + falseNegative);
    System.out.println("Recall: " + recall);
    System.out.println("Precision: " + precision);
    System.out.println("F1: " + f1);
    System.out.println("Accuracy: " + accuracy);
    
    
  }
  
  public static ArrayList<ArrayList<String>> chunks(ArrayList<String> sentences, int size) {
    final ArrayList<ArrayList<String>> chunks = new ArrayList<ArrayList<String>>();
    for (int i = 0; i < sentences.size(); i += size) {
      final List<String> chunk = sentences.subList(i, Math.min(sentences.size(), i + size));
      chunks.add(new ArrayList<String>(chunk));
    }
    return chunks;
  }
  
  public ArrayList<String> labelSequenceToStringArrayList(LabelSequence ls) {
    ArrayList<String> labels = new ArrayList<String>();
    for (int j = 0; j < ls.size(); j++)
      labels.add((String) ls.get(j));
    return labels;
  }

  public void writeCRF(String filename) {
    if (crf == null) {
      new IllegalStateException("CRF not initialized");
    }
    try {
      final FileOutputStream fos = new FileOutputStream(new File(filename + ".gz"));
      final GZIPOutputStream gout = new GZIPOutputStream(fos);
      final ObjectOutputStream oos = new ObjectOutputStream(gout);
      oos.writeObject(crf);
      oos.close();
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  public void writeSentences(ArrayList<String> sentences, String filename) {
    try {
      final FileWriter writer = new FileWriter(filename);
      for (final String sentence : sentences) {
        writer.write(sentence.toString() + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  public void readCRF(String filename) throws IOException, FileNotFoundException,
      ClassNotFoundException {
    final FileInputStream fis = new FileInputStream(new File(filename));
    final GZIPInputStream gin = new GZIPInputStream(fis);
    final ObjectInputStream ois = new ObjectInputStream(gin);
    crf = (CRF) ois.readObject();
    ois.close();
    crf.getInputPipe().getDataAlphabet().stopGrowth();
  }
}
