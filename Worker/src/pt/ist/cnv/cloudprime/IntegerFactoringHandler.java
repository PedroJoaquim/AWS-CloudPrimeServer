package pt.ist.cnv.cloudprime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ist.cnv.cloudprime.instrumentation.CloudPrimeIT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;


class IntegerFactoringHandler implements HttpHandler {


    private static final String URL_PARAMETER_NAME = "n";

    public void handle(HttpExchange httpExchange) throws IOException {

        String numberToFactor = getRequestNumber(httpExchange);

        if(!isValid(numberToFactor)){
            writeResponseToClient(httpExchange, "usage: http://.../iFactor?number=(number to factor)", 400);
            return;
        }

        System.out.println("[FACTORING][THREAD:" + Thread.currentThread().getId() + "] " + numberToFactor + "...");
        CloudPrimeIT.addThreadRequest(Thread.currentThread().getId(), numberToFactor);
        String result = IntegerFactoring.main(numberToFactor);
        System.out.println("[RESULT][THREAD:" + Thread.currentThread().getId() + "] " + numberToFactor + " = " + result);

        writeResponseToClient(httpExchange, result, 200);
    }

    private boolean isValid(String numberToFactor) {
        return numberToFactor != null && numberToFactor.matches("^[0-9]+$");
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


