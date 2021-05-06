package net.mixednutz.app.server.entity.post.journal;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractReaction;
import net.mixednutz.app.server.entity.post.Post;

@Entity
@DiscriminatorValue(JournalReaction.TYPE)
public class JournalReaction extends AbstractReaction {

	public static final String TYPE = "Journal";
	
	private Journal journal;
	
	public JournalReaction(Journal journal, String emojiId) {
		super(TYPE, emojiId);
		this.journal = journal;
	}
	
	public JournalReaction(Journal journal, String emojiId, User user) {
		super(TYPE, emojiId, user.getUserId());
		this.journal = journal;
	}

	public JournalReaction() {
		super(TYPE);
	}

	@JsonIgnore
	@ManyToOne()
	@JoinColumn(name="journal_id")
	@NotFound(action=NotFoundAction.IGNORE)
	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	@Transient
	public String getParentSubject() {
		return journal.getSubject();
	}

	@Transient
	public Long getParentAuthorId() {
		return journal.getAuthorId();
	}

	@Transient
	public String getParentUri() {
		return journal.getUri();
	}
	
	@Override
	public <P extends Post<?>> void setPost(P post) {
		this.journal = (Journal) post;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transient
	@JsonIgnore
	public <P extends Post<?>> P getPost() {
		return (P) journal;
	}
	
}
