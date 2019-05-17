package net.mixednutz.app.server.manager;

import java.time.Instant;

import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.User;

public interface TimelineManager {
	
	/**
	 * Return objects from a user's friends-timeline
	 * @param user
	 * @param pageSize
	 * @param paging
	 * @return
	 */
	public IPage<? extends ITimelineElement,Instant> getHomeTimeline(User user, PageRequest<String> paging);

}
