package bb.service.servlets.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import bb.service.database.entities.UserEntity;
import bb.service.database.managers.UserManager;
import bb.service.exceptions.UserDataException;
import bb.service.files.managers.AvatarFileManager;

@WebServlet(urlPatterns={"/getUserInfo"},
		initParams = {
			@WebInitParam(name="security",value="user")
		})
public class GetUserInfo extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject returnObject = new JSONObject();
		try {
			String field = request.getParameter("field");
			String value = request.getParameter(field);
			UserManager userManager = UserManager.getInstance();
			UserEntity user = userManager.getUser(field, value);
			if(user == null) {
				returnObject.append("error", "no such user");
				return;
			}
			JSONObject userObject = new JSONObject();
			userObject.append("name", user.getName());
			userObject.append("email", user.getEmail());
			try {
				boolean x = userManager.getMute(user.getName(), request);
				userObject.append("muted", x);
			} catch(UserDataException e) {
				returnObject.append("error", e.getMessage());
			}
			String avatarPath = AvatarFileManager.getInstance().buildAvatarServerPath(request, user.getName());
			if(avatarPath != null) {
				userObject.append("avatar", avatarPath);
			}
			returnObject.append("result", userObject);
		} catch(NullPointerException e) {
			returnObject = new JSONObject();
			returnObject.append("error", "field and value not specified properly");
		} finally {
			response.getWriter().print(returnObject);
		}
	}

}
