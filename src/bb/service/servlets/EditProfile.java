package bb.service.servlets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import bb.service.database.managers.UserManager;
import bb.service.exceptions.UserDataException;
import bb.service.sessionstorage.StatusSessionStorage;
import bb.service.sessionstorage.UserSessionStorage;

@WebServlet(urlPatterns={"/editProfile"},
		initParams= {
			@WebInitParam(name="security", value="user"),
			@WebInitParam(name="view", value="editProfile"),
		})
@MultipartConfig()
public class EditProfile extends HttpServlet {

	private final int MAX_AVATAR_PIXELS = 500;
	private final int MAX_AVATAR_BYTES = 1024*512;//0.5MB
	public static final String[] AVATAR_EXTENSIONS = {"png","jpg"};
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action").toString();
		try {
			switch(action) {
				case "password": {
					String oldPassword = request.getParameter("old-password");
					String password = request.getParameter("password");
					String passwordRepeat = request.getParameter("password-repeat");
					if(UserManager.getInstance().updatePassword(request.getSession(), oldPassword, password, passwordRepeat)) {
						throw new UserDataException("password changed");
					} else {
						throw new UserDataException("an error occured");
					}
				}
				case "picture": {
					//String uploadPath = request.getServletContext().getRealPath("")+UserManager.getAvatarsUploadPath();
					Part fileToUpload = request.getPart("file");
					System.out.println();
					if(fileToUpload.getSize() > MAX_AVATAR_BYTES) {
						throw new UserDataException("avatar must not exceed " + MAX_AVATAR_BYTES + " bytes");
					}
					//UserSessionStorage user = (UserSessionStorage)request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE);
					String extension = getFileExtension(fileToUpload);
					BufferedImage bufferImage = ImageIO.read(fileToUpload.getInputStream());
					if(bufferImage.getWidth() > MAX_AVATAR_PIXELS || bufferImage.getHeight() > MAX_AVATAR_PIXELS) {
						throw new UserDataException("avatar max size is " + MAX_AVATAR_PIXELS + "x" + MAX_AVATAR_PIXELS + "[px]" );
					}
					if(!Arrays.asList(AVATAR_EXTENSIONS).contains(extension)) {
						throw new UserDataException("avatar must be one of those formats: " + String.join(", ", AVATAR_EXTENSIONS));
					}
					//fileToUpload.write(uploadPath + File.separator + user.getName() + "." + extension);
					fileToUpload.write(UserManager.buildAvatarFilePath(request, extension));
					//size check!
					System.out.println("-------------" + fileToUpload.getSize());
					throw new UserDataException("file uploaded successfully");
					/*for(Part part : request.getParts()) {
						String fileName = null;
						String header = part.getHeader("content-disposition");
						String[] headerArray = header.split(";");
						for(int i=0 ; i<headerArray.length ; ++i) {
							if(headerArray[i].contains("filename")) {
								fileName = headerArray[i].substring(headerArray[i].indexOf("=")+2, headerArray[i].length()-1);
								System.out.println("---HERE: " + fileName + " | " + fileUploadDir.getPath());
								break;
							}
						}
						if(fileName == null) {
							throw new UserDataException("file upload error");
						}
						part.write(fileUploadDir.getPath() + File.separator +"my_prefix_" + fileName);
					}*/
				}
				case "delete_picture": {
					File avatarFile = new File(UserManager.buildAvatarFilePath(request));
					if(avatarFile.delete()) {
						throw new UserDataException("avatar deleted");
					} else {
						throw new UserDataException("avatar could not be deleted");
					}
				}
			}
		} catch(UserDataException e) {
			request.getSession().setAttribute(StatusSessionStorage.STORAGE_TITLE, 
					new StatusSessionStorage(new String[] {e.getMessage()}));
			response.sendRedirect(Home.getPathPrefix() + "editProfile");
			return;
		}
	}
	
	private String getFileExtension(Part part) {
		String fileName = null;
		String header = part.getHeader("content-disposition");
		String[] headerArray = header.split(";");
		for(int i=0 ; i<headerArray.length ; ++i) {
			if(headerArray[i].contains("filename")) {
				fileName = headerArray[i].substring(headerArray[i].indexOf("=")+2, headerArray[i].length()-1);
				break;
			}
		}
		String[] splittedName = fileName.split("\\.");
		return splittedName[splittedName.length-1];
	}

}
