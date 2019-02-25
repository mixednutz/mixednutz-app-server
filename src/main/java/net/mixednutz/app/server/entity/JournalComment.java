package net.mixednutz.app.server.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue(JournalComment.JOURNAL_COMMENT_TYPE)
public class JournalComment extends AbstractPostComment {
	
	public static final String JOURNAL_COMMENT_TYPE = "Journal";
	
	private Journal journal;
	
	public JournalComment(String type) {
		super(JOURNAL_COMMENT_TYPE);
	}

	@JsonIgnore
	@ManyToOne()
	@JoinColumn(name="journal_id",
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Journal getJournal() {
		return journal;
	}
	
	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	@Transient
	public String getUri() {
		return journal.getUri()+"#"+getCommentId();
	}

	@Override
	public <P extends Post<C>, C extends PostComment> void setPost(P post) {
		this.journal = (Journal) post;
	}

}