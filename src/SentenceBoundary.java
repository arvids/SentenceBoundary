import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.Sequence;

public class SentenceBoundary {

  private static int          numberOfSentences;
  private static int          numberOfWords;
  private static final int    numberOfChunks = 20;
  private static int          chunkSize;
  private static File file;
  public static final String EOS = "EOS";
  public static final String IS = "IS";
  private CRF crf;
  
  
  
  public static final int[][] uniGram        = new int[][] { {-1}, {0}, {1}};
  public static final int[][] biGram         = new int[][] { {-1, 0}, {0, 1}};
  public static final int[][] triGram        = new int[][] {{-1, 0, 1}};
  
  public SentenceBoundary(File file) {
    
    ArrayList<String> testText = new ArrayList();
    testText.add("a pony walks into a police building it happens in england a security camera captures the moment an officer tries to make the pony leave it does not care it leaves later when it wants to");
    this.file = file;
    
    numberOfWords = 0;
    long start = System.currentTimeMillis();
    final ArrayList<String> sentences = readUkWac(file);
    numberOfSentences = sentences.size();
    System.out.println("Converted " + numberOfWords + " words to " + numberOfSentences
        + " sentences in " + ((System.currentTimeMillis() - start) / 1000) + " s");
    chunkSize = numberOfSentences / numberOfChunks;
    System.out.println("ChunkSize: " + chunkSize);
    
    start = System.currentTimeMillis();
    final ArrayList<ArrayList<String>> sentencesChunks = chunks(sentences, chunkSize);
    System.out.println("Split " + numberOfSentences + " in to " + sentencesChunks.size()
        + " chunks in " + (System.currentTimeMillis() - start) + " ms");
    
    InstanceList instanceList = createTrainingData(sentencesChunks);
    
    train(instanceList);
    
    System.out.println(predict(testText));
    
    
    
    
  }

  public static InstanceList createTrainingData(ArrayList<ArrayList<String>> sentencesChunks) {
    
    final LabelAlphabet labelAlphabet = new LabelAlphabet();
    labelAlphabet.lookupLabel(SentenceBoundary.EOS, true); 
    labelAlphabet.lookupLabel(SentenceBoundary.IS, true);
    
    final Pipe pipe =
        new SerialPipes(new Pipe[] {new SimpleSentence2Pipe(), new OffsetConjunctions(uniGram),
            new TokenSequence2FeatureVectorSequence(true, true)});
    final InstanceList instanceList = new InstanceList(pipe);
    
    

    double chunkPercentage = (chunkSize * 100.0) / numberOfSentences;
    double total = chunkPercentage;
    double i = 0.0;
    final long totalStart = System.currentTimeMillis();
    for (final ArrayList<String> chunk : sentencesChunks) {
      long start = System.currentTimeMillis();
      instanceList.addThruPipe(new Instance(chunk, "", "", file.getName() + i));
      i += 1;
      total = i * chunkPercentage;
      if (total > 100) {
        chunkPercentage = 100.0 - (i-1) * chunkPercentage;
        total = 100.0;
      }
      System.out.println("Completed: " + chunkPercentage + "% in "
          + (System.currentTimeMillis() - start) + " ms");
      System.out.println((total) + "% of Total completed in "
          + ((System.currentTimeMillis() - totalStart) / 1000) + " s");
    }
    return instanceList;
  }

  public static ArrayList<String> readUkWac(File file) {
    final ArrayList<String> sentences = new ArrayList<String>();
    final Symbols symbols = new Symbols();
    final StringBuilder sb = new StringBuilder();;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null;) {
        if (line.startsWith("<text")) {
          continue;
        } else if (line.equals("<s>")) {
          sb.setLength(0);
          continue;
        } else if (line.equals("</s>")) {
          sentences.add(sb.toString());
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
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return sentences;
  }
  
  public String predict(ArrayList<String> sentences) {
    long start = System.currentTimeMillis();
    Instance instance = crf.getInputPipe().instanceFrom(new Instance(sentences, "", "", ""));
    System.out.println("Predict complete");
    System.out.println("Time: " + (System.currentTimeMillis() - start));
    return predict(instance);

  }
  
  public String predict(Instance instance) {
    Sequence input = (Sequence) instance.getData();
    ArrayList<Word> words = (ArrayList<Word>) instance.getName();
    ArrayList<String> labels = new ArrayList<String>();
    
    Sequence<String> labelresults = crf.transduce(input);
    for (int i = 0; i < labelresults.size(); i++) {
      words.get(i).setLabel(labelresults.get(i));
    }
    return new Text(words).getText();
    
  }

  public void train(InstanceList instanceList) {
    long start = System.currentTimeMillis();
    final CRF crf = new CRF(instanceList.getPipe(), (Pipe) null);
    crf.addStatesForLabelsConnectedAsIn(instanceList);
    // get trainer
    final CRFTrainerByLabelLikelihood crfTrainer = new CRFTrainerByLabelLikelihood(crf);
    crfTrainer.trainIncremental(instanceList);
    crf.getInputPipe().getDataAlphabet().stopGrowth();
    System.out.println("Trainingcomplete");
    System.out.println("Time: " + (System.currentTimeMillis() - start));
  }

  public static ArrayList<ArrayList<String>> chunks(ArrayList<String> sentences, int size) {
    final ArrayList<ArrayList<String>> chunks = new ArrayList<ArrayList<String>>();
    for (int i = 0; i < sentences.size(); i += size) {
      final List<String> chunk = sentences.subList(i, Math.min(sentences.size(), i + size));
      chunks.add(new ArrayList<String>(chunk));
    }
    return chunks;
  }
}
