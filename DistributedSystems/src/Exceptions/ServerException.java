package Exceptions;

public class ServerException extends Exception {
	public String code;

	 public ServerException() { super(); }
	 
	  public ServerException(String code, String message) { 
		  super(message); 
		  this.code = code;
	  }
}
