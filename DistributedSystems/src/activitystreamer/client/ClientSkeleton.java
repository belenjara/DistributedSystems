package activitystreamer.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.util.Settings;
import messages.server.Message;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private TextFrame textFrame;
	public static Socket socket;
	private static JSONParser parser = new JSONParser();

	public static ClientSkeleton getInstance(){
		if(clientSolution==null){
			clientSolution = new ClientSkeleton();
			socket = null;
			try {
				log.info("Client: going to connect to server");
				socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
				log.info("Connection established");
				doLogin();
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

		String msg = activityObj.toJSONString();

		writeMsg(msg);
	}


	public void disconnect(){
		try {
			Message msg = new Message();
			msg.setCommand(Message.LOGOUT);
			writeMsg(msg.toString());
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
		String username = Settings.getUsername();
		String secret = Settings.getSecret();
		Message msg = new Message();

		if (username.equals("anonymous")) {
			msg.setCommand(Message.LOGIN);
			msg.setUsername(username);
			log.info("Sending login as an anonymous: " + msg.toString());
			writeMsg(msg.toString());

		} else {
			boolean newSecret = false;
			if (secret == null || secret.equals("")) {
				secret = Settings.nextSecret();
				newSecret = true;
			}

			if (socket != null) {
				BufferedReader in;
				try {
					log.info("Sending login message to the server, please wait...");
					msg.setCommand(Message.LOGIN);
					msg.setUsername(username);
					msg.setSecret(secret);
					writeMsg(msg.toString());

					in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
					JSONObject output = (JSONObject)parser.parse(in.readLine());				
					String msgStr = output.toJSONString();
					Message messageResp = new Message(msgStr);

					log.info("After login => The server response: " + msgStr);

					if (messageResp.getCommand().equals(Message.LOGIN_SUCCESS)) {	
						if (newSecret) {
							log.info("Secret generated: " + secret);
						}
					} else if (messageResp.getCommand().equals(Message.LOGIN_FAILED)) {
						log.info("Sending register message to the server, please wait...");
						try {
							socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
						}
						catch (Exception e){
							
						}
						msg = new Message();
						msg.setCommand(Message.REGISTER);
						msg.setUsername(username);
						msg.setSecret(secret);
						writeMsg(msg.toString());

						in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
						String msgServer = in.readLine();
						output = (JSONObject)parser.parse(msgServer);				
						msgStr = output.toJSONString();
						messageResp = new Message(msgStr);

						log.info("After register => The server response: " + msgStr);

						if (messageResp.getCommand().equals(Message.REGISTER_SUCCESS)) {
							log.info("Sending login message after registering to the server, please wait...");
							msg.setCommand(Message.LOGIN);
							msg.setUsername(username);
							msg.setSecret(secret);
							writeMsg(msg.toString());

							in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
							output = (JSONObject)parser.parse(in.readLine());				
							msgStr = output.toJSONString();
							messageResp = new Message(msgStr);

							log.info("After login => The server response: " + msgStr);

							if (messageResp.getCommand().equals(Message.LOGIN_SUCCESS)) {	
								if (newSecret) {
									log.info("Secret generated: " + secret);
								}
							}
						}
					}

				} catch (ParseException | IOException e) {
					// TODO Auto-generated catch block
					log.error("An error ocurred.");
					e.printStackTrace();
				}
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

	public void run(){
	}
}
