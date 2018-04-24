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

import activitystreamer.server.Control;
import activitystreamer.util.Settings;
import messages.server.Message;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private static TextFrame textFrame;
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

		// 1) if the user the gives no username 
		// on the command line arguments then login as anonymous on start
		if (username.equals(Message.ANONYMOUS)) {
			if (socket != null) {		
				msg.setCommand(Message.LOGIN);
				msg.setUsername(username);
				log.info("Sending login as an anonymous: " + msg.toString());
				writeMsg(msg.toString());

				try {
					JSONObject output;
					String msgStr;

					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
					output = (JSONObject)parser.parse(in.readLine());		
					showTextInframe(output);
					msgStr = output.toJSONString();
					log.info("After login => The server response: " + msgStr);

					// In case we receive one more response..
					while(in.ready()){
						String data = in.readLine();
						output = (JSONObject)parser.parse(data);		
						showTextInframe(output);
						msgStr = output.toJSONString();
						log.info("After login => The server response: " + msgStr);
					}
				} catch (ParseException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
				Message messageResp = new Message();
				BufferedReader in;
				JSONObject output;
				String msgStr;

				msg = new Message();
				msg.setCommand(Message.REGISTER);
				msg.setUsername(username);
				msg.setSecret(secret);
				writeMsg(msg.toString());

				try {
					in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
					String msgServer = in.readLine();
					output = (JSONObject)parser.parse(msgServer);
					showTextInframe(output);
					msgStr = output.toJSONString();			
					log.info("After register => The server response: " + msgStr);

					messageResp = new Message(msgStr);
					if (messageResp.getCommand().equals(Message.REGISTER_SUCCESS)) {
						log.info("Sending login message after registering to the server, please wait...");
						msg.setCommand(Message.LOGIN);
						msg.setUsername(username);
						msg.setSecret(secret);
						writeMsg(msg.toString());

						/*in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
						output = (JSONObject)parser.parse(in.readLine());	
						showTextInframe(output);
						msgStr = output.toJSONString();
						log.info("After login => The server response: " + msgStr);

						messageResp = new Message(msgStr);
						if (messageResp.getCommand().equals(Message.LOGIN_SUCCESS)) {	
							log.info(">>>>> This is the secret generated for user " + username + " : " + secret );
						}*/
						// In case we receive one more response..
						while(in.ready()){
							String data = in.readLine();
							output = (JSONObject)parser.parse(data);		
							showTextInframe(output);
							msgStr = output.toJSONString();

							messageResp = new Message(msgStr);
							if (messageResp.getCommand().equals(Message.LOGIN_SUCCESS)) {	
								log.info(">>>>> This is the secret generated for user " + username + " : " + secret );
							}

							log.info("After login => The server response: " + msgStr);
						}
						//in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
						//output = (JSONObject)parser.parse(in.readLine());
						//showTextInframe(output);
						//msgStr = output.toJSONString();
						//messageResp = new Message(msgStr);
					}
				} catch (ParseException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				// 3) if the user gives a username and secret then login on start
				if (socket != null) {
					msg.setCommand(Message.LOGIN);
					msg.setUsername(username);
					msg.setSecret(secret);
					log.info("Sending login : " + msg.toString());
					writeMsg(msg.toString());
					try {
						JSONObject output;
						String msgStr;

						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));		
						//JSONObject output = (JSONObject)parser.parse(in.readLine());		
						//showTextInframe(output);
						//String msgStr = output.toJSONString();
						//log.info("After login => The server response: " + msgStr);

						// In case we receive one more response..
						while(in.ready()){
							String data = in.readLine();
							output = (JSONObject)parser.parse(data);		
							showTextInframe(output);
							msgStr = output.toJSONString();
							log.info("After login => The server response: " + msgStr);
						}
					} catch (ParseException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

	private static void showTextInframe(JSONObject msg) {
		if (msg != null) {
			textFrame.setOutputText(msg);
		}
	}

	public void run(){
	}
}
