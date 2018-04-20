package messages.server;

import java.util.ArrayList;
import java.util.List;

import activitystreamer.server.Connection;
import activitystreamer.util.Response;

public class MessageProcessing {
	
	public List<Response> processMsg(Connection conn, String msg) {
		Message message = new Message(msg);
		
		String command = message.getCommand();
		
		List<Response> responses = new ArrayList<Response>();
				
		Response response = new Response();
		response.setMessage(null);
		
		switch(command) {					
		case Message.REGISTER:
			conn.setType(Connection.TYPE_CLIENT);

			break;
		
		case Message.LOCK_REQUEST:
			conn.setType(Connection.TYPE_SERVER);
			break;
			
		case Message.LOGIN:
			conn.setType(Connection.TYPE_CLIENT);
			// TODO: login
			//// First login, if success, then check for redirect:
			//// The server will follow up a LOGIN_SUCCESS message with a REDIRECT message if the server knows of
			////any other server with a load at least 2 clients less than its own.

			// if login OK, then:
			Response responseRedirect = new Redirection().redirect();
	        if (response != null) { responses.add(responseRedirect); }
			break;
			
		case Message.LOGOUT:
			conn.setType(Connection.TYPE_CLIENT);
			response.setCloseConnection(true);
			responses.add(response);
			break;
			
		case Message.AUTHENTICATE:
			conn.setType(Connection.TYPE_SERVER);
			// the server receive a authentication message. 
			Authentication authen = new Authentication();
			//authen.processAuthentication();
			
			response = authen.processAuthentication(conn,message);
			responses.add(response);
			break;
			
		case Message.ACTIVITY_MESSAGE:
			conn.setType(Connection.TYPE_CLIENT);

			break;
			
		case Message.SERVER_ANNOUNCE:
			// here this server is receiving a server announce...
			conn.setType(Connection.TYPE_SERVER);
			response = new ServerAnnounce().receiveServerAnnounce(message, conn);
			responses.add(response);
			break;
			
		case Message.ACTIVITY_BROADCAST:
			conn.setType(Connection.TYPE_SERVER);
			break;
			
		case Message.AUTHENTICATION_FAIL:
			conn.setType(Connection.TYPE_SERVER);
			response.setCloseConnection(true);
			responses.add(response);
			break;
			
		case Message.LOCK_ALLOWED:
			conn.setType(Connection.TYPE_SERVER);
			break;
			
		case Message.LOCK_DENIED:
			conn.setType(Connection.TYPE_SERVER);
			break;
			
		case Message.INVALID_MESSAGE:
			response.setMessage(message.toString());
			response.setCloseConnection(true);
			responses.add(response);
			break;
						
		default:
			response.setMessage(new Message().getInvalidMessage());
			response.setCloseConnection(true);	
			responses.add(response);
			break;
		}
		
		return responses;	
	}
}