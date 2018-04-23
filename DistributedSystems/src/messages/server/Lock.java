package messages.server;
import java.util.List;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Response;
import connections.server.LockRequestInfo;
import connections.server.RegisteredClient;
public class Lock {
	private String username;
	private String secret;
	Control conm = Control.getInstance();

	public Lock (String user, String secret) {
		this.username = user;
		this.secret = secret;
	}

	/**
	 * This method is called from the registration when this server does not have the specific client info.
	 * Here we do the LOCK_REQUEST broadcast to other servers, if we have other servers connected, 
	 * otherwise we send a REGISTER_SUCCESS message as a response to the client.
	 * @param conn
	 * @return
	 */
	public Response sendLockRequest(Connection conn) {
		Response lockrep = new Response();
		Message messageResp = new Message();

		lockrep.setCloseConnection(false);
		lockrep.setMessage(null);

		int serversConnected = Control.getInstance().getNumberServersConnected();

		// if this server is connected to other servers, we do the LOCK_REQUEST.
		if (serversConnected > 0) {
			messageResp.setCommand(Message.LOCK_REQUEST);
			messageResp.setUsername(this.username);
			messageResp.setSecret(this.secret);
			LockRequestInfo lockInfo = new LockRequestInfo();
			lockInfo.setUsername(this.username);
			lockInfo.setServerResponses(0);
			lockInfo.setServersNumber(serversConnected);
			lockInfo.setClientConnection(conn);
			Control.getInstance().setLockInfolist(lockInfo);			
			Control.getInstance().broadcastServers(messageResp.toString(), conn);
		}else {
			// if this server is not connected to other servers, we register the client.
			messageResp.setCommand(Message.REGISTER_SUCCESS);
			messageResp.setInfo(String.format(Message.REGISTER_SUCCESS_INFO, this.username));
			lockrep.setMessage(messageResp.toString());
		}

		return lockrep;
	}

	/**
	 * When this server receives a LOCK_ALLOWED message from other server, after sending a lock_request.
	 * @param message
	 * @return
	 */
	public Response receiveLockAllowed(Connection conn, Message message) {	
		Response response = new Response();
		response.setCloseConnection(false);

		// First: validation of the message (format and properties).
		Message responseMsg = Message.CheckMessage(message, Message.USERNAME);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}

