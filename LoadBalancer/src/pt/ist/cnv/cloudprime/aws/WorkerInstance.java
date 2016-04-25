package pt.ist.cnv.cloudprime.aws;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.ec2.model.Instance;
import pt.ist.cnv.cloudprime.autoscaling.AutoScaler;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;
import pt.ist.cnv.cloudprime.aws.metrics.WorkInfo;
import pt.ist.cnv.cloudprime.util.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerInstance {



    private Instance instance;
    private CPUMetric cpuMetric;
    private long startTime;
    private String lbPublicIP;
    private Map<Integer, WorkInfo> jobs = new ConcurrentHashMap<Integer, WorkInfo>();
    private boolean markedToFinish;
    private long markTimestamp;

    public WorkerInstance(Instance instance, String lbPublicIP) {
        this.instance = instance;
        this.cpuMetric = new CPUMetric(new ArrayList<Datapoint>());
        this.lbPublicIP = lbPublicIP;
        this.startTime = System.currentTimeMillis();
        this.markedToFinish = false;
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

        return "running".equals(getState())                                             &&
                startTime + Config.GRACE_PERIOD <= System.currentTimeMillis()     &&
                getPublicIP() != null                                                   &&
                !markedToFinish;
    }

    public void doRequest(WorkInfo wi) {
        jobs.put(wi.getRequestID(), wi);
        if(!sendGETRequest(wi.getNumberToFactor(), wi.getRequestID())){
            AutoScaler.getInstance().newRequestMissed();
        }
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
}
