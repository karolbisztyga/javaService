package bb.service.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.database.managers.UserManager;
import bb.service.exceptions.UserDataException;
import bb.service.sessionstorage.StatusSessionStorage;

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
			String userName = request.getParameter("userName");
			boolean muted = request.getParameter("muted").equals("true");
			boolean res = UserManager.getInstance().setMute(userName, !muted, request);
			String mutedText = (muted) ? "unmuted" : "muted" ;
			if(res) {
				request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{
						"user " + userName + " successfuly " + mutedText
					}));
			}
		} catch(UserDataException e) {
			request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{e.getMessage()}));
		} finally {
			response.sendRedirect(Home.getPathPrefix() + "findUser");
		}
		
	}

}
