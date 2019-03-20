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
	
	protected abstract <T extends ITimelineElement> T toTimelineElement(P post, User viewer, Class<T> returnType);
	
	public IPage<? extends ITimelineElement,Instant> getTimelineInternal(
			User owner, IPageRequest<String> paging) {
			
		List<P> contents = null;
		net.mixednutz.api.core.model.PageRequest<Instant> pageRequest;
		if (paging.getStart()==null) {
			pageRequest = net.mixednutz.api.core.model.PageRequest.first(
					paging.getPageSize(), paging.getDirection(), Instant.class);
			contents = postRepository.getMyPostsLessThan(owner, ZonedDateTime.now(), 
					PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = ZonedDateTime.parse(paging.getStart());
			pageRequest = net.mixednutz.api.core.model.PageRequest.next(
					start.toInstant(), paging.getPageSize(), paging.getDirection());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = postRepository.getMyPostsLessThan(owner, start, 
						PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = postRepository.getMyPostsGreaterThan(owner, start, 
						PageRequest.of(0, paging.getPageSize()));
			}
		}
		List<ITimelineElement> elements = new ArrayList<>();
		for (P content: contents) {
			elements.add(toTimelineElement(content, null, ITimelineElement.class));
		}
		
		return new PageBuilder<ITimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTokenCallback(new GetTokenCallback<ITimelineElement, Instant>(){
				@Override
				public Instant getToken(ITimelineElement item) {
					return item.getPostedOnDate().toInstant();
				}})
			.build();
	}
	
	public IPage<? extends ITimelineElement,Instant> getUserTimelineInternal(
			User owner, User viewer, IPageRequest<String> paging) {
			
		List<P> contents = null;
		net.mixednutz.api.core.model.PageRequest<Instant> pageRequest;
		if (paging.getStart()==null) {
			pageRequest = net.mixednutz.api.core.model.PageRequest.first(
					paging.getPageSize(), paging.getDirection(), Instant.class);
			contents = postRepository.getUsersPostsByDateCreatedLessThanEquals(
					owner, viewer, ZonedDateTime.now(), 
					PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = ZonedDateTime.parse(paging.getStart());
			pageRequest = net.mixednutz.api.core.model.PageRequest.next(
					start.toInstant(), paging.getPageSize(), paging.getDirection());
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
		List<ITimelineElement> elements = new ArrayList<>();
		for (P content: contents) {
			elements.add(toTimelineElement(content, viewer, ITimelineElement.class));
		}
		
		return new PageBuilder<ITimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTokenCallback(new GetTokenCallback<ITimelineElement, Instant>(){
				@Override
				public Instant getToken(ITimelineElement item) {
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
