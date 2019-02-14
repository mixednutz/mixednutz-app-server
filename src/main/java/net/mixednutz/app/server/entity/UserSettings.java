package net.mixednutz.app.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class UserSettings {
	
	private Long userId;
	private User user;
	
	private boolean showCombinedExternalFeedsOnProfile = false;
	private Page indexPage = Page.SPLASH;
	
	@Id
	@Column(name="user_id", nullable = false, updatable=false)
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@JsonIgnore
	@OneToOne(fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isShowCombinedExternalFeedsOnProfile() {
		return showCombinedExternalFeedsOnProfile;
	}

	public void setShowCombinedExternalFeedsOnProfile(boolean showCombinedExternalFeedsOnProfile) {
		this.showCombinedExternalFeedsOnProfile = showCombinedExternalFeedsOnProfile;
	}
	
	public Page getIndexPage() {
		return indexPage;
	}

	public void setIndexPage(Page indexPage) {
		this.indexPage = indexPage;
	}

	public enum Page {
		SPLASH,
		USER_PROFILE
	}

}
