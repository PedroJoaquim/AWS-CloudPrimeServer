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
    private RequestMetrics requestMetrics;

    public WorkInfo(HttpExchange httpExchange, String numberToFactor, int requestID) {
        this.httpExchange = httpExchange;
        this.numberToFactor = numberToFactor;
        this.requestID = requestID;
        this.requestMetrics = new RequestMetrics(numberToFactor);
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

    public RequestMetrics getRequestMetrics() {
        return requestMetrics;
    }
}
