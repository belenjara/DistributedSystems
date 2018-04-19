package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import messages.server.Authentication;
import messages.server.MessageProcessing;
import messages.server.ServerAnnounce;
import activitystreamer.util.Response;
import activitystreamer.util.Settings;
import connections.server.AnnouncedServer;
import connections.server.RegisteredClient;

public class Control extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ArrayList<Connection> connections;
	
	private static ArrayList<AnnouncedServer> announcedServers;
	
	private static ArrayList<RegisteredClient> registeredClients;
	
	//// TODO: add logged clients list...
	
	private static boolean term=false;
	private static Listener listener;
	
	protected static Control control = null;
	
	public static Control getInstance() {
		if(control==null){
			control=new Control();
		} 
		return control;
	}
	
	public Control() {
		// initialize the connections array
		connections = new ArrayList<Connection>();		
		// start a listener
		try {
			listener = new Listener();
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: "+e1);
			System.exit(-1);
		}
		
		// Initialize the registered clients list.
		registeredClients = new ArrayList<RegisteredClient>();
		
		// Initialize the announced servers list.
		announcedServers = new ArrayList<AnnouncedServer>();
		
		//// here or in run method??
		initiateConnection();
	}
	
	/*
	 * Make a connection to another server if remote hostname is supplied.
	 */
	public void initiateConnection(){
		// make a connection to another server if remote hostname is supplied
		if(Settings.getRemoteHostname()!=null){
			try {
				Connection conn = outgoingConnection(new Socket(Settings.getRemoteHostname(),Settings.getRemotePort()));
				
				//// Authentication to other server.
				Authentication auth = new Authentication();
				auth.doAuthentication(conn);
				
				if (conn.isOpen() && connections.contains(conn)) {
					//int i = connections.indexOf(conn);
					//// The connection is updated, type server is specified and that is authenticated.
					conn.setType(Connection.TYPE_SERVER);
					conn.setAuth(true);
					//connections.set(i, conn);
				}
			} catch (IOException e) {
				log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
				System.exit(-1);
			}
		}
	}
	
	/*
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con,String msg){
		log.info("I received a msg from the client: " + msg);
		
		//// Process the message according to its command. A list of responses is returned.
		List<Response> responses = new MessageProcessing().processMsg(con, msg);
		
		//// Each response message is send back to the client (or server).
		for(Response response : responses) {
			if (response.getMessage() != null && !response.getMessage().equals("")) {
				//// Write the response to the client (or server).
				con.writeMsg(response.getMessage());
				
				//// If is necessary to close the connection.
				if (response.getCloseConnection()) {
					return true;
				}
			}
		}
		
		//// If is not necessary to close the connection for now.
		return false;
	}
	
	/*
	 * The connection has been closed by the other party.
	 */
	public synchronized void connectionClosed(Connection con){
		if(!term) connections.remove(con);
	}
	
	/*
	 * A new incoming connection has been established, and a reference is returned to it
	 */
	public synchronized Connection incomingConnection(Socket s) throws IOException{
		log.info("incomming connection: "+Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
		return c;
		
	}
	
	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized Connection outgoingConnection(Socket s) throws IOException{
		log.debug("outgoing connection: "+Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
		return c;
		
	}
	
	@Override
	public void run(){
		log.info("using activity interval of "+Settings.getActivityInterval()+" milliseconds");
		while(!term){
			// do something with 5 second intervals in between
			try {
				Thread.sleep(Settings.getActivityInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
			if(!term){
				log.debug("doing activity");
			//// Server announce every 5 seconds.
				term=doActivity();
			}
			
		}
		log.info("closing "+connections.size()+" connections");
		// clean up
		for(Connection connection : connections){
			connection.closeCon();	
		}
		listener.setTerm(true);
	}
	
	public boolean doActivity(){
		//// Server announce every 5 seconds.
		return new ServerAnnounce().sendServerAnnounce();
	}
	
	public final void setTerm(boolean t){
		term=t;
	}
	
	public final ArrayList<Connection> getConnections() {
		return connections;
	}

	
	/**
	 *  * New methods *  
	 */
	
	/**
	 * Return the list of the servers that were announced.
	 */
	public static ArrayList<AnnouncedServer> getAnnouncedServers() {
		return announcedServers;
	}

	/**
	 * Add a new server to the list of announced servers, if the server already is in the list, is updated.
	 * @param announcedServer
	 */
	public static void addAnnouncedServers(AnnouncedServer announcedServer) {	
		if (!Control.announcedServers.contains(announcedServer)) {
			Control.announcedServers.add(announcedServer);
		}
		else {		
			Control.announcedServers.set(Control.announcedServers.indexOf(announcedServer), announcedServer);		
		}
	}

	/**
	 * Get the list of clients that are registered.
	 * @return LIst of registered clients.
	 */
	public static ArrayList<RegisteredClient> getRegisteredClients() {
		return registeredClients;
	}

	/**
	 * Add a registered client to the list.
	 * @param registeredClient
	 */
	public static void addRegisteredClients(RegisteredClient registeredClient) {
		Control.registeredClients.add(registeredClient);
	}
	
	/**
	 * Broadcast a message to all servers (only) connected, except the original sender.
	 * @param msg
	 * @param senderConn
	 */
	public synchronized void broadcastServers(String msg, Connection senderConn) {
		// Broadcast to all connected servers.
		List<Connection> connServers = Control.getInstance().getConnections();
		for(Connection sc : connServers) {			
			if (sc.getType() == Connection.TYPE_SERVER && sc.getAuth() && sc.isOpen()) {
				Boolean isSender = (senderConn != null && sc.equals(senderConn));
				//// We don't want to send to the original sender
				if (!isSender) {		
					sc.writeMsg(msg);
				}	
			}
		}
	}
	
	/**
	 * Broadcast a message to all servers and clients connected, except the original sender.
	 * @param msg
	 * @param senderConn
	 */
	public synchronized void broadcastAll(String msg, Connection senderConn) {
		// Broadcast to all connected servers & clients.
		List<Connection> connections = Control.getInstance().getConnections();
		for(Connection c : connections) {			
			if (c.getAuth() && c.isOpen()) {
				Boolean isSender = (senderConn != null && c.equals(senderConn));
				//// We don't want to send to the original sender
				if (!isSender) {		
					c.writeMsg(msg);
				}	
			}
		}
	}
	
	/**
	 * Check if the server is authenticated. The property Auth has to be True to be authenticated, otherwise is not.
	 * @param conn
	 * @return TRUE if is authenticated, otherwise FALSE.
	 */
	public Boolean serverIsAuthenticated(Connection conn) {
		return conn.getAuth();
	}
	
	/**
	 * @return number of clients connected.
	 */
	public int getNumberClientsConnected(){		
		List<Connection> connections = Control.getInstance().getConnections();
		int countClients = 0;
		
		for(Connection c : connections) {
			if (c.getType() == Connection.TYPE_CLIENT && c.getAuth() && c.isOpen()) {
				countClients++;
			}
		}
		
		return countClients;	
	}
}
