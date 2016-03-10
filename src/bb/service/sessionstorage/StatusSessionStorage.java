package bb.service.sessionstorage;

import java.io.Serializable;
import java.util.ArrayDeque;

@SuppressWarnings("serial")
public class StatusSessionStorage implements Serializable  {
	
	public static final String STORAGE_TITLE = "status";
	
	private final ArrayDeque<String> alerts;

	public StatusSessionStorage(ArrayDeque<String> alerts) {
		this.alerts = alerts;
	}
	
	public StatusSessionStorage(String[] alerts) {
		this.alerts = new ArrayDeque<String>();
		for(String alert : alerts) {
			this.alerts.add(alert);
		}
	}

	public ArrayDeque<String> getAlerts() {
		return alerts;
	}
		
}
