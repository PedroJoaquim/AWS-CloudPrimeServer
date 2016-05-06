package pt.ist.cnv.cloudprime.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ist.cnv.cloudprime.LoadBalancer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Pedro Joaquim on 20-04-2016.
 */
public class ResponseRequesthandler extends BaseHandler {

    private static final String URL_PARAMETER_NAME = "r";
    private static final String URL_REQUEST_NAME = "rid";

    @Override
    protected void executeRequest(HttpExchange httpExchange) {

        String response = null;

        try {
            response = URLDecoder.decode(getURLParameter(URL_PARAMETER_NAME), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            response = null;
        }

        String requestID = getURLParameter(URL_REQUEST_NAME);

        if(response == null || "".equals(response) || requestID == null || "".equals(requestID)){
            writeResponseToClient(httpExchange, "Invalid Request", 400);
        }

        HttpExchange pendingRequest = LoadBalancer.getInstance().endPendingRequest(Integer.valueOf(requestID));

        if(pendingRequest == null) { return;}

        writeResponseToClient(pendingRequest, response, 200);
        writeResponseToClient(httpExchange, "OK", 200);
    }
}
