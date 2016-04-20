package pt.ist.cnv.cloudprime.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by Pedro Joaquim on 20-04-2016.
 */
public abstract class BaseHandler implements HttpHandler {


    private HashMap<String, String> urlQueyParams = new HashMap<String, String>();




    protected abstract void executeRequest(HttpExchange httpExchange);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException{
        readURLParameters(httpExchange);
        executeRequest(httpExchange);
    }


    protected String getURLParameter(String urlParameterName) {
        return this.urlQueyParams.containsKey(urlParameterName) ? this.urlQueyParams.get(urlParameterName) : null;
    }


    protected void readURLParameters(HttpExchange httpExchange) {

        String query = httpExchange.getRequestURI().getQuery();

        if (query != null) {
            for (String param : query.split("&")) {

                try {
                    String pair[] = param.split("=");
                    if (pair.length > 1) {
                        urlQueyParams.put(pair[0], pair[1]);
                    } else {
                        urlQueyParams.put(pair[0], "");
                    }
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }

    protected void writeResponseToClient(HttpExchange httpExchange, String result, int code) {
        try {
            byte[] answerBytes = result.getBytes();
            httpExchange.sendResponseHeaders(code, answerBytes.length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(answerBytes);
            os.close();
        } catch (IOException e) {
            System.err.print("Connection closed on client, response not set");
        }
    }
}
