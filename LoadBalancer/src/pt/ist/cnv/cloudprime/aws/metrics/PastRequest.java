package pt.ist.cnv.cloudprime.aws.metrics;

import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 24-04-2016.
 */
public class PastRequest {

    private String requestNumber;
    private BigInteger totalInstructions;
    private BigInteger totalComparisons;
    private BigInteger totalAllocations;
    private BigInteger totalFunctionCalls;
    private BigInteger maxFunctionInvocationDeptj;

    public PastRequest(String requestNumber, BigInteger totalInstructions, BigInteger totalComparisons, BigInteger totalAllocations, BigInteger totalFunctionCalls, BigInteger maxFunctionInvocationDeptj) {
        this.requestNumber = requestNumber;
        this.totalInstructions = totalInstructions;
        this.totalComparisons = totalComparisons;
        this.totalAllocations = totalAllocations;
        this.totalFunctionCalls = totalFunctionCalls;
        this.maxFunctionInvocationDeptj = maxFunctionInvocationDeptj;
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

    public BigInteger getMaxFunctionInvocationDeptj() {
        return maxFunctionInvocationDeptj;
    }

    public void setMaxFunctionInvocationDeptj(BigInteger maxFunctionInvocationDeptj) {
        this.maxFunctionInvocationDeptj = maxFunctionInvocationDeptj;
    }
}
