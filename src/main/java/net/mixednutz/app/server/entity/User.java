package net.mixednutz.app.server.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User extends BaseUserDetails {
	
	/**
	 * NOTE : IF YOU EVER CHANGE THIS CLASS, YOU HAVE TO PURGE THE TOKEN STORE
	 * BECAUSE THIS CLASS IS PART OF THE TOKEN SERIALIZATION.
	 */
	private static final long serialVersionUID = -8968161792289432148L;
	private int hashCode = Integer.MIN_VALUE;
	
	private Long userId;
	private String displayName;
	private ZonedDateTime memberSince;
	private String avatarFilename;
	private boolean _private;
	
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
	
	@PrePersist
	public void onCreate() {
		this.memberSince = ZonedDateTime.now();
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
	@Transient
	@JsonIgnore
	public String getProviderId() {
		return userId!=null?userId.toString():null;
	}
	@Override
	@Column(name="username",
		length=50,
		nullable=false)
	public String getUsername() {
		return super.getUsername();
	}
	@Override
	@JsonIgnore
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
	public boolean isPrivate() {
		return _private;
	}
	public void setPrivate(boolean _private) {
		this._private = _private;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	@Column(updatable=false)
	public ZonedDateTime getMemberSince() {
		return memberSince;
	}
	public void setMemberSince(ZonedDateTime memberSince) {
		this.memberSince = memberSince;
	}
	@JsonIgnore
	public String getAvatarFilename() {
		return avatarFilename;
	}
	public void setAvatarFilename(String avatarSrc) {
		this.avatarFilename = avatarSrc;
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
	
	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getProviderId()) return super.hashCode();
			
				String hashStr = this.getClass().getName() + ":" + this.getProviderId().hashCode();
				this.hashCode = hashStr.hashCode();
			
		}
		return this.hashCode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof User)) {
			return false;
		}

		User obj2 = (User) obj;
		
		if (this.getProviderId()==null && obj2.getProviderId()==null) {
			return true;
		} else if (this.getProviderId()==null ^ obj2.getProviderId()==null) {
			return false;
		}
		return (this.getProviderId().equals(obj2.getProviderId()));
		
	}

	
}
