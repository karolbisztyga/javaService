package bb.service.database.managers;

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

import bb.service.database.entities.MessageEntity;
import bb.service.database.entities.UserEntity;
import bb.service.exceptions.UserDataException;
import bb.service.sessionstorage.UserSessionStorage;

public class UserManager {
	
	private static ThreadLocal<UserManager> instance;
	private SessionFactory sessionFactory;
	
	private UserManager() {
		this.sessionFactory = new AnnotationConfiguration().configure()
                .addAnnotatedClass(UserEntity.class)
                .buildSessionFactory();
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
	
	public boolean checkLogin(HttpSession session, String ip) {
		UserSessionStorage userSessionStorage = (UserSessionStorage)session.getAttribute(UserSessionStorage.STORAGE_TITLE);
		if(userSessionStorage == null) {
			return false;
		}
	    Session databaseSession = null;
	    try {
	        databaseSession = this.sessionFactory.openSession();
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
	    	//ex.printStackTrace();
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
	        String query = "FROM UserEntity u WHERE u.name=:name AND password=:password";
	        List result = databaseSession.createQuery(query)
	                .setParameter("name", name)
	                .setParameter("password", hashedPassword)
	                .list();
	        if(result.size()==1) {
	        	UserEntity user = ((UserEntity)result.get(0));
        		session.setAttribute(UserSessionStorage.STORAGE_TITLE, new UserSessionStorage(user.getName(), ip));
        		user.setSessionId(session.getId());
    	        transaction = databaseSession.beginTransaction();
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
	    	ex.printStackTrace();
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
	    	ex.printStackTrace();
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
	    	ex.printStackTrace();
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

	public UserEntity getUser(String field, String data) {
		Session databaseSession = null;
	    try {
	        databaseSession = this.sessionFactory.openSession();
	        String query = "FROM UserEntity u WHERE u."+field+"=:data";
	        List result = databaseSession.createQuery(query)
	                .setParameter("data", data)
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
	
	public boolean sendMessage(String authorName, String targetName, String messageContent) throws UserDataException {
	    Session databaseSession = null;
	    Transaction transaction = null;
        databaseSession = this.sessionFactory.openSession();
        transaction = databaseSession.beginTransaction();
        UserEntity author = this.getUser("name", authorName);
        if(author == null) {
        	throw new UserDataException("author user does not exist");
        }
        UserEntity target = this.getUser("name", targetName);
        if(target == null) {
        	throw new UserDataException("target user does not exist");
        }
	    try {
	    	MessageEntity message = new MessageEntity(author, target, messageContent, System.currentTimeMillis());
	    	databaseSession.save(message);
        	transaction.commit();
        	return true;
	    } catch(Exception ex) {
	    	ex.printStackTrace();
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
	
	public boolean setMute(String userName, boolean newMute, HttpServletRequest request) throws UserDataException {
	    Session databaseSession = null;
	    Transaction transaction = null;
        databaseSession = this.sessionFactory.openSession();
        String currentUserName = ((UserSessionStorage)(request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE))).getName();
        if(currentUserName.equals(userName)) {
        	throw new UserDataException("you can not mute yourself");
        }
        UserEntity mutingUser = this.getUser("name", currentUserName);
        if(mutingUser == null) {
        	throw new UserDataException("muting user does not exist");
        }
        UserEntity mutedUser = this.getUser("name", userName);
        if(mutedUser == null) {
        	throw new UserDataException("target user does not exist");
        }
        try {
    	    if(newMute) { //mute
    	    	if(mutingUser.getMutedUsers().contains(mutedUser)) {
        	    	throw new UserDataException("user already muted");
    	    	}
    	    	mutingUser.getMutedUsers().add(mutedUser);
                transaction = databaseSession.beginTransaction();
                databaseSession.update(mutingUser);
            	transaction.commit();
    	    } else { //unmute
    	    	if(!mutingUser.getMutedUsers().contains(mutedUser)) {
        	    	throw new UserDataException("user is not muted");
    	    	}
    	    	mutingUser.getMutedUsers().remove(mutedUser);
                transaction = databaseSession.beginTransaction();
                databaseSession.update(mutingUser);
            	transaction.commit();
    	    }
        	return true;
	    } catch(Exception ex) {
	    	ex.printStackTrace();
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
	
	public boolean getMute(String userName, HttpServletRequest request) throws UserDataException {
		Session databaseSession = null;
	    Transaction transaction = null;
        try {
	        databaseSession = this.sessionFactory.openSession();
	        transaction = databaseSession.beginTransaction();
	        String currentUserName = ((UserSessionStorage)(request.getSession().getAttribute(UserSessionStorage.STORAGE_TITLE))).getName();
	        UserEntity mutingUser = this.getUser("name", currentUserName);
	        if(mutingUser == null) {
	        	throw new UserDataException("muting user does not exist");
	        }
	        UserEntity mutedUser = this.getUser("name", userName);
	        if(mutedUser == null) {
	        	throw new UserDataException("muted user does not exist");
	        }
    	    return (mutingUser.getMutedUsers().contains(mutedUser));
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}
	
}
