package net.mixednutz.app.server.entity;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name="Journal")
public class Journal extends AbstractJournal<JournalComment> {
	
	private Long journalId;
	
	private ZonedDateTime publishDate; //date to be published
	private LocalDate publishDateKey; //For URL lookups

	private List<JournalComment> comments;
	
	
	@Override
	public void onPersist() {
		super.onPersist();
		if (publishDate!=null) {
			publishDateKey = publishDate.toLocalDate();
		} else {
			//TODO Grab the current user's timezone and do LocalDate.now(zoneId)
			publishDateKey = LocalDate.now();
		}
	}

	@Id
	@Column(name="journal_id")
	@GeneratedValue(generator="system-native")
	@GenericGenerator(name="system-native", strategy = "native")
	public Long getJournalId(){
		return journalId;
	}
		
	@Column(name="publish_date")
	public ZonedDateTime getPublishDate() {
		return publishDate;
	}

	public LocalDate getPublishDateKey() {
		return publishDateKey;
	}

	@OneToMany(mappedBy="journal", fetch=FetchType.EAGER, cascade={CascadeType.REMOVE})
	@OrderBy("dateCreated asc")
	public List<JournalComment> getComments() {
		return comments;
	}

	public void setJournalId(Long journalId) {
		this.journalId = journalId;
	}

	public void setPublishDate(ZonedDateTime publishDate) {
		this.publishDate = publishDate;
	}
	
	public void setComments(List<JournalComment> comments) {
		this.comments = comments;
	}

	public void setPublishDateKey(LocalDate publishDateKey) {
		this.publishDateKey = publishDateKey;
	}

	@Transient
	public String getUri() {
		if (getSubjectKey()!=null && getOwner()!=null && getOwner().getUsername()!=null) {
			return "/"+getOwner().getUsername().replaceAll(" ", "")+
					"/journal/"+
					publishDateKey.get(ChronoField.YEAR)+"/"+
					publishDateKey.get(ChronoField.MONTH_OF_YEAR)+"/"+
					publishDateKey.get(ChronoField.DAY_OF_MONTH)+"/"+
					getSubjectKey();
		}
		return "/journal/id/"+getJournalId();
	}
	
}
