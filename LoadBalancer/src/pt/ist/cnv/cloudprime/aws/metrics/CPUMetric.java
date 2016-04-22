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
        double result = 0;

        for (Datapoint dp : this.datapoints) {
            result += dp.getAverage();
        }

        return result / this.datapoints.size();
    }
}
