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
		//Boolean isAuth = connMan.serverIsAuthenticated(conn); //clientIsLogin should be in this case PERO PARECE QUE YA SE CONTROLA ANTES DE HACER EL BROADCAST EN CONTROL

		//"validate logged on client" is only from this server's clients - ADECUAR	
		//Validate structure of this message
		Response valid = validateMessage(message, conn);
		if (valid.getCloseConnection()) {
			return valid;
		}

		//Build the ACTIVITY_BROADCAST Message and then send it to everyone (clients and servers)
		Message msg = new Message();
		//From PDF: To process the object it will add a single field to the object called authenticated_user
		//(PAGE 25)
		message.getActivity().put(Message.AUTHENTICATED_USER, message.getUsername());
		msg.setCommand(Message.ACTIVITY_BROADCAST);
		msg.setActivity(message.getActivity());
		connMan.broadcastAll(msg.toString(), conn);

		return response;

	}

	//This is to validate the body of this Message
	private Response validateMessage(Message msg, Connection conn) {
		Response response = new Response();
		response.setCloseConnection(false);

		Message responseMsg = Message.CheckMessage(msg, Message.USERNAME);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}

		if (!msg.getUsername().equals(Message.ANONYMOUS)) {
			responseMsg = Message.CheckMessage(msg, Message.SECRET);
			if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
				response.setCloseConnection(true);
				response.setMessage(responseMsg.toString());
				return response;
			}
		}

		//We also need to validate in this case is there is a mismatch between the username and secret!!
		responseMsg = Message.CheckMessage(msg, Message.ACTIVITY);
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}			

		if (!msg.getUsername().equals(Message.ANONYMOUS)) {
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
		}
		
		if (!msg.getUsername().equals(Message.ANONYMOUS) && !conn.getAuth()) {
			Message message = new Message();
			message.setCommand(Message.AUTHENTICATION_FAIL);
			message.setInfo(Message.ERROR_AUTH_INFO3);
			response.setCloseConnection(true);
			response.setMessage(message.toString());
			return response;
		}

		return response;
	}
}
