package bb.service.servlets;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.sessionstorage.UserSessionStorage;

@WebServlet(urlPatterns={"","/home"},
		initParams = {
			@WebInitParam(name="security",value="none"),
			@WebInitParam(name="view",value="home"),
		})
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//...
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}
	
	public static String getPathPrefix() {
		return "http://localhost:8080/Service/";
	}

}
