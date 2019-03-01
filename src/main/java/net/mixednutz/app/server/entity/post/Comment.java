/**
 * 
 */
package net.mixednutz.app.server.entity.post;

import java.time.ZonedDateTime;

import net.mixednutz.app.server.entity.User;

/**
 * @author Andy
 *
 */
public interface Comment {
	
	public void setBody(String body);
	
	public void setAuthor(User author);
	
	public void setDateCreated(ZonedDateTime timestamp);
	
	public <C extends Comment> void setParentComment(C parentComment);

}
