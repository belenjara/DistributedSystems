package connections.server;

import activitystreamer.server.Connection;

public class LockRequestInfo {
	private String username;
	private int serversNumber;
	private int serverResponses;
	private Connection clientConnection;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getServersNumber() {
		return serversNumber;
	}
	public void setServersNumber(int serversNumber) {
		this.serversNumber = serversNumber;
	}
	public int getServerResponses() {
		return serverResponses;
	}
	public void setServerResponses(int serverResponses) {
		this.serverResponses = serverResponses;
	}
	public Connection getClientConnection() {
		return clientConnection;
	}
	public void setClientConnection(Connection clientConnection) {
		this.clientConnection = clientConnection;
	}
}
