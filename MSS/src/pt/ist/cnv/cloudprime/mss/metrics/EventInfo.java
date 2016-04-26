package pt.ist.cnv.cloudprime.mss.metrics;

import org.json.simple.JSONObject;

/**
 * Created by Pedro Joaquim on 26-04-2016.
 */
public class EventInfo extends AbstractMetric{

    private int requestID;
    private String metricName;
    private String metricValue;

    public EventInfo(int requestID, String metricName, String metricValue) {
        this.requestID = requestID;
        this.metricName = metricName;
        this.metricValue = metricValue;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", 2);
        jsonObj.put("metric_name", metricName);
        jsonObj.put("metric_value", metricValue);
        return jsonObj;
    }
}
