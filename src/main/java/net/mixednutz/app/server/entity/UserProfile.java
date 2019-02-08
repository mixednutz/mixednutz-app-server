package net.mixednutz.app.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import net.mixednutz.api.model.IUserProfile;

@Entity
public class UserProfile implements IUserProfile {

	private Long userId;
	private User user;
	
	

	@Id
	@Column(name="user_id", nullable = false, updatable=false)
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
