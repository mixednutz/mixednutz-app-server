package net.mixednutz.app.server.entity;

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

import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.provider.ICredentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;

public class ExternalFeeds {
	
	@Entity
	@Table(name = "feed")
	@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
	@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
	public static abstract class AbstractFeed {

		private Long feedId;
		private String feedInfoClassName;
		private User user;
		private String name;
		private String imageUrl;
		private String type;
		private ZonedDateTime dateCreated;
		private ZonedDateTime dateModified;

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

		@Column(name="feed_info_class")
		public String getFeedInfoClassName() {
			return feedInfoClassName;
		}

		public void setFeedInfoClassName(String feedInfoClassName) {
			this.feedInfoClassName = feedInfoClassName;
		}
		
		@Transient
		public Class<? extends INetworkInfoSmall> getFeedInfoClass() {
			try {
				return Class.forName(feedInfoClassName).asSubclass(INetworkInfoSmall.class);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public void setFeedInfoClass(Class<? extends INetworkInfoSmall> feedInfoClass) {
			this.feedInfoClassName = feedInfoClass.getName();
		}

		@Column(name="name")
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Column(name="image_url")
		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
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
	@DiscriminatorValue(Oauth1AuthenticatedFeed.TYPE)
	public static class Oauth1AuthenticatedFeed extends AbstractAuthenticatedFeed<Oauth1Credentials> {

		public static final String TYPE = "oauth1";
		
		public Oauth1AuthenticatedFeed() {
			super(TYPE);
		}
		
		@ManyToOne
		@JoinColumn(name = "cred_oauth1_id")
		@JsonIgnore
		public Oauth1Credentials getCredentials() {
			return credentials;
		}
				
	}
	
	@Entity
	@DiscriminatorValue(Oauth2AuthenticatedFeed.TYPE)
	public static class Oauth2AuthenticatedFeed extends AbstractAuthenticatedFeed<Oauth2Credentials> {

		public static final String TYPE = "oauth2";
		
		public Oauth2AuthenticatedFeed() {
			super(TYPE);
		}
		
		@ManyToOne
		@JoinColumn(name = "cred_oauth2_id")
		@JsonIgnore
		public Oauth2Credentials getCredentials() {
			return credentials;
		}
				
	}

}
