import java.io.*;
import java.util.*;
import java.nio.file.Files;

public class program1 { 
  // args[0] - Path to the zip text file
  // args[1] - Path to the data and the metadata
  public static void main(String[] args) {
    if (!CheckIfInputArgumentsAreValid(args)) {
      System.out.println("Please enter the appropriate arguments for the program"
                        + "\nThe first argument should contain the path to the compressed data"
                        + "\nThe second argument should contain the path to the directory to store the documents and metadata");
        return;
    }
    // Check if the directory to store the document and its metadata already exists
    if (new File(args[1]).exists()) {
        System.out.println("The directory to where you want to store the" 
                        + "documents and metadata already exists");
        return;
    }
    if (!(new File(args[0]).exists())) {
      System.out.println("The latimes.gz compressed file doesn't exist in the specific directory");
      return;
    }
    // Create the directory to store the raw data and its metadata
    File dir = new File(args[0]);
    TextReader obj = new TextReader();
    obj.ProcessInput(args[0], args[1]);
  }

  public static boolean CheckIfInputArgumentsAreValid(String[] args) {
    if (args.length <= 1) {
      return false;
    }
    return true;
  }
}
