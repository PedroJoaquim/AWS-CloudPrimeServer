package pt.ist.cnv.cloudprime.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ist.cnv.cloudprime.LoadBalancer;

import java.io.IOException;

/**
 * Created by Pedro Joaquim on 20-04-2016.
 */
public class ResponseRequesthandler extends BaseHandler {

    private static final String URL_PARAMETER_NAME = "r";
    private static final String URL_REQUEST_NAME = "rid";

    @Override
    protected void executeRequest(HttpExchange httpExchange) {

        String response = getURLParameter(URL_PARAMETER_NAME);
        String requestID = getURLParameter(URL_REQUEST_NAME);

        if(response == null || "".equals(response) || requestID == null || "".equals(requestID)){
            writeResponseToClient(httpExchange, "Invalid Request", 400);
        }

        HttpExchange pendingRequest = LoadBalancer.getInstance().endPendingRequest(Integer.valueOf(requestID));

        writeResponseToClient(pendingRequest, response, 200);
        writeResponseToClient(httpExchange, "OK", 200);
    }
}
