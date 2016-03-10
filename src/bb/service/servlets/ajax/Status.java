package bb.service.servlets.ajax;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import bb.service.sessionstorage.StatusSessionStorage;
import bb.service.sessionstorage.UserSessionStorage;

@WebServlet("/status")
public class Status extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		JSONObject result = new JSONObject();
		HttpSession session = request.getSession();
		Object preventNullPointer;
		//user
		preventNullPointer = session.getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(preventNullPointer != null) {
			UserSessionStorage user = (UserSessionStorage)preventNullPointer;
			result.append("userName", user.getName());
		}
		//status
		preventNullPointer = session.getAttribute(StatusSessionStorage.STORAGE_TITLE);
		if(preventNullPointer != null) {
			StatusSessionStorage status = (StatusSessionStorage)preventNullPointer;
			ArrayDeque<String> alerts = status.getAlerts();
			if(!alerts.isEmpty()) {
				Iterator<String> it = alerts.iterator();
				JSONObject alertsOb = new JSONObject();
				int i=0;
				while(it.hasNext()) {
					String alert = it.next();
					alertsOb.append(""+i, alert);
				}
				result.append("alerts", alertsOb.toString());
			}
			session.removeAttribute(StatusSessionStorage.STORAGE_TITLE);
		}
		//
        response.getWriter().print(result.toString());
	}

}
