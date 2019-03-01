package net.mixednutz.app.server.entity.post;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.mixednutz.app.server.entity.User;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractPostView {

	private ViewPK id;
	private User viewer;
	private Date lastViewed;
	
	@EmbeddedId
	public ViewPK getId() {
		return id;
	}
	
	public void setId(ViewPK id) {
		this.id = id;
	}

	@Column(name="lastviewed")
	public Date getLastViewed() {
		return lastViewed;
	}

	public void setLastViewed(Date lastViewed) {
		this.lastViewed = lastViewed;
	}
	
	@ManyToOne()
	@JoinColumn(name="viewer_id",updatable=false, insertable=false)
	public User getViewer() {
		return viewer;
	}
	
	public void setViewer(User viewer) {
		this.viewer = viewer;
	}

	@Embeddable
	public static class ViewPK implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public static final String POST_ID_COLUMN_NAME = "post_id";
		
		private Long postId;
		private Long accountId;

		public ViewPK() {
			super();
		}
		
		public ViewPK(Long postId, Long accountId) {
			super();
			this.postId = postId;
			this.accountId = accountId;
		}

		@Column(name=POST_ID_COLUMN_NAME, insertable=true, updatable=false)
		public Long getPostId() {
			return postId;
		}

		public void setPostId(Long postId) {
			this.postId = postId;
		}

		@Column(name="account_id")
		public Long getAccountId() {
			return accountId;
		}

		public void setAccountId(Long accountId) {
			this.accountId = accountId;
		}
		
		@Override
		public int hashCode() {
			return accountId.hashCode() + postId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ViewPK) {
				ViewPK other = (ViewPK) obj;
				return (other.accountId.equals(accountId) && 
						other.postId.equals(postId));
			}
			return false;
		}
		
	}
	
}
