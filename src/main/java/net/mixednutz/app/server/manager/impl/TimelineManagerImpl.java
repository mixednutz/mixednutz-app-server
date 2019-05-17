package net.mixednutz.app.server.manager.impl;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.model.PageBuilder.GetTokenCallback;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.TimelineManager;
import net.mixednutz.app.server.manager.post.journal.JournalManager;

@Transactional
@Service
public class TimelineManagerImpl implements TimelineManager {
	
	@Autowired
	private JournalManager journalManager;

	@Override
	public IPage<? extends ITimelineElement, Instant> getHomeTimeline(User user, 
			PageRequest<String> paging) {
		
		List<InternalTimelineElement> timeline = new ArrayList<>();
		
		net.mixednutz.api.core.model.PageRequest<Instant> pageRequest;
		if (paging.getStart()==null) {
			pageRequest = net.mixednutz.api.core.model.PageRequest.first(
					paging.getPageSize(), paging.getDirection(), Instant.class);
		} else {
			ZonedDateTime start = ZonedDateTime.parse(paging.getStart());
			pageRequest = net.mixednutz.api.core.model.PageRequest.next(
					start.toInstant(), paging.getPageSize(), paging.getDirection());
		}
		
		// Query Journals
		final IPage<InternalTimelineElement, Instant> journals = journalManager.getTimelineInternal(
				user, paging);
		for (InternalTimelineElement journal : journals.getItems()) {
			timeline.add(journal);
		}
		
		return new PageBuilder<InternalTimelineElement, Instant>()
				.setItems(timeline)
				.setPageRequest(pageRequest)
				.setTrimToPageSize(true)
				.setReSortComparator(new Comparator<InternalTimelineElement>(){
					@Override
					public int compare(InternalTimelineElement o1, InternalTimelineElement o2) {
						return -o1.getPostedOnDate().compareTo(o2.getPostedOnDate());
					}})
				.setTokenCallback(new GetTokenCallback<InternalTimelineElement, Instant>(){
					@Override
					public Instant getToken(InternalTimelineElement item) {
						return item.getPostedOnDate().toInstant();
					}})
				.build();
	}

}
