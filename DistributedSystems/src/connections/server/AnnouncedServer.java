package connections.server;
import messages.server.Message;

public class AnnouncedServer {
	private String serverId;
    private String hostname;
    private int port;
    private int load;
    
   
    public AnnouncedServer(Message msg) {
    	this.setServerId(msg.getId());
    	this.setHostname(msg.getHostname());
    	this.setPort(msg.getPort());
    	this.setLoad(msg.getLoad());
	 }
    
    public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getLoad() {
		return load;
	}

	public void setLoad(int load) {
		this.load = load;
	}	

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
