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

@WebServlet(urlPatterns={"/muteUser"},
initParams = {
	@WebInitParam(name="security",value="user")
})
public class MuteUser extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String mutingName = ((UserSessionStorage)request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE)).getName();
			String mutedName = request.getParameter("userName");
			boolean muted = request.getParameter("muted").equals("true");
			boolean res = UserManager.getInstance().setMute(mutingName, mutedName, !muted);
			String mutedText = (muted) ? "unmuted" : "muted" ;
			if(res) {
				request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{
						"user " + mutedName + " successfuly " + mutedText
					}));
			}
		} catch(UserDataException | NoSuchUserException e) {
			request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{e.getMessage()}));
		} finally {
			response.sendRedirect(Home.getPathPrefix() + "findUser");
		}
		
	}

}
