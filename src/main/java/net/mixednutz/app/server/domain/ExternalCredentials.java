package net.mixednutz.app.server.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.springframework.social.connect.ConnectionData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class ExternalCredentials {
	
	@Entity
	@Table(name = "credentials")
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	@JsonTypeInfo(property = "providerId", use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = As.PROPERTY)
	@JsonSubTypes({
		@Type(TwitterCredentials.class),
		@Type(MixednutzCredentials.class)})
	public static class ExternalAccountCredentials extends AbstractCredentials {

		private Integer id;
		private ZonedDateTime dateCreated;
		private ZonedDateTime dateModified;

//		private Account account;

		public ExternalAccountCredentials() {
			super();
		}

		public ExternalAccountCredentials(String type) {
			this();
			super.setProviderId(type);
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

//		@JsonIgnore
//		@ManyToOne()
//		@JoinColumn(name = "account_id")
//		public Account getAccount() {
//			return account;
//		}

		@Column
		public ZonedDateTime getDateModified() {
			return this.dateModified;
		}
		
		@Column
		public ZonedDateTime getDateCreated() {
			return dateCreated;
		}

		public String getType() {
			return getProviderId();
		}

//		public void setAccount(Account account) {
//			this.account = account;
//		}

		public void setType(String type) {
			this.setProviderId(type);
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

		public ConnectionData createConnectionData() {
			throw new UnsupportedOperationException("Not implemented yet!");
		}

	}
	
	@MappedSuperclass
	public abstract static class AbstractOauth1Credentials extends ExternalAccountCredentials 
		implements Oauth1Credentials {
		
		private String accessToken;
		private String secret;
		
		public AbstractOauth1Credentials() {
			super();
		}		
		public AbstractOauth1Credentials(String type) {
			super(type);
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
	
	@MappedSuperclass
	public abstract static class AbstractOauth2Credentials extends ExternalAccountCredentials 
		implements Oauth2Credentials {
			
		private String authCode;
		private String refreshToken;
		private Long expireTime;
		
		
		public AbstractOauth2Credentials() {
			super();
		}
		public AbstractOauth2Credentials(String type) {
			super(type);
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
	
	@Entity
	@Table(name = "credentials_twitter")
	@JsonTypeName(TwitterCredentials.PROVIDER_ID)
	public static class TwitterCredentials extends AbstractOauth1Credentials {

		public static final String PROVIDER_ID = "twitter";
				
		public TwitterCredentials() {
			super(PROVIDER_ID);
		}

	}
	
	@Entity
	@Table(name = "credentials_mixednutz")
	@JsonTypeName(MixednutzCredentials.PROVIDER_ID)
	public static class MixednutzCredentials extends AbstractOauth2Credentials {
		
		public static final String PROVIDER_ID = "mixednutz";

		public MixednutzCredentials() {
			super(PROVIDER_ID);
		}

	}

}
