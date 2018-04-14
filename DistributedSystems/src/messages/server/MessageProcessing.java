package messages.server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageProcessing {

	public void ProcessMsg(String msg) {
		JSONParser parser = new JSONParser();
		JSONObject jsonMsg = null;
		try {
			jsonMsg = (JSONObject) parser.parse(msg);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// INVALID MSG
		}
		
		String command = (String)jsonMsg.get("command");
		
		switch(command) {
		case "LOGIN":
			//DO something

			break;
			// add more commands
		
		}
		
	}
}
