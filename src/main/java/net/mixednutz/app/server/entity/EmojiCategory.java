package net.mixednutz.app.server.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="Emoji_Category")
public class EmojiCategory {

	private Integer id;
	private String name;
	private List<EmojiSubCategory> subCategories;
	
	@Id
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@OneToMany(mappedBy="parentCategory", cascade=CascadeType.ALL, orphanRemoval=true)
	public List<EmojiSubCategory> getSubCategories() {
		return subCategories;
	}
	public void setSubCategories(List<EmojiSubCategory> subCategories) {
		this.subCategories = subCategories;
	}
		
}
