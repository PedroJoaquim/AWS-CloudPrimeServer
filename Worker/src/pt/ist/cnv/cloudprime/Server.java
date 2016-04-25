package pt.ist.cnv.cloudprime;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Server {
    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/f.html", new IntegerFactoringHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("[SERVER START] RUNNING ON PORT: " + port);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(8085);
    }
}

