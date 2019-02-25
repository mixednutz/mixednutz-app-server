package net.mixednutz.app.server.manager.impl;

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
import net.mixednutz.app.server.entity.Post;
import net.mixednutz.app.server.entity.PostComment;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.PostManager;
import net.mixednutz.app.server.repository.PostRepository;

public class PostManagerImpl<P extends Post<C>, C extends PostComment> 
	implements PostManager<P,C> {
	
	private PostRepository<P,C> postRepository;
	
	@Autowired
	private ApiManager apiManager;
	
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
			elements.add(apiManager.toTimelineElement(content));
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
			elements.add(apiManager.toTimelineElement(content));
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

	public void setPostRepository(PostRepository<P, C> postRepository) {
		this.postRepository = postRepository;
	}
	

}
