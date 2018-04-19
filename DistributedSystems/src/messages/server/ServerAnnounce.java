package messages.server;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Response;
import activitystreamer.util.Settings;
import connections.server.AnnouncedServer;

public class ServerAnnounce {
	
    public static final String ID = "TAIPY-SERVER-1234";

			
	public Boolean sendServerAnnounce() {
		Boolean closeConn = false;
		try {
			Message msg = new Message();
			msg.setCommand(Message.SERVER_ANNOUNCE);
			msg.setId(ID);
			msg.setHostname(Settings.getLocalHostname());
			msg.setPort(Settings.getLocalPort());
			
			Control connMan = Control.getInstance();
			
			int load = connMan.getNumberClientsConnected();		
			msg.setLoad(load);
			
			connMan.broadcastServers(msg.toString(), null);	
		}
		catch(Exception e) {
			closeConn =true;
		}
		
		return closeConn;
	}
	
	public Response receiveServerAnnounce(Message message, Connection conn) {	
		Response response = new Response();
		Control connMan = Control.getInstance();
		Boolean isAuth = connMan.serverIsAuthenticated(conn);
		
		if (!isAuth) {
			Message msg = new Message();
			msg.setCommand(Message.INVALID_MESSAGE);
			 msg.setInfo(Message.ERROR_AUTH_INFO);
			response.setCloseConnection(true);
			response.setMessage(msg.toString());
			return response;
		}
		
		Response valid = validateMessage(message);
		if (valid.getCloseConnection()) {
			Message msg = new Message();
			msg.setCommand(Message.INVALID_MESSAGE);
			msg.setInfo(valid.getMessage());
			response.setCloseConnection(true);
			response.setMessage(msg.toString());
			return response;
		}

		AnnouncedServer aserver = new AnnouncedServer(message);
		Control.addAnnouncedServers(aserver);
			
		connMan.broadcastServers(message.toString(), conn);	
		
		return response;
	}
	
	private Response validateMessage(Message msg) {
		Response response = new Response();
		response.setCloseConnection(false);
		
		if (msg.getId() == null) {
			response.setCloseConnection(true);
			response.setMessage(String.format(Message.ERROR_PROPERTIES_INFO, "id"));
		}
		
		if(msg.getPort() <= 0) {
			response.setCloseConnection(true);
			response.setMessage(String.format(Message.ERROR_PROPERTIES_INFO, "port"));
		}
		
		if (msg.getHostname() == null) {
			response.setCloseConnection(true);
			response.setMessage(String.format(Message.ERROR_PROPERTIES_INFO, "hostname"));
		}
		
		if (msg.getLoad() == null) {
			response.setCloseConnection(true);
			response.setMessage(String.format(Message.ERROR_PROPERTIES_INFO, "load"));
		}
		
		return response;
	}
}
