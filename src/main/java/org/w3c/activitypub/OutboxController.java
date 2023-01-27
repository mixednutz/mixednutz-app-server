package org.w3c.activitypub;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.activitystreams.model.LinkImpl;
import org.w3c.activitystreams.model.OrderedCollectionImpl;
import org.w3c.activitystreams.model.OrderedCollectionPageImpl;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.TimelineManager;
import net.mixednutz.app.server.repository.UserRepository;

@RestController
@RequestMapping(URI_PREFIX)
public class OutboxController {
	
	public static final String USER_OUTBOX_ENDPOINT = 
			"/{username}/outbox";
	public static final String USER_OUTBOX_NEXTPAGE_ENDPOINT = 
			"/{username}/outbox/next";
	
	public static final String PAGE_SIZE_STR = "30";
		
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TimelineManager timelineManager;
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private ActivityPubManager activityPubManager;
	
	
	@RequestMapping(value={USER_OUTBOX_ENDPOINT}, 
			method = RequestMethod.GET)
	public OrderedCollectionImpl getUserOutbox(
			@PathVariable String username,
			@AuthenticationPrincipal User user, HttpServletRequest request) {
		return toOrderedCollection(PageRequest.first(30, Direction.LESS_THAN, String.class), username);
	}
	
	@RequestMapping(value={USER_OUTBOX_NEXTPAGE_ENDPOINT}, 
			method = RequestMethod.GET,
			params = {"!start"})
	public OrderedCollectionPageImpl getUserOutboxFirstPage(
			@PathVariable String username,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user, HttpServletRequest request) {
		
		return getUserOutbox(username, 
				PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				pageSize, user, request);
	}
	
	@RequestMapping(value={USER_OUTBOX_NEXTPAGE_ENDPOINT}, 
			method = RequestMethod.GET,
			params = {"start"})
	public OrderedCollectionPageImpl getUserOutboxNextPage(
			@PathVariable String username,
			@RequestParam(value="start") String start, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user, HttpServletRequest request) {
		
		return getUserOutbox(username, 
				PageRequest.next(start, pageSize, Direction.LESS_THAN),
				pageSize, user, request);
	}

	protected OrderedCollectionPageImpl getUserOutbox(
			String username,
			PageRequest<String> prevPage, 
			int pageSize,
			User user, HttpServletRequest request) {
		
		URI collectionId = UriComponentsBuilder
			.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+USER_OUTBOX_ENDPOINT)
			.buildAndExpand(Map.of("username",username)).toUri();
		
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		
		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		final long totalItems = timelineManager.countUserTimeline(profileUser.get(), user);
		
		final IPage<? extends ITimelineElement,Instant> internalContent = 
				timelineManager.getUserTimeline(profileUser.get(), user, prevPage, 
						false);
				
		return toOrderedCollectionPage(collectionId, request, internalContent, totalItems, username);
	}
	
	protected OrderedCollectionImpl toOrderedCollection(
			PageRequest<String> firstPage, 
			String username) {
		OrderedCollectionImpl orderedcollection = new OrderedCollectionImpl();
		activityPubManager.initRoot(orderedcollection);
		orderedcollection.setId(UriComponentsBuilder
				.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+USER_OUTBOX_ENDPOINT)
				.buildAndExpand(Map.of("username",username)).toUri());
		orderedcollection.setFirst(new LinkImpl(UriComponentsBuilder
				.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+USER_OUTBOX_NEXTPAGE_ENDPOINT)
				.buildAndExpand(Map.of("username",username)).toUri()));
		return orderedcollection;
	}
	
	protected OrderedCollectionPageImpl toOrderedCollectionPage(
			URI partOf,
			HttpServletRequest request, 
			IPage<? extends ITimelineElement,Instant> page, long totalItems,
			String username) {
			
		OrderedCollectionPageImpl orderedcollection = new OrderedCollectionPageImpl();
		activityPubManager.initRoot(orderedcollection);
		orderedcollection.setItems(page.getItems().stream()
				.map(element->activityPubManager.toCreate(element, username, request))
				.collect(Collectors.toList()));
		orderedcollection.setTotalItems(totalItems); 
		orderedcollection.setPartOf(partOf);
		orderedcollection.setId(UriComponentsBuilder
				.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+USER_OUTBOX_NEXTPAGE_ENDPOINT)
				.buildAndExpand(Map.of("username",username)).toUri());
		if (page.hasNext()) {
			orderedcollection.setNext(new LinkImpl(UriComponentsBuilder
					.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+USER_OUTBOX_NEXTPAGE_ENDPOINT)
					.queryParam("start", page.getNextPage().getStart())
					.queryParam("pageSize", page.getNextPage().getPageSize())
					.buildAndExpand(Map.of("username",username)).toUri()));
		}
		return orderedcollection;
	}
	

}
