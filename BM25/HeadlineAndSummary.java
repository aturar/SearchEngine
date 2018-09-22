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

public class HeadlineAndSummary {
    private String headline = "";
    private String summary = "";
    private String date = "";
    private List<String> queryTerms = new ArrayList<String>();     
    private int rank;

    public HeadlineAndSummary(String headline, String summary, String date, List<String> queryTerms, int rank) {
        this.headline = headline;
        // Process summary into parsed sentences 
        this.queryTerms = queryTerms;
        this.summary = getSnippet(summary);
        this.date = date;
        this.rank = rank;
    }

    Integer getRank() {
        return this.rank;
    }

    String return50CharsFromSummary() {
        return getHeadline();
    }

    String getDate() {
        return this.date;
    }

    String getSummary() {
        return this.summary;
    }

    String getHeadline() {
        if (this.headline == "") {
            return get50CharsFromSummary();
        }
        return this.headline;
    }

    String get50CharsFromSummary() {
        String headline = "";
        for (int i = 0; i < 50; ++i) {
            headline += this.summary.charAt(i);
        }
        return headline;
    }

    String getSnippet(String summary) {
        int start = 0, i = 0;
        List<String> sentences  = new ArrayList<String>();     
        for (i = 0; i < summary.length(); ++i) {
            char c = summary.charAt(i);
            if ((c == '!' || c == '.' || c == '?')) {
                if (start != i) {
                  if (summary.charAt(start) == ' ') {
                    start++;
                  }
                  sentences.add(summary.substring(start, i));
                }   
                start = i + 1;
            }
        } 
        if (start != i) {
            sentences.add(summary.substring(start, i));
        }

        HashMap<String, Integer> sentencesWithMostTerms = new HashMap<String, Integer>();
        // Looping through sentences and getting the snippet
        DocumentTokenizer docTokenizer = new DocumentTokenizer();
        
        for (int k = 0; k < sentences.size(); ++k) {
            // Break the sentence into tokens
            List<String> termsInSentence = docTokenizer.Tokenize(sentences.get(k));
            int termFrequency = 0;
            // Iterating through the sentence and counting how many terms there are from a query
            for (int j = 0; j < this.queryTerms.size(); ++j) {
                for (String term: termsInSentence) {
                    if (term.equals(this.queryTerms.get(j))) {
                        termFrequency++;
                    }
                }                
            }
            if (termFrequency != 0) {
                sentencesWithMostTerms.put(sentences.get(k), termFrequency);
            }
        }

        String snippet = "";
        Map<String, Integer> sortedMap = sortByComparator(sentencesWithMostTerms, false);
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            snippet += entry.getKey() + ". ";
            if (snippet.length() > 300) {
                break;
            }
        }
        return snippet;
    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());
        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
        {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2)
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
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
   
    // taken from https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
}

