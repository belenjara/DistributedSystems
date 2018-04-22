package messages.server;

import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Message {
	private String command;
	private String info;
	private String username;
	private String secret;
	private String hostname;
	private String id;
	private int port;
	private Integer load;
	private HashMap<String, Object> activity;

	public static final String COMMAND = "command";
	public static final String INFO = "info";
	public static final String USERNAME = "username";
	public static final String SECRET = "secret";
	public static final String HOSTNAME = "hostname";
	public static final String PORT = "port";
	public static final String ACTIVITY = "activity";	
	public static final String ID_SERVER = "id";
	public static final String LOAD = "load";

	public static final String INVALID_MESSAGE = "INVALID_MESSAGE";
	public static final String ERROR_JSON_INFO = "JSON parse error while parsing message";
	public static final String ERROR_COMMAND_INFO = "Unknown command received";
	public static final String ERROR_AUTH_INFO = "Server not authenticated";
	
	public static final String ERROR_AUTH_INFO2 = "Server already authenticated";

	public static final String ERROR_PROPERTIES_INFO = "the received message did not contain %s";

	public static final String REGISTER = "REGISTER";
	public static final String REGISTER_SUCCESS = "REGISTER_SUCCESS";
	public static final String REGISTER_FAILED = "REGISTER_FAILED";
	public static final String REGISTER_FAILED_INFO = "%s is already registered with the system";
	public static final String REGISTER_SUCCESS_INFO = "register success for %s";

	public static final String LOGIN = "LOGIN";
	public static final String LOGOUT = "LOGOUT";
	public static final String LOGIN_FAILED = "LOGIN_FAILED";
	public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
	public static final String LOGIN_FAILED_INFO = "attempt to login with wrong secret";
	public static final String LOGIN_SUCCESS_INFO = "logged in as user %s";

	public static final String REDIRECT = "REDIRECT";

	public static final String ACTIVITY_MESSAGE = "ACTIVITY_MESSAGE";

	public static final String SERVER_ANNOUNCE = "SERVER_ANNOUNCE";

	public static final String ACTIVITY_BROADCAST = "ACTIVITY_BROADCAST";

	public static final String AUTHENTICATE = "AUTHENTICATE";
	public static final String AUTHENTICATION_FAIL = "AUTHENTICATION_FAIL";
	public static final String AUTHENTICATION_FAIL_INFO = "the supplied secret is incorrect: %s";

	public static final String LOCK_REQUEST = "LOCK_REQUEST";
	public static final String LOCK_DENIED = "LOCK_DENIED";
	public static final String LOCK_ALLOWED = "LOCK_ALLOWED";


	public Message() {
	}

	public Message(String msg) {
		this.prepareMessage(msg);
	}	

	public String getInvalidMessage() {
		this.command = INVALID_MESSAGE;
		this.info = ERROR_COMMAND_INFO;

		return this.toString();
	}
	
	public static Message CheckMessage(Message msg, String property) {
		JSONParser parser = new JSONParser();
		JSONObject jsonMsg = null;
		Message message = new Message();
		try {
			jsonMsg = (JSONObject) parser.parse(msg.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// INVALID MSG
			message.setCommand(INVALID_MESSAGE);
			message.setInfo(ERROR_JSON_INFO);
			return message;
		}
		
		if (!jsonMsg.containsKey(property) || jsonMsg.get(property) == null || jsonMsg.get(property).equals("")){
			message.setCommand(INVALID_MESSAGE);
			message.setInfo(String.format(ERROR_PROPERTIES_INFO, property));
			return message;
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject jsonMsg = new JSONObject();

		if (this.activity != null && !this.activity.isEmpty()) {
			JSONObject jsonAct = new JSONObject();

			for(String k : this.activity.keySet()) {
				jsonAct.put(k, this.activity.get(k));
			}

			jsonMsg.put(ACTIVITY, jsonAct);
		}

		if (this.port > 0) {
			jsonMsg.put(PORT, this.port);
		}

		if (this.hostname != null && !this.hostname.equals("")) {
			jsonMsg.put(HOSTNAME, this.hostname);
		}

		if (this.secret != null && !this.secret.equals("")) {
			jsonMsg.put(SECRET, this.secret);
		}

		if (this.username != null && !this.command.equals("")) {
			jsonMsg.put(USERNAME, this.username);
		}

		if (this.info != null && !this.info.equals("")) {
			jsonMsg.put(INFO, this.info);
		}

		if (this.command != null && !this.command.equals("")) {
			jsonMsg.put(COMMAND, this.command);
		}

		if (this.id != null && !this.id.equals("")) {
			jsonMsg.put(ID_SERVER, this.id);
		}

		if (this.load != null) {
			jsonMsg.put(LOAD, this.load);
		}

		////TODO: see if we can return null in some cases...

		return jsonMsg.toJSONString();
	}	

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
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

	public HashMap<String, Object> getActivity() {
		return activity;
	}

	public void setActivity(HashMap<String, Object> activity) {
		this.activity = activity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getLoad() {
		return load;
	}

	public void setLoad(Integer load) {
		this.load = load;
	}

	private void prepareMessage(String msg) {
		JSONParser parser = new JSONParser();
		JSONObject jsonMsg = null;
		try {
			jsonMsg = (JSONObject) parser.parse(msg);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// INVALID MSG
			this.setCommand(INVALID_MESSAGE);
			this.setInfo(ERROR_JSON_INFO);
			return;
		}

		if (jsonMsg.containsKey(COMMAND)) {
			this.command = jsonMsg.get(COMMAND).toString();
		}

		if (jsonMsg.containsKey(ID_SERVER)) {
			this.id = jsonMsg.get(ID_SERVER).toString();
		}

		if (jsonMsg.containsKey(INFO)) {
			this.info = jsonMsg.get(INFO).toString();
		}

		if (jsonMsg.containsKey(USERNAME)) {
			this.username = jsonMsg.get(USERNAME).toString();
		}

		if (jsonMsg.containsKey(SECRET)) {
			this.secret = jsonMsg.get(SECRET).toString();
		}

		if (jsonMsg.containsKey(HOSTNAME)) {
			this.hostname = jsonMsg.get(HOSTNAME).toString();
		}

		if (jsonMsg.containsKey(PORT)) {
			this.port =(int)jsonMsg.get(PORT);
		}

		if (jsonMsg.containsKey(LOAD)) {
			this.load =(Integer)jsonMsg.get(LOAD);
		}

		if (jsonMsg.containsKey(ACTIVITY)) {
			JSONObject jsonAct = null;
			try {
				jsonAct = (JSONObject)jsonMsg.get(ACTIVITY);

				for (Object key : jsonAct.keySet()) {
					String keyStr = (String)key;
					Object keyvalue = jsonAct.get(keyStr);

					this.activity.put(keyStr, keyvalue);
				}	
			}catch (Exception e) {
				// INVALID MSG
				this.setCommand(INVALID_MESSAGE);
				this.setInfo(ERROR_JSON_INFO);
				return;
			}
		}	
	}
}
