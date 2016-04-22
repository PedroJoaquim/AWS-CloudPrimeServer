package pt.ist.cnv.cloudprime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import pt.ist.cnv.cloudprime.aws.AWSManager;
import pt.ist.cnv.cloudprime.aws.WorkerInstance;
import pt.ist.cnv.cloudprime.aws.metrics.WorkInfo;
import pt.ist.cnv.cloudprime.httpserver.ReadRequestHandler;
import pt.ist.cnv.cloudprime.httpserver.ResponseRequesthandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class LoadBalancer {

    public static final int GRACE_PERIOD = 90000; //1 30 minute

    /**
     * Singleton instance for Load Balancer
     */
    private static LoadBalancer instance = null;

    /**
     * Map that associates a requestID to the worker that is handling that request
     */
    private Map<Integer, WorkerInstance> pendingRequests = new ConcurrentHashMap<Integer, WorkerInstance>();

    /**
     * List with all worker instances
     */
    private List<WorkerInstance> workers = new ArrayList<>();

    /**
     * object responsible for aws communications related issues
     */
    private AWSManager awsManager = null;

    /**
     * current requestID var
     */
    private int requestID = 1;

    /**
     * LoadBalancer public ip
     */
    private String publicIP;


    public static void main(String[] args) throws Exception {
        LoadBalancer lb = new LoadBalancer();
        lb.start();
    }


    public static LoadBalancer getInstance(){
        if(instance == null){
            instance = new LoadBalancer();
        }
        return instance;
    }

    /**
     * Starts the web server to receive the requests
     * And gets the machine public ip
     */
    private void start() throws Exception {

        LoadBalancer.instance = this;
        this.publicIP = getPublicIP();
        this.awsManager = AWSManager.getInstance();
        this.awsManager.setLbPublicIP(this.publicIP);

        startServer(8000);
    }

    /**
     * Thread safe method to get a new unique requestID
     */
    private synchronized int getRequestID(){
        return this.requestID++;
    }

    /**
     * Function to start the load balancer web server with a
     * thread pool to handle the requests
     */
    private void startServer(int port) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/f.html", new ReadRequestHandler());
        server.createContext("/r.html", new ResponseRequesthandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("LOAD BALANCER STARTED AND RUNNING ON: " + port);
    }

    public void handleNewRequest(HttpExchange httpExchange, String numberToFactor) {

        WorkerInstance worker = chooseWorker(numberToFactor);
        int requestID = getRequestID();

        this.pendingRequests.put(requestID, worker);

        worker.doRequest(new WorkInfo(httpExchange, numberToFactor, requestID));
    }

    public HttpExchange endPendingRequest(int requestID){
        WorkerInstance instance = this.pendingRequests.get(requestID);
        HttpExchange he = instance.getWorkInfo(requestID).getHttpExchange();

        instance.endJob(requestID);

        return he;
    }


    /**
     * Function with the logic to choose a worker instance to handle the request
     *
     * @param numberToFactor
     * @return the assigned instance to process the request
     */
    private synchronized WorkerInstance chooseWorker(String numberToFactor) {
        List<WorkerInstance> availableWorkers = getAvailableWorkers();

        return availableWorkers.size() > 0 ? availableWorkers.get(0) : null;
    }



    public List<WorkerInstance> getAvailableWorkers() {
        List<WorkerInstance> result = new ArrayList<WorkerInstance>();
        //todo
        return result;
    }

    /**
     * Function that finds the machine public ip using aws checkip service
     */
    public String getPublicIP() throws IOException {

        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

        return in.readLine();
    }

    /**
     * Adds new workers to the list of available workers
     */
    public synchronized void addNewWorker(WorkerInstance wi) {
        this.workers.add(wi);
    }


    public boolean canDecrease(WorkerInstance wi) {
        return false; //todo
    }
}
