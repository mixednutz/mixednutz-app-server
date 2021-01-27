package net.mixednutz.app.server.entity.post.journal;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import net.mixednutz.app.server.entity.post.AbstractScheduledPost;
import net.mixednutz.app.server.entity.post.Post;

@Entity
@DiscriminatorValue(ScheduledJournal.SCHEDULED_POST_TYPE)
public class ScheduledJournal extends AbstractScheduledPost {
	
	public static final String SCHEDULED_POST_TYPE = "Journal";

	private Journal journal;
	
	public ScheduledJournal() {
		super(SCHEDULED_POST_TYPE);
	}

	@OneToOne(mappedBy="scheduled", targetEntity=Journal.class)
	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}
	
	@Override
	public Post<?> post() {
		return journal;
	}

}
