package pt.ist.cnv.cloudprime.mss;

import com.sun.net.httpserver.HttpServer;
import pt.ist.cnv.cloudprime.mss.httpserver.PostInfoHandler;
import pt.ist.cnv.cloudprime.mss.httpserver.RequestInfoHandler;
import pt.ist.cnv.cloudprime.mss.metrics.AbstractMetric;
import pt.ist.cnv.cloudprime.mss.metrics.MetricsInfoContainer;
import pt.ist.cnv.cloudprime.mss.util.Config;
import pt.ist.cnv.cloudprime.mss.util.RequestFileParser;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;


public class MSServer {

    private static MSServer instance;

    private List<AbstractMetric> metricsInfo = new ArrayList<AbstractMetric>();

    public static synchronized MSServer getInstance(){
        return instance;
    }

    public static void main(String[] args) throws Exception {
        MSServer mss = new MSServer();
        mss.start();
    }

    private void start() throws IOException {
        MSServer.instance = this;
        initFromStorage();
        startServer(Config.MSS_PORT);
    }

    private void initFromStorage() throws IOException {
        File folder = new File(Config.STORAGE_DIR);

        for (File file :  folder.listFiles()) {
            if (file.isFile()) {
                loadRequestInfoFromFile(file.getAbsolutePath());
            }
        }
    }

    private synchronized void loadRequestInfoFromFile(String absolutePath) throws IOException {
        this.metricsInfo.add(RequestFileParser.parse(absolutePath));
    }

    private void startServer(int mssPort) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(mssPort), 0);
        server.createContext("/info", new RequestInfoHandler());
        server.createContext("/requestInfo", new PostInfoHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("MSS STARTED AND RUNNING ON: " + mssPort);
    }


    public synchronized MetricsInfoContainer getUpdatesSince(int lastUpdateID) {
        List<AbstractMetric> result = new ArrayList<>();

        for (int i = lastUpdateID; i < this.metricsInfo.size() ; i++) {
            result.add(this.metricsInfo.get(i));
        }

        return new MetricsInfoContainer(result, lastUpdateID, this.metricsInfo.size());
    }

    public synchronized void addNewInfo(AbstractMetric m){
        this.metricsInfo.add(m);
    }

    public int getLastUpdateID() {
        return this.metricsInfo.size();
    }
}
