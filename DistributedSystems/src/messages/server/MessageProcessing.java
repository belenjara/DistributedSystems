package messages.server;

import java.util.ArrayList;
import java.util.List;

import activitystreamer.server.Connection;
import activitystreamer.util.Response;

public class MessageProcessing {
	
	public List<Response> processMsg(Connection conn, String msg) {
		Message message = new Message(msg);
		Response response = new Response();
		List<Response> responses = new ArrayList<Response>();				

		// First we verify if the message contains the property "command".
		message = Message.CheckMessage(message, Message.COMMAND);	
			
		String command = message.getCommand();		
		response.setMessage(null);
		
		switch(command) {					
		case Message.REGISTER:
			conn.setType(Connection.TYPE_CLIENT);
			response = new Register().doRegistration(conn, message);
			responses.add(response);
			break;
		
		
		case Message.LOCK_REQUEST:
			conn.setType(Connection.TYPE_SERVER);
			Lock lockrequest = new Lock(message.getUsername(), message.getSecret());
			response = lockrequest.receiveLockRequest(conn, message);
			responses.add(response);
			break;
			
		case Message.LOGIN:
			conn.setType(Connection.TYPE_CLIENT);
			//// First login, if success, then check for redirect:
			//// The server will follow up a LOGIN_SUCCESS message with a REDIRECT message if the server knows of
			////any other server with a load at least 2 clients less than its own.
			response = new Login().loginProcess(conn,message);
			responses.add(response);
			
			// if login OK, then we verify if is necessary to redirect:
			Response responseRedirect = new Redirection().redirect();
	        if (responseRedirect != null) { responses.add(responseRedirect); }
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
			response = authen.processAuthentication(conn, message);
			responses.add(response);
			break;
			
		case Message.ACTIVITY_MESSAGE:
			conn.setType(Connection.TYPE_CLIENT);
			response = new ActivityMsg().receiveActivityMsg(message, conn);
			responses.add(response);
			break;
			
		case Message.SERVER_ANNOUNCE:
			// here this server is receiving a server announce...
			conn.setType(Connection.TYPE_SERVER);
			response = new ServerAnnounce().receiveServerAnnounce(message, conn);
			responses.add(response);
			break;
			
		case Message.ACTIVITY_BROADCAST:
			conn.setType(Connection.TYPE_SERVER);
			response = new ActivityBroadcast().receiveServerBroadcast(message, conn);
			responses.add(response);
			break;
			
		case Message.AUTHENTICATION_FAIL:
			conn.setType(Connection.TYPE_SERVER);
			response.setCloseConnection(true);
			responses.add(response);
			break;
			
		case Message.LOCK_ALLOWED:
			conn.setType(Connection.TYPE_SERVER);
			Lock lockAllowed = new Lock(message.getUsername(), message.getSecret());
			response = lockAllowed.receiveLockAllowed(conn, message);
			responses.add(response);
			break;
						
		case Message.LOCK_DENIED:
			conn.setType(Connection.TYPE_SERVER);
			Lock lockDenied = new Lock(message.getUsername(), message.getSecret());
			response = lockDenied.receiveLockDenied(conn, message);
			responses.add(response);
			break;
			
		case Message.INVALID_MESSAGE:
			response.setMessage(message.toString());
			response.setCloseConnection(true);
			responses.add(response);
			break;
				
		// any unknown command
		default:
			response.setMessage(new Message().getInvalidMessage());
			response.setCloseConnection(true);	
			responses.add(response);
			break;
		}
		
		return responses;	
	}
}