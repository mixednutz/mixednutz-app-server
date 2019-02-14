package net.mixednutz.app.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
public class SiteSettings {
	
	private Long settingsId;
	
	private Long adminUserId;
	private User adminUser;
	
	private Page indexPage = Page.SPLASH;
	
	public SiteSettings() {
		super();
	}

	public SiteSettings(Long settingsId, User adminUser) {
		super();
		this.settingsId = settingsId;
		this.adminUser = adminUser;
		this.adminUserId = adminUser.getUserId();
	}

	@Id
	public Long getSettingsId() {
		return settingsId;
	}

	public void setSettingsId(Long settingsId) {
		this.settingsId = settingsId;
	}

	@Column(name="admin_user_id", nullable = false, updatable=false)
	public Long getAdminUserId() {
		return adminUserId;
	}

	public void setAdminUserId(Long adminUserId) {
		this.adminUserId = adminUserId;
	}

	@JsonIgnore
	@OneToOne(fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="admin_user_id")
	public User getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(User adminUser) {
		this.adminUser = adminUser;
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
