package pt.ist.cnv.cloudprime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;


public class ReadRequestHandler implements HttpHandler {


    private static final String URL_PARAMETER_NAME = "n";

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String numberToFactor = getRequestNumber(httpExchange);

        if(numberToFactor == null || "".equals(numberToFactor) || !numberToFactor.matches("^\\d+$")){
            writeResponseToClient(httpExchange, "Invalid Request", 400);
        }

    }

    private String getRequestNumber(HttpExchange httpExchange) {


        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> urlQueyParams = new HashMap<String, String>();

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

        return urlQueyParams.containsKey(URL_PARAMETER_NAME) ? urlQueyParams.get(URL_PARAMETER_NAME) : null;
    }

    private void writeResponseToClient(HttpExchange httpExchange, String result, int code) {
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
