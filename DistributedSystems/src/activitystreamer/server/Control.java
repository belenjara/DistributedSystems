package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		
		registeredClients = new ArrayList<RegisteredClient>();
		
		//?
		announcedServers = new ArrayList<AnnouncedServer>();
		initiateConnection();
	}
	
	public void initiateConnection(){
		// make a connection to another server if remote hostname is supplied
		if(Settings.getRemoteHostname()!=null){
			try {
				Connection conn = outgoingConnection(new Socket(Settings.getRemoteHostname(),Settings.getRemotePort()));
								
				//TODO: authentication
				Boolean authOk = true;
				
				if (authOk) {						 
					conn.setType(Connection.TYPE_SERVER);
					conn.setAuth(true);
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
		
		Response response = new MessageProcessing().processMsg(con, msg);
		
		if (response.getMessage() != null && !response.getMessage().equals("")) {
			//// Write the response to the client (or server).
			con.writeMsg(response.getMessage());
		}
			
		return response.getCloseConnection();
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
		return new ServerAnnounce().sendServerAnnounce();
	}
	
	public final void setTerm(boolean t){
		term=t;
	}
	
	public final ArrayList<Connection> getConnections() {
		return connections;
	}

	public static ArrayList<AnnouncedServer> getAnnouncedServers() {
		return announcedServers;
	}

	public static void addAnnouncedServers(AnnouncedServer announcedServer) {
		Control.announcedServers.add(announcedServer);
	}

	public static ArrayList<RegisteredClient> getRegisteredClients() {
		return registeredClients;
	}

	public static void addRegisteredClients(RegisteredClient registeredClient) {
		Control.registeredClients.add(registeredClient);
	}
}
