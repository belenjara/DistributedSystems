package messages.server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
public class Register {

	public static JSONObject registed_client;
	public static JSONArray client_list;
	private Message msg;

 
 		

	public Register (Message message) {
		this.msg = message;
	 
		if (check_client(client_list, message.getUsername()) != false) {
		JSONObject client= new JSONObject();

		client.put("Name", msg.getUsername());
		client.put("Secret", msg.getSecret());
		client_list.add(client);
		registed_client.put("Registed Client",client_list);
		System.out.println(registed_client);
		}
		else {
			System.out.println("The User_name already in the list ");
		}
	
	}
	
	
	public Boolean check_client (JSONArray jsonArray, String usernameToFind) {
		if(jsonArray.toString().contains(usernameToFind)) {
			return false;
		}
		else {
			return true; 
		}
		}
		
		
	
	
}
