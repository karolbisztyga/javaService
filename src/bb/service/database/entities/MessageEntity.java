package bb.service.database.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="messages")
public class MessageEntity {

	@Id
	@GeneratedValue
	@Column(name="id", nullable=false)
	private int id;

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(nullable=false)
	private UserEntity author;

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(nullable=false)
	private UserEntity target;
	
	@Column(name="message", nullable=false)
	private String message;
	
	@Column(name="sendDate", nullable=false)
	private Long sendDate;

	public MessageEntity(UserEntity author, UserEntity target, String message, Long sendDate) {
		this.author = author;
		this.target = target;
		this.message = message;
		this.sendDate = sendDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UserEntity getAuthor() {
		return author;
	}

	public void setAuthor(UserEntity author) {
		this.author = author;
	}

	public UserEntity getTarget() {
		return target;
	}

	public void setTarget(UserEntity target) {
		this.target = target;
	}

	public Long getSendDate() {
		return sendDate;
	}

	public void setSendDate(Long sendDate) {
		this.sendDate = sendDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
