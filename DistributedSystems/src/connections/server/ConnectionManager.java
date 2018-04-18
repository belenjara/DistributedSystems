package connections.server;

import java.util.ArrayList;
import java.util.List;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;

public class ConnectionManager {

	private static ConnectionManager instance;
		
	private ConnectionManager() {
	}
	
	public static synchronized ConnectionManager getInstance() {
		if (instance == null) {
			instance = new ConnectionManager();
		}
		
		return instance;
	}
	
	public synchronized void broadcastServers(String msg, Connection senderConn) {
		// Broadcast to all connected servers.
		List<Connection> connServers = Control.getInstance().getConnections();
		for(Connection sc : connServers) {			
			if (sc.getType() == Connection.TYPE_SERVER && sc.getAuth()) {
				Boolean isSender = (senderConn != null && sc.equals(senderConn));
				//// We don't want to send to the original sender
				if (!isSender) {		
					sc.writeMsg(msg);
				}	
			}
		}
	}
	
	public synchronized void broadcastAll(String msg, Connection senderConn) {
		// Broadcast to all connected servers & clients.
		List<Connection> connections = Control.getInstance().getConnections();
		for(Connection c : connections) {			
			if (c.getAuth()) {
				Boolean isSender = (senderConn != null && c.equals(senderConn));
				//// We don't want to send to the original sender
				if (!isSender) {		
					c.writeMsg(msg);
				}	
			}
		}
	}
	
	public Boolean serverIsAuthenticated(Connection conn) {
		return conn.getAuth();
	}
	
	public int getNumberClients(){		
		List<Connection> connections = Control.getInstance().getConnections();
		int countClients = 0;
		
		for(Connection c : connections) {
			if (c.getType() == Connection.TYPE_CLIENT && c.getAuth()) {
				countClients++;
			}
		}
		
		return countClients;	
	}
}
