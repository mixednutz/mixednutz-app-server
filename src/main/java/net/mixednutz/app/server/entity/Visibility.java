package net.mixednutz.app.server.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;


/**
 * Common properties or fields for entities that have visibility attributes.
 * 
 * @see VisibilityType
 * @author apfesta
 *
 */
@Embeddable
public class Visibility {
	
	private VisibilityType visibilityType;
	private Set<User> selectFollowers;
//	private Set<FGroup> friendGroups;

	/**
	 * Default Constructor
	 */
	public Visibility() {
		super();
	}
	/**
	 * All possible arguments constructor
	 * 
	 * @param visibilityType
	 * @param selectFollowers
	 */
	private Visibility(
			VisibilityType visibilityType, 
			Set<User> selectFollowers) {
		super();
		this.visibilityType = visibilityType;
		this.selectFollowers = selectFollowers;
		if (VisibilityType.SELECT_FOLLOWERS.equals(visibilityType) && 
				(selectFollowers==null||selectFollowers.isEmpty())) {
			throw new IllegalArgumentException(
					"SELECT_FOLLOWERS requires a non-empty set of followers");
		}
	}
	/**
	 * Visibility for PRIVATE, ALL_FOLLOWERS, ALL_FRIENDS, ALL_USERS, WORLD.
	 * Other visibility types use a different constructor because they require
	 * more information.
	 * 
	 * @param visibilityType
	 */
	public Visibility(VisibilityType visibilityType) {
		this(visibilityType, null);
	}
	
	@NotNull
	@Enumerated(value=EnumType.STRING)
	@Column(name="visibility")
	public VisibilityType getVisibilityType() {
		return visibilityType;
	}

	public void setVisibilityType(VisibilityType visibilityType) {
		this.visibilityType = visibilityType;
	}

	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable
	public Set<User> getSelectFollowers() {
		return selectFollowers;
	}

	public void setSelectFollowers(Set<User> selectFollowers) {
		this.selectFollowers = selectFollowers;
	}
	
	public static Visibility asPrivate() {
		return new Visibility(VisibilityType.PRIVATE);
	}
	public static Visibility toWorld() {
		return new Visibility(VisibilityType.WORLD);
	}
	public static Visibility toAllUsers() {
		return new Visibility(VisibilityType.ALL_USERS);
	}
	/**
	 * VisibilityType.SELECT_FOLLOWERS visibility with set of followers.
	 * 
	 * @param selectFollowers
	 */
	public static Visibility toSelectFollowers(Set<User> selectFollowers) {
		return new Visibility(VisibilityType.SELECT_FOLLOWERS, selectFollowers);
	}
	
	
}
