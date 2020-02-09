package net.mixednutz.app.server.entity;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.mixednutz.api.core.model.TimelineElement;
import net.mixednutz.api.model.IAction;
import net.mixednutz.api.model.IAlternateLink;
import net.mixednutz.api.model.IGroupSmall;
import net.mixednutz.api.model.IReactionCount;
import net.mixednutz.api.model.IReshareCount;
import net.mixednutz.api.model.ITagCount;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.api.model.IUserSmall;

@Entity
@Table(name = "x_content")
public class ExternalFeedTimelineElement implements ITimelineElement {
		
	//Deserialized Element
	private ITimelineElement element;
	
	//We store the entire element as JSON
	private String elementJson;
	
	//Class for deserialization
	private String elementClassName;
	private Class<? extends ITimelineElement> elementClass;
	
	//But we extract the following properties for search and index
	private String uri;
	private ZonedDateTime providerPostedOnDate;
	
	//Date we saved this record
	private ZonedDateTime crawledDateTime;
	
	
	public ExternalFeedTimelineElement() {
	}
	
	public ExternalFeedTimelineElement(ITimelineElement element) {
		this.element = element;
		this.elementClass = element.getClass();
		this.uri = element.getUri();
		this.providerPostedOnDate = element.getPostedOnDate();
		this.serializeElement();
	}
	
	@PrePersist
	public void onPersist() {
		this.crawledDateTime = ZonedDateTime.now();
	}
	
	@PostLoad
	public void onLoad() {
		this.deserializeElement();
	}

	@Transient
	public ITimelineElement getElement() {
		return element;
	}
	public void setElement(ITimelineElement element) {
		this.element = element;
	}
	@Lob
	@Column(name="element_json")
//	@Column(name="element_json", columnDefinition="clob")
	public String getElementJson() {
		return elementJson;
	}
	public void setElementJson(String elementJson) {
		this.elementJson = elementJson;
	}
	@Column(name="element_java_class")
	public String getElementClassName() {
		return elementClassName;
	}
	public void setElementClassName(String elementClassName) {
		this.elementClassName = elementClassName;
	}
	@Transient
	public Class<? extends ITimelineElement> getElementClass() {
		return elementClass;
	}
	public void setElementClass(Class<? extends ITimelineElement> elementClass) {
		this.elementClass = elementClass;
	}
	
	public void deserializeElement() {
		elementClass = this.fromClassName();
		element = this.fromJson();
	}
	public void serializeElement() {
		elementClassName = this.toClassName();
		elementJson = this.toJson();
	}

	@Id
	@Column(name="uri")
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	@Column(name="posted_on_date")
	public ZonedDateTime getProviderPostedOnDate() {
		return providerPostedOnDate;
	}
	public void setProviderPostedOnDate(ZonedDateTime providerPostedOnDate) {
		this.providerPostedOnDate = providerPostedOnDate;
	}
	@Column(name="crawled_date")
	public ZonedDateTime getCrawledDateTime() {
		return crawledDateTime;
	}
	public void setCrawledDateTime(ZonedDateTime crawledDateTime) {
		this.crawledDateTime = crawledDateTime;
	}
	
	private ITimelineElement fromJson() {
		return JsonUtils.fromJson(this.elementJson, this.elementClass);
	}
	
	private String toJson() {
		return JsonUtils.toJson(this.element);
	}
	
	private Class<? extends ITimelineElement> fromClassName() {
		return fromClassName(this.elementClassName);
	}
	
	private static Class<? extends ITimelineElement> fromClassName(String className) {
		try {
			return Class.forName(className).asSubclass(ITimelineElement.class);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return TimelineElement.class;
		}
	}
	
	private String toClassName() {
		return this.element.getClass().getName();
	}
	

	@Transient
	public Serializable getProviderId() {
		return element.getProviderId();
	}

	@Transient
	public Type getType() {
		return element.getType();
	}

	@Transient
	public IUserSmall getPostedByUser() {
		return element.getPostedByUser();
	}

	@Transient
	public IGroupSmall getPostedToGroup() {
		return element.getPostedToGroup();
	}

	@Transient
	public String getUrl() {
		return element.getUrl();
	}

	@Transient
	public ZonedDateTime getPostedOnDate() {
		return element.getPostedOnDate();
	}

	@Transient
	public List<? extends IAction> getActions() {
		return element.getActions();
	}

	@Transient
	public ZonedDateTime getUpdatedOnDate() {
		return element.getUpdatedOnDate();
	}

	@Transient
	public Serializable getPaginationId() {
		return element.getPaginationId();
	}

	@Transient
	public String getTitle() {
		return element.getTitle();
	}

	@Transient
	public String getDescription() {
		return element.getDescription();
	}

	@Transient
	public Collection<? extends IAlternateLink> getAlternateLinks() {
		return element.getAlternateLinks();
	}

	@Transient
	public List<? extends IReactionCount> getReactions() {
		return element.getReactions();
	}
	
	@Transient
	public List<? extends ITagCount> getTags() {
		return element.getTags();
	}

	@Transient
	public List<? extends IReshareCount> getReshares() {
		return element.getReshares();
	}
	
}
