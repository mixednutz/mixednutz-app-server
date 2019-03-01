package net.mixednutz.app.server.manager.post;

import java.time.Instant;

import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;

public interface PostManager<P extends Post<C>, C extends PostComment> {
	
	IPage<? extends ITimelineElement,Instant> getTimelineInternal(
			User owner, IPageRequest<String> paging);
	
	IPage<? extends ITimelineElement,Instant> getUserTimelineInternal(
			User owner, User viewer, IPageRequest<String> paging);

}
