import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.sql.rowset.spi.SyncResolver;


public class RequestThread extends Thread {
	private int complexity = -1;
	private String ipAddress = null;
	private Thread t = null;
	URL url = null;
	HttpURLConnection conn = null;
	
	public RequestThread(int complexity, String ipAddress){
		
		if (complexity < 0 || complexity > 3)
			this.complexity = 0;
		else
			this.complexity = complexity;
		
		this.ipAddress = ipAddress;
	}
	
	public void run(){
		methodGET();
		RequestScript.addResponseReceived();
	}
	
	public void start(){
		t = new Thread(this);
		t.start();
	}

	public void joinThread(){
		try{t.join();} catch(Exception e) {}
	}

	private String generateNumber(){
		int number = -1;
		Random rnd = new Random();
		int rangeMin = 0;
		int rangeMax = 0;
		int numberAux = -1;
		
		switch (complexity) {
			case 1:
				rangeMin = 100000;
				rangeMax = 999999;
				number = rnd.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
				return String.valueOf(number);
			case 2:
				rangeMin = 10000000;
				rangeMax = 100000000;
				number = rnd.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
				return String.valueOf(number);
			case 3:
				BigInteger lowerBound = new BigInteger("100000000000000000000000000000");
				BigInteger upperBound = new BigInteger("10000000000000000000000000000000000");
				BigInteger bigNumber = null;
				//wait for a random bigger than lowerBound
				do { bigNumber = new BigInteger(upperBound.bitLength(), rnd);}
				while (bigNumber.compareTo(lowerBound) == -1);
				return bigNumber.toString();
			//case complexity = 0 or others
			default:
				number = rnd.nextInt(10000);
				return String.valueOf(number);		
		}
	}
	
	private void methodGET(){
		String rndNumber = generateNumber();
		BufferedReader reader = null;
		String response = null;

		try {
			this.url = new URL("http://" + ipAddress + ":8000/f.html?n=" + rndNumber);
			this.conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			//receive the number refactored
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			response = reader.readLine();
			System.out.println("**************************");
			System.out.println("Thread ID: " + this.getId());
			System.out.println("Complexiy: " + this.complexity);
			System.out.println("Number: " + rndNumber);
			System.out.println("Response: " + response);
			System.out.println("**************************\n");					 
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private synchronized void methodPOST(){
		String rndNumber = generateNumber();
		BufferedReader reader = null;
		DataOutputStream wr = null;
		String response = null;
		String postParams = null;
		byte[] postData = null;
		
		
		
		try {
			postParams = "number=" + rndNumber + "&" + "complexity=" + this.complexity;
			postData = postParams.getBytes("UTF-8");
			
			this.url = new URL("http://" + ipAddress + ":8000/f.html");
			this.conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString(postData.length));
			//sending data
			wr = new DataOutputStream( conn.getOutputStream());
			wr.write( postData );
			wr.close();
			
			//receive response
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//reader the number refactored
			response = reader.readLine();
			reader.close();
			System.out.println("**************************");
			System.out.println("Thread ID: " + this.getId());
			System.out.println("Complexiy: " + this.complexity);
			System.out.println("Number: " + rndNumber);
			System.out.println("Response: " + response);
			System.out.println("**************************\n");					 
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
