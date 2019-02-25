package net.mixednutz.app.server.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;


/**
 * @author Andy
 */
@MappedSuperclass
public abstract class AbstractJournal<C extends AbstractPostComment> extends AbstractPost<C>   {
	
	private String subject;
	private String subjectKey; //For URL lookups
	private String body; 
		
	
	@Transient
	@Override
	public String getUri() {
		return null;
	}
	@Column(name="subject")
	public String getSubject() {
		return this.subject;
	}
	@Column(name="subject_key")
	public String getSubjectKey() {
		return subjectKey;
	}
//	@Column(name="body", columnDefinition="LONGTEXT")
	public String getBody() {
		return body;
	}		
	public void setBody(String body) {
		this.body = body;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setSubjectKey(String subjectKey) {
		this.subjectKey = subjectKey;
	}	
}
