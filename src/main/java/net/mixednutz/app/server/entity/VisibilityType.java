package net.mixednutz.app.server.entity;

/**
 * The basis for site level security based on the relation with other users
 * 
 * <ul>
 * <li>PRIVATE - Only the author of the entity can see it</li>
 * <li>SELECT_FOLLOWERS - Only a select list of followers can see it</li>
 * <li>ALL_FOLLOWERS - All followers can see it</li>
 * <li>FRIEND_GROUPS - One or more groups of friends (mutual followers) can see it</li>
 * <li>ALL_FRIENDS - All friends (mutual followers) can see it
 * <li>ALL_USERS - All authenticated users can see it
 * <li>WORLD - Everyone including unauthenticated users can see it
 * </ul>
 * 
 * @author apfesta
 *
 */
public enum VisibilityType {

	PRIVATE, 
	SELECT_FOLLOWERS, 
	FRIEND_GROUPS, 
	ALL_FRIENDS, 
	ALL_FOLLOWERS, 
	ALL_USERS,
	WORLD
		
}
