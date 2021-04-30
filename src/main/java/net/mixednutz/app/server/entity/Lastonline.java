package net.mixednutz.app.server.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name="LastOnline")
public class Lastonline {

	private Long userId;
	private ZonedDateTime timestamp;
	
	public Lastonline() {
		super();
	}

	public Lastonline(Long userId) {
		super();
		this.userId = userId;
	}
	
	public Lastonline(User user) {
		this(user.getUserId());
	}
	
	@PreUpdate
	@PrePersist
	public void setTimestamp() {
		this.timestamp = ZonedDateTime.now();
	}

	@Id
	@Column(name="user_id", nullable = false, updatable=false)
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(ZonedDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
}
