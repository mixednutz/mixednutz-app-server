/**
 * 
 */
package net.mixednutz.app.server.entity.post;

import java.time.ZonedDateTime;
import java.util.List;

import net.mixednutz.app.server.entity.User;

/**
 * @author Andy
 *
 */
public interface Post<C extends PostComment> {
	
	Long getId();
	void setId(Long id);
	
	String getDescription();
	void setDescription(String description);
	
	User getAuthor();
	void setAuthor(User author);
	
	void setOwner(User owner);
	
	ZonedDateTime getDateCreated();
	void setDateCreated(ZonedDateTime timestamp);
	
	void setCommentsAllowed(boolean commentsAllowed);
	
	void setComments(List<C> comments);
	
	String getUri();

}
