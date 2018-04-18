package activitystreamer.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.util.Settings;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private TextFrame textFrame;
	public static Socket socket;

	
	public static ClientSkeleton getInstance(){
		if(clientSolution==null){
			clientSolution = new ClientSkeleton();
			socket = null;
			try {
				System.out.println("Client: going to connect to server");
				socket = new Socket("sunrise.cis.unimelb.edu.au", 3780);
				System.out.println("Connection established");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
		}
		return clientSolution;
	}
	
	public ClientSkeleton(){
		
		textFrame = new TextFrame();
		start();
	}
	

	@SuppressWarnings("unchecked")
	public void sendActivityObject(JSONObject activityObj){
		
		if (socket != null) {
			String msg = activityObj.toJSONString();
			log.info("Msg: " + msg);
			
			try {
				//BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				
				out.write(msg + "\n");
				out.flush();
				System.out.println("Message sent");
				
				// Receive the reply from the server by reading from the socket input stream
				/*String received = in.readLine(); // This method blocks until there
													// is something to read from the
													// input stream
				System.out.println("Message received: " + received);*/
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			log.info("socket is null...");
		}
	}
	
	
	public void disconnect(){
	}
	
	
	public void run(){
		log.info("This is run()!!");	
	}

	
}
