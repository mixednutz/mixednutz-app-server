package net.mixednutz.app.server.entity.post;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import net.mixednutz.app.server.entity.User;


/**
 * @author Andy
 */
@MappedSuperclass
public abstract class AbstractComment implements Comment{
	
	private String body;
	private ZonedDateTime dateCreated;
	private User author;
				
	@PrePersist
	public void onPersist() {
		this.dateCreated=ZonedDateTime.now();
	}
		
	/**
	 * @return Returns the author.
	 */
	@ManyToOne()
	@JoinColumn(name="author_id")
	public User getAuthor() {
		return author;
	}
	
	/**
	 * @return Returns the body.
	 */
//	@Column(name="body", columnDefinition="LONGTEXT")
	public String getBody() {
		return body;
	}
	
	@Column(name="timestamp")
	public ZonedDateTime getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setDateCreated(ZonedDateTime timestamp) {
		this.dateCreated = timestamp;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

}
