package pt.ist.cnv.cloudprime.aws.metrics;

import com.sun.net.httpserver.HttpExchange;

import java.math.BigInteger;

/**
 * Created by Pedro Joaquim on 20-04-2016.
 */
public class WorkInfo {


    private HttpExchange httpExchange;
    private String numberToFactor;
    private int requestID;
    private BigInteger alreadyExecutedInstructions;

    public WorkInfo(HttpExchange httpExchange, String numberToFactor, int requestID) {
        this.httpExchange = httpExchange;
        this.numberToFactor = numberToFactor;
        this.requestID = requestID;
        this.alreadyExecutedInstructions = new BigInteger("0");
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    public String getNumberToFactor() {
        return numberToFactor;
    }

    public int getRequestID() {
        return requestID;
    }

    public BigInteger getAlreadyExecutedInstructions() {
        return alreadyExecutedInstructions;
    }

    public void setAlreadyExecutedInstructions(BigInteger alreadyExecutedInstructions) {
        this.alreadyExecutedInstructions = alreadyExecutedInstructions;
    }
}
