package net.mixednutz.app.server.entity.post.journal;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(name="Journal")
public class Journal extends AbstractJournal<JournalComment> {
	
	private ZonedDateTime publishDate; //date to be published
	private LocalDate publishDateKey; //For URL lookups
	private List<JournalComment> comments;
	private Set<JournalTag> tags;
	private Set<JournalReaction> reactions;
	private Set<JournalView> views;
	private String filteredBody;
	
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
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(mappedBy="journal", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<JournalTag> getTags() {
		return tags;
	}
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(mappedBy="journal", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<JournalReaction> getReactions() {
		return reactions;
	}
	
	@OneToMany(mappedBy="journal", orphanRemoval=true)
	public Set<JournalView> getViews() {
		return views;
	}

	@Transient
	public String getFilteredBody() {
		return filteredBody;
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
	
	public void setTags(Set<JournalTag> tags) {
		this.tags = tags;
	}

	public void setReactions(Set<JournalReaction> reactions) {
		this.reactions = reactions;
	}
	
	public void setViews(Set<JournalView> views) {
		this.views = views;
	}
	
	public void setFilteredBody(String filteredBody) {
		this.filteredBody = filteredBody;
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
		return "/journal/id/"+getId();
	}
	
}
