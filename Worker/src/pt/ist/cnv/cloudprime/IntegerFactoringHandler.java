package pt.ist.cnv.cloudprime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ist.cnv.cloudprime.instrumentation.CloudPrimeIT;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


class IntegerFactoringHandler implements HttpHandler {


    private static final String URL_PARAMETER_NAME = "n";
    private static final String URL_REQUEST_ID_NAME = "rid";
    private static final String URL_IP_NAME = "ip";

    private HashMap<String, String> urlQueyParams = new HashMap<String, String>();

    public void handle(HttpExchange httpExchange) throws IOException {

        readURLParameters(httpExchange);

        String numberToFactor = getRequestNumber(httpExchange);
        String requestID = getRequestID(httpExchange);
        String lbIP = getResponseIP(httpExchange);
        
        if(!isValid(numberToFactor) || lbIP == null || requestID == null){
            writeResponseToClient(httpExchange, "usage: http://.../f.html?n=(number to factor)", 400);
            return;
        }

        System.out.println("[FACTORING][THREAD:" + Thread.currentThread().getId() + "] " + numberToFactor + "...");
        CloudPrimeIT.addThreadRequest(Thread.currentThread().getId(), numberToFactor);
        String result = IntegerFactoring.main(numberToFactor);
        System.out.println("[RESULT][THREAD:" + Thread.currentThread().getId() + "] " + numberToFactor + " = " + result);
        
        sendResponseToLB(result, requestID, lbIP);
        
    }

    private void sendResponseToLB(String result, String requestID, String ip) {
        HttpURLConnection connection = null;
        String resultURL;

        try {
            resultURL = URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            resultURL = "";
        }

        String targetURL = "http://" + ip + ":80/r.html?r=" + resultURL + "&rid=" + requestID;
        String line;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = rd.readLine()) != null) {
                //ignore
            }

            rd.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isValid(String numberToFactor) {
        return numberToFactor != null && numberToFactor.matches("^[0-9]+$");
    }

    private void readURLParameters(HttpExchange httpExchange){

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

    private String getRequestNumber(HttpExchange httpExchange) {
        return urlQueyParams.containsKey(URL_PARAMETER_NAME) ? urlQueyParams.get(URL_PARAMETER_NAME) : null;
    }

    private String getRequestID(HttpExchange httpExchange) {
        return urlQueyParams.containsKey(URL_PARAMETER_NAME) ? urlQueyParams.get(URL_REQUEST_ID_NAME) : null;
    }

    private String getResponseIP(HttpExchange httpExchange) {
        return urlQueyParams.containsKey(URL_PARAMETER_NAME) ? urlQueyParams.get(URL_IP_NAME) : null;
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


