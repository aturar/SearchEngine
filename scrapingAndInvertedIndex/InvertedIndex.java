import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class InvertedIndex { 

    public void BuildInvertedIndex(List<DocumentTokenizer> listOfDocs, String pathToTheOutput) {
        DocumentTokenizer docTokenizer = new DocumentTokenizer();
        List<String> tokens = null;
        HashMap<String, Integer> lexiconTokenToId = new HashMap<String, Integer>();
        HashMap<Integer, String> lexiconIdToToken = new HashMap<Integer, String>();
        HashMap<Integer, Integer> wordCount = new HashMap<Integer, Integer>();
        HashMap<Integer, List<Integer>> invIndex = new HashMap<Integer, List<Integer>>();
        HashMap<Integer, Integer> docIdToFrequency = new HashMap<Integer, Integer>();
        List<Integer> tokensIds;
        int docId = 0;
        
        for (DocumentTokenizer doc: listOfDocs) {
            // tokens - list of tokens in a single doc
            tokens = docTokenizer.Tokenize(doc);
            // list of token ids in a single doc
           // System.out.println(tokens);
            tokensIds = convertTokensToIds(tokens, lexiconTokenToId, lexiconIdToToken);
            // frequence of the words within a single doc
            // key = word id
            // value = frequency of the word
            wordCount = countWords(tokensIds);
            addToPostings(wordCount, docId, invIndex);
            docIdToFrequency.put(docId, tokens.size());
            docId++;
        }
        writeInvertedIndexToDisk(invIndex, lexiconTokenToId, lexiconIdToToken, pathToTheOutput);
        writeDocIdToFrequencyToDisk(docIdToFrequency, pathToTheOutput);
    }

    List<Integer> convertTokensToIds(List<String> tokens, HashMap<String, Integer> lexiconTokenToId, HashMap<Integer, String> lexiconIdToToken) {
        List<Integer> tokenIds = new ArrayList<Integer>();
        int id = 0;
        for (int i = 0; i < tokens.size(); ++i) {
            if (lexiconTokenToId.containsKey(tokens.get(i))) {
                tokenIds.add(lexiconTokenToId.get(tokens.get(i)));
            } else {
                id = lexiconTokenToId.size();
                lexiconTokenToId.put(tokens.get(i), id);
                lexiconIdToToken.put(id, tokens.get(i));
                tokenIds.add(id);
            }
        }
        return tokenIds;
    }

    HashMap<Integer, Integer> countWords(List<Integer> tokenIds) {
        HashMap<Integer, Integer> wordCount = new HashMap<Integer, Integer>();
        for (int i = 0; i < tokenIds.size(); ++i) {
            if (wordCount.containsKey(tokenIds.get(i))) {
                wordCount.put(tokenIds.get(i), wordCount.get(tokenIds.get(i))+1);
            } else {
                wordCount.put(tokenIds.get(i), 1);
            }
        }
        return wordCount;
    }

    void addToPostings(Map<Integer, Integer> wordCount, int docId, Map<Integer, List<Integer>> invIndex) {
        List<Integer> postings;
        for (Map.Entry<Integer, Integer> entry : wordCount.entrySet()) {
            int wordId = entry.getKey();
            int frequency = entry.getValue();
            if (invIndex.containsKey(wordId)) {
                postings = invIndex.get(wordId);
            } else {
                postings = new ArrayList();
            }
            postings.add(docId);
            postings.add(frequency);
            invIndex.put(wordId, postings);
        }
    }

    // Serialization of the inverted index and two lexicon 
    // writing to a index folder
    void writeInvertedIndexToDisk(HashMap<Integer, List<Integer>> invIndex, HashMap<String, Integer> lexiconTokenToId,
                                HashMap<Integer, String> lexiconIdToToken, String pathToTheOutput) {
        try {
           Files.createDirectories(Paths.get(pathToTheOutput + "/latimes-index"));
           FileOutputStream fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/invertedIndex.txt");
           ObjectOutputStream out = new ObjectOutputStream(fileOutput);
           out.writeObject(invIndex);
           out.close();
           fileOutput.close();

           fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/lexiconIdToToken.txt");
           out = new ObjectOutputStream(fileOutput);
           out.writeObject(lexiconIdToToken);
           out.close();
           fileOutput.close();

           fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/lexiconTokenToId.txt");
           out = new ObjectOutputStream(fileOutput);
           out.writeObject(lexiconTokenToId);
           out.close();
           fileOutput.close();
        } catch (IOException i) {
           i.printStackTrace();
        }
        // taken from https://www.tutorialspoint.com/java/java_serialization.htm 
    }

    void writeDocIdToFrequencyToDisk(HashMap<Integer, Integer> docIdToFreq, String pathToTheOutput) {
        try {
            Files.createDirectories(Paths.get(pathToTheOutput + "/latimes-index"));
            FileOutputStream fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/docIDToFrequency.txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOutput);
            out.writeObject(docIdToFreq);
            out.close();    
            fileOutput.close();
         } catch (IOException i) {
            i.printStackTrace();
         }
         // taken from https://www.tutorialspoint.com/java/java_serialization.htm 
    }
}
