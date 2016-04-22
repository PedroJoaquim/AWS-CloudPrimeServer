package pt.ist.cnv.cloudprime.aws;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.State;
import com.sun.net.httpserver.HttpExchange;
import pt.ist.cnv.cloudprime.LoadBalancer;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;
import pt.ist.cnv.cloudprime.aws.metrics.WorkInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerInstance {

    private static final String PORT = "8000";

    private Instance instance;
    private List<CPUMetric> cpuMetrics;
    private long startTime;
    private String lbPublicIP;
    private Map<Integer, WorkInfo> jobs = new ConcurrentHashMap<Integer, WorkInfo>();

    public WorkerInstance(Instance instance, String lbPublicIP) {
        this.instance = instance;
        this.cpuMetrics = new ArrayList<CPUMetric>();
        this.lbPublicIP = lbPublicIP;
        this.startTime = System.currentTimeMillis();
    }

    public String getInstanceID(){
        return this.instance.getInstanceId();
    }

    public String getPublicIP(){
       return this.instance.getPublicIpAddress();
    }

    public String getState(){
        return this.instance.getState().getName();
    }

    public void addCPUMetrics(CPUMetric metric) {
        this.cpuMetrics.add(metric);
    }

    public boolean isActive(){
        return "running".equals(getState())                                             &&
                startTime + LoadBalancer.GRACE_PERIOD <= System.currentTimeMillis()     &&
                getPublicIP() != null;
    }

    public void doRequest(WorkInfo wi) {
        jobs.put(wi.getRequestID(), wi);
        sendGETRequest(wi.getNumberToFactor(), wi.getRequestID());
    }

    public WorkInfo getWorkInfo(int requestID){
        return this.jobs.containsKey(requestID) ? this.jobs.get(requestID) : null;
    }

    public void endJob(int requestID){
        this.jobs.remove(requestID);
    }

    private void sendGETRequest(String numberToFactor, int requestID){

        HttpURLConnection connection = null;
        String targetURL = "http://" + getPublicIP() + ":" + PORT + "/f.html?n=" + numberToFactor + "&rid=" + requestID +"&ip=" + lbPublicIP;
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

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public CPUMetric getLastCPUMetric() { return this.cpuMetrics.size() > 0 ? this.cpuMetrics.get(this.cpuMetrics.size() -1) : null;}
}
