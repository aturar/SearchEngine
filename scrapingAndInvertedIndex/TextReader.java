import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class TextReader {

    public void ProcessInput(String pathStringToCompressedTextFile, String pathToTheOutput) {
        SnippetEngine snipEng = new SnippetEngine();
        int internalId = 0;
        BufferedWriter out = null;
        List<DocumentTokenizer> listOfDocs = new ArrayList<DocumentTokenizer>();
        HashMap<Integer, MetaData> idAndMetadata = new HashMap<Integer, MetaData>();
        HashMap<String, Integer> docnoAndId = new HashMap<String, Integer>();
        HashMap<Integer, String> idAndDocno = new HashMap<Integer, String>();
        HashMap<String, String> docnoToDocPath = new HashMap<String, String>();

        if (pathStringToCompressedTextFile != null) {
            try {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(pathStringToCompressedTextFile));
                BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
                StringBuffer strBuffer = new StringBuffer();
                String line = getLine(strBuffer, br);
                while (line != null) {
                    DocumentTokenizer docTokenizer = new DocumentTokenizer();
                    docTokenizer.stringToTokenize = "";
                    MetaData metaData = new MetaData();
                    // Assume that the first line always starts with "DOC"
                    // The line after DOC is DOCNO
                    line = getLine(strBuffer, br);
                    String filePath = processDocno(line, snipEng, pathToTheOutput, metaData, internalId);
                    try {
                        FileWriter fstream = new FileWriter(filePath + ".txt");
                        out = new BufferedWriter(fstream);
                    } catch (IOException e) {
                    }
                    docTokenizer.stringToTokenize = "";
                    while(!line.equals("</DOC>")) {
                        if (line.contains("<HEADLINE>")) {
                            metaData.Headline = "";
                            getToHeadline("</HEADLINE>", strBuffer, br, line, docTokenizer, metaData);
                        } else if (line.contains("<TEXT>")) {
                            getTo("</TEXT>", strBuffer, br, line, docTokenizer, "<TEXT>");
                        } else if (line.contains("<GRAPHIC>")) {
                            getTo("</GRAPHIC>", strBuffer, br, line, docTokenizer, "<GRAPHIC>");
                        }
                        line = getLine(strBuffer, br);
                    }
                    listOfDocs.add(docTokenizer);
                    out.write(strBuffer.toString());
                    idAndMetadata.put(internalId, metaData);
                    docnoAndId.put(metaData.Docno, internalId);
                    idAndDocno.put(internalId, metaData.Docno);
                    docnoToDocPath.put(metaData.Docno, filePath);
                    internalId++;
                    out.close();
                    // Writing to the document as we go
                    strBuffer = new StringBuffer();
                    line = getLine(strBuffer, br);
                }
                InvertedIndex invIndex = new InvertedIndex();
                invIndex.BuildInvertedIndex(listOfDocs, pathToTheOutput);
                PrintHashMapsToTheDisc(idAndMetadata, docnoAndId,idAndDocno, docnoToDocPath, pathToTheOutput);
                } catch (IOException e) {
                    System.out.println(e);
                }
        }
    }
    
    void getTo(String endingTag, StringBuffer strBuffer, BufferedReader br,
                String line, DocumentTokenizer docTokenizer, String startingTag) {
        line = getLine(strBuffer, br); // Process the tag line
        while (!line.equals(endingTag)) {
            line = getLine(strBuffer, br);
            if (!(line.contains("<P>") || line.contains("</P>"))) {
                docTokenizer.stringToTokenize += line;
            }
        }
    }

    void getToHeadline(String endingTag, StringBuffer strBuffer, BufferedReader br,
                String line, DocumentTokenizer docTokenizer, MetaData metaData) {
        line = getLine(strBuffer, br); // Process the tag line
        while (!line.equals(endingTag)) {
            line = getLine(strBuffer, br); 
            if (!(line.contains("<P>") || line.contains("</P>") || line.contains("</HEADLINE>"))) {
                docTokenizer.stringToTokenize += line;
                metaData.Headline += line;
            }
        }
    }

    String processDocno(String line, SnippetEngine snipEng, String pathToTheOutput, 
                                                MetaData metaData, int internalId) {
        String filePathToTheRawData = snipEng.DirectoryPathToSaveRawData(line, pathToTheOutput);
            // Building the file name, DOCNO
        String[] docnoList = line.split(" ");
        String docno = docnoList[1];
        String dateInteger = "";
        // Tokenizing the LAmmddyy into mmddyy
        for (int i = 2; i < 8; ++i) {
            dateInteger += docno.charAt(i);
        }
        metaData.Date = dateInteger;
        metaData.Docno = docno;
        metaData.Headline = "";
        String filePath = filePathToTheRawData + "/" + docno;
        return filePath;
    }

    // Appending all the lines in a document in one string buffer
    // Writing to the file as soon as the program is done reading one <DOC> 
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

    // serializing two hashmaps to write in bytes to the disk
    public static void PrintHashMapsToTheDisc(HashMap<Integer, MetaData> idAndMetadataMap
                                            ,HashMap<String, Integer> docnoAndIdMap
                                            ,HashMap<Integer, String> idAndDocnoMap
                                            ,HashMap<String, String> docnoToDocPath
                                            ,String pathToTheOutput) {
        try {
            FileOutputStream fileOutput = new FileOutputStream(pathToTheOutput + "/idAndMetadata.txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOutput);
            out.writeObject(idAndMetadataMap);
            out.close();
            fileOutput.close();

            fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/idAndMetadata.txt");
            out = new ObjectOutputStream(fileOutput);
            out.writeObject(idAndMetadataMap);
            out.close();
            fileOutput.close();
 
            fileOutput = new FileOutputStream(pathToTheOutput + "/docnoAndId.txt"); 
            out = new ObjectOutputStream(fileOutput);
            out.writeObject(docnoAndIdMap);
            out.close();
            fileOutput.close();

            fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/idAndDocno.txt"); 
            out = new ObjectOutputStream(fileOutput);
            out.writeObject(idAndDocnoMap);
            out.close();
            fileOutput.close();

            fileOutput = new FileOutputStream(pathToTheOutput + "/latimes-index/docnoToDocPath.txt"); 
            out = new ObjectOutputStream(fileOutput);
            out.writeObject(docnoToDocPath);
            out.close();
            fileOutput.close();
         } catch (IOException i) {
            i.printStackTrace();
         }
         // taken from https://www.tutorialspoint.com/java/java_serialization.htm 
    }
}
    
