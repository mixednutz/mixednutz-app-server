package net.mixednutz.app.server.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name="Comment")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type",
    discriminatorType=DiscriminatorType.STRING
)
public abstract class AbstractPostComment extends AbstractComment
	 implements PostComment {

	private Long commentId;
//	private AbstractPostComment inReplyTo;
	private String type;

	public AbstractPostComment(String type) {
		super();
		this.type = type;
	}

	@Id
	public Long getCommentId() {
		return commentId;
	}
	
	@Column(name="type",insertable=false, updatable=false)
	public String getType() {
		return type;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

//	public AbstractPostComment getInReplyTo() {
//		return inReplyTo;
//	}
//
//	public void setInReplyTo(AbstractPostComment inReplyTo) {
//		this.inReplyTo = inReplyTo;
//	}
//
	@Override
	public <C extends Comment> void setParentComment(C parentComment) {
//		this.inReplyTo = (AbstractPostComment) parentComment;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
}
