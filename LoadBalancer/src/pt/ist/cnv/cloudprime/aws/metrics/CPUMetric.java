package pt.ist.cnv.cloudprime.aws.metrics;

import com.amazonaws.services.cloudwatch.model.Datapoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pedro Joaquim on 19-04-2016.
 */
public class CPUMetric {

    private List<Datapoint> datapoints = new ArrayList<Datapoint>();

    public void addDatapoint(Datapoint dp) {
        datapoints.add(dp);
    }
}
