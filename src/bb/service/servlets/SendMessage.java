package bb.service.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.database.managers.UserManager;
import bb.service.exceptions.NoSuchUserException;
import bb.service.exceptions.UserDataException;
import bb.service.sessionstorage.StatusSessionStorage;
import bb.service.sessionstorage.UserSessionStorage;

@WebServlet(urlPatterns={"/sendMessage"},
initParams = {
	@WebInitParam(name="security",value="user")
})
public class SendMessage extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String result = null;
		try {
			String currentUserName = ((UserSessionStorage)request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE)).getName();
			String targetName = request.getParameter("target");
			String message = request.getParameter("message");
			UserManager.getInstance().sendMessage(currentUserName, targetName, message);
			result = "message sent";
		} catch(UserDataException | NoSuchUserException e) {
			result = e.getMessage();
		} finally {
			request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{result}));
			response.sendRedirect(Home.getPathPrefix() + "findUser");
		}
	}

}
