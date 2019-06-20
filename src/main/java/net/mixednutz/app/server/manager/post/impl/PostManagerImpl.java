package net.mixednutz.app.server.manager.post.impl;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.model.PageBuilder.GetTokenCallback;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractPostView;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.post.PostManager;
import net.mixednutz.app.server.manager.post.PostViewManager;
import net.mixednutz.app.server.repository.PostRepository;

public abstract class PostManagerImpl<P extends Post<C>, C extends PostComment, V extends AbstractPostView> 
	implements PostManager<P,C> {
	
	protected PostRepository<P,C> postRepository;
	
	protected PostViewManager<P,C,V> postViewManager;
		
	@Autowired
	protected ApiManager apiManager;
	
	protected abstract InternalTimelineElement toTimelineElement(P post, User viewer);
	
	public IPage<InternalTimelineElement,Instant> getTimelineInternal(
			User owner, IPageRequest<String> paging) {
			
		List<P> contents = null;
		final net.mixednutz.api.core.model.PageRequest<Instant> pageRequest = net.mixednutz.api.core.model.PageRequest
				.convert(paging, Instant.class, (str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		if (paging.getStart()==null) {
			contents = postRepository.getMyPostsLessThan(owner, ZonedDateTime.now(), 
					PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = ZonedDateTime.from(pageRequest.getStart());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = postRepository.getMyPostsLessThan(owner, start, 
						PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = postRepository.getMyPostsGreaterThan(owner, start, 
						PageRequest.of(0, paging.getPageSize()));
			}
		}
		List<InternalTimelineElement> elements = new ArrayList<>();
		for (P content: contents) {
			elements.add(toTimelineElement(content, null));
		}
		
		return new PageBuilder<InternalTimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTokenCallback(new GetTokenCallback<InternalTimelineElement, Instant>(){
				@Override
				public Instant getToken(InternalTimelineElement item) {
					return item.getPostedOnDate().toInstant();
				}})
			.build();
	}
	
	public IPage<InternalTimelineElement,Instant> getUserTimelineInternal(
			User owner, User viewer, IPageRequest<String> paging) {
			
		List<P> contents = null;
		final net.mixednutz.api.core.model.PageRequest<Instant> pageRequest = net.mixednutz.api.core.model.PageRequest
				.convert(paging, Instant.class, (str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		if (paging.getStart()==null) {
			contents = postRepository.getUsersPostsByDateCreatedLessThanEquals(
					owner, viewer, ZonedDateTime.now(), 
					PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = ZonedDateTime.from(pageRequest.getStart());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = postRepository.getUsersPostsByDateCreatedLessThanEquals(
						owner, viewer, start, 
						PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = postRepository.getUsersPostsByDateCreatedGreaterThan(
						owner, viewer, start, 
						PageRequest.of(0, paging.getPageSize()));
			}
		}
		List<InternalTimelineElement> elements = new ArrayList<>();
		for (P content: contents) {
			elements.add(toTimelineElement(content, viewer));
		}
		
		return new PageBuilder<InternalTimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTokenCallback(new GetTokenCallback<InternalTimelineElement, Instant>(){
				@Override
				public Instant getToken(InternalTimelineElement item) {
					return item.getPostedOnDate().toInstant();
				}})
			.build();
	}
	
	public List<? extends ITimelineElement> getUserTimelineInternal(User user, User viewer, int pageSize) {
		return this.getUserTimelineInternal(user, viewer, 
				net.mixednutz.api.core.model.PageRequest.first(pageSize, Direction.LESS_THAN, String.class)).getItems();
	}
	
	public void incrementViewCount(P post, User viewer) {
		postViewManager.addView(post, viewer);
	}

}
