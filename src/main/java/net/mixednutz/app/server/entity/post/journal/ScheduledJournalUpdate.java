package net.mixednutz.app.server.entity.post.journal;

import java.time.ZonedDateTime;

import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.mixednutz.app.server.entity.post.AbstractScheduledPostUpdate;
import net.mixednutz.app.server.entity.post.Post;

@Entity
@DiscriminatorValue(ScheduledJournalUpdate.SCHEDULED_UPDATE_TYPE)
public class ScheduledJournalUpdate extends AbstractScheduledPostUpdate {
	
	public static final String SCHEDULED_UPDATE_TYPE = "Journal";
	
	private Journal journal;
	
	public ScheduledJournalUpdate() {
		super(SCHEDULED_UPDATE_TYPE);
	}
	
	public ScheduledJournalUpdate(ZonedDateTime effectiveDate) {
		super(SCHEDULED_UPDATE_TYPE, effectiveDate);
	}

	public ScheduledJournalUpdate(ZonedDateTime effectiveDate, Journal journal) {
		this(effectiveDate);
		this.journal = journal;
	}

	public static ScheduledJournalUpdate with(ZonedDateTime effectiveDate, Journal journal) {
		return new ScheduledJournalUpdate(effectiveDate, journal);
	}

	@ManyToOne()
	@JoinColumn(name="journal_id",
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}
	
	@Override
	public Journal post() {
		return journal;
	}
	
	@Override
	public Post<?> inReplyTo() {
		return null;
	}

}
