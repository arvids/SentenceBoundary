import java.io.File;

public class Main {

  public static void main(String[] args) {
    final String filename = args[0];
    final File file = new File(filename);
    new SimpleSentenceBoundaryDetection(file,20000);
  }
}
