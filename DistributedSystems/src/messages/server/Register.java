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
	
		/*if (check_client(client_list, message.getUsername()) != false) {
		JSONObject client= new JSONObject();

		client.put("Name", msg.getUsername());
		client.put("Secret", msg.getSecret());
		client_list.add(client);
		registed_client.put("Registed Client",client_list);
		System.out.println(registed_client);
		
		JSONObject prompt_com = new JSONObject();
		JSONObject prompt_info = new JSONObject();
		prompt_com.put("command", "REGISTER_SUCCESS");
		prompt_info.put("info", "register success for "+msg.getUsername());
		prompt_reg.add(prompt_com);
		prompt_reg.add(prompt_info);
		
		Lock lock = new Lock(msg.getUsername(),msg.getSecret());
		System.out.println(prompt_reg);*/
		
		}

		public Response doRegistration(Connection conn,Message message) {
			
			Response response = new Response();
			
			List<RegisteredClient> registeredClients = Control.getInstance().getRegisteredClients();
			
			if (check_message(message.getUsername()) == false ||check_message(message.getSecret())==false) {
				Message messageResp = new Message();
				messageResp.setCommand(Message.INVALID_MESSAGE);
				messageResp.setInfo("Username or Secret cannot be empty");
				response.setMessage(messageResp.toString());
				response.setCloseConnection(true);
			}
			
						
			if (check_client(registeredClients, message.getUsername()) != false) {
				//Lock lock = new Lock(message.getUsername(),message.getSecret());
				
				//lock.Lock_request(conn, message.getUsername(), message.getSecret());
				
				RegisteredClient client = new RegisteredClient();
				client.setUsername(message.getUsername());
				client.setSecret(message.getSecret());
				Control.getInstance().addRegisteredClients(client) ;
				System.out.println("ok");
	
			
			}
			
			else if(check_client(registeredClients, message.getUsername()) == true){
				Message messageResp = new Message();
				messageResp.setCommand(Message.REGISTER_FAILED);
				messageResp.setInfo(String.format(Message.REGISTER_FAILED_INFO, message.getUsername()));
				response.setMessage(messageResp.toString());
				response.setCloseConnection(true);
			
			
			/*Message messageResp = new Message();
			messageResp.setCommand(Message.REGISTER_SUCCESS);
			messageResp.setInfo(String.format(Message.REGISTER_SUCCESS_INFO, msg.getUsername()));
			
			response.setMessage(messageResp.toString());
			response.setCloseConnection(false);*/
				
				// TODO: the broadcast of lockrequest
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
