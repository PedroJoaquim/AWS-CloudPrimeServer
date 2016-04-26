package pt.ist.cnv.cloudprime.mss.metrics;

import org.json.simple.JSONObject;

import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 26-04-2016.
 */
public class RequestMetrics extends AbstractMetric{

    private String requestNumber;
    private BigInteger totalInstructions;
    private BigInteger totalComparisons;
    private BigInteger totalFunctionCalls;

    public RequestMetrics(String requestNumber, BigInteger totalInstructions, BigInteger totalComparisons, BigInteger totalFunctionCalls) {
        this.requestNumber = requestNumber;
        this.totalInstructions = totalInstructions;
        this.totalComparisons = totalComparisons;
        this.totalFunctionCalls = totalFunctionCalls;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public BigInteger getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(BigInteger totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public BigInteger getTotalComparisons() {
        return totalComparisons;
    }

    public void setTotalComparisons(BigInteger totalComparisons) {
        this.totalComparisons = totalComparisons;
    }



    public BigInteger getTotalFunctionCalls() {
        return totalFunctionCalls;
    }

    public void setTotalFunctionCalls(BigInteger totalFunctionCalls) {
        this.totalFunctionCalls = totalFunctionCalls;
    }



    @Override
    public JSONObject toJSON() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", 1);
        jsonObj.put("request_number", requestNumber.toString());
        jsonObj.put("total_instructions", totalInstructions.toString());
        jsonObj.put("total_comparisons", totalComparisons.toString());
        jsonObj.put("total_function_calls", totalFunctionCalls.toString());
        return jsonObj;

    }
}
