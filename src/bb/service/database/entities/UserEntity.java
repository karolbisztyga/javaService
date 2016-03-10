package bb.service.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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

	@Column(name="imagePath")
	private String imagePath;

	@Column(name="sessionId")
	private String sessionId;

	public UserEntity() {}

	public UserEntity(String name, String password, String email) {
		this.name = name;
		this.password = password;
		this.email = email;
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

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
}
