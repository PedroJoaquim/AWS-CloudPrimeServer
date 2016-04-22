package pt.ist.cnv.cloudprime.autoscaling;

import pt.ist.cnv.cloudprime.LoadBalancer;
import pt.ist.cnv.cloudprime.aws.AWSManager;
import pt.ist.cnv.cloudprime.aws.WorkerInstance;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ASUS on 22/04/2016.
 */
public class AutoScaler {

    private static final int MIN_INSTANCES_NR = 1;
    private static final int MAX_INSTANCES_NR = 4;
    private static final long AUTO_SCALER_SLEEP_TIME = 30000;

    private List<WorkerInstance> workers;
    private List<Reading> readings;
    private AWSManager awsManager;
    private LoadBalancer lb;

    public AutoScaler(LoadBalancer lb) {
        this.lb = lb;
        this.awsManager = AWSManager.getInstance();
        this.workers = new ArrayList<WorkerInstance>();
        this.readings = new ArrayList<Reading>();
    }

    public void start(){
        new Thread() {
            public void run() {
                try {
                    initialize();
                    while(true){
                        Thread.sleep(AUTO_SCALER_SLEEP_TIME);
                        monitor();
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initialize() {
        for (int i = 0; i < MIN_INSTANCES_NR; i++) {
            WorkerInstance wi = this.awsManager.startNewWorker();
            addNewInstanceToLB(wi);
            this.workers.add(wi);
        }
    }

    private void monitor() {
        updateInstanceCloudWatchMetrics();
        calcNewSystemReading();
        applyRules();
    }

    private void updateInstanceCloudWatchMetrics() {
        for (WorkerInstance wi : this.workers) {
            this.awsManager.updateCloudWatchMetrics(wi);
        }
    }

    private void calcNewSystemReading() {

        double averageSystemCPU = 0, maxCPU = 0, minCPU = 100, aux;
        int nrInstances = this.workers.size();

        for (WorkerInstance wi:  this.workers) {
            CPUMetric cpu = wi.getLastCPUMetric();
            if(cpu == null) continue;

            aux = cpu.getValue();
            if(aux < minCPU) { minCPU = aux; }
            if(aux > maxCPU) { maxCPU = aux; }
            averageSystemCPU += aux;
        }

        averageSystemCPU = averageSystemCPU / nrInstances;

        this.readings.add(new Reading(minCPU, maxCPU, averageSystemCPU, nrInstances));

        if(readings.size() > 30){
            Iterator<Reading> iterator = readings.iterator();
            for (int i = 0; i < 15; i++) {
                iterator.next();
                iterator.remove();
            }
        }
    }


    private void applyRules() {

        if(matchesIncreaseRule()){

            WorkerInstance wi = this.awsManager.startNewWorker();
            addNewInstanceToLB(wi);

        }else if(matchesDecreaseRule()){

            WorkerInstance wi = selectInstanceForDecrease();
            if(this.lb.canDecrease(wi)){
                this.workers.remove(wi);
                this.awsManager.terminateWorker(wi);
            }

        }
    }

    private WorkerInstance selectInstanceForDecrease() {
        return null; //todo
    }

    private boolean matchesDecreaseRule() {
        return false; //todo
    }

    private boolean matchesIncreaseRule() {
        return false; //todo
    }

    private void addNewInstanceToLB(WorkerInstance wi){
        this.lb.addNewWorker(wi);
    }
}
