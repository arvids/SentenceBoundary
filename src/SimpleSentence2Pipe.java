import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

class SimpleSentence2Pipe extends Pipe {

  private static final long    serialVersionUID = 1L;
  private static final Pattern splitPattern     = Pattern.compile("[^\\s]+");
  TreeSet<String>              eosSymbols;

  SimpleSentence2Pipe() {
    super(new Alphabet(), new LabelAlphabet());
    eosSymbols = new Symbols().getEOSSymbols();
  }

  @Override
  public Instance pipe(Instance instance) {
    final String abstractFileName = (String) instance.getSource();
    final ArrayList<String> lines = (ArrayList<String>) instance.getData();
    final HashMap<String, Integer> wordFrequency = getWordFrequency(lines);
    final TokenSequence tokenSequence = new TokenSequence();
    final LabelSequence labelSequence = new LabelSequence(getTargetAlphabet());
    
    //Should probably make a sentence class
    final ArrayList<Word> subSentenceInfo = new ArrayList<Word>();

    for (final String line : lines) {
      if (line.length() == 0) {
        continue;
      }
      final ArrayList<Word> words = getSubSentences(line);
      if (words.size() == 0) {
        continue;
      }
      for (int j = 0; j < words.size(); j++) {
        String label = SentenceBoundary.IS;
        final String currentSentence = words.get(j).getSentence();
        final String plainCurrentSentence = getPlainWord(currentSentence);
        final Token token = new Token(currentSentence);
        
        if (containsEOSSymbol(currentSentence)) {
          token.setFeatureValue("endwithEOSSymb=" + getEOSSymbol(currentSentence), 1);
        }
        if ((j + 1) == words.size()) {
          label = SentenceBoundary.EOS;
        }
        final int count = nrEOSSymbolsContained(plainCurrentSentence);
        if (count > 0) {
          token.setFeatureValue("hasinnerEOSSymb=" + count, 1);
        }
        // the token itself
        token.setFeatureValue("TOKEN=" + currentSentence, 1);
        // check whether token with EOSsymbol occurs more than once in
        // abstract
        if (containsEOSSymbol(currentSentence)) {
          final int freq = wordFrequency.get(currentSentence).intValue();
          if (freq > 1) {
            token.setFeatureValue("FreqTokenEOSSymbol", 1);
          }
        }
        tokenSequence.add(token);
        labelSequence.add(label);
      }
      subSentenceInfo.addAll(words);
    }
    
    instance.setData(tokenSequence);
    instance.setTarget(labelSequence);
    instance.setName(subSentenceInfo);
    instance.setSource(abstractFileName);
    return instance;
  }

  private int nrEOSSymbolsContained(String token) {
    int count = 0;
    final char[] c = token.toCharArray();
    for (final char element : c) {
      final char[] cc = {element};
      if (eosSymbols.contains(new String(cc))) {
        count++;
      }
    }
    return count;
  }

  private boolean containsEOSSymbol(String token) {
    if (token.length() > 0) {
      final String lastChar = token.substring(token.length() - 1, token.length());
      if (eosSymbols.contains(lastChar)) {
        return true;
      }
    }
    return false;
  }

  private String getEOSSymbol(String token) {
    if (token.length() > 0) {
      final String lastChar = token.substring(token.length() - 1, token.length());
      if (eosSymbols.contains(lastChar)) {
        return lastChar;
      }
    }
    return "";
  }

  private String getPlainWord(String word) {
    if (containsEOSSymbol(word)) {
      return word.substring(0, word.length() - 1);
    } else {
      return word;
    }
  }

  private HashMap<String, Integer> getWordFrequency(ArrayList<String> lines) {
    final HashMap<String, Integer> freq = new HashMap<String, Integer>();
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      final ArrayList<Word> words = getSubSentences(line);
      for (int j = 0; j < words.size(); j++) {
        final Word word = words.get(j);
        int count = 0;
        if (freq.containsKey(word.getSentence())) {
          count = freq.get(word.getSentence());
        }
        count++;
        freq.put(word.getSentence(), count);
      }
    }
    return freq;
  }

  private ArrayList<Word> getSubSentences(String line) {
    final Matcher matcher = splitPattern.matcher(line);
    final ArrayList<Word> words = new ArrayList<Word>();
    while (matcher.find()) {
      final int start = matcher.start();
      final int end = matcher.end();
      final String rep = matcher.group();
      words.add(new Word(start, end, rep));
    }
    return words;
  }
}
