package net.mixednutz.app.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="Emoji")
public class Emoji  {
	
	private String id;
	private Integer sortId;
	private String description;
	private EmojiSubCategory subCategory;
	
	
	@Column(name="emoji_id")
	@GeneratedValue(generator="system-native")
	@GenericGenerator(name="system-native", strategy = "native")
	public Integer getSortId() {
		return sortId;
	}
	public void setSortId(Integer sortId) {
		this.sortId = sortId;
	}
	@Id
	@Column(name="htmlEntity")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne()
	public EmojiSubCategory getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(EmojiSubCategory subCategory) {
		this.subCategory = subCategory;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String name) {
		this.description = name;
	}
	@Transient
	public String getHtmlCode() {
		if (getId()!=null) {
			StringBuffer buffer = new StringBuffer();
			for (String part: getId().split("_")) {
				buffer.append("&#x");
				buffer.append(part);
				buffer.append(";");
			}
			return buffer.toString();
		}
		return null;
	}
	
}
