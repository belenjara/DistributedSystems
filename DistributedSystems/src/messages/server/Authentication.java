package messages.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.server.Connection;
import activitystreamer.util.Settings;

public class Authentication {

	private static final Logger log = LogManager.getLogger();

	//// TODO: receive authentication
	
	
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