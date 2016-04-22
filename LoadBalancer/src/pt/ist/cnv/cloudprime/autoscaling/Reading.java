package pt.ist.cnv.cloudprime.autoscaling;

/**
 * Created by ASUS on 22/04/2016.
 */
public class Reading {

    private long readingTime;
    private double minCPU;
    private double maxCPU;
    private double averageSystemCPU;
    private int instanceNumber;

    public Reading(double minCPU, double maxCPU, double averageSystemCPU, int instanceNumber) {
        this.minCPU = minCPU;
        this.maxCPU = maxCPU;
        this.averageSystemCPU = averageSystemCPU;
        this.instanceNumber = instanceNumber;
        this.readingTime = System.currentTimeMillis();
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public long getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(long readingTime) {
        this.readingTime = readingTime;
    }

    public double getMinCPU() {
        return minCPU;
    }

    public void setMinCPU(double minCPU) {
        this.minCPU = minCPU;
    }

    public double getMaxCPU() {
        return maxCPU;
    }

    public void setMaxCPU(double maxCPU) {
        this.maxCPU = maxCPU;
    }

    public double getAverageSystemCPU() {
        return averageSystemCPU;
    }

    public void setAverageSystemCPU(double averageSystemCPU) {
        this.averageSystemCPU = averageSystemCPU;
    }
}
