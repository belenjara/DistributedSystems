package messages.server;

public class Redirection {

	
	public String redirect() {
		
		//// TODO: check if this server has reached the limit of incoming connections. 
		Boolean redirect = true;
		
		if (redirect) {	
			Message msg = new Message();
			msg.setCommand(Message.REDIRECT);
			msg.setHostname("localhost"); //?
			msg.setPort(1234); //?
			
			return msg.toString();
		}
		
		return null;
	}
	
}
