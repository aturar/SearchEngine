import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class DocumentTokenizer {
    
    public int docId;
    public String stringToTokenize;

    public List<String> Tokenize(DocumentTokenizer doc) {
        int start = 0, i = 0;
        List<String> tokens = new ArrayList<String>();
        String token = "";
        String text = doc.stringToTokenize.toLowerCase();
        for (i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (!(Character.isDigit(c) || Character.isLetter(c))) {
                if (start != i) {
                    token = text.substring(start, i);
                    tokens.add(token);
                }
                start = i + 1;
            }
        } 
        if (start != i) {
            tokens.add(text.substring(start, i));
        }
        return tokens;
    }
}
