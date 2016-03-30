package bb.service.servlets;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import bb.service.database.managers.UserManager;
import bb.service.sessionstorage.UserSessionStorage;

@WebServlet(urlPatterns={"","/home"},
		initParams = {
			@WebInitParam(name="security",value="none"),
			@WebInitParam(name="view",value="home"),
		})
public class Home extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	public static String getPathPrefix() {
		return "http://localhost:8080/ServiceGit/";
	}

}
