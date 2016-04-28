package pt.ist.cnv.cloudprime.mss.metrics;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by Pedro Joaquim on 27-04-2016.
 */
public class MetricsInfoContainer {

    private final List<AbstractMetric> result;
    private final int from;
    private final int to;

    public MetricsInfoContainer(List<AbstractMetric> result, int from, int to) {
        this.result = result;
        this.from = from;
        this.to = to;
    }

    public JSONObject toJSON(){

        JSONObject jsonObj = new JSONObject();
        int index = 0;

        for (int i = from; i < to; i++) {
            jsonObj.put("obj" + i, result.get(index++).toJSON());
        }

        return jsonObj;
    }
}
