package net.mixednutz.app.server.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;

/**
 * Associates a Feed to a Saved element.  This enforces the idea that
 * users can only see content that they subscribe to.
 * 
 * @author apfesta
 *
 */
@Entity
@Table(name = "x_feed_content")
public class ExternalFeedContent {

	private ExternalFeedContentPK id;
	private AbstractFeed feed;
	private ExternalFeedTimelineElement element;
	private TimelineType type;
	
	
	public ExternalFeedContent() {
	}
	public ExternalFeedContent(ExternalFeedContentPK id) {
		this.id = id;
	}
	public ExternalFeedContent(AbstractFeed feed, 
			ExternalFeedTimelineElement element, TimelineType type) {
		this(new ExternalFeedContentPK(feed.getFeedId(), element.getUri()));
		this.feed = feed;
		this.element = element;
		this.type = type;
	}
	@Id
	public ExternalFeedContentPK getId() {
		return id;
	}
	public void setId(ExternalFeedContentPK id) {
		this.id = id;
	}
	@ManyToOne
	@JoinColumn(name="feed_id", insertable=false, updatable=false)
	public AbstractFeed getFeed() {
		return feed;
	}
	public void setFeed(AbstractFeed feed) {
		this.feed = feed;
	}
	@ManyToOne
	@JoinColumn(name="url", insertable=false, updatable=false)
	public ExternalFeedTimelineElement getElement() {
		return element;
	}
	public void setElement(ExternalFeedTimelineElement element) {
		this.element = element;
	}
	@Enumerated(EnumType.STRING)
	public TimelineType getType() {
		return type;
	}
	public void setType(TimelineType type) {
		this.type = type;
	}
	
	public enum TimelineType {
		HOME,
		USER
	}


	@Embeddable
	public static class ExternalFeedContentPK implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3807293673765490224L;
		private Long feedId;
		private String uri;
		
		public ExternalFeedContentPK(Long feedId, String uri) {
			super();
			this.feedId = feedId;
			this.uri = uri;
		}
		
		public ExternalFeedContentPK() {
			super();
		}

		@Column(name="feed_id", insertable=true, updatable=true)
		public Long getFeedId() {
			return feedId;
		}
		public void setFeedId(Long feedId) {
			this.feedId = feedId;
		}
		@Column(name="url", insertable=true, updatable=true)
		public String getUri() {
			return uri;
		}
		public void setUri(String uri) {
			this.uri = uri;
		}
		@Override
		public int hashCode() {
			return feedId.hashCode()+uri.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ExternalFeedContentPK) {
				ExternalFeedContentPK obj2 = (ExternalFeedContentPK) obj;
				return (this.feedId.equals(obj2.feedId)&&
						this.uri.equals(obj2.uri));
			}
			return false;
		}
		
	}
	
}
