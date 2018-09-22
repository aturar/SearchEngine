import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.io.IOException;
import java.lang.Object;

public class Main {
    // args[0] - the directory location of the data with index and raw data
    // args[1] - directory to where to store the bm25
    // Output
    public static void main(String[] args) {
        try {   
            // Reading inverted index   
            FileInputStream invIndexInputStream = new FileInputStream(args[0] + "/latimes-index/invertedIndex.txt");
            ObjectInputStream in = new ObjectInputStream(invIndexInputStream);
            HashMap<Integer, List<Integer>> invertedIndex = (HashMap<Integer, List<Integer>>) in.readObject();

            // Reading lexicon that maps Token to Id
            FileInputStream lexiconTokenToIdInputStream = new FileInputStream(args[0] + "/latimes-index/lexiconTokenToId.txt");
            in = new ObjectInputStream(lexiconTokenToIdInputStream);
            HashMap<String, Integer> lexiconTokenToId = (HashMap<String, Integer>) in.readObject();

            // Reading lexicon that maps Id to Token
            FileInputStream lexiconIdToTokenInputStream = new FileInputStream(args[0] + "/latimes-index/lexiconIdToToken.txt");
            in = new ObjectInputStream(lexiconIdToTokenInputStream);
            HashMap<Integer, String> lexiconIdToToken = (HashMap<Integer, String>) in.readObject();


            FileInputStream idAndDocnoInputStream = new FileInputStream(args[0] + "/latimes-index/idAndDocno.txt"); 
            in = new ObjectInputStream(idAndDocnoInputStream);
            HashMap<Integer, String> idAndDocno = (HashMap<Integer, String>) in.readObject();

            // Document ids and corresponding frequencies
            FileInputStream docIDToFrequencyStream = new FileInputStream(args[0] + "/latimes-index/docIDToFrequency.txt");
            in = new ObjectInputStream(docIDToFrequencyStream);
            HashMap<Integer, Integer> docIDToFrequency = (HashMap<Integer, Integer>) in.readObject();
            //Docno to it's path in the document 
            FileInputStream docidToDocPathStream = new FileInputStream(args[0] + "/latimes-index/docnoToDocPath.txt");
            in = new ObjectInputStream(docidToDocPathStream);
            HashMap<String, String> docnoToDocPath = (HashMap<String, String>) in.readObject();

            // Calculating the constants for BM25
            double cumilativeScore = 0;
            for (Map.Entry<Integer, Integer> entry : docIDToFrequency.entrySet()) {
                cumilativeScore += entry.getValue();
            }
            double avgTotalNumberOfDocsInCollection = cumilativeScore/docIDToFrequency.size();
            double totalNumberOfDocsInCollection = docIDToFrequency.size();

            while (true) {
                //done processing the index, let's ask for the user query
                System.out.println("Please input the query");
                Scanner scanner = new Scanner(System.in);
                String query = scanner.nextLine();
                if (query.toLowerCase().equals("q")) {
                    break;
                }

                // docTokenizer to read through the query
                DocumentTokenizer docTokenizer = new DocumentTokenizer();
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter(args[1]));
                    int topicId = 427;
                    List<String> tokens = docTokenizer.Tokenize(query);
                    BM25 bm25 = new BM25();
                    bm25.printBM25(tokens, totalNumberOfDocsInCollection, avgTotalNumberOfDocsInCollection, 
                                        docIDToFrequency, idAndDocno, topicId,
                                        invertedIndex, lexiconTokenToId, lexiconIdToToken, pw);
                    pw.close(); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long startTime = System.nanoTime();
                List<String> queryTerms = docTokenizer.Tokenize(query);
                QueryBasedSummary.createQueryBasedSummary(docnoToDocPath, args[1], queryTerms);
                long endTime = System.nanoTime();
                long duration = (endTime - startTime)/1000000; 
                System.out.println("Retrieval took " + (duration/1000.0) + " seconds.");
                System.out.println("Would you like to repeat? type N to type a new query");
                System.out.println("Type q to quit");
                

                String response = scanner.nextLine();
                if (response.toLowerCase().equals("n")) {
                    // repeating again
                }
                if (response.toLowerCase().equals("q")) {
                    break;
                }
            }
            System.out.println("Bye :(");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
            return;
        }
    }

    // Reads the line by line the txt file and appends it to the string buffer
    public static String getLine(StringBuffer strBuffer, BufferedReader br) { 
        String line = "";
        try {
            line = br.readLine();
            strBuffer.append(line);
            strBuffer.append("\n");
        } catch (IOException e) {
            System.out.println(e);
        }
        return line;
    }
}   
