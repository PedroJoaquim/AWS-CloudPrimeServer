package pt.ist.cnv.cloudprime.mss.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashMap;


public abstract class BaseHandler implements HttpHandler {


    private HashMap<String, String> urlQueyParams = new HashMap<String, String>();


    protected abstract void executeRequest(HttpExchange httpExchange) throws IOException;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        try {
            readURLParameters(httpExchange);
            executeRequest(httpExchange);
        } catch (Exception e){
            writeResponseToClient(httpExchange, e.getMessage(), 400);
        }
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

    protected String getRequestBody(HttpExchange httpExchange) throws IOException {
        String body = "";
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(),"utf-8");
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();

        while(line != null){
            body += line;
            line = br.readLine();
        }

        return body;
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

