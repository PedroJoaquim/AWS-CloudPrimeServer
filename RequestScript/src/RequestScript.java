

public class RequestScript {
	
	private static int responsesReceived;

    public static void main(String[] args) throws InterruptedException{
        
    	if(args.length <  4){
    		System.out.println("Usage: java RequestScript complexity nr_requests lb_ipaddr request_interval");
		return;
    	}

        int complexity = Integer.parseInt(args[0]);
        int nRequests = Integer.parseInt(args[1]);
        String ipAddress = args[2];
        long interval = Long.valueOf(args[3]);

        RequestThread[] threads = new RequestThread[nRequests];

        for (int i=0; i < nRequests; i++){
        	System.out.println("NEW REQUEST " + i);
        	threads[i] = new RequestThread(complexity, ipAddress);
        	threads[i].start();
        	Thread.sleep(interval);
        }

        for (int i=0; i < nRequests; i++){
        	threads[i].joinThread();
        }

        printData();
    }

    public static synchronized void addResponseReceived(){
    	responsesReceived++;
    }

    public static void printData(){
	System.out.println("\nResponses Received: " + responsesReceived);
    }	
}
