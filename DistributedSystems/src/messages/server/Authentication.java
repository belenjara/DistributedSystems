package messages.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Response;
import activitystreamer.util.Settings;

public class Authentication {

	private static final Logger log = LogManager.getLogger();

	/**
	 * When this server receives an authentication from other server.
	 * @param conn
	 * @param msg
	 * @return
	 */
	public Response processAuthentication(Connection conn,Message msg) {
		Response response = new Response();
		Control connMan = Control.getInstance();
        Boolean isAuth = connMan.serverIsAuthenticated(conn);
		//check if the server had already successfully authenticated
		if (isAuth) {
			msg.setCommand(Message.INVALID_MESSAGE);
			msg.setInfo(Message.ERROR_AUTH_INFO2);
			response.setCloseConnection(true);
			response.setMessage(msg.toString());
			return response;
		}
		// check the secret
		if (!msg.getSecret().equals(Settings.getSecret())) {
			msg.setCommand(Message.AUTHENTICATION_FAIL);
			msg.setInfo(String.format(Message.AUTHENTICATION_FAIL_INFO, msg.getSecret() ));
			response.setCloseConnection(true);
			response.setMessage(msg.toString());
			return response;
		}
		// do nothing if the authentication succeeded.
		conn.setAuth(true);
		response.setMessage(null);
		response.setCloseConnection(false);
		return response;
	}
	
	/**
	 * Sent from one server to another always and only as the first message when connecting.
	 * @param conn
	 */
	public void doAuthentication(Connection conn) {
		Message msg = new Message();
		msg.setCommand(Message.AUTHENTICATE);
		msg.setSecret(Settings.getSecret());
		
		String msgStr = msg.toString();
		log.info("Sending authentication msg: " + msgStr);
		conn.writeMsg(msgStr);
	}
	
}