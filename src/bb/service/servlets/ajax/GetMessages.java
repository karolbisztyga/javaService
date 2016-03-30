package bb.service.servlets.ajax;

import java.io.IOException;
import java.util.List;

import javax.jws.soap.SOAPBinding.Use;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import bb.service.database.entities.MessageEntity;
import bb.service.database.managers.UserManager;
import bb.service.database.managers.UserManager.MESSAGE_TYPE;
import bb.service.exceptions.CacheException;
import bb.service.exceptions.NoSuchUserException;
import bb.service.exceptions.UserDataException;
import bb.service.sessionstorage.UserSessionStorage;

@WebServlet(urlPatterns={"/getMessages"},
		initParams = {
			@WebInitParam(name="security",value="user")
		})
public class GetMessages extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject resultObject = new JSONObject();
		try {
			String query = request.getParameter("query");
			Integer lastMessageId = (request.getParameter("lastMessageId") != null && !request.getParameter("lastMessageId").toString().equals("")) ? 
					Integer.parseInt(request.getParameter("lastMessageId")) :
					null;
			UserManager.MESSAGE_TYPE type = null;
			if(query.equals("sent")) type = UserManager.MESSAGE_TYPE.SENT;
			if(query.equals("received")) type = UserManager.MESSAGE_TYPE.RECEIVED;
			String userName = ((UserSessionStorage)request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE)).getName();
			List messages = UserManager.getInstance().getMessages(userName, lastMessageId, type);
			JSONObject messagesOb = new JSONObject();
			lastMessageId = -1;
			for(int i=0 ; i<messages.size() ; ++i) {
				MessageEntity message = (MessageEntity)messages.get(i);
				JSONObject messageOb = new JSONObject();
				if(type != MESSAGE_TYPE.SENT) {
					messageOb.append("author", message.getAuthor());
				}
				else if(type != MESSAGE_TYPE.RECEIVED)  {
					messageOb.append("target", message.getTarget().getName());
				}
				messageOb.append("message", message.getMessage());
				messageOb.append("sendDate", message.getSendDate());
				messagesOb.append("message"+i, messageOb);
				if(message.getId() > lastMessageId) lastMessageId = message.getId();
			}
			resultObject.append("result", messagesOb);
			resultObject.append("lastMessageId", lastMessageId);
		} catch(NullPointerException | NoSuchUserException e) {
			e.printStackTrace();
			resultObject = new JSONObject();
			resultObject.append("error", e.getMessage());
		} catch(CacheException e) {
			resultObject.append("result", "cache");
		} finally {
			response.getWriter().write(resultObject.toString());
		}
	}

}
