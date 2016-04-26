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
    private BigInteger totalAllocations;
    private BigInteger totalFunctionCalls;
    private BigInteger maxFunctionInvocationDepth;

    private static final BigInteger ONE_HUNDRED = new BigInteger("100");

    public RequestMetrics(String requestNumber, BigInteger totalInstructions, BigInteger totalComparisons, BigInteger totalAllocations, BigInteger totalFunctionCalls, BigInteger maxFunctionInvocationDepth) {
        this.requestNumber = requestNumber;
        this.totalInstructions = totalInstructions;
        this.totalComparisons = totalComparisons;
        this.totalAllocations = totalAllocations;
        this.totalFunctionCalls = totalFunctionCalls;
        this.maxFunctionInvocationDepth = maxFunctionInvocationDepth;
    }

    public RequestMetrics(String requestNumber) {
        this.requestNumber = requestNumber;
        this.totalInstructions = new BigInteger("0");
        this.totalComparisons = new BigInteger("0");
        this.totalAllocations = new BigInteger("0");
        this.totalFunctionCalls = new BigInteger("0");
        this.maxFunctionInvocationDepth = new BigInteger("0");
    }

    /**
     * Method that given a request gives the request complexity metric based
     * on our heuristic
     * @return request complexity
     */

    public int calcRequestComplexity(){

        int result = 0;

        int instructionsFactor = Integer.valueOf(this.totalInstructions.divide(Config.INSTRUCTIONS_THRESHOLD).toString());

        int comparisonsFactor = Integer.valueOf(this.totalComparisons.multiply(ONE_HUNDRED).divide(this.totalInstructions).toString());
        int functionCallsFactor = Integer.valueOf(this.totalFunctionCalls.multiply(ONE_HUNDRED).divide(this.totalFunctionCalls).toString());

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

    public BigInteger getTotalAllocations() {
        return totalAllocations;
    }

    public void setTotalAllocations(BigInteger totalAllocations) {
        this.totalAllocations = totalAllocations;
    }

    public BigInteger getTotalFunctionCalls() {
        return totalFunctionCalls;
    }

    public void setTotalFunctionCalls(BigInteger totalFunctionCalls) {
        this.totalFunctionCalls = totalFunctionCalls;
    }

    public BigInteger getMaxFunctionInvocationDepth() {
        return maxFunctionInvocationDepth;
    }

    public void setMaxFunctionInvocationDepth(BigInteger maxFunctionInvocationDepth) {
        this.maxFunctionInvocationDepth = maxFunctionInvocationDepth;
    }
}
