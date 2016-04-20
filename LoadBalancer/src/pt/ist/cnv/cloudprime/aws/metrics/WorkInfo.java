package pt.ist.cnv.cloudprime.aws.metrics;

import com.sun.net.httpserver.HttpExchange;

/**
 * Created by Pedro Joaquim on 20-04-2016.
 */
public class WorkInfo {


    private final HttpExchange httpExchange;
    private final String numberToFactor;
    private final int requestID;

    public WorkInfo(HttpExchange httpExchange, String numberToFactor, int requestID) {
        this.httpExchange = httpExchange;
        this.numberToFactor = numberToFactor;
        this.requestID = requestID;
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
}
