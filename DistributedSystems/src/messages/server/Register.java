package messages.server;
import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Response;
import connections.server.RegisteredClient;
public class Register {

Message msg;

 
 		

	public Register (Message message) {
		this.msg = message;		
		}

		public Response doRegistration(Connection conn,Message message) {
			
			Response response = new Response();
			Message messageResp = new Message();
			response.setCloseConnection(false);
			
			List<RegisteredClient> registeredClients = Control.getInstance().getRegisteredClients();
			
			if (check_message(message.getUsername()) == false ||check_message(message.getSecret())==false) {
				messageResp.setCommand(Message.INVALID_MESSAGE);
				messageResp.setInfo("Username or Secret cannot be empty");
				response.setMessage(messageResp.toString());
				response.setCloseConnection(true);
				System.out.println("empty");

			}
									
			if (check_client(registeredClients, message.getUsername()) != false) {
			    Lock lock = new Lock(message.getUsername(),message.getSecret());
				
			    Response responselock = lock.Lock_request(conn, message.getUsername(), message.getSecret());
			    
			    Message msglock = new Message(responselock.getMessage());

			    if (msglock.getCommand() == Message.REGISTER_SUCCESS) {
					RegisteredClient client = new RegisteredClient();		
					client.setUsername(message.getUsername());
					client.setSecret(message.getSecret());
					Control.getInstance().addRegisteredClients(client) ;
					messageResp.setCommand(Message.REGISTER_SUCCESS);
					messageResp.setInfo(String.format(Message.REGISTER_SUCCESS_INFO, message.getUsername()));
					response.setMessage(messageResp.toString());
					response.setCloseConnection(false);
					System.out.println("ok");
			    }			
			}	
			else if(check_client(registeredClients, message.getUsername()) == true){
				messageResp.setCommand(Message.REGISTER_FAILED);
				messageResp.setInfo(String.format(Message.REGISTER_FAILED_INFO, message.getUsername()));
				response.setMessage(messageResp.toString());
				response.setCloseConnection(true);
				System.out.println("Error");
			}
			
			return response;
		}
			
			
		
	public Boolean check_message(String message) {
		if (message != null && !message.equals("")) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public Boolean check_client (List<RegisteredClient> clientsList, String usernameToFind) {
		if(clientsList.contains(usernameToFind)) {
			return false;
		}
		else {
			return true; 
		}
		}
		
		
	
	
}
