package pt.ist.cnv.cloudprime.mss.metrics;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by Pedro Joaquim on 27-04-2016.
 */
public class MetricsInfoContainer {

    private final List<AbstractMetric> result;

    public MetricsInfoContainer(List<AbstractMetric> result) {
        this.result = result;
    }

    public JSONObject toJSON(){

        JSONObject jsonObj = new JSONObject();

        for (int i = 0; i < result.size(); i++) {
            jsonObj.put("obj" + i, result.get(i).toJSON());
        }

        return jsonObj;
    }
}
