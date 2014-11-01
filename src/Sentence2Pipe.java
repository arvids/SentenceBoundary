import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * @author Arvid
 * 
 */
class Sentence2Pipe extends Pipe {

  private static final long    serialVersionUID = 1L;
  private static final Pattern splitPattern     = Pattern.compile("[^\\s]+");
  private final Symbols        symbols;
  private static final int WORD_WINDOW = 5;

  Sentence2Pipe() {
    super(new Alphabet(), new LabelAlphabet());
    symbols = new Symbols();
  }

  /* (non-Javadoc)
   * @see cc.mallet.pipe.Pipe#pipe(cc.mallet.types.Instance)
   */
  @Override
  public Instance pipe(Instance instance) {
    
    final String abstractFileName = (String) instance.getSource();
    @SuppressWarnings("unchecked")
    final ArrayList<String> lines = (ArrayList<String>) instance.getData();

    final TokenSequence tokenSequence = new TokenSequence();
    final LabelSequence labelSequence = new LabelSequence(getTargetAlphabet());

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
//        if ((j + 1) == words.size()) {
//          label = SentenceBoundary.EOS;
//        }
        final String currentWord = words.get(j).toString();
        final String plainCurrentWord = getPlainWord(currentWord);
        final Token token = new Token(plainCurrentWord);
        
        if (symbols.wordEndsWithEOSSymbol(currentWord)) {
          //token.setFeatureValue(getSymbol(currentWord), 1);
          label = SentenceBoundary.EOS;
        }
        
        token.setFeatureValue(plainCurrentWord + "@0", 1);
        
        //current word as feature
        
        
        //unigram for each word before current word.
        for (int k = 1; j - k > 0 & k < 5; k++) {
          token.setFeatureValue(getPlainWord(words.get(j-k).toString()) + "@" + k, 1);
        }
        
        //unigram of each word around current word.
        for (int l = 1; j + l < words.size() & l < WORD_WINDOW; l++) {
          token.setFeatureValue(getPlainWord(words.get(j+l).toString()) + "@" + l, 1);
        }
        for (int k = 1; j - k > 0 & k < 5; k++) {
          token.setFeatureValue(getPlainWord(words.get(j-k).toString()) + "@" + k, 1);
        }
        
        //bigram for each set of words around current word.
        if (j-2 > 0) {
          token.setFeatureValue(getPlainWord(words.get(j-1).toString()) + "_" + getPlainWord(words.get(j-2).toString()) + "@" + (j-1) + "_" + (j-2), 1);
        }
        if (j+2 < words.size()) {
          token.setFeatureValue(getPlainWord(words.get(j+1).toString()) + "_" + getPlainWord(words.get(j+2).toString()) + "@" + (j+1) + "_" + (j+2), 1);
        }
        
        //trigram around word
        if (j-3 > 0) {
          token.setFeatureValue(getPlainWord(words.get(j-1).toString()) + "_" + getPlainWord(words.get(j-2).toString()) + "_" + getPlainWord(words.get(j-3).toString()) + "@" + (j-1) + "_" + (j-3), 1);
        }
        if (j+3 < words.size()) {
          token.setFeatureValue(getPlainWord(words.get(j+1).toString()) + "_" + getPlainWord(words.get(j+2).toString()) + "_" + getPlainWord(words.get(j+3).toString()) + "@" + (j+1) + "_" + (j+3), 1);
        }
        
//        //position 
//        if (j<3) {
//          token.setFeatureValue("START", 1);
//        } 
//        else if (j > words.size() - 3) {
//          token.setFeatureValue("END", 1);
//        }
        
        
        
        
        
        
        
        
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
    return word.toLowerCase();
  }

  private String getSymbol(String word) {
    return word.substring(word.length() - 1, word.length());
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
