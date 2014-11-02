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
 *
 * @author Arvid
 *
 */
class Sentence2Pipe extends Pipe {

  private static final long    serialVersionUID = 1L;
  private static final Pattern splitPattern     = Pattern.compile("[^\\s]+");
  private final Symbols        symbols;
  private final int            wordWindow;
  private final int            nGrams;

  Sentence2Pipe(int nGrams, int wordWindow) {
    super(new Alphabet(), new LabelAlphabet());
    symbols = new Symbols();
    if ((nGrams > 0) && (nGrams < 4)) {
      this.nGrams = nGrams;
    } else {
      this.nGrams = 3;
    }
    if (wordWindow > 0) {
      this.wordWindow = wordWindow;
    } else {
      this.wordWindow = 5;
    }
  }

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
      for (int j = 0; j < words.size(); j++) {
        /*
         * Add correct label, it is presumed that the word that is EOS has the string "EOS" attached
         * to it.
         */
        String label = SentenceBoundary.IS;
        String currentWord = words.get(j).toString();
        if (symbols.wordEndsWithEOS(currentWord)) {
          label = SentenceBoundary.EOS;
          currentWord = currentWord.substring(0, currentWord.length() - 3);
        }
        final String plainCurrentWord = getPlainWord(currentWord);
        final Token token = new Token(plainCurrentWord);
        /*
         * Add uni-gram features, up to wordWindow.
         */
        /*token.setFeatureValue(plainCurrentWord + "@0", 1);
        for (int i = 1; ((j + i) < words.size()) & (i < wordWindow); i++) {
          token.setFeatureValue(getPlainWord(words.get(j + i).toString()) + "@" + i, 1);
        }
        for (int i = 1; ((j - i) > 0) & (i < 5); i++) {
          token.setFeatureValue(getPlainWord(words.get(j - i).toString()) + "@" + i, 1);
        }
        
         * Add bi-gram features, up to wordWindow
         
        if (nGrams > 1) {
          int i = 0;
          while (((j - i - 1) > 0) && ((i - 1) < wordWindow)) {
            token.setFeatureValue(getPlainWord(words.get(j - i - 1).toString()) + "_"
                + getPlainWord(words.get(j - i).toString()) + "@" + (j - i) + "_" + (j - i - 1), 1);
            i++;
          }
          i = 0;
          while (((j + i + 1) < wordWindow) && ((j + i + 1) < words.size())) {
            token.setFeatureValue(
                getPlainWord(words.get(j + i).toString()) + "_"
                    + getPlainWord(words.get(j + i + 1).toString()) + "@" + (j + i) + "_"
                    + (j + i + 1), 1);
            i++;
          }
        }
        
         * Add tri-gram features, up to wordWindow.
         
        if (nGrams > 2) {
          int i = 0;
          while (((j - i - 2) > 0) && ((i - 2) < wordWindow)) {
            token
                .setFeatureValue(
                    getPlainWord(words.get(j - i - 2).toString()) + "_"
                        + getPlainWord(words.get(j - i - 1).toString()) + "_"
                        + getPlainWord(words.get(j - i).toString()) + "@" + (j - i) + "_"
                        + (j - i - 2), 1);
            i++;
          }
          i = 0;
          while (((j + i + 2) < wordWindow) && ((j + i + 2) < words.size())) {
            token.setFeatureValue(
                getPlainWord(words.get(j + i).toString()) + "_"
                    + getPlainWord(words.get(j + i + 1).toString()) + "_"
                    + getPlainWord(words.get(j + i + 1).toString()) + "@" + (j + i) + "_"
                    + (j + i + 2), 1);
            i++;
          }
        }*/
        token.setFeatureValue(plainCurrentWord + "@WORD", 1);
        for (int i = 1; ((j + i) < words.size()) & (i < wordWindow); i++) {
          token.setFeatureValue(getPlainWord(words.get(j + i).toString()) + "@AFTER", 1);
        }
        for (int i = 1; ((j - i) > 0) & (i < 5); i++) {
          token.setFeatureValue(getPlainWord(words.get(j - i).toString()) + "@BEFORE" + i, 1);
        }
        /*
         * Add bi-gram features, up to wordWindow
         */
        if (nGrams > 1) {
          int i = 0;
          while (((j - i - 1) > 0) && ((i - 1) < wordWindow)) {
            token.setFeatureValue(getPlainWord(words.get(j - i - 1).toString()) + "_"
                + getPlainWord(words.get(j - i).toString()) + "@BEFORE", 1);
            i++;
          }
          i = 0;
          while (((j + i + 1) < wordWindow) && ((j + i + 1) < words.size())) {
            token.setFeatureValue(
                getPlainWord(words.get(j + i).toString()) + "_"
                    + getPlainWord(words.get(j + i + 1).toString()) + "@AFTER", 1);
            i++;
          }
        }
        /*
         * Add tri-gram features, up to wordWindow.
         */
        if (nGrams > 2) {
          int i = 0;
          while (((j - i - 2) > 0) && ((i - 2) < wordWindow)) {
            token
                .setFeatureValue(
                    getPlainWord(words.get(j - i - 2).toString()) + "_"
                        + getPlainWord(words.get(j - i - 1).toString()) + "_"
                        + getPlainWord(words.get(j - i).toString()) + "@BEFORE", 1);
            i++;
          }
          i = 0;
          while (((j + i + 2) < wordWindow) && ((j + i + 2) < words.size())) {
            token.setFeatureValue(
                getPlainWord(words.get(j + i).toString()) + "_"
                    + getPlainWord(words.get(j + i + 1).toString()) + "_"
                    + getPlainWord(words.get(j + i + 1).toString()) + "@AFTER", 1);
            i++;
          }
        }
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
