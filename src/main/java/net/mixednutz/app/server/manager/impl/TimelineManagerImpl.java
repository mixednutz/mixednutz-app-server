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
import net.mixednutz.app.server.manager.TimelineElementManager;
import net.mixednutz.app.server.manager.TimelineManager;
import net.mixednutz.app.server.manager.post.CommentManager;

@Transactional
@Service
public class TimelineManagerImpl implements TimelineManager {
	
	@Autowired
	private List<TimelineElementManager> timelineElementManagers;

	@Override
	public IPage<? extends ITimelineElement, Instant> getHomeTimeline(User user, 
			PageRequest<String> paging) {
		
		List<InternalTimelineElement> timeline = new ArrayList<>();
		
		PageRequest<Instant> pageRequest = PageRequest.convert(paging, Instant.class,
				(str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		
		// Query Posts
		
		for (TimelineElementManager postManager: timelineElementManagers) {
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
			PageRequest<String> paging, boolean includeCommentsAsElements) {
		List<InternalTimelineElement> timeline = new ArrayList<>();
		
		PageRequest<Instant> pageRequest = PageRequest.convert(paging, Instant.class,
				(str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
				
		// Query Posts
		timelineElementManagers.stream()
			.filter(manager->
				includeCommentsAsElements || !(manager instanceof CommentManager))
			.map(postManager->postManager.getUserTimelineInternal(profileUser, viewer, paging))
			.flatMap(journals->journals.getItems().stream())
			.forEach(journal->timeline.add(journal));
				
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

	@Override
	public long countUserTimeline(User profileUser, User viewer) {
		return timelineElementManagers.stream()
				.mapToLong(postManager -> postManager.countUserTimelineInteral(profileUser, viewer))
				.sum();
	}

}
