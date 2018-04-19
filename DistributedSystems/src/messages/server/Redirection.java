package messages.server;

import java.util.List;

import activitystreamer.server.Control;
import activitystreamer.util.Response;
import connections.server.AnnouncedServer;

public class Redirection {
	
	public Response redirect() {
		int clientsNum = Control.getInstance().getNumberClientsConnected();	
		List<AnnouncedServer> servers = Control.getInstance().getAnnouncedServers();
		
		for(AnnouncedServer s : servers) {
			//REDIRECT message if the server knows of any other server with a load at least 2 clients less than its own.
			if (clientsNum - s.getLoad() >= 2) {
				Message msg = new Message();
				msg.setCommand(Message.REDIRECT);
				msg.setHostname(s.getHostname());
				msg.setPort(s.getPort());
				
				Response response = new Response();
				response.setMessage(msg.toString());
				response.setCloseConnection(true);
				
				return response;
			}
		}
		
		return null;
	}
}