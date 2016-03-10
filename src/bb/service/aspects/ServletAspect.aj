package bb.service.aspects;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.database.managers.UserManager;
import bb.service.servlets.Home;
import bb.service.sessionstorage.UserSessionStorage;

public aspect ServletAspect {

	void around(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException:
			execution(* bb.service.servlets.*.doGet(HttpServletRequest, HttpServletResponse)) &&
			!execution(* bb.service.servlets.StaticContent.doGet(HttpServletRequest, HttpServletResponse)) &&
			args(request, response){
        response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
		ServletConfig config = ((HttpServlet)thisJoinPoint.getThis()).getServletConfig();
		String securityLevel = config.getInitParameter("security");
		String view = config.getInitParameter("view");
		boolean renderView = true;
		switch(securityLevel) {
			case "user": {
				renderView = UserManager.getInstance().checkLogin(request.getSession(), request.getRemoteAddr());
				if(!renderView) {
					request.getSession().setAttribute("queuedRedirect", request.getRequestURL());
					response.sendRedirect(Home.getPathPrefix() + "login");
				}
				break;
			}
			case "stranger": {
				renderView = !UserManager.getInstance().checkLogin(request.getSession(), request.getRemoteAddr());
				if(!renderView) {
					response.sendRedirect(Home.getPathPrefix() + "home");
				}
				break;
			}
			//other cases...
			default: {
				if(!config.getServletName().equals(bb.service.servlets.Login.class.getName())) {
					request.getSession().removeAttribute("queuedRedirect");
				}
			}
		}
		if(renderView) {
			proceed(request, response);
			request.getRequestDispatcher("/WEB-INF/"+view+".html").forward(request, response);
		}
	}
	
	void around(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException:
			execution(* bb.service.servlets.*.doPost(HttpServletRequest, HttpServletResponse)) && args(request, response){
		ServletConfig config = ((HttpServlet)thisJoinPoint.getThis()).getServletConfig();
		String securityLevel = config.getInitParameter("security");
		
		proceed(request, response);
	}
	
}