		// First: validation of the message (format and properties).
		responseMsg = Message.CheckMessage(message, Message.SECRET);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}
		
		// Then check if the other server is not authenticated.
		if (!conn.getAuth()) {
			// server not authenticated
			Message messageResp = new Message();
			messageResp.setCommand(Message.AUTHENTICATION_FAIL);
			messageResp.setInfo("Server not authenticated.");
			response.setMessage(messageResp.toString());
			response.setCloseConnection(true);
			return response;
		}
		
		// We get the lock request list and we update it to know of all the server allowed  
		// the registration of the client.
		List<LockRequestInfo> lockList = Control.getInstance().getLockInfolist();
		for(LockRequestInfo log : lockList) {
			if(log.getUsername().equals(this.username)) {
				int respNum = log.getServerResponses() + 1;
				log.setServerResponses(respNum);

				// This means that all the servers responded 'LOCK_ALLOWED'.
				if (respNum == log.getServersNumber()) {
					Message messageResp = new Message();
					messageResp.setCommand(Message.REGISTER_SUCCESS);
					messageResp.setInfo(String.format(Message.REGISTER_SUCCESS_INFO, this.username));

					// We respond back to the client REGISTER_SUCCESS
					log.getClientConnection().writeMsg(messageResp.toString());
				}
				break;
			}
		}
		
		return response;
	}

	/**
	 * When this server receives a LOCK_DENIED message from other server, after sending a lock_request.
	 * @param message
	 * @return
	 */
	public Response receiveLockDenied(Connection conn, Message message) {
		Response response = new Response();
		response.setCloseConnection(false);

		LockRequestInfo logreq = null;

		// First: validation of the message (format and properties).
		Message responseMsg = Message.CheckMessage(message, Message.USERNAME);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}

		// First: validation of the message (format and properties).
		responseMsg = Message.CheckMessage(message, Message.SECRET);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}	

		// Then check if the other server is not authenticated.
		if (!conn.getAuth()) {
			// server not authenticated
			Message messageResp = new Message();
			messageResp.setCommand(Message.AUTHENTICATION_FAIL);
			messageResp.setInfo("Server not authenticated.");
			response.setMessage(messageResp.toString());
			response.setCloseConnection(true);
			return response;
		}

		// We obtain the lock request list.
		List<LockRequestInfo> lockList = Control.getInstance().getLockInfolist();
		for(LockRequestInfo log : lockList) {
			if(log.getUsername().equals(this.username)) {
				// When we find the lock request for the client, we send a REGISTER_FAILED response to its connection.
				Message messageResp = new Message();
				messageResp.setCommand(Message.REGISTER_FAILED);
				messageResp.setInfo(String.format(Message.REGISTER_FAILED_INFO, this.username));	
				log.getClientConnection().writeMsg(messageResp.toString());
				// we need to close client's connection.
				log.getClientConnection().closeCon();
				// Just save this object from the lock request lst to then remove it.
				logreq = log;
				break;
			}
		}
	
		// We don't need to keep the lock info anymore..
		if (logreq != null && lockList.contains(logreq)) {
			lockList.remove(logreq);
		}
		
		// In the PDF says:
		//When a server receives this message, it will remove the username from its local storage only if the
		//secret matches the associated secret in its local storage.
		boolean removeClient = false;
		RegisteredClient client = null;
		List<RegisteredClient> registeredClients = Control.getInstance().getRegisteredClients();
		for (RegisteredClient c : registeredClients) {
			if (c.getUsername().equals(this.username) && c.getSecret().equals(this.secret)) {				
				removeClient = true;
				client = c;
			}
		}
		
		if (removeClient && client != null) {
			registeredClients.remove(client);
		}
		
		return response;
	}
	
	/**
	 * When this server receives a lock request from other server.
	 * We can respond a LOCK_ALLOWED or LOCK_DENIED message.
	 * @param conn
	 * @param message
	 * @return
	 */
	public Response receiveLockRequest(Connection conn,Message message) {	
		Response response = new Response();
		response.setCloseConnection(false);

		// First: validation of the message (format and properties).
		Message responseMsg = Message.CheckMessage(message, Message.USERNAME);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}

		// First: validation of the message (format and properties).
		responseMsg = Message.CheckMessage(message, Message.SECRET);	
		if (responseMsg.getCommand().equals(Message.INVALID_MESSAGE)) {
			response.setCloseConnection(true);
			response.setMessage(responseMsg.toString());
			return response;
		}	

		// Then check if the other server is not authenticated.
		if (!conn.getAuth()) {
			// server not authenticated
			Message messageResp = new Message();
			messageResp.setCommand(Message.AUTHENTICATION_FAIL);
			messageResp.setInfo("Server not authenticated.");
			response.setMessage(messageResp.toString());
			response.setCloseConnection(true);
			return response;
		}

		// We search in our registered clients list if the client exists with the username and secret.
		boolean clientExists = false;
		List<RegisteredClient> registeredClients = Control.getInstance().getRegisteredClients();
		for (RegisteredClient c : registeredClients) {		
			if (c.getUsername().equals(this.username)) {
				clientExists = true;
				//From PDF: Broadcast a LOCK_DENIED to all other servers (between servers only) 
				//if the username is already known to the server with a different secret.
				if (!c.getSecret().equals(this.secret)) {
					Message messageResp = new Message();
					messageResp.setCommand(Message.LOCK_DENIED);
					messageResp.setUsername(this.username);
					messageResp.setSecret(this.secret);
					response.setMessage(messageResp.toString());
					return response;
				}
			}
		}

		// From PDF:
		// Broadcast a LOCK_ALLOWED to all other servers (between servers only) if the username is not
		//already known to the server. The server will record this username and secret pair in its local
		//storage.
		if (!clientExists) {
			RegisteredClient client = new RegisteredClient();		
			client.setUsername(this.username);
			client.setSecret(this.secret);
			Control.getInstance().addRegisteredClients(client);
		}	
		
		Message messageResp = new Message();
		messageResp.setCommand(Message.LOCK_ALLOWED);
		messageResp.setUsername(this.username);
		messageResp.setSecret(this.secret);
		response.setMessage(messageResp.toString());
		return response;
	}
}
