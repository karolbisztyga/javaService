package bb.service.database.managers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

import bb.service.database.entities.UserEntity;
import bb.service.exceptions.UserDataException;
import bb.service.servlets.EditProfile;
import bb.service.servlets.Home;
import bb.service.sessionstorage.UserSessionStorage;

public class UserManager {
	
	private static ThreadLocal<UserManager> instance;
	private SessionFactory sessionFactory;
	
	private UserManager() {
		this.sessionFactory = new AnnotationConfiguration().configure()
                .addAnnotatedClass(UserEntity.class)
                .buildSessionFactory();
	}
	
	public boolean checkLogin(HttpSession session, String ip) {
		UserSessionStorage userSessionStorage = (UserSessionStorage)session.getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(userSessionStorage == null) {
			return false;
		}
	    Session databaseSession = null;
	    Transaction transaction = null;
	    try {
	        databaseSession = this.sessionFactory.openSession();
	        transaction = databaseSession.beginTransaction();
	        String query = "FROM UserEntity u WHERE u.name=:name";
	        List result = databaseSession.createQuery(query)
	                .setParameter("name", userSessionStorage.getName())
	                .list();
	        if(result.size()==1) {
	        	UserEntity user = (UserEntity)result.get(0);
	        	if(user.getSessionId().equals(session.getId()) && userSessionStorage.getIp().equals(ip)) {
	        		return true;
	        	}
	        }
        	session.removeAttribute("loginData");
            return false;
	    } catch(NullPointerException ex) {
	        if(transaction!=null) {
	            transaction.rollback();
	        }
	        return false;
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}
	
	public boolean login(String name, String password, String ip, HttpSession session) {
	    Session databaseSession = null;
	    Transaction transaction = null;
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(password.getBytes("UTF-8"));
	        String hashedPassword = new BigInteger(1, md.digest()).toString(16);
	        databaseSession = this.sessionFactory.openSession();
	        transaction = databaseSession.beginTransaction();
	        String query = "FROM UserEntity u WHERE u.name=:name AND password=:password";
	        List result = databaseSession.createQuery(query)
	                .setParameter("name", name)
	                .setParameter("password", hashedPassword)
	                .list();
	        if(result.size()==1) {
	        	UserEntity user = ((UserEntity)result.get(0));
        		session.setAttribute(UserSessionStorage.STORAGE_TITLE, new UserSessionStorage(user.getName(), ip));
        		user.setSessionId(session.getId());
        		databaseSession.save(user);
    	        if(!transaction.wasCommitted()) {
    	        	transaction.commit();
    	        }
	            return true;
	        } else {
	            session.removeAttribute(UserSessionStorage.STORAGE_TITLE);
	            return false;
	        }
	    } catch(NullPointerException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
	        if(transaction!=null) {
	            transaction.rollback();
	        }
	        return false;
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}
	
	public boolean register(UserEntity user) throws UserDataException {
	    Session databaseSession = null;
	    Transaction transaction = null;
        databaseSession = this.sessionFactory.openSession();
        String query = "FROM UserEntity WHERE name=:name OR email=:email";
        List result = databaseSession.createQuery(query)
    			.setParameter("name", user.getName())
    			.setParameter("email", user.getEmail())
    			.list();
        if(result.size() > 0) {
        	throw new UserDataException("email or name taken");
        }
	    try {
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(user.getPassword().getBytes("UTF-8"));
	        String hashedPassword = new BigInteger(1, md.digest()).toString(16);
	        user.setPassword(hashedPassword);
	        transaction = databaseSession.beginTransaction();
	        databaseSession.save(user);
        	transaction.commit();
        	return true;
	    } catch(Exception ex) {
	        if(transaction!=null) {
	            transaction.rollback();
	        }
	        return false;
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}
	
	public boolean updatePassword(HttpSession session, String oldPassword, String password, String passwordRepeat) 
			throws UserDataException {
		if(!password.equals(passwordRepeat)) {
			throw new UserDataException("passwords are not equal");
		}
	    Session databaseSession = null;
	    Transaction transaction = null;
        databaseSession = this.sessionFactory.openSession();
        String hashedPassword = "";
        try {
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(oldPassword.getBytes("UTF-8"));
	        hashedPassword = new BigInteger(1, md.digest()).toString(16);
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
        	throw new UserDataException(e.getMessage());
        }
        
        String query = "FROM UserEntity WHERE name=:name AND password=:password";
        Object preventNullPointer = session.getAttribute(UserSessionStorage.STORAGE_TITLE);
        if(preventNullPointer == null) {
        	throw new UserDataException("there is no logged in user");
        }
        List result = databaseSession.createQuery(query)
    			.setParameter("name", ((UserSessionStorage)(preventNullPointer)).getName())
    			.setParameter("password", hashedPassword)
    			.list();
        if(result.isEmpty()) {
        	throw new UserDataException("old password incorrect");
        }
	    try {
	        UserEntity user = (UserEntity)result.get(0);
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(password.getBytes("UTF-8"));
	        hashedPassword = new BigInteger(1, md.digest()).toString(16);
	        
	        user.setPassword(hashedPassword);
	        transaction = databaseSession.beginTransaction();
	        databaseSession.save(user);
        	transaction.commit();
        	return true;
	    } catch(Exception ex) {
	        if(transaction!=null) {
	            transaction.rollback();
	        }
	        return false;
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}

	public boolean isNameTaken(String name) {
	    Session databaseSession = this.sessionFactory.openSession();
	    String query = "FROM UserEntity u WHERE u.name=:name";
	    List result = databaseSession.createQuery(query)
	            .setParameter("name", name)
	            .list();
        databaseSession.close();
	    return (result.size()>0);
	}

	public boolean isEmailTaken(String email) {
	    Session databaseSession = this.sessionFactory.openSession();
	    String query = "FROM UserEntity u WHERE u.email=:email";
	    List result = databaseSession.createQuery(query)
	            .setParameter("email", email)
	            .list();
        databaseSession.close();
	    return (result.size()>0);
	}
	
	public UserEntity findUser(String name) {
		Session databaseSession = null;
	    Transaction transaction = null;
	    try {
	        databaseSession = this.sessionFactory.openSession();
	        transaction = databaseSession.beginTransaction();
	        String query = "FROM UserEntity u WHERE u.name=:name";
	        List result = databaseSession.createQuery(query)
	                .setParameter("name", name)
	                .list();
	        if(result.size()==1) {
        		return ((UserEntity)result.get(0));
	        }
	        return null;
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}

	public static UserManager getInstance() {
		if(UserManager.instance==null) {
			UserManager.instance = new ThreadLocal<UserManager>(){
				@Override
				public UserManager initialValue() {
					return new UserManager();
				}
			};
		}
		return UserManager.instance.get();
	}

	public static String getAvatarsUploadPath() {
		return getAvatarsUploadPath(File.separator);
	}
	
	public static String getAvatarsUploadPath(String separator) {
		return "users" + separator + "avatars";
	}

	public static String buildAvatarFilePath(HttpServletRequest request) {
		return buildAvatarFilePath(request, null);
	}
	
	public static String buildAvatarFilePath(HttpServletRequest request, String extension) {
		Object preventNullPointer = request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(preventNullPointer == null) {
			return null;
		}
		UserSessionStorage user = (UserSessionStorage)preventNullPointer;
		String avatarPath = request.getServletContext().getRealPath("")+UserManager.getAvatarsUploadPath() + File.separator;
		File createIfNotExists = new File(avatarPath);
		if(!createIfNotExists.exists()) {
			createIfNotExists.mkdirs();
		}
		String avatarExt = (extension == null) ? UserManager.getAvatarExtension(avatarPath, user.getName()) : extension;
		if(avatarExt == null) return null;
		return avatarPath + user.getName() + "." + avatarExt;
	}
	
	public static String buildAvatarServerPath(HttpServletRequest request) {
		Object preventNullPointer = request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(preventNullPointer == null) {
			return null;
		}
		UserSessionStorage user = (UserSessionStorage)preventNullPointer;
		String avatarExt = UserManager.getAvatarExtension(
				request.getServletContext().getRealPath("")+UserManager.getAvatarsUploadPath(), 
				user.getName());
		if(avatarExt == null) return null;
		return Home.getPathPrefix()+UserManager.getAvatarsUploadPath("/") + "/" + user.getName() + "." + avatarExt;
	}
	
	public static String getAvatarExtension(String avatarPath, String userName) {
		for(String ext : EditProfile.AVATAR_EXTENSIONS) {
			File avatarFile = new File(avatarPath+"/"+userName+"."+ext);
			if(avatarFile.exists()) {
				return ext;
			}
		}
		return null;
	}
	
	/*
	private static String generateRandomKey(String salt) {
		try {
			String rand = salt + Double.toString(Math.random());
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(rand.getBytes("UTF-8"));
	        return new BigInteger(1, md.digest()).toString(16);
		} catch(NoSuchAlgorithmException |UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	*/
}
