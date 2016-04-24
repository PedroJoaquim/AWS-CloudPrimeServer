package pt.ist.cnv.cloudprime.util;

import pt.ist.cnv.cloudprime.aws.metrics.PastRequest;

import java.io.*;
import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 24-04-2016.
 */
public class RequestFileParser {


    private static final String TOTAL_INSTRUCTIONS = "TOTAL INSTRUCTIONS";
    private static final String TOTAL_COMPARISSONS = "TOTAL COMPARISONS";
    private static final String TOTAL_ALLOCATIONS = "TOTAL ALLOCATIONS";
    private static final String TOTAL_FUNCTION_CALLS = "TOTAL FUNCTION CALLS";
    private static final String MAX_INVOCATION_DEPTH = "MAX FUNCTION INVOCATION DEPTH";

    public static PastRequest parse(String filePath) throws IOException {

        String requestNumber = readRequestNumber(filePath);
        String fileContents = readFileContents(filePath);

        String totalInstructions = readFileSegment(fileContents, TOTAL_INSTRUCTIONS);
        String totalComparissons = readFileSegment(fileContents, TOTAL_COMPARISSONS);
        String totalAllocations = readFileSegment(fileContents, TOTAL_ALLOCATIONS);
        String totalFunctionCalls = readFileSegment(fileContents, TOTAL_FUNCTION_CALLS);
        String maxFunctionCallDepth = readFileSegment(fileContents, MAX_INVOCATION_DEPTH);

        return new PastRequest(requestNumber, new BigInteger(totalInstructions),
                new BigInteger(totalComparissons), new BigInteger(totalAllocations), new BigInteger(totalFunctionCalls), new BigInteger(maxFunctionCallDepth));
    }


    public static String readRequestNumber(String filePath){
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.lastIndexOf('.'));
    }

    public static String readFileContents(String filePath) throws IOException {
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

    public static String readFileSegment (String fileContents, String segment){
        String[] fileSegments = fileContents.split("\\|");

        for (int i = 0; i < fileSegments.length; i++ ) {
            if(fileSegments[i].contains(segment)){
                return fileSegments[i+1].substring(fileSegments[i+1].lastIndexOf("=") + 1, fileSegments[i+1].lastIndexOf("\n"));
            }
        }

        return "";
    }
    
    public static String readMaxFunctionInvocationDeptj(String fileContents){
        return "todo";
    }

}
