package net.mixednutz.app.server.manager.post;

import java.time.Instant;

import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;

public interface PostManager<P extends Post<C>, C extends PostComment> {
	
	IPage<InternalTimelineElement,Instant> getTimelineInternal(
			User owner, IPageRequest<String> paging);
	
	IPage<InternalTimelineElement,Instant> getUserTimelineInternal(
			User owner, User viewer, IPageRequest<String> paging);

}
