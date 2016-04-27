package pt.ist.cnv.cloudprime.instrumentation;

public class ThreadMetrics {

    private static final long INSTRUCTIONS_THRESHHOLD = 1000000000;
    private static final long COMPARISSONS_THRESHHOLD = 100000000;
    private static final long FUNCTION_CALSS_THRESHHOLD = 250000000;

    private long iCount;
    private long compCount;
    private long allocCount;
    private long functionCallsCount;
    private long maxInvocationDepth;

    private long maxInvocationDepthAux;

    private long threadID;
    private String request;
    private String requestID;

    private int currentInstructionThreshold;
    private int currentComparisonsThreshold;
    private int currentFunctionsCallThreshold;

    public ThreadMetrics(long threadID) {
        this.iCount = 0;
        this.compCount = 0;
        this.allocCount = 0;
        this.functionCallsCount = 0;
        this.maxInvocationDepth = 0;
        this.maxInvocationDepthAux = 0;
        this.threadID = threadID;
        this.currentInstructionThreshold = 1;
        this.currentComparisonsThreshold = 1;
        this.currentFunctionsCallThreshold = 1;
    }

    public void resetMetrics(){
        this.currentInstructionThreshold = 1;
        this.iCount = 0;
        this.compCount = 0;
        this.allocCount = 0;
        this.functionCallsCount = 0;
        this.maxInvocationDepth = 0;
        this.maxInvocationDepthAux = 0;
    }
    
    public boolean incICount(long diff){
        iCount += diff;

        if(iCount >= INSTRUCTIONS_THRESHHOLD * currentInstructionThreshold){
            currentInstructionThreshold += 1;
            return true;
        }
        return false;
    }

    public boolean incFunctionCalls() {
        this.functionCallsCount++;
        this.maxInvocationDepthAux++;

        if(this.maxInvocationDepthAux > this.maxInvocationDepth){
            this.maxInvocationDepth = this.maxInvocationDepthAux;
        }

        if(this.functionCallsCount >= FUNCTION_CALSS_THRESHHOLD * currentFunctionsCallThreshold){
            currentFunctionsCallThreshold += 1;
            return true;
        }

        return false;
    }

    public void incReturnFunction() {
        this.maxInvocationDepthAux--;
    }

    public boolean incCompCount(long diff){
        compCount += diff;

        if(compCount >= COMPARISSONS_THRESHHOLD * currentComparisonsThreshold){
            currentComparisonsThreshold += 1;
            return true;
        }

        return false;
    }

    public void incAllocCount(long diff){
        allocCount += diff;
    }


    public long getiCount() {
        return iCount;
    }

    public long getThreadID() {
        return threadID;
    }

    public String getRequest() {
        return request;
    }

    public long getCompCount() { return compCount; }

    public long getAllocCount() { return allocCount; }

    public long getMaxInvocationDepth() { return maxInvocationDepth;}

    public long getFunctionCallsCount() { return functionCallsCount; }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getRequestID() {
        return requestID;
    }
}


