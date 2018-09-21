import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class SnippetEngine {

    public String DirectoryPathToSaveRawData(String docno, String pathToTheRawDataOutput) {
        String docnoLowerCase = docno.toLowerCase();
        // Pass to the method to return the day and the year
        String[] dateList = TokenizedDateString(docnoLowerCase);
        try{
            // [0] - month
            // [1] - day
            // [2] - year
            Files.createDirectories(Paths.get(pathToTheRawDataOutput + "/" + dateList[2]));
            Files.createDirectories(Paths.get(pathToTheRawDataOutput + "/" + dateList[2] + "/" + dateList[0]));
            Files.createDirectories(Paths.get(pathToTheRawDataOutput + "/" + dateList[2] + "/" + dateList[0] + "/" + dateList[1]));
        }catch(IOException e){
            System.out.println(e);
        }

        String fullPathDirectoryForTheRawDataFile = Paths.get(pathToTheRawDataOutput + "/"
                                                     + dateList[2] + "/" 
                                                     + dateList[0] + "/" 
                                                     + dateList[1])
                                                     .toString();  
        return fullPathDirectoryForTheRawDataFile;

    }

    public String[] TokenizedDateString(String docno) {
        String[] dateList = new String[3];
        docno = docno.replaceAll("\\<docno\\>\\</docno\\>", "");
        String[] trimmedList = docno.split(" ");
        String[] docnoList = trimmedList[1].split("-");
        int length = docnoList[0].length() - 4;
        int index = 2;
        //Skipping the first two characters "LA" to grab the month;
        for (int i = 0; i < 3; ++i) {
            String dateInteger = "";
            for (int j = index; j < length; ++j) {
                //dateList[i] += docnoList[0].charAt(j);  
                dateInteger += docnoList[0].charAt(j);
            }
            dateList[i] = dateInteger;
            length += 2;
            index += 2;
        }
        return dateList;
    } 
}