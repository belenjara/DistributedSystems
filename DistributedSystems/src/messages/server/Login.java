package messages.server;

public class Login {
	String username,secret;
	
	public Response loginProcess(Message msg) {
		username = msg.getUsername();
		secret = msg.getSecret();
		
		
	}

}
