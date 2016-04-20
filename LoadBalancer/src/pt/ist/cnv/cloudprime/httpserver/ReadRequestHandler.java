package pt.ist.cnv.cloudprime.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ist.cnv.cloudprime.LoadBalancer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;


public class ReadRequestHandler extends BaseHandler {

    private static final String URL_PARAMETER_NAME = "n";

    @Override
    protected void executeRequest(HttpExchange httpExchange) {

        String numberToFactor = getURLParameter(URL_PARAMETER_NAME);

        if(numberToFactor == null || "".equals(numberToFactor) || !numberToFactor.matches("^\\d+$")){
            writeResponseToClient(httpExchange, "Invalid Request", 400);
        }

        LoadBalancer.getInstance().handleNewRequest(httpExchange, numberToFactor);
    }

}
