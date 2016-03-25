package bb.service.files.managers;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import bb.service.servlets.EditProfile;
import bb.service.servlets.Home;
import bb.service.sessionstorage.UserSessionStorage;

public class AvatarFileManager {

	private static ThreadLocal<AvatarFileManager> instance;
	
	private AvatarFileManager(){}
	
	public static AvatarFileManager getInstance() {
		if(AvatarFileManager.instance == null) {
			AvatarFileManager.instance = new ThreadLocal<AvatarFileManager>(){
				@Override
				public AvatarFileManager initialValue() {
					return new AvatarFileManager();
				}
			};
		}
		return AvatarFileManager.instance.get();
	}
	
	public String getAvatarsUploadPath() {
		return getAvatarsUploadPath(File.separator);
	}
	
	public String getAvatarsUploadPath(String separator) {
		return "users" + separator + "avatars";
	}

	public String buildAvatarFilePath(HttpServletRequest request) {
		return buildAvatarFilePath(request, null);
	}
	
	public String buildAvatarFilePath(HttpServletRequest request, String extension) {
		Object preventNullPointer = request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(preventNullPointer == null) {
			return null;
		}
		UserSessionStorage user = (UserSessionStorage)preventNullPointer;
		String avatarPath = request.getServletContext().getRealPath("")+this.getAvatarsUploadPath() + File.separator;
		File createIfNotExists = new File(avatarPath);
		if(!createIfNotExists.exists()) {
			createIfNotExists.mkdirs();
		}
		String avatarExt = (extension == null) ? this.getAvatarExtension(avatarPath, user.getName()) : extension;
		if(avatarExt == null) return null;
		return avatarPath + user.getName() + "." + avatarExt;
	}
	
	public String buildAvatarServerPath(HttpServletRequest request, String specifiedName) {
		String avatarExt = this.getAvatarExtension(
				request.getServletContext().getRealPath("")+this.getAvatarsUploadPath(), 
				specifiedName);
		if(avatarExt == null) return null;
		return Home.getPathPrefix()+this.getAvatarsUploadPath("/") + "/" + specifiedName + "." + avatarExt;
	}
	
	public String buildAvatarServerPath(HttpServletRequest request) {
		Object preventNullPointer = request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(preventNullPointer == null) {
			return null;
		}
		UserSessionStorage user = (UserSessionStorage)preventNullPointer;
		return this.buildAvatarServerPath(request, user.getName());
	}
	
	public String getAvatarExtension(String avatarPath, String userName) {
		for(String ext : EditProfile.AVATAR_EXTENSIONS) {
			File avatarFile = new File(avatarPath+"/"+userName+"."+ext);
			if(avatarFile.exists()) {
				return ext;
			}
		}
		return null;
	}

}
