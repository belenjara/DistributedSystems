package messages.server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
	
	public Response Lock_request(Connection conn,String user, String secret) {
		Response lockrep = new Response();
		Message messageResp = new Message();

		
		int serversConnected = Control.getInstance().getNumberServersConnected();
		
		// if this server is connected to other servers, we do the lock_request
		if (serversConnected > 0) {
			messageResp.setCommand(Message.LOCK_REQUEST);
			messageResp.setUsername(user);
			messageResp.setSecret(secret);
			LockRequestInfo lockInfo = new LockRequestInfo();
			lockInfo.setUsername(user);
			lockInfo.setServerResponses(0);
			lockInfo.setServersNumber(serversConnected);
			lockInfo.setClientConnection(conn);
			Control.getInstance().setLockInfolist(lockInfo);			
			Control.getInstance().broadcastServers(messageResp.toString(), conn);
		}else {
			// if this server is not connected to other servers, we register the client.
			messageResp.setCommand(Message.REGISTER_SUCCESS);
			messageResp.setInfo(String.format(Message.REGISTER_SUCCESS_INFO, this.username));
		}
				
		return lockrep;
	}
	
	public Response receiveLock_allowed(Message message) {
		
		List<LockRequestInfo> lockList = Control.getInstance().getLockInfolist();
		
		Response response = new Response();
		response.setCloseConnection(false);
		
		// TODO: verify the username and secret.
		for(LockRequestInfo log : lockList) {
			if(log.getUsername().equals(this.username)) {
				int respNum = log.getServerResponses() + 1;
				log.setServerResponses(respNum);
				
				if (respNum == log.getServersNumber()) {
					Message messageResp = new Message();
					messageResp.setCommand(Message.REGISTER_SUCCESS);
					messageResp.setInfo(String.format(Message.REGISTER_SUCCESS_INFO, this.username));
					
					log.getClientConnection().writeMsg(messageResp.toString());
					//response.setMessage(messageResp.toString());
					//response.setCloseConnection(false);
				}
				break;
			}
		}
			
		return response;
	}
	
	public Response receiveLockDenied(Message message) {
		List<LockRequestInfo> lockList = Control.getInstance().getLockInfolist();
		
		Response response = new Response();
		response.setCloseConnection(false);
		
		LockRequestInfo logreq = null;
		// TODO: verify the username and secret.
		for(LockRequestInfo log : lockList) {
			if(log.getUsername().equals(this.username)) {
				Message messageResp = new Message();
				messageResp.setCommand(Message.REGISTER_FAILED);
				messageResp.setInfo(String.format(Message.REGISTER_FAILED_INFO, this.username));	
				log.getClientConnection().writeMsg(messageResp.toString());
				response.setCloseConnection(true);
				logreq = log;
				break;
			}
		}
		
		if (logreq != null && lockList.contains(logreq)) {
			lockList.remove(logreq);
		}
		
		return response;
	}
	
	public Response receiveLockRequest(Connection conn,Message message) {
		
		List<RegisteredClient> registeredClients = Control.getInstance().getRegisteredClients();
		
		Response response = new Response();
		response.setCloseConnection(false);
		
		if (!conn.getAuth()) {
			// server not authenticated
			Message messageResp = new Message();
			messageResp.setCommand(Message.INVALID_MESSAGE);
			messageResp.setInfo(Message.ERROR_COMMAND_INFO);
			response.setMessage(messageResp.toString());
			response.setCloseConnection(true);
			return response;
		} else if(this.username == null || this.username == "") {
			Message messageResp = new Message();
			messageResp.setCommand(Message.INVALID_MESSAGE);
			messageResp.setInfo(Message.ERROR_COMMAND_INFO);
			response.setMessage(messageResp.toString());
			response.setCloseConnection(true);
			return response;
			// TODO: check message
		}
		
		
		
		boolean clientExists = false;
		for (RegisteredClient c : registeredClients) {
			if (c.getUsername() == this.username) {
				clientExists = true;
				//if (c.getSecret() != this.secret) {
					Message messageResp = new Message();
					messageResp.setCommand(Message.LOCK_DENIED);
					messageResp.setUsername(this.username);
					messageResp.setSecret(this.secret);
					response.setMessage(messageResp.toString());
					return response;
					
				//}				 
			}
		}
		
		if (!clientExists) {
			RegisteredClient client = new RegisteredClient();		
			client.setUsername(this.username);
			client.setSecret(this.secret);
			Control.getInstance().addRegisteredClients(client);
			
			Message messageResp = new Message();
			messageResp.setCommand(Message.LOCK_ALLOWED);
			messageResp.setUsername(this.username);
			messageResp.setSecret(this.secret);
			response.setMessage(messageResp.toString());
			
			// allowed
		}	
		return response;
	}
	
}
