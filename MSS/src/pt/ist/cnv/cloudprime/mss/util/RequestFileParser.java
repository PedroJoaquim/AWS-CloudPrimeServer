package pt.ist.cnv.cloudprime.mss.util;

import pt.ist.cnv.cloudprime.mss.metrics.RequestMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 26-04-2016.
 */
public class RequestFileParser {


    public static RequestMetrics parse(String filePath) throws IOException {

        String fileContents = readFileContents(filePath);
        return (RequestMetrics) new RequestMetrics().fromJSON(fileContents);
    }


    private static String readFileContents(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
