package bb.service.database.managers;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

import bb.service.database.entities.MessageEntity;
import bb.service.database.entities.UserEntity;
import bb.service.exceptions.CacheException;
import bb.service.exceptions.NoSuchUserException;
import bb.service.exceptions.UserDataException;
import bb.service.sessionstorage.UserSessionStorage;

public class UserManager {
	
	public enum MESSAGE_TYPE {
		SENT,
		RECEIVED
	}
	
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
	        UserEntity user = (UserEntity)databaseSession.createQuery(query)
	                .setParameter("name", name)
	                .setParameter("password", hashedPassword)
	                .uniqueResult();
	        if(user != null) {
        		session.setAttribute(UserSessionStorage.STORAGE_TITLE, new UserSessionStorage(user.getName(), ip, user.getUnreadMessages()));
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

	public boolean updatePassword(String userName, String oldPassword, String password, String passwordRepeat) 
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
        List result = databaseSession.createQuery(query)
    			.setParameter("name", userName)
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
	
	public boolean sendMessage(String authorName, String targetName, String messageContent) 
			throws UserDataException, NoSuchUserException {
		if(messageContent.length() == 0) {
			throw new UserDataException("message is empty");
		}
		if(this.getMute(targetName, authorName)) {
			throw new UserDataException("you have been muted");
		}
	    Session databaseSession = null;
	    Transaction transaction = null;
        databaseSession = this.sessionFactory.openSession();
        UserEntity author = this.getUser("name", authorName);
        if(author == null) {
        	throw new NoSuchUserException("author user does not exist");
        }
        UserEntity target = this.getUser("name", targetName);
        if(target == null) {
        	throw new NoSuchUserException("target user does not exist");
        }
	    try {
	    	MessageEntity message = new MessageEntity(author, target, messageContent, System.currentTimeMillis());
	        transaction = databaseSession.beginTransaction();
	    	databaseSession.save(message);
	    	target.setUnreadMessages(target.getUnreadMessages()+1);
	    	databaseSession.update(target);
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
	
	public boolean setMute(String mutingName, String mutedName, boolean newMute) throws UserDataException, NoSuchUserException {
	    Session databaseSession = null;
	    Transaction transaction = null;
        databaseSession = this.sessionFactory.openSession();
        if(mutingName.equals(mutedName)) {
        	throw new UserDataException("you can not mute yourself");
        }
        UserEntity mutingUser = this.getUser("name", mutingName);
        if(mutingUser == null) {
        	throw new NoSuchUserException("muting user does not exist");
        }
        UserEntity mutedUser = this.getUser("name", mutedName);
        if(mutedUser == null) {
        	throw new NoSuchUserException("target user does not exist");
        }
        try {
            transaction = databaseSession.beginTransaction();
            databaseSession.refresh(mutingUser);
        	transaction.commit();
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
	
	public boolean getMute(String mutingName, String mutedName) throws NoSuchUserException {
		Session databaseSession = null;
		Transaction transaction = null;
        try {
	        databaseSession = this.sessionFactory.openSession();
	        UserEntity mutingUser = this.getUser("name", mutingName);
	        if(mutingUser == null) {
	        	throw new NoSuchUserException("muting user does not exist");
	        }
	        UserEntity mutedUser = this.getUser("name", mutedName);
	        if(mutedUser == null) {
	        	throw new NoSuchUserException("muted user does not exist");
	        }
	        transaction = databaseSession.beginTransaction();
	        databaseSession.refresh(mutingUser);
    	    boolean result = (mutingUser.getMutedUsers().contains(mutedUser));
    	    return result;
	    } catch(NoSuchUserException e) {
	    	throw e;
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	if(transaction!=null) {
	    		transaction.rollback();
	    		transaction = null;
	    	}
	    	return false;
	    } finally {
	    	if(transaction != null) {
	    		transaction.commit();
	    	}
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}
	
	/**
	 * 
	 * @param userName
	 * @param lastMessageId id of the last message that affected certain user and it is supposed to be stored on the client as a cache
	 * @param type
	 * @return
	 * @throws NoSuchUserException 
	 * @throws CacheException when @param lastMessageId is the same as in the database so there is no need to update results on client
	 */
	public List getMessages(String userName, Integer lastMessageId, MESSAGE_TYPE type) throws NoSuchUserException, CacheException {
		Session databaseSession = null;
        try {
	        databaseSession = this.sessionFactory.openSession();
	        UserEntity user = this.getUser("name", userName);
	        if(user == null) {
	        	throw new NoSuchUserException("such user does not exist");
	        }
	        String query = null;
	        switch(type) {
	        	case SENT: {
	        		query = "FROM MessageEntity WHERE author_id=:id";
	        		break;
	        	}
	        	case RECEIVED: {
	        		query = "FROM MessageEntity WHERE target_id=:id";
	        		break;
	        	}
	        }
	        List messages = databaseSession.createQuery(query)
	        		.setParameter("id", user.getId())
	        		.list();
	        if(lastMessageId != null && ((MessageEntity)messages.get(messages.size()-1)).getId() == lastMessageId) {
	        	throw new CacheException();
	        }
	        return messages;
	    } catch(NoSuchUserException | CacheException e) {
	    	throw e;
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	return null;
	    } finally {
	        if(databaseSession!=null) {
	            databaseSession.close();
	        }
	    }
	}
	
}
