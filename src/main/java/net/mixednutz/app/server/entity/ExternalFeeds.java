package net.mixednutz.app.server.entity;

import static net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials.OAUTH1;
import static net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials.OAUTH2;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
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
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.provider.ICredentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;

public class ExternalFeeds {
	
	@Entity
	@Table(name = "x_feed")
	@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
	@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
	public static abstract class AbstractFeed {

		private Long feedId;
		private String providerId;
		private User user;
		private String name;
		private String imageUrl;
		private String type;
		private ZonedDateTime dateCreated;
		private ZonedDateTime dateModified;
		private ZonedDateTime lastCrawled;
		private String lastCrawledKey; //used for seek pagination

		public AbstractFeed() {
			super();
		}

		public AbstractFeed(String type) {
			this();
			this.type = type;
		}
		
		@Id
		@Column(name = "feed_id")
		@GeneratedValue(strategy = GenerationType.TABLE)
		@GenericGenerator(name = "system-native", strategy = "native")
		public Long getFeedId() {
			return feedId;
		}

		public void setFeedId(Long feedId) {
			this.feedId = feedId;
		}

		@Column(name="type",insertable=false, updatable=false)
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Column(name="provider_id",nullable=false)
		public String getProviderId() {
			return providerId;
		}

		public void setProviderId(String providerId) {
			this.providerId = providerId;
		}

		@Column(name="name")
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Column(name="image_url")
		@JsonIgnore
		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		
		@Transient
		public Image getImage() {
			return new Image(this.imageUrl);
		}

		@ManyToOne()
		@JoinColumn(name="user_id")
		@JsonIgnore
		public User getUser() {
			return user;
		}
				
		public void setUser(User user) {
			this.user = user;
		}

		public ZonedDateTime getDateCreated() {
			return dateCreated;
		}

		public void setDateCreated(ZonedDateTime dateCreated) {
			this.dateCreated = dateCreated;
		}

		public ZonedDateTime getDateModified() {
			return dateModified;
		}

		public void setDateModified(ZonedDateTime dateUpdated) {
			this.dateModified = dateUpdated;
		}

		public ZonedDateTime getLastCrawled() {
			return lastCrawled;
		}

		public void setLastCrawled(ZonedDateTime lastCrawled) {
			this.lastCrawled = lastCrawled;
		}

		public String getLastCrawledKey() {
			return lastCrawledKey;
		}

		public void setLastCrawledKey(String lastCrawledKey) {
			this.lastCrawledKey = lastCrawledKey;
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
	
	public static abstract class AbstractAuthenticatedFeed<C extends ICredentials> extends AbstractFeed {
		
		protected C credentials;

		public AbstractAuthenticatedFeed(String type) {
			super(type);
		}

		public abstract C getCredentials();

		public void setCredentials(C credentials) {
			this.credentials = credentials;
		}

	}
	
	@Entity
	@DiscriminatorValue(OAUTH1)
	public static class Oauth1AuthenticatedFeed extends AbstractAuthenticatedFeed<Oauth1Credentials> {

		public Oauth1AuthenticatedFeed() {
			super(OAUTH1);
		}
		
		@ManyToOne
		@JoinColumn(name = "cred_oauth1_id")
		@JsonIgnore
		public Oauth1Credentials getCredentials() {
			return credentials;
		}
				
	}
	
	@Entity
	@DiscriminatorValue(OAUTH2)
	public static class Oauth2AuthenticatedFeed extends AbstractAuthenticatedFeed<Oauth2Credentials> {

		public Oauth2AuthenticatedFeed() {
			super(OAUTH2);
		}
		
		@ManyToOne
		@JoinColumn(name = "cred_oauth2_id")
		@JsonIgnore
		public Oauth2Credentials getCredentials() {
			return credentials;
		}
				
	}

}
