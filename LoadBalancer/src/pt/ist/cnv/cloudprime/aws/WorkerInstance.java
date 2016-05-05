package pt.ist.cnv.cloudprime.aws;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.ec2.model.Instance;
import pt.ist.cnv.cloudprime.autoscaling.AutoScaler;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;
import pt.ist.cnv.cloudprime.aws.metrics.WorkInfo;
import pt.ist.cnv.cloudprime.util.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkerInstance {



    private Instance instance;
    private CPUMetric cpuMetric;
    private long startTime;
    private String lbPublicIP;
    private Map<Integer, WorkInfo> jobs = new ConcurrentHashMap<Integer, WorkInfo>();
    private Queue<WorkInfo> pendingJobs = new ConcurrentLinkedQueue<>();
    private boolean markedToFinish;
    private long markTimestamp;
    private Thread prThread;

    public WorkerInstance(Instance instance, String lbPublicIP) {
        this.instance = instance;
        this.cpuMetric = new CPUMetric(new ArrayList<Datapoint>());
        this.lbPublicIP = lbPublicIP;
        this.startTime = System.currentTimeMillis();
        this.markedToFinish = false;


        this.prThread = new Thread() {
            public void run() {
                try {
                    while(!Thread.interrupted()){
                        Thread.sleep(Config.WORKER_PENDING_REQ_SLEEP_TIME);
                        executePendingRequests();
                    }
                } catch(InterruptedException e) {
                    //finish
                }
            }
        };

        this.prThread.start();
    }

    /**
     * Method executed by the prTrhead to check if there are any pending requests
     */
    private void executePendingRequests() {


        WorkInfo wi;

        if(pendingJobs.size() == 0 || !canRunRequest()){
            return;
        }

        Iterator<WorkInfo> iter = pendingJobs.iterator();
        while (iter.hasNext()) {
            wi = iter.next();

            this.jobs.put(wi.getRequestID(),wi);
            sendGETRequest(wi.getNumberToFactor(), wi.getRequestID());
            this.pendingJobs.remove(wi);
        }
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

    public boolean isActive(){

        if(markedToFinish){
            if(markTimestamp + (Config.AUTO_SCALER_SLEEP_TIME * 3) > System.currentTimeMillis()){
                this.markedToFinish = false;
            }
        }

        return ("running".equals(getState()) || "pending".equals(getState()))           &&
                !markedToFinish;
    }

    public void doRequest(WorkInfo wi) {

        if(canRunRequest()){
            jobs.put(wi.getRequestID(), wi);
            sendGETRequest(wi.getNumberToFactor(), wi.getRequestID());
        }
        else{
            pendingJobs.add(wi);
        }
    }

    /*
     * check if worker is ready to handle a request
     */
    private boolean canRunRequest() {
        return "running".equals(getState()) &&
                getPublicIP() != null       &&
                startTime + Config.GRACE_PERIOD <= System.currentTimeMillis();
    }

    public WorkInfo getWorkInfo(int requestID){
        return this.jobs.containsKey(requestID) ? this.jobs.get(requestID) : null;
    }

    public void endJob(int requestID){
        this.jobs.remove(requestID);
    }

    public List<WorkInfo> getCurrentJobs() { return new ArrayList<>(this.jobs.values()); }

    private boolean sendGETRequest(String numberToFactor, int requestID){

        HttpURLConnection connection = null;
        String targetURL = "http://" + getPublicIP() + ":" + Config.WORKER_PORT + "/f.html?n=" + numberToFactor + "&rid=" + requestID +"&ip=" + lbPublicIP;
        String line;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            rd.close();
            return true;

        } catch (Exception e) {
            return false;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public CPUMetric getCpuMetric() {
        return cpuMetric;
    }

    public void setCpuMetric(CPUMetric cpuMetric) {
        this.cpuMetric = cpuMetric;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public void markInstanceToFinish() {
        this.markedToFinish = true;
        this.markTimestamp = System.currentTimeMillis();
    }

    public synchronized void updateMetricValue(int requestID, String metricName, BigInteger metricValue) {
        WorkInfo wi = this.jobs.get(requestID);

        if(wi != null){
            wi.updateMetric(metricName, metricValue);
        }
    }

    public void terminate() {
        prThread.interrupt();
    }
}
