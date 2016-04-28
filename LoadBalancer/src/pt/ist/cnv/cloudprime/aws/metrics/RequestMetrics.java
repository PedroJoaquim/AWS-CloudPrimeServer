package pt.ist.cnv.cloudprime.aws.metrics;

import pt.ist.cnv.cloudprime.util.Config;

import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 24-04-2016.
 */
public class RequestMetrics {

    private String requestNumber;
    private BigInteger totalInstructions;
    private BigInteger totalComparisons;
    private BigInteger totalFunctionCalls;

    private static final BigInteger ONE_HUNDRED = new BigInteger("100");

    public RequestMetrics(String requestNumber, BigInteger totalInstructions, BigInteger totalComparisons, BigInteger totalFunctionCalls) {
        this.requestNumber = requestNumber;
        this.totalInstructions = totalInstructions;
        this.totalComparisons = totalComparisons;
        this.totalFunctionCalls = totalFunctionCalls;
    }

    public RequestMetrics(String requestNumber) {
        this.requestNumber = requestNumber;
        this.totalInstructions = new BigInteger("0");
        this.totalComparisons = new BigInteger("0");
        this.totalFunctionCalls = new BigInteger("0");
    }

    /**
     * Method that given a request gives the request complexity metric based
     * on our heuristic
     * @return request complexity
     */

    public int calcRequestComplexity(){

        if(totalInstructions.compareTo(BigInteger.ZERO) == 0){
            return 0;
        }

        int instructionsFactor = Double.valueOf(this.totalInstructions.divide(Config.INSTRUCTIONS_THRESHOLD).toString()).intValue();
        int comparisonsFactor = Double.valueOf(this.totalComparisons.multiply(ONE_HUNDRED).divide(totalInstructions).toString()).intValue();
        int functionCallsFactor = Double.valueOf(this.totalFunctionCalls.multiply(ONE_HUNDRED).divide(totalInstructions).toString()).intValue();
        return instructionsFactor * 100 + (comparisonsFactor * 10) + (functionCallsFactor * 10);
    }

    /*
     * Getters and Setters
     */

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


    public void addTotalInstructions(BigInteger metricValue) {
        this.totalInstructions = this.totalInstructions.add(metricValue);
    }

    public void addComparisons(BigInteger metricValue) {
        this.totalComparisons = this.totalComparisons.add(metricValue);
    }

    public void addFunctionCalls(BigInteger metricValue) {
        this.totalFunctionCalls = this.totalFunctionCalls.add(metricValue);
    }
}
