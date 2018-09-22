import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BM25 {

    void printBM25(List<String> tokens, double totalNumberOfDocsInCollection, double avgTotalNumberOfDocsInCollection,
                HashMap<Integer, Integer> docIDToFrequency, HashMap<Integer, String> idAndDocno, int topicId,
                HashMap<Integer, List<Integer>> invertedIndex, HashMap<String, Integer> lexiconTokenToId,
                HashMap<Integer, String> lexiconIdToToken, PrintWriter pw) {

        HashMap<String, Double> accumulator = new HashMap<String, Double>();
        for (int i = 0; i < tokens.size(); ++i) {
            String term = tokens.get(i).toLowerCase();
            if (lexiconTokenToId.get(term) == null) {
                continue;
            }
            int termId = lexiconTokenToId.get(term);
            List<Integer> postingsList = invertedIndex.get(termId);
            for (int id = 0; id < postingsList.size(); id += 2) {
                String docno = idAndDocno.get(postingsList.get(id));
                double docLength = docIDToFrequency.get(postingsList.get(id));
                double freqOfTermInDoc = postingsList.get(id+1);
                double numberOfDocsWithTerm = postingsList.size()/2;
                double bm25Score = calculateBM25(docLength, freqOfTermInDoc, numberOfDocsWithTerm, avgTotalNumberOfDocsInCollection, totalNumberOfDocsInCollection);
                if (accumulator.containsKey(docno)) {
                    accumulator.put(docno, accumulator.get(docno) + bm25Score);
                } else {
                    accumulator.put(docno, bm25Score);
                }
            }
        }
        String q0 = "Q0";
        String runTag = "aturarAND";
        Map<String, Double> sortedMapDesc = sortByComparator(accumulator, false);
        int rank = 1;
        for (Map.Entry<String, Double> entry : sortedMapDesc.entrySet()) {
            // Iterating through all the doc ids and writing them to a file with a topicID
            pw.print( topicId + " "
                        + q0 + " "
                        + entry.getKey() + " "
                        + rank + " "
                        + entry.getValue() + " "
                        + runTag
                        + "\n");
            rank++;
            if (rank == 1000) break;
        }
    }
    
    double calculateBM25(double docnoLength, double freqOfTermInDoc, double numberOfDocsWithTerm, double avgTotalNumberOfDocsInCollection, double totalNumberOfDocsInCollection) {
        double k = 1.2 * ((1 - 0.75) + 0.75 * docnoLength/avgTotalNumberOfDocsInCollection);
        double numerator = (1.2 + 1)  * freqOfTermInDoc * (7 + 1);
        double denominator = (k + freqOfTermInDoc) * (7+1);
        double bm25Score = (numerator/denominator) * Math.log((totalNumberOfDocsInCollection - numberOfDocsWithTerm + 0.5)/(numberOfDocsWithTerm + 0.5));  
        return bm25Score;
    }

 private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order)
 {

     List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

     // Sorting the list based on values
     Collections.sort(list, new Comparator<Entry<String, Double>>()
     {
         public int compare(Entry<String, Double> o1,
                 Entry<String, Double> o2)
         {
             if (order)
             {
                 return o1.getValue().compareTo(o2.getValue());
             }
             else
             {
                 return o2.getValue().compareTo(o1.getValue());

             }
         }
     });

     // Maintaining insertion order with the help of LinkedList
     Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
     for (Entry<String, Double> entry : list)
     {
         sortedMap.put(entry.getKey(), entry.getValue());
     }
     return sortedMap;
 }

 // taken from https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values




}
