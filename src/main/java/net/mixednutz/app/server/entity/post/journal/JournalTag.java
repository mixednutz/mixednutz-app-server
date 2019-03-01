package net.mixednutz.app.server.entity.post.journal;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractTag;

@Entity
@DiscriminatorValue(JournalTag.TYPE)
public class JournalTag extends AbstractTag {

	public static final String TYPE = "Journal";
	
	private Journal journal;
	
	public JournalTag(Journal journal, String tag) {
		super(TYPE, tag);
		this.journal = journal;
	}
	
	public JournalTag(Journal journal, String tag, User user) {
		super(TYPE, tag, user.getUserId());
		this.journal = journal;
	}

	public JournalTag() {
		super(TYPE);
	}

	@ManyToOne()
	@JoinColumn(name="journal_id")
	@NotFound(action=NotFoundAction.IGNORE)
	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}
	
}
