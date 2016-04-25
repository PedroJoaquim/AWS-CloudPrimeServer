

public class RequestScript {
	
	private static int responsesReceived;

    public static void main(String[] args) throws InterruptedException{
        int complexity = Integer.parseInt(args[0]);
        int nRequests = Integer.parseInt(args[1]);
        String ipAddress = args[2];
        RequestThread[] threads = new RequestThread[nRequests];

        for (int i=0; i < nRequests; i++){
        	threads[i] = new RequestThread(complexity, ipAddress);
        	threads[i].start();
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
