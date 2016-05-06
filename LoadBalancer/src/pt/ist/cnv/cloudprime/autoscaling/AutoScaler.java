package pt.ist.cnv.cloudprime.autoscaling;

import pt.ist.cnv.cloudprime.LoadBalancer;
import pt.ist.cnv.cloudprime.aws.AWSManager;
import pt.ist.cnv.cloudprime.aws.WorkerInstance;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;
import pt.ist.cnv.cloudprime.util.Config;

import java.util.ArrayList;
import java.util.List;

public class AutoScaler {

    private static AutoScaler instance;

    private List<WorkerInstance> workers;
    private Reading reading;
    private AWSManager awsManager;
    private LoadBalancer lb;
    private long lastRuleApplied;
    private int requestsMissed;
    private Thread asThread;

    public AutoScaler(LoadBalancer lb) {
        this.lb = lb;
        this.awsManager = AWSManager.getInstance();
        this.workers = new ArrayList<WorkerInstance>();
        this.reading = null;
        this.lastRuleApplied = System.currentTimeMillis();
        this.requestsMissed = 0;
        AutoScaler.instance = this;
    }

    public static synchronized AutoScaler getInstance(){
        return AutoScaler.instance;
    }

    public void start(){
        this.asThread = new Thread() {
            public void run() {
                try {
                    initialize();
                    while(!Thread.interrupted()){
                        Thread.sleep(Config.AUTO_SCALER_SLEEP_TIME);
                        monitor();
                    }
                } catch(InterruptedException e) {
                   //finish
                }
            }
        };

        this.asThread.start();
    }

    private void initialize() {
        for (int i = 0; i < Config.MIN_INSTANCES_NR; i++) {
            startNewInstance();
        }
    }

    private synchronized void monitor() {
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

        if ((getRequestsMissedAndReset() > Config.MAX_REQUESTS_MISSED) || (matchesIncreaseRule() && this.workers.size() < Config.MAX_INSTANCES_NR &&
                lastRuleApplied + Config.TIME_BETWEEN_RULES <= System.currentTimeMillis())) {

            startNewInstance();

        } else if (matchesDecreaseRule() && this.workers.size() > Config.MIN_INSTANCES_NR &&
                lastRuleApplied + Config.TIME_BETWEEN_RULES <= System.currentTimeMillis()) {

            WorkerInstance wi = selectInstanceForDecrease();
            if (this.lb.canDecrease(wi)) {
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

    public synchronized int getRequestsMissedAndReset() {
        int result =  requestsMissed;
        this.requestsMissed = 0;
        return result;
    }

    public synchronized void terminate() {
        this.asThread.interrupt();
    }

    public synchronized void startNewInstance() {
        WorkerInstance wi = this.awsManager.startNewWorker();
        addNewInstanceToLB(wi);
        this.workers.add(wi);
        this.lastRuleApplied = System.currentTimeMillis();
    }

    public synchronized void instanceFailed(WorkerInstance wi) {
        this.workers.remove(wi);
    }
}
