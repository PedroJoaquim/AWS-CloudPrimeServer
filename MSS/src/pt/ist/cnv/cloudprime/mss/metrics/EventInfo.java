package pt.ist.cnv.cloudprime.mss.metrics;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.ist.cnv.cloudprime.mss.util.Config;

import java.math.BigInteger;

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

    public EventInfo() {

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
        jsonObj.put("type", Config.UPDATE_ID);
        jsonObj.put("metric_name", metricName);
        jsonObj.put("metric_value", metricValue);
        jsonObj.put("request_id", requestID);
        return jsonObj;
    }

    @Override
    public AbstractMetric fromJSON(String json) {

        try {

            JSONObject jsonObj = (JSONObject) new JSONParser().parse(json);
            this.metricName = (String) jsonObj.get("metric_name");
            this.metricValue = (String) jsonObj.get("metric_value");
            this.requestID = Integer.valueOf((String) jsonObj.get("request_id"));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }
}
