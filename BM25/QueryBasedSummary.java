import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class QueryBasedSummary {
    private int rank;

    static void createQueryBasedSummary(HashMap<String, String> docnoToDocPath, 
                                String pathToTheOutput, List<String> queryTerms) {
        try {
            HashMap<String, HeadlineAndSummary> docnoAndSnippet = new LinkedHashMap<String, HeadlineAndSummary>();
            // reading through the created bm25 txt file
            BufferedReader br = new BufferedReader(new FileReader(pathToTheOutput));
            StringBuffer strBuffer = new StringBuffer();
            String line = getLine(strBuffer, br);
            for (int i = 0; i < 10; ++i) {
                if (line == null) {
                    System.out.println("nothing found, sorry :( ");
                    return;
                }
                List<String> tokens = Arrays.asList(line.split("\\s+"));

                String docno = tokens.get(2).toUpperCase();
                int rank = Integer.parseInt(tokens.get(3));
                // Return the full path to the given docno
                String docPath = docnoToDocPath.get(docno);
                // Return list of all sentences from the document
                docnoAndSnippet.put(docno, ProcessInput(docPath, queryTerms, rank));
                line = getLine(strBuffer, br);
            }
            outputFormattedHeadlineAndSummary(docnoAndSnippet);
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

    static void outputFormattedHeadlineAndSummary(HashMap<String, HeadlineAndSummary> docnoAndSnippet) {
        for (Map.Entry<String, HeadlineAndSummary> entry : docnoAndSnippet.entrySet()) {
            System.out.print(entry.getValue().getRank());
            System.out.print(". ");
            System.out.print(entry.getValue().getHeadline() + " (");
            System.out.println(entry.getValue().getDate() + ")");
            // new line
            System.out.print(entry.getValue().getSummary() + " (");
            System.out.println(entry.getKey() + ")");
            System.out.println("---------------------------------------------------------------------");
        }
    }

    public static HeadlineAndSummary ProcessInput(String pathToDoc, List<String> queryTerms, int rank) {
        HeadlineAndSummary HandS = null;
        try {
            File file = new File(pathToDoc + ".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer strBuffer = new StringBuffer();
            String line = getLine(strBuffer, br);
            DocumentTokenizer docTokenizer = new DocumentTokenizer();
            MetaData metaData = new MetaData();
            // Assume that the first line always starts with "DOC"
            // The line after DOC is DOCNO
            line = getLine(strBuffer, br);
            processDate(line, metaData);
            docTokenizer.stringToTokenize = "";
            while(!line.equals("</DOC>")) {
                if (line.contains("<HEADLINE>")) {
                    metaData.Headline = "";
                    getToHeadline("</HEADLINE>", strBuffer, br, line, metaData);
                } else if (line.contains("<TEXT>")) {
                    getTo("</TEXT>", strBuffer, br, line, docTokenizer, "<TEXT>");
                } else if (line.contains("<GRAPHIC>")) {
                    getTo("</GRAPHIC>", strBuffer, br, line, docTokenizer, "<GRAPHIC>");
                }
                line = getLine(strBuffer, br);
            }
            HandS = new HeadlineAndSummary(metaData.Headline, docTokenizer.stringToTokenize, metaData.Date, queryTerms, rank);
            } catch (IOException e) {
                System.out.println(e);
            }
        return HandS;
    }
    
    static void getTo(String endingTag, StringBuffer strBuffer, BufferedReader br,
                String line, DocumentTokenizer docTokenizer, String startingTag) {
        line = getLine(strBuffer, br); // Process the tag line
        while (!line.equals(endingTag)) {
            line = getLine(strBuffer, br);
            if (!(line.contains("<P>") || line.contains("</P>"))) {
                docTokenizer.stringToTokenize += line;
            }
        }
    }

    static void getToHeadline(String endingTag, StringBuffer strBuffer, BufferedReader br,
                        String line, MetaData metaData) {
        line = getLine(strBuffer, br); // Process the tag line
        while (!line.equals(endingTag)) {
            line = getLine(strBuffer, br); 
            if (!(line.contains("<P>") || line.contains("</P>") || line.contains("</HEADLINE>"))) {
                metaData.Headline += line;
            }
        }
    }

    static void processDate(String line, MetaData metaData) {
        String[] docnoList = line.split(" ");
        String docno = docnoList[1];
        String dateInteger = "";
        // Tokenizing the LAmmddyy into mmddyy
        // Month
        dateInteger = docno.charAt(2) + "" + docno.charAt(3) + "/";
        // Day
        dateInteger += docno.charAt(4) + "" + docno.charAt(5) + "/";
        // year
        dateInteger += docno.charAt(6) + "" + docno.charAt(7);

        metaData.Date = dateInteger;
        metaData.Docno = docno;
        metaData.Headline = "";
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
    
