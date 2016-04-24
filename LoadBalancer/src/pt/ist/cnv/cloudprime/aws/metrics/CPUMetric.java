package pt.ist.cnv.cloudprime.aws.metrics;

import com.amazonaws.services.cloudwatch.model.Datapoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pedro Joaquim on 19-04-2016.
 */
public class CPUMetric {

    private List<Datapoint> datapoints = new ArrayList<Datapoint>();

    public CPUMetric(List<Datapoint> datapoints) {
        this.datapoints = datapoints;
    }

    public double getValue(){

        Datapoint maxDP = null;

        for (Datapoint dp : this.datapoints) {
            if(maxDP == null || dp.getTimestamp().getTime() > maxDP.getTimestamp().getTime()){
                maxDP = dp;
            }
        }

        return maxDP == null ? 0 : maxDP.getAverage();
    }
}
