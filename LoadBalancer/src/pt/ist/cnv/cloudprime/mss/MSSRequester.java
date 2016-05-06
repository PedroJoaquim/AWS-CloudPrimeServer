package pt.ist.cnv.cloudprime.mss;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.ist.cnv.cloudprime.LoadBalancer;
import pt.ist.cnv.cloudprime.aws.metrics.RequestMetrics;
import pt.ist.cnv.cloudprime.util.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;


public class MSSRequester {

    private String mssIP;
    private LoadBalancer lb;
    private long updateID;
    private Thread mssThread;

    public MSSRequester(String mssIP, LoadBalancer lb) {
        this.mssIP = mssIP;
        this.lb = lb;
        this.updateID = 0;
    }

    public void start(){
        this.mssThread = new Thread() {
            public void run() {
                try {
                    while(!Thread.interrupted()){
                        Thread.sleep(Config.MSS_REQUEST_INTERVAL);
                        requestAndUpdate();
                    }
                } catch (InterruptedException e) {
                    //finish
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        this.mssThread.start();
    }

    private void requestAndUpdate() throws ParseException {

        String mssUpdates = doGETRequest();
        JSONObject jsonObj = (JSONObject) new JSONParser().parse(mssUpdates);

        long newUpdateID = (long) jsonObj.get("new_update_id");

        JSONObject slideContent = (JSONObject) jsonObj.get("updates");

        long from = (updateID == 0 ? 1 : updateID);
        long to = newUpdateID;

        for (long i = from; i < to; i++) {
            JSONObject slide = (JSONObject) slideContent.get("obj" + i);
            long type = (long) slide.get("type");

            if(type == 1){
                hadleRequestMetrics(slide);
            }
            else if(type == 2){
                handleEventInfo(slide);
            }
        }

        this.updateID = newUpdateID;

    }

    private void handleEventInfo(JSONObject slide) {

        int requestID = Integer.valueOf((String) slide.get("request_id"));
        String metricName = (String) slide.get("metric_name");
        BigInteger metricValue = new BigInteger((String) slide.get("metric_value"));

        this.lb.updateMetric(requestID, metricName, metricValue);
    }

    private void hadleRequestMetrics(JSONObject slide) {

        String requestNumber = (String) slide.get("request_number");
        BigInteger totalInstructions = new BigInteger((String) slide.get("total_instructions"));
        BigInteger totalComparisons = new BigInteger((String) slide.get("total_comparisons"));
        BigInteger totalFunctionCalls = new BigInteger((String) slide.get("total_function_calls"));

        this.lb.addNewRequestMetrics(new RequestMetrics(requestNumber, totalInstructions, totalComparisons, totalFunctionCalls));
    }

    private String doGETRequest() {
        try {
            URL url = new URL("http://" + mssIP + ":" + Config.MSS_PORT + "/info?updateID=" + this.updateID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }

            reader.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }


    public synchronized void terminate() {
        this.mssThread.interrupt();
    }
}
