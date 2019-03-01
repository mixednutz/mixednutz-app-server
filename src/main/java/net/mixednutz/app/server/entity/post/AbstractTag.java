package net.mixednutz.app.server.entity.post;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.mixednutz.app.server.entity.User;

@Entity
@Table(name="tag")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type",
    discriminatorType=DiscriminatorType.STRING
)
public abstract class AbstractTag {

	private Long id;
	private Instant dateCreated;
	private Long userIdCreated;
	private String type;
	private String tag;
	private Long taggerId;
	private User tagger;
	
	
	public AbstractTag(String type) {
		this(type, null, null);
	}

	public AbstractTag(String type, String tag) {
		this(type, tag, null);
	}
	
	public AbstractTag(String type, String tag, Long taggerId) {
		super();
		this.type = type;
		this.tag = tag;
		this.taggerId = taggerId;
	}
	
	@PrePersist
	public void onPersist() {
		dateCreated = Instant.now();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated()) {
			User user = (User) auth.getPrincipal();
			this.userIdCreated = user.getUserId();
		}
	}
	
	@Id
	@Column(name="tag_id")
	@GeneratedValue(generator="system-native")
	@GenericGenerator(name="system-native", strategy = "native")
	public Long getId(){
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Column(name="type",insertable=false, updatable=false)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@Column(name="tagger_id", insertable=true, updatable=false)
	public Long getTaggerId() {
		return taggerId;
	}

	public void setTaggerId(Long authorId) {
		this.taggerId = authorId;
	}

	@ManyToOne
	@JoinColumn(name="tagger_id", insertable=false, updatable=false,
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@NotFound(action=NotFoundAction.IGNORE)
	public User getTagger() {
		return tagger;
	}

	public void setTagger(User tagger) {
		this.tagger = tagger;
	}

	@Column(name="date_created")
	public Instant getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Instant dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Column(name="user_created")
	public Long getUserIdCreated() {
		return userIdCreated;
	}

	public void setUserIdCreated(Long userIdCreated) {
		this.userIdCreated = userIdCreated;
	}
	
}
