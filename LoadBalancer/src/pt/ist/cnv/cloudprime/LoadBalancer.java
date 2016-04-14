package pt.ist.cnv.cloudprime;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class LoadBalancer {

    private static LoadBalancer instance = null;
    private AmazonEC2 ec2;
    private AmazonCloudWatchClient cloudWatch;

    public static void main(String[] args) throws Exception {
        LoadBalancer lb = new LoadBalancer();
        lb.start();
    }


    private static LoadBalancer getInstance(){
        if(instance == null){
            instance = new LoadBalancer();
        }

        return instance;
    }


    private void start() throws Exception {
        init();
        startServer(80);

    }

    /**
     * Function to start the load balancer
     */
    private void startServer(int port) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/f.html", new ReadRequestHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("LOAD BALANCER STARTED AND RUNNING ON: " + port);
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

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
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

        ec2 = new AmazonEC2Client(credentials);
        cloudWatch = new AmazonCloudWatchClient(credentials);
    }

}
