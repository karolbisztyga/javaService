package bb.service.sessionstorage;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserSessionStorage implements Serializable {
	
	public static final String STORAGE_TITLE = "user";
	
	private String name;
	private String ip;
	
	public UserSessionStorage(String name, String ip) {
		this.name = name;
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
}
