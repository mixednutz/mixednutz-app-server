package net.mixednutz.app.server.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.springframework.social.connect.ConnectionData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import net.mixednutz.api.provider.IOauth1Credentials;
import net.mixednutz.api.provider.IOauth2Credentials;

public class ExternalCredentials {
	
	@Entity
	@Table(name = "credentials")
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
	public abstract static class ExternalAccountCredentials extends AbstractCredentials {

		private Integer id;
		private String type;
		private ZonedDateTime dateCreated;
		private ZonedDateTime dateModified;

		private User user;

		public ExternalAccountCredentials() {
			super();
		}

		public ExternalAccountCredentials(String type) {
			this();
			this.type = type;
		}

		@Id
		@Column(name = "cred_id")
		@TableGenerator(
	            name = "CREDS_GEN",
	            table = "Creds_ID_Generator",
	            pkColumnName = "id",
	            valueColumnName = "sequence",
	            allocationSize = 1)
		@GeneratedValue(strategy = GenerationType.TABLE, generator = "CREDS_GEN")
		public Integer getId() {
			return this.id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
		
		@Column(name="type",insertable=false, updatable=false)
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@JsonIgnore
		@ManyToOne()
		@JoinColumn(name = "user_id")
		public User getUser() {
			return user;
		}

		@Column
		public ZonedDateTime getDateModified() {
			return this.dateModified;
		}
		
		@Column
		public ZonedDateTime getDateCreated() {
			return dateCreated;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public void setDateModified(ZonedDateTime dateModified) {
			this.dateModified = dateModified;
		}
		public void setDateCreated(ZonedDateTime dateCreated) {
			this.dateCreated = dateCreated;
		}
		
		@PrePersist
		void createdAt() {
			setDateCreated(ZonedDateTime.now());
			setDateModified(getDateCreated());
		}

		@PreUpdate
		void updatedAt() {
			setDateModified(ZonedDateTime.now());
		}

	}
	
	@Entity
	@Table(name = "credentials_oauth1")
	@JsonTypeName(Oauth1Credentials.TYPE)
	public abstract static class Oauth1Credentials extends ExternalAccountCredentials 
		implements IOauth1Credentials {
		
		public static final String TYPE = "oauth1";
		
		private String accessToken;
		private String secret;
		
		public Oauth1Credentials() {
			super(TYPE);
		}

		public String getAccessToken() {
			return accessToken;
		}
		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
		public String getSecret() {
			return secret;
		}
		public void setSecret(String secret) {
			this.secret = secret;
		}
		
	}
	
	@Entity
	@Table(name = "credentials_oauth2")
	@JsonTypeName(Oauth2Credentials.TYPE)
	public abstract static class Oauth2Credentials extends ExternalAccountCredentials 
		implements IOauth2Credentials {
			
		public static final String TYPE = "oauth2";
		
		private String authCode;
		private String refreshToken;
		private Long expireTime;
		
		
		public Oauth2Credentials() {
			super(TYPE);
		}

		public String getAuthCode() {
			return authCode;
		}
		public void setAuthCode(String authCode) {
			this.authCode = authCode;
		}
		@JsonIgnore
		public String getRefreshToken() {
			return refreshToken;
		}
		public void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}
		public Long getExpireTime() {
			return expireTime;
		}
		public void setExpireTime(Long expireTime) {
			this.expireTime = expireTime;
		}
		
		public ConnectionData createConnectionData() {
			return new ConnectionData(getProviderId(), null, null, null, null, 
					getAuthCode(), null, getRefreshToken(), 
					getExpireTime());
		}
		
	}
	
}
