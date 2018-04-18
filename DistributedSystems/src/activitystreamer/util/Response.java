package activitystreamer.util;

public class Response {
	private String message;
	private Boolean closeConnection;
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Boolean getCloseConnection() {
		return closeConnection;
	}
	
	public void setCloseConnection(Boolean closeConnection) {
		this.closeConnection = closeConnection;
	}
}
