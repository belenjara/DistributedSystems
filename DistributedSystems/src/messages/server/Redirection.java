package messages.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

import activitystreamer.server.Control;
import activitystreamer.util.Response;
import connections.server.AnnouncedServer;

public class Redirection {
	
	private static final Logger log = LogManager.getLogger();
	
	public Response redirect() {
		Control control = Control.getInstance();
		int clientsNum = control.getNumberClientsConnected();	
		List<AnnouncedServer> servers = control.getAnnouncedServers();
		
		for(AnnouncedServer s : servers) {
			//REDIRECT message if the server knows of any other server with a load at least 2 clients less than its own.
			if (clientsNum - s.getLoad() >= 2) {
				Message msg = new Message();
				msg.setCommand(Message.REDIRECT);
				msg.setHostname(s.getHostname());
				msg.setPort(s.getPort());
				
				String msgStr = msg.toString();
				
				log.info("Sending redirection msg: " + msgStr);
				
				Response response = new Response();
				response.setMessage(msgStr);
				response.setCloseConnection(true);
				
				return response;
			}
		}
		
		return null;
	}
}