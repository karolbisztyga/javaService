package bb.service.database.entities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="users")
public class UserEntity {

	@Id
	@GeneratedValue
	@Column(name="id", nullable=false)
	private int id;
	
	@Column(name="name", nullable=false)
	private String name;

	@Column(name="password", nullable=false)
	private String password;

	@Column(name="email", nullable=false)
	private String email;

	@Column(name="sessionId")
	private String sessionId;

	@OneToMany
	private Set<UserEntity> mutedUsers;
	
	@Column(name="unreadMessages")
	private int unreadMessages;
	
	public UserEntity() {
	}
	
	public UserEntity(String name, String password, String email) {
		super();
		this.name = name;
		this.password = password;
		this.email = email;
		this.sessionId = null;
		this.mutedUsers = new HashSet<>();
		this.unreadMessages = 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public Set<UserEntity> getMutedUsers() {
		return mutedUsers;
	}

	public void setMutedUsers(Set<UserEntity> mutedUsers) {
		this.mutedUsers = mutedUsers;
	}

	public int getUnreadMessages() {
		return unreadMessages;
	}

	public void setUnreadMessages(int unreadMessages) {
		this.unreadMessages = unreadMessages;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserEntity other = (UserEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
