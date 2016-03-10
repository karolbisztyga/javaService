package bb.service.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.database.managers.UserManager;
import bb.service.sessionstorage.StatusSessionStorage;

@WebServlet(urlPatterns = {"/login"},
		initParams = {
			@WebInitParam(name="security",value="stranger"),
			@WebInitParam(name="view",value="login")
		})
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		String 	redirectTo = "",
				message = "";
		if(UserManager.getInstance().login(name, password, request.getRemoteAddr(), request.getSession())) {
			message = "logged in successfully";
			try {
				String queuedRedirect = request.getSession().getAttribute("queuedRedirect").toString();
				request.getSession().removeAttribute("queuedRedirect");
				redirectTo = queuedRedirect;
			} catch(NullPointerException e) {
				redirectTo = Home.getPathPrefix();
			}
		} else {
			message = "not logged in";
			redirectTo = Home.getPathPrefix() + "login";
		}
		request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{message}));
		response.sendRedirect(redirectTo);
	}

}
