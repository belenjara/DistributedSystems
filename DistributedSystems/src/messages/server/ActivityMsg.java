package messages.server;

import java.util.ArrayList;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Response;
import connections.server.RegisteredClient;

public class ActivityMsg {
	
	//sendActivityMsg is already configured in the client (GUI) - Do we need to worry about the client sending the message well formatted? I think the client writes the whole JSON Code
	
	public Response receiveActivityMsg(Message message, Connection conn) {
		Response response = new Response();
		response.setCloseConnection(false);
		Control connMan = Control.getInstance();
		Boolean isAuth = connMan.serverIsAuthenticated(conn); //clientIsLogin should be in this case PERO PARECE QUE YA SE CONTROLA ANTES DE HACER EL BROADCAST EN CONTROL
		
		//"validate logged on client" is only from this server's clients - ADECUAR
		if (!isAuth) {
			Message msg = new Message();
			msg.setCommand(Message.AUTHENTICATION_FAIL);
			msg.setInfo(Message.ERROR_AUTH_INFO);
			response.setCloseConnection(true);
			response.setMessage(msg.toString());
			return response;
		}
		
		//Validate structure of this message
		Response valid = validateMessage(message);
		if (valid.getCloseConnection()) {
			Message msg = new Message();
			msg.setCommand(Message.INVALID_MESSAGE);
			msg.setInfo(valid.getMessage());
			response.setCloseConnection(true);
			response.setMessage(msg.toString());
			return response;
		}
		
		//Build the ACTIVITY_BROADCAST Message and then send it to everyone (clients and servers)
		Message msg = new Message();
		msg.setCommand(Message.ACTIVITY_BROADCAST);
		msg.setActivity(message.getActivity());
		connMan.broadcastAll(msg.toString(), conn);
				
		return response;
		
	}
	
		//This is to validate the body of this Message
		private Response validateMessage(Message msg) {
			Response response = new Response();
			response.setCloseConnection(false);
			
			Message responseMsg = Message.CheckMessage(msg, Message.USERNAME);	
			if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
				response.setCloseConnection(true);
				response.setMessage(responseMsg.toString());
				return response;
			}
			
			responseMsg = Message.CheckMessage(msg, Message.SECRET);
			if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
				response.setCloseConnection(true);
				response.setMessage(responseMsg.toString());
				return response;
			}
			
			//We also need to validate in this case is there is a mismatch between the username and secret!!
			responseMsg = Message.CheckMessage(msg, Message.ACTIVITY);
			if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
				response.setCloseConnection(true);
				response.setMessage(responseMsg.toString());
				return response;
			}			
			
			Control connMan = Control.getInstance();
			ArrayList<RegisteredClient> registeredClients=connMan.getRegisteredClients();
			boolean isClientCredentialMatch = false;
			
			for(RegisteredClient r : registeredClients){
			  if (msg.getUsername().equals(r.getUsername()) && msg.getSecret().equals(r.getSecret())) {
				  isClientCredentialMatch = true;
				  break;
			  }
			}
			
			if (isClientCredentialMatch == false) {
				response.setCloseConnection(true);
				Message authFailedMsg = new Message();
				authFailedMsg.setCommand(Message.AUTHENTICATION_FAIL);
				authFailedMsg.setInfo("the supplied secret is incorrect: " + msg.getSecret());
						
				return response;
				
			}
			
			return response;
		}
		
}
