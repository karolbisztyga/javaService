package bb.service.servlets;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bb.service.database.entities.UserEntity;
import bb.service.database.managers.UserManager;
import bb.service.exceptions.UserDataException;
import bb.service.servlets.special.Captcha;
import bb.service.sessionstorage.StatusSessionStorage;

@WebServlet(urlPatterns={"/register"},
		initParams = {
			@WebInitParam(name="security",value="stranger"),
			@WebInitParam(name="view",value="register"),
		})
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String name = request.getParameter("name");
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			String passwordRepeat = request.getParameter("password-repeat");
			String captcha = request.getParameter("captcha");
			if(!password.equals(passwordRepeat)) {
				throw new UserDataException("passwords are not equal");
			}
			Captcha.STATUS captchaStatus = Captcha.checkCaptcha(request, captcha);
			if(captchaStatus != Captcha.STATUS.PASSED) {
				throw new UserDataException(Captcha.getStatusMessage(captchaStatus));
			}
	        /*MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(password.getBytes("UTF-8"));
	        String hashedPassword = new BigInteger(1, md.digest()).toString(16);*/
			UserEntity user = new UserEntity(name, password, email);
			if(UserManager.getInstance().register(user)) {
				request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, 
						new StatusSessionStorage(new String[]{"account created, You can login now"}));
			} else {
				request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, 
						new StatusSessionStorage(new String[]{"account could not be created"}));
			}
		} catch(UserDataException e) {
			//e.printStackTrace();
			request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, new StatusSessionStorage(new String[]{e.getMessage()}));
			response.sendRedirect(Home.getPathPrefix() + "register");
			return;
		}
		response.sendRedirect(Home.getPathPrefix() + "login");
	}

}
