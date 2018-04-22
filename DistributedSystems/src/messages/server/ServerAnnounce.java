package messages.server;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Response;
import activitystreamer.util.Settings;
import connections.server.AnnouncedServer;

public class ServerAnnounce {
	//private static final Logger log = LogManager.getLogger();
	
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
			
			String msgStr = msg.toString();
						
			connMan.broadcastServers(msgStr, null);	
		}
		catch(Exception e) {
			closeConn =true;
		}
		
		return closeConn;
	}
	
	public Response receiveServerAnnounce(Message message, Connection conn) {	
		Response response = new Response();
		response.setCloseConnection(false);
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
			return valid;
		}

		AnnouncedServer aserver = new AnnouncedServer(message);
		Control.addAnnouncedServers(aserver);
			
		connMan.broadcastServers(message.toString(), conn);	
		
		return response;
	}
	
	private Response validateMessage(Message msg) {
		Response response = new Response();
		response.setCloseConnection(false);
		
		Message responseMsg = Message.CheckMessage(msg, Message.ID_SERVER);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}
		
		responseMsg = Message.CheckMessage(msg, Message.PORT);
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}
		
		responseMsg = Message.CheckMessage(msg, Message.LOAD);
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}	
		
		responseMsg = Message.CheckMessage(msg, Message.HOSTNAME);
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}	
		
		return response;
	}
}
