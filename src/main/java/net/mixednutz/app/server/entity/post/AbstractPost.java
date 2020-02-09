/**
 * 
 */
package net.mixednutz.app.server.entity.post;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.Visibility;

/**
 * @author Andy
 *
 */
@MappedSuperclass
public abstract class AbstractPost<C extends AbstractPostComment> implements Post<C> {
			
	private Long id;
	private String description;
	private ZonedDateTime dateCreated; //creation Date
	private User author;
	private Long authorId;
	private User owner;
	private Long ownerId;
	private Visibility visibility = Visibility.toAllUsers();
		
	boolean commentsAllowed;
	/**
	 * Note: Subclasses should create a getter for this
	 */
	protected List<C> comments;
	

	@PrePersist
	public void onPersist() {
		dateCreated = ZonedDateTime.now();
	}
	
	@Transient
	public abstract String getUri();
	
	@Id
	@GeneratedValue(generator="system-native")
	@GenericGenerator(name="system-native", strategy = "native")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Lob
	@Column(name="description")
	//	@Column(name="description", columnDefinition="LONGTEXT")
	public String getDescription() {
		return description;
	}

	@ManyToOne()
	@JoinColumn(name="author_id", insertable=false, updatable=false, 
			foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@NotFound(action=NotFoundAction.IGNORE)
	public User getAuthor() {
		return author;
	}

	@Column(name="author_id", insertable=true, updatable=false)
	public Long getAuthorId() {
		return authorId;
	}

	@ManyToOne()
	@JoinColumn(name="owner_id",insertable=false, updatable=false, 
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@NotFound(action=NotFoundAction.IGNORE)
	public User getOwner() {
		return owner;
	}

	@Column(name="owner_id", insertable=true, updatable=false)
	public Long getOwnerId() {
		return ownerId;
	}

	@Column(name="timestamp")
	public ZonedDateTime getDateCreated() {
		return dateCreated;
	}
	
	@Column(name="allowcomments")
	public boolean isCommentsAllowed() {
		return commentsAllowed;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setDateCreated(ZonedDateTime timestamp) {
		this.dateCreated = timestamp;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setComments(List<C> comments) {
		this.comments = comments;
	}
		
	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}
		
	public void setCommentsAllowed(boolean commentsAllowed) {
		this.commentsAllowed = commentsAllowed;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}
	

}
