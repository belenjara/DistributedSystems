package messages.server;

import activitystreamer.server.Connection;
import activitystreamer.util.Response;

public class MessageProcessing {
	
	public Response  processMsg(Connection conn, String msg) {
		Message message = new Message(msg);
		
		String command = message.getCommand();
				
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
			// This is just a test!! 
		//	String redirectMsg = new Redirection().redirect();
		//	if (redirectMsg != null) { responseMessage = redirectMsg; break; }
			
			//DO something
            break;
			
		case Message.LOGOUT:
			conn.setType(Connection.TYPE_CLIENT);

			response.setCloseConnection(true);
			break;
			
		case Message.AUTHENTICATE:
			conn.setType(Connection.TYPE_SERVER);
			
			break;
			
		case Message.ACTIVITY_MESSAGE:
			conn.setType(Connection.TYPE_CLIENT);

			break;
			
		case Message.SERVER_ANNOUNCE:
			// here this server is receiving a server announce...
			conn.setType(Connection.TYPE_SERVER);
			response = new ServerAnnounce().receiveServerAnnounce(message, conn);
			break;
			
		case Message.ACTIVITY_BROADCAST:
			conn.setType(Connection.TYPE_SERVER);
			break;
			
		case Message.AUTHENTICATION_FAIL:
			conn.setType(Connection.TYPE_SERVER);
			response.setCloseConnection(true);
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
			break;
						
		default:
			response.setMessage(new Message().getInvalidMessage());
			response.setCloseConnection(true);
			break;
		}
		
		return response;	
	}
}