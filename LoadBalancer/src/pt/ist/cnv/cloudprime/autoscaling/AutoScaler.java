package pt.ist.cnv.cloudprime.autoscaling;

import pt.ist.cnv.cloudprime.LoadBalancer;
import pt.ist.cnv.cloudprime.aws.AWSManager;
import pt.ist.cnv.cloudprime.aws.WorkerInstance;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;
import pt.ist.cnv.cloudprime.util.Config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ASUS on 22/04/2016.
 */
public class AutoScaler {

    private List<WorkerInstance> workers;
    private Reading reading;
    private AWSManager awsManager;
    private LoadBalancer lb;
    private long lastRuleApplied;

    public AutoScaler(LoadBalancer lb) {
        this.lb = lb;
        this.awsManager = AWSManager.getInstance();
        this.workers = new ArrayList<WorkerInstance>();
        this.reading = null;
        this.lastRuleApplied = System.currentTimeMillis();
    }

    public void start(){
        new Thread() {
            public void run() {
                try {
                    initialize();
                    while(true){
                        Thread.sleep(Config.AUTO_SCALER_SLEEP_TIME);
                        monitor();
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initialize() {
        for (int i = 0; i < Config.MIN_INSTANCES_NR; i++) {
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
        WorkerInstance minCPUInstance = null;

        for (WorkerInstance wi:  this.workers) {
            CPUMetric cpu = wi.getCpuMetric();
            if(cpu == null) continue;

            aux = cpu.getValue();
            if(aux <= minCPU) { minCPU = aux; minCPUInstance = wi;}
            if(aux >= maxCPU) { maxCPU = aux; }
            averageSystemCPU += aux;
        }

        averageSystemCPU = averageSystemCPU / nrInstances;

        this.reading = new Reading(minCPU, maxCPU, averageSystemCPU, nrInstances, minCPUInstance);
    }


    private void applyRules() {

        if(matchesIncreaseRule() && this.workers.size() < Config.MAX_INSTANCES_NR &&
                lastRuleApplied + Config.TIME_BETWEEN_RULES <= System.currentTimeMillis()){

            WorkerInstance wi = this.awsManager.startNewWorker();
            addNewInstanceToLB(wi);
            this.workers.add(wi);
            this.lastRuleApplied = System.currentTimeMillis();

        }else if(matchesDecreaseRule() && this.workers.size() > Config.MIN_INSTANCES_NR &&
                lastRuleApplied + Config.TIME_BETWEEN_RULES <= System.currentTimeMillis()){

            WorkerInstance wi = selectInstanceForDecrease();
            if(this.lb.canDecrease(wi)){
                this.workers.remove(wi);
                this.awsManager.terminateWorker(wi);
            }
            this.lastRuleApplied = System.currentTimeMillis();

        }
    }

    private boolean matchesDecreaseRule() {
        return reading != null && reading.getAverageSystemCPU() <= Config.DECREASE_CPU_LEVEL;
    }

    private boolean matchesIncreaseRule() {
        return reading != null && reading.getAverageSystemCPU() >= Config.INCREASE_CPU_LEVEL;
    }

    private WorkerInstance selectInstanceForDecrease() {
        return reading.getMinCPUInstance();
    }

    private void addNewInstanceToLB(WorkerInstance wi){
        this.lb.addNewWorker(wi);
    }
}
