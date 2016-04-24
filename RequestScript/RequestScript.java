

public class RequestScript {
	
    public static void main(String[] args) {
        int complexity = Integer.parseInt(args[0]);
        int nRequests = Integer.parseInt(args[1]);
        String ipAddress = args[2];
        
        for (int i=0; i < nRequests; i++){
        	RequestThread reqThread = new RequestThread(complexity, ipAddress);
        	reqThread.start();
        }
        
    }
}