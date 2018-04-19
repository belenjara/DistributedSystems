package messages.server;

import activitystreamer.server.Connection;
import activitystreamer.util.Settings;

public class Authentication {

	//// TODO: receive authentication
	
	
	/**
	 * Sent from one server to another always and only as the first message when connecting.
	 * @param conn
	 */
	public void doAuthentication(Connection conn) {
		Message msg = new Message();
		msg.setCommand(Message.AUTHENTICATE);
		msg.setSecret(Settings.getSecret());
		conn.writeMsg(msg.toString());
	}
	
}