package bb.service.servlets.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.database.entities.UserEntity;
import bb.service.database.managers.UserManager;

@WebServlet(urlPatterns={"/searchUser"},
		initParams = {
			@WebInitParam(name="security",value="user")
		})
public class SearchUser extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = request.getParameter("name");
		UserEntity user = UserManager.getInstance().findUser(name);
	}

}

