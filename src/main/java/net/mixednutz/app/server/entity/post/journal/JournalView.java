package net.mixednutz.app.server.entity.post.journal;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.mixednutz.app.server.entity.post.AbstractPostView;

@Entity
public class JournalView extends AbstractPostView {
	
	private Journal journal;

	@ManyToOne
	@JoinColumn(name=ViewPK.POST_ID_COLUMN_NAME, insertable=false, updatable=false)
	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}
	
}
