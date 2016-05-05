package pt.ist.cnv.cloudprime.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import pt.ist.cnv.cloudprime.aws.metrics.CPUMetric;

import java.util.*;

public class AWSManager {

    private static final String AVAILABILITY_ZONE = "ec2.eu-central-1.amazonaws.com";
    private static final String AMI_ID = "ami-71bf5d1e";
    private static final String INSTANCE_TYPE = "t2.micro";
    private static final String KEY_NAME = "cnvir-cloudprime";
    private static final String SECURITY_GROUP = "CNV-SSH+HTTP";
    private static final long CW_OFFSET_MILI = 1000 * 60 * 5;
    private static final String CW_NAMESPACE = "AWS/EC2";
    private static final String CW_METRIC = "CPUUtilization";
    private static final int CW_PERIOD = 60;
    private static final String CW_STATISTICS = "Average";
    private static final String MONITORING_AVAILABILITY_ZONE = "monitoring.eu-central-1.amazonaws.com";
    private static final long ONE_HOUR = 1000 * 3600 * 24;

    private static AWSManager instance = null;
    private AmazonEC2Client ec2;
    private AmazonCloudWatchClient cloudWatch;
    private String lbPublicIP;

    public AWSManager()  {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AWSManager getInstance(){
        if(instance == null){
            instance = new AWSManager();
        }

        return instance;
    }

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private void init() throws Exception {

        AWSCredentials credentials = null;

        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        this.ec2 = new AmazonEC2Client(credentials);
        this.ec2.setEndpoint(AVAILABILITY_ZONE);

        this.cloudWatch = new AmazonCloudWatchClient(credentials);
        this.cloudWatch.setEndpoint(MONITORING_AVAILABILITY_ZONE);
    }


    /**
     *  Starts a new instance running a worker AMI
     *  Returns the object representing that instance
     */
    public WorkerInstance startNewWorker() {

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        runInstancesRequest.withImageId(AMI_ID)
                           .withInstanceType(INSTANCE_TYPE)
                           .withMinCount(1)
                           .withMaxCount(1)
                           .withKeyName(KEY_NAME)
                           .withSecurityGroups(SECURITY_GROUP)
                           .withMonitoring(true);

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        Instance instance = runInstancesResult.getReservation().getInstances().get(0);

        return new WorkerInstance(instance, this.lbPublicIP);
    }


    /**
     * Terminates a given instance
     */
    public void terminateWorker(WorkerInstance instance){

        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instance.getInstanceID());
        this.ec2.terminateInstances(termInstanceReq);
    }


    public void updateCloudWatchMetrics(WorkerInstance instance){

        if(!"running".equals(instance.getState())){
            return;
        }

        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        instanceDimension.setValue(instance.getInstanceID());

        GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                                    .withStartTime(new Date(new Date().getTime() - CW_OFFSET_MILI))
                                    .withNamespace(CW_NAMESPACE)
                                    .withPeriod(CW_PERIOD)
                                    .withMetricName(CW_METRIC)
                                    .withStatistics(CW_STATISTICS)
                                    .withDimensions(Arrays.asList(instanceDimension))
                                    .withEndTime(new Date());

        GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);
        List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();

        instance.setCpuMetric(new CPUMetric(datapoints));
    }

    public synchronized void updateInstance(WorkerInstance w){
        DescribeInstancesResult result= ec2.describeInstances();
        List <Reservation> list  = result.getReservations();

        for (Reservation res:list) {
            List <Instance> instanceList = res.getInstances();
            for(Instance i : instanceList){
                if(i.getInstanceId().equals(w.getInstanceID())){
                    w.setInstance(i);
                    break;
                }
            }
        }
    }

    public void setLbPublicIP(String lbPublicIP) {
        this.lbPublicIP = lbPublicIP;
    }
}
