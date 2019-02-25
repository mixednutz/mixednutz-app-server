package net.mixednutz.app.server.manager;

import java.time.Instant;

import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.Post;
import net.mixednutz.app.server.entity.PostComment;
import net.mixednutz.app.server.entity.User;

public interface PostManager<P extends Post<C>, C extends PostComment> {
	
	IPage<? extends ITimelineElement,Instant> getTimelineInternal(
			User owner, IPageRequest<String> paging);
	
	IPage<? extends ITimelineElement,Instant> getUserTimelineInternal(
			User owner, User viewer, IPageRequest<String> paging);

}
