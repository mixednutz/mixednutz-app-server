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
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.mixednutz.app.server.entity.Emoji;
import net.mixednutz.app.server.entity.User;

@Entity
@Table(name="reaction")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type",
    discriminatorType=DiscriminatorType.STRING
)
public abstract class AbstractReaction {

	private Long id;
	private Instant dateCreated;
	private Long userIdCreated;
	private String type;
	private Emoji emoji;
	private String emojiId;
	private Long reactorId;
	private User reactor;
	
	public AbstractReaction(String type) {
		this(type, null, null);
	}

	public AbstractReaction(String type, String emojiId) {
		this(type, emojiId, null);
	}
	
	public AbstractReaction(String type, String emojiId, Long reactorId) {
		super();
		this.type = type;
		this.emojiId = emojiId;
		this.reactorId = reactorId;
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
	@Column(name="reaction_id")
	@GeneratedValue(generator="system-native")
	@GenericGenerator(name="system-native", strategy = "native")
	public Long getId(){
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="type",insertable=false, updatable=false)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@ManyToOne
	@JoinColumn(name="emoji_code",insertable=false, updatable=false, 
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Emoji getEmoji() {
		return emoji;
	}

	public void setEmoji(Emoji emoji) {
		this.emoji = emoji;
	}

	@Column(name="reactor_id", insertable=true, updatable=false)
	public Long getReactorId() {
		return reactorId;
	}

	public void setReactorId(Long reactorId) {
		this.reactorId = reactorId;
	}
	
	@ManyToOne
	@JoinColumn(name="reactor_id", insertable=false, updatable=false,
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@NotFound(action=NotFoundAction.IGNORE)
	public User getReactor() {
		return reactor;
	}

	public void setReactor(User reactor) {
		this.reactor = reactor;
	}

	@Column(name="emoji_code", insertable=true, updatable=false)
	public String getEmojiId() {
		return emojiId;
	}

	public void setEmojiId(String emojiId) {
		this.emojiId = emojiId;
	}

	@Column(name="date_created", insertable=true, updatable=false)
	public Instant getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Instant dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Column(name="user_created", insertable=true, updatable=false)
	public Long getUserIdCreated() {
		return userIdCreated;
	}
	
	public void setUserIdCreated(Long userId) {
		this.userIdCreated = userId;
	}
	
	@Transient
	public String getEmojiCode() {
		if (emoji!=null) {
			return emoji.getHtmlCode();
		}
		return null;
	}
	
	@Transient
	public abstract String getParentSubject();
	
	@Transient
	public abstract Long getParentAuthorId();
	
	@Transient
	public abstract String getParentUri();
	
}
