package net.mixednutz.app.server.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.model.IAction;
import net.mixednutz.api.model.IImage;
import net.mixednutz.api.model.IUser;

@Entity
public class User extends BaseUserDetails implements IUser {
	
	/**
	 * NOTE : IF YOU EVER CHANGE THIS CLASS, YOU HAVE TO PURGE THE TOKEN STORE
	 * BECAUSE THIS CLASS IS PART OF THE TOKEN SERIALIZATION.
	 */
	private static final long serialVersionUID = -8968161792289432148L;
	
	private Long userId;
	private String displayName;
	private String avatarSrc;
	private String uri;
	private String url;
	
	/*
	 * password = encrypted password in database
	 * passwordRaw = pre-encrypted password prior to encryption
	 * passwordConfirm = only used for password creation
	 */
	private String passwordConfirm;	
	private String passwordRaw;
	
	public User() {
		super();
		// Right now we have very simple authentication
		this.setAccountNonExpired(true);
		this.setAccountNonLocked(true);
		this.setCredentialsNonExpired(true);
	}
	@Id
	@GeneratedValue(generator="system-native")
	@GenericGenerator(name="system-native", strategy = "native")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	@Override
	@Column(name="username",
		length=50,
		nullable=false)
	public String getUsername() {
		return super.getUsername();
	}
	@Override
	@Column(name="password_enc",
			columnDefinition="CHAR(60)",
			length=60,
			nullable=true)
	public String getPassword() {
		return super.getPassword();
	}
	@Override
	@Column(name="enabled")
	public boolean isEnabled() {
		return super.isEnabled();
	}
	
	@Transient
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	@JsonIgnore
	public String getAvatarSrc() {
		return avatarSrc;
	}
	public void setAvatarSrc(String avatarSrc) {
		this.avatarSrc = avatarSrc;
	}
	@Transient
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	@Transient
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Transient
	@Override
	public IImage getAvatar() {
		return new Image(this.avatarSrc, this.getUsername()+"'s avatar");
	}
	@Transient
	@Override
	public List<? extends IAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}
	@Transient
	@Override
	public UserProfile getProfileData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Transient
	@JsonIgnore
	public java.lang.String getPasswordConfirm() {
		return passwordConfirm;
	}
	public void setPasswordConfirm(java.lang.String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}
	@Transient
	@JsonIgnore
	public String getPasswordRaw() {
		return passwordRaw;
	}
	public void setPasswordRaw(String passwordRaw) {
		this.passwordRaw = passwordRaw;
	}

	
}
