package net.mixednutz.app.server.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class MenuItem {
	
	Long id;
	String name;
	String uri;
	boolean authenticated;
	Long parentId;
	
	MenuItem parent;
	List<MenuItem> submenus;
	
	@Id
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	@Column(name="parent_id", insertable=true, updatable=true)
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	@ManyToOne
	@JoinColumn(name="parent_id", insertable=false, updatable=false,
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public MenuItem getParent() {
		return parent;
	}
	public void setParent(MenuItem parent) {
		this.parent = parent;
	}
	@OneToMany(mappedBy="parent", fetch=FetchType.EAGER)
	public List<MenuItem> getSubmenus() {
		return submenus;
	}
	public void setSubmenus(List<MenuItem> submenus) {
		this.submenus = submenus;
	}

}
