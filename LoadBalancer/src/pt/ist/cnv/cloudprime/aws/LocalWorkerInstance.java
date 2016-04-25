package pt.ist.cnv.cloudprime.aws;

import com.amazonaws.services.ec2.model.Instance;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;
import pt.ist.cnv.cloudprime.aws.metrics.WorkInfo;
import pt.ist.cnv.cloudprime.util.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pedro Joaquim on 25-04-2016.
 */
public class LocalWorkerInstance extends WorkerInstance {

    public LocalWorkerInstance(String lbPublicIP) {
        super(null, lbPublicIP);
    }


    public String getPublicIP(){
        return "85.246.104.195";
    }

    public String getState(){
        return "running";
    }

    public boolean isActive(){
        return true;
    }


    public CPUMetric getCpuMetric() {
        return new CPUMetric(new ArrayList<>());
    }

    public void setCpuMetric(CPUMetric cpuMetric) {

    }

    public void setInstance(Instance instance) {

    }

    public void markInstanceToFinish() {

    }
}
