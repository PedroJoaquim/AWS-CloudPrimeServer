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


    private static final String TOTAL_INSTRUCTIONS = "TOTAL INSTRUCTIONS";
    private static final String TOTAL_COMPARISONS = "TOTAL COMPARISONS";
    private static final String TOTAL_FUNCTION_CALLS = "TOTAL FUNCTION CALLS";

    public static RequestMetrics parse(String filePath) throws IOException {

        String requestNumber = readRequestNumber(filePath);
        String fileContents = readFileContents(filePath);

        String totalInstructions = readFileSegment(fileContents, TOTAL_INSTRUCTIONS);
        String totalComparissons = readFileSegment(fileContents, TOTAL_COMPARISONS);
        String totalFunctionCalls = readFileSegment(fileContents, TOTAL_FUNCTION_CALLS);

        return new RequestMetrics(requestNumber, new BigInteger(totalInstructions),
                new BigInteger(totalComparissons),  new BigInteger(totalFunctionCalls));
    }


    private static String readRequestNumber(String filePath){
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.lastIndexOf('.'));
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

    private static String readFileSegment (String fileContents, String segment){
        String[] fileSegments = fileContents.split("\\|");

        for (int i = 0; i < fileSegments.length; i++ ) {
            if(fileSegments[i].contains(segment)){
                return fileSegments[i+1].substring(fileSegments[i+1].lastIndexOf("=") + 1, fileSegments[i+1].lastIndexOf("\n"));
            }
        }

        return "";
    }
}
