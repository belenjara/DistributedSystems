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

import javax.xml.stream.events.StartDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.server.Control;
import activitystreamer.util.Settings;
import messages.server.Message;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private static TextFrame textFrame;
	public static Socket socket;
	private static JSONParser parser = new JSONParser();

	private static DataInputStream in;
	//private DataOutputStream out;
	private static BufferedReader inreader;
	//private PrintWriter outwriter;
	//private boolean open = false;

	private static String username;
	private static String secret;

	public static ClientSkeleton getInstance(){
		if(clientSolution==null){
			textFrame = new TextFrame();
			socket = null;
			try {
				log.info("Client: going to connect to server");
				socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
				log.info("Connection established");

				in = new DataInputStream(socket.getInputStream());
				// out = new DataOutputStream(socket.getOutputStream());
				inreader = new BufferedReader( new InputStreamReader(in));
				// outwriter = new PrintWriter(out, true);

				doLogin();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

		clientSolution = new ClientSkeleton();		

		return clientSolution;
	}

	public ClientSkeleton(){

		start();
	}


	public void sendActivityObject(JSONObject activityObj){
		textFrame.cleanOutputText();
		String msg = activityObj.toJSONString();
		writeMsg(msg);
	}


	public void disconnect(){
		try {
			textFrame.cleanOutputText();
			Message msg = new Message();
			msg.setCommand(Message.LOGOUT);
			writeMsg(msg.toString());
			textFrame.setVisible(false);
		}finally {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void doLogin() {	
		username = Settings.getUsername();
		secret = Settings.getSecret();
		// 1) if the user the gives no username 
		// on the command line arguments then login as anonymous on start
		if (username.equals(Message.ANONYMOUS)) {
			if (socket != null) {		
				doLogin2();
			}
		} else {
			boolean newSecret = false;
			if (secret == null || secret.trim().equals("")) {
				secret = Settings.nextSecret();
				newSecret = true;
			}
			// 2)  if the user gives only a username but no secret then first register the user, 
			// by generating a new secret (print to screen for subsequent use), 
			// then login after/if receiving register success
			if (newSecret) {
				log.info(">>>>> This is the secret generated for user " + username + " : " + secret );
				doRegistration();
			}
			else {
				// 3) if the user gives a username and secret then login on start
				doLogin2();
			}
		}
	}

	private static void writeMsg(String msg){
		if (socket != null) {
			log.info("Msg to send to the server: " + msg);
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

				out.write(msg + "\n");
				out.flush();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			log.error("socket is null...");
		}
	}

	private static void showTextInframe(JSONObject msg) {
		if (msg != null) {
			textFrame.setOutputText(msg);
		}
	}

	public void run(){
		try {		
			String data ;
			while ((data = inreader.readLine())!=null){
				JSONObject output = (JSONObject)parser.parse(data);		
				showTextInframe(output);
				process(data);
			}		
		} catch (IOException e) {

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void process(String msgStr) {
		Message messageResp = new Message(msgStr);
		if (messageResp.getCommand().equals(Message.REGISTER_SUCCESS)) {
			doLogin2();
		}
		else {
			textFrame.cleanInputText();
		}
	}

	private static void doLogin2() {
		log.info("Sending login message after registering to the server, please wait...");
		Message msg = new Message();
		msg.setCommand(Message.LOGIN);
		msg.setUsername(username);
		if (!username.equals(Message.ANONYMOUS)) {
			msg.setSecret(secret);
		}
		writeMsg(msg.toString());
	}

	private static void doRegistration() {
		log.info("Sending login message after registering to the server, please wait...");
		Message msg = new Message();
		msg = new Message();
		msg.setCommand(Message.REGISTER);
		msg.setUsername(username);
		msg.setSecret(secret);
		writeMsg(msg.toString());
	}
}
