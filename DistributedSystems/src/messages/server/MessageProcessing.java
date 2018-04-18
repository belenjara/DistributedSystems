package messages.server;

import activitystreamer.server.Connection;

public class MessageProcessing {
	
	public String processMsg(Connection conn, String msg) {
		Message message = new Message(msg);
		
		String command = message.getCommand();
		
		String responseMessage = "NULL";
		
		switch(command) {					
		case Message.REGISTER:
			break;
		
		case Message.LOCK_REQUEST:
			break;
			
		case Message.LOGIN:
			// This is just a test!! 
		//	String redirectMsg = new Redirection().redirect();
		//	if (redirectMsg != null) { responseMessage = redirectMsg; break; }
			
			//DO something
            break;
			
		case Message.LOGOUT:
			break;
			
		case Message.AUTHENTICATE:
			break;
			
		case Message.ACTIVITY_MESSAGE:
			break;
			
		case Message.SERVER_ANNOUNCE:
			break;
			
		case Message.ACTIVITY_BROADCAST:
			break;
			
		default:
			return new Message().getInvalidFormatMessage();
		}
		
		return responseMessage;	
	}
}