import java.util.ArrayList;
import java.util.HashMap;
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
  private final Symbols        symbols;

  SimpleSentence2Pipe() {
    super(new Alphabet(), new LabelAlphabet());
    symbols = new Symbols();
  }

  @Override
  public Instance pipe(Instance instance) {
    final String abstractFileName = (String) instance.getSource();
    final ArrayList<String> lines = (ArrayList<String>) instance.getData();
    final HashMap<String, Integer> wordFrequency = getWordFrequency(lines);
    final TokenSequence tokenSequence = new TokenSequence();
    final LabelSequence labelSequence = new LabelSequence(getTargetAlphabet());
    // Should probably make a sentence class
    final ArrayList<Word> wordInfo = new ArrayList<Word>();
    for (final String line : lines) {
      if (line.length() == 0) {
        continue;
      }
      final ArrayList<Word> words = getWords(line);
      if (words.size() == 0) {
        continue;
      }
      // loop through the sentence
      for (int j = 0; j < words.size(); j++) {
        String label = SentenceBoundary.IS;
        if ((j + 1) == words.size()) {
          label = SentenceBoundary.EOS;
        }
        final String currentWord = words.get(j).toString();
        final String plainCurrentWord = getPlainWord(currentWord);
        final Token token = new Token(plainCurrentWord);
        if (j == 0) {
          token.setFeatureValue("BOS", 1);
        } else if ((j + 1) == words.size()) {
          label = SentenceBoundary.EOS;
          token.setFeatureValue("EOS", 1);
        }
        if (symbols.wordEndsWithEOSSymbol(currentWord)) {
          token.setFeatureValue("SYMBOL=" + getSymbol(currentWord), 1);
        }
        token.setFeatureValue("TOKEN=" + plainCurrentWord, 1);
        tokenSequence.add(token);
        labelSequence.add(label);
      }
      wordInfo.addAll(words);
    }
    instance.setData(tokenSequence);
    instance.setTarget(labelSequence);
    instance.setName(wordInfo);
    instance.setSource(abstractFileName);
    return instance;
  }

  private String getPlainWord(String word) {
    if (symbols.wordEndsWithSymbol(word)) {
      word = word.substring(0, word.length() - 1);
    } else if (symbols.wordStartsWithSymbol(word)) {
      word = word.substring(1, word.length());
    }
    return word;
  }

  private String getSymbol(String word) {
    return word.substring(word.length() - 1, word.length());
  }

  private HashMap<String, Integer> getWordFrequency(ArrayList<String> lines) {
    final HashMap<String, Integer> freq = new HashMap<String, Integer>();
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      final ArrayList<Word> words = getWords(line);
      for (int j = 0; j < words.size(); j++) {
        final Word word = words.get(j);
        int count = 0;
        if (freq.containsKey(word.toString())) {
          count = freq.get(word.toString());
        }
        count++;
        freq.put(word.toString(), count);
      }
    }
    return freq;
  }

  private ArrayList<Word> getWords(String line) {
    final Matcher matcher = splitPattern.matcher(line);
    final ArrayList<Word> words = new ArrayList<Word>();
    while (matcher.find()) {
      final String word = matcher.group();
      words.add(new Word(word));
    }
    return words;
  }
}
