package net.mixednutz.app.server.manager.impl;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.TimelineManager;
import net.mixednutz.app.server.manager.post.PostManager;

@Transactional
@Service
public class TimelineManagerImpl implements TimelineManager {
	
	@Autowired
	private List<PostManager<?,?>> postManagers;

	@Override
	public IPage<? extends ITimelineElement, Instant> getHomeTimeline(User user, 
			PageRequest<String> paging) {
		
		List<InternalTimelineElement> timeline = new ArrayList<>();
		
		PageRequest<Instant> pageRequest = PageRequest.convert(paging, Instant.class,
				(str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		
		// Query Posts
		
		for (PostManager<?,?> postManager: postManagers) {
			final IPage<InternalTimelineElement, Instant> posts = postManager.getTimelineInternal(
					user, paging);
			if (!posts.getItems().isEmpty()) {
				timeline.addAll(posts.getItems());
			}
		}
				
		return new PageBuilder<InternalTimelineElement, Instant>()
				.addItems(timeline)
				.setPageRequest(pageRequest)
				.setTrimToPageSize(true)
				.setReSortComparator((o1, o2) -> {
						return -o1.getPostedOnDate().compareTo(o2.getPostedOnDate());
					})
				.setTokenCallback((item) -> {
						return item.getPostedOnDate().toInstant();
					})
				.build();
	}

	@Override
	public IPage<? extends ITimelineElement, Instant> getUserTimeline(User profileUser, User viewer,
			PageRequest<String> paging) {
		List<InternalTimelineElement> timeline = new ArrayList<>();
		
		PageRequest<Instant> pageRequest = PageRequest.convert(paging, Instant.class,
				(str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
				
		// Query Posts
		for (PostManager<?,?> postManager: postManagers) {
			final IPage<InternalTimelineElement, Instant> journals = 
					postManager.getUserTimelineInternal(profileUser, viewer, paging);
			for (InternalTimelineElement journal : journals.getItems()) {
				timeline.add(journal);
			}
		}
		
		return new PageBuilder<InternalTimelineElement, Instant>()
				.setItems(timeline)
				.setPageRequest(pageRequest)
				.setTrimToPageSize(true)
				.setReSortComparator((o1, o2) -> {
						return -o1.getPostedOnDate().compareTo(o2.getPostedOnDate());
					})
				.setTokenCallback((item) -> {
						return item.getPostedOnDate().toInstant();
					})
				.build();
	}

}
