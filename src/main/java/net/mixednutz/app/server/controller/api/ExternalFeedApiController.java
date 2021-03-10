package net.mixednutz.app.server.controller.api;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.controller.exception.NotAuthorizedException;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.ExternalFeeds;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.UserRepository;


@Controller
@RequestMapping(value="/internal")
public class ExternalFeedApiController {
	
	public static final String PAGE_SIZE_STR = "20";
	public static final int PAGE_SIZE = Integer.parseInt(PAGE_SIZE_STR);
	
	@Autowired
	private ExternalFeedManager feedManager;
	
	@Autowired
	private ExternalFeedRepository feedRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@RequestMapping(value="/feeds", method = RequestMethod.GET)
	public @ResponseBody ExternalFeedsList externalFeeds(@AuthenticationPrincipal User user) {
		final Map<INetworkInfoSmall, List<AbstractFeed>> externalFeeds = 
				feedManager.feedsForUser(user);
		
		//Grab reference data for each feed
		externalFeeds.values().stream().forEach((l)->l.stream().forEach((feed)->{
			feed.setReferenceData(feedManager.referenceData(feed));
		}));
		
		return new ExternalFeedsList(externalFeeds.entrySet().stream()
			.map((entry)->new ExternalFeed(entry.getKey(), entry.getValue(), 
					feedManager.getCompatibleFeedsForCrossposting(entry.getKey())))
			.collect(Collectors.toList()));
	}
	
	@RequestMapping(value="/{username}/feeds", method = RequestMethod.GET)
	public @ResponseBody ExternalFeedsList externalFeeds(
			@PathVariable String username) {
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		
		final Map<INetworkInfoSmall, List<AbstractFeed>> externalFeeds = 
				feedManager.feedsForUserVisibleToWorld(profileUser.get());
		
		//Grab reference data for each feed
		externalFeeds.values().stream().forEach((l)->l.stream().forEach((feed)->{
			feed.setReferenceData(feedManager.referenceData(feed));
		}));
		
		return new ExternalFeedsList(externalFeeds.entrySet().stream()
				.map((entry)->new ExternalFeed(entry.getKey(), entry.getValue(), 
						feedManager.getCompatibleFeedsForCrossposting(entry.getKey())))
				.collect(Collectors.toList()));
	}
	
	@RequestMapping(value="/feeds/timeline", method = RequestMethod.GET)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> apiExternalFeedTimeline(
			@RequestParam("feedId") Long feedId, Authentication auth,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {
		
		return apiExternalFeedTimeline(feedId, auth,
				hashtag, PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				pageSize);
	}
		
	@RequestMapping(value="/feeds/timeline/nextpage", method = RequestMethod.POST)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> apiExternalFeedTimeline(
			@RequestParam("feedId") Long feedId, Authentication auth,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestBody PageRequest<String> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {
		User user = (User) auth.getPrincipal();
		Optional<AbstractFeed> feed = feedRepository.findById(feedId);
		if (!feed.isPresent()) {
			throw new ResourceNotFoundException("Feed not found");
		}
		/*
		 * Only the feed's owner can see the Home Timeline
		 */
		if (!feed.get().getUser().equals(user)) {
			throw new NotAuthorizedException("User "+user.getUsername()+" is not authorized to view this feed.");
		}
		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		return feedManager.getTimeline(feed.get(), 
				hashtag.length()>0?hashtag:null, prevPage);
	}
	
	@RequestMapping(value="/{username}/feeds/timeline", method = RequestMethod.GET)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> apiExternalFeedUserTimeline(
			@PathVariable String username,
			@RequestParam("feedId") Long feedId, 
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user) {
		
		return apiExternalFeedUserTimeline(username, feedId, hashtag, pageSize, 
				PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				user);
	}
		
	@RequestMapping(value="/{username}/feeds/timeline/nextpage", method = RequestMethod.POST)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> apiExternalFeedUserTimeline(
			@PathVariable String username,
			@RequestParam("feedId") Long feedId, 
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@RequestBody PageRequest<String> prevPage, 
			@AuthenticationPrincipal User user) {
		
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		Optional<AbstractFeed> feed = feedRepository.findById(feedId);
		if (!feed.isPresent()) {
			throw new ResourceNotFoundException("Feed not found");
		}

		/*
		 * Other users can see the user time line only if Visibility is set to WORLD
		 * Feed owner can see it if set to PRIVATE
		 * TODO - add other VisibilityTypes
		 */
		if ((user==null && !VisibilityType.WORLD.equals(feed.get().getVisibility())) ||
				VisibilityType.PRIVATE.equals(feed.get().getVisibility()) && user!=null && 
				!user.getUsername().equals(feed.get().getUser().getUsername())) {
			throw new NotAuthorizedException("User "+user.getUsername()+" is not authorized to view this feed.");
		}
		
		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		return feedManager.getUserTimeline(feed.get(), 
				hashtag.length()>0?hashtag:null, prevPage);
	}
	
	@ResponseBody IPage<? extends ITimelineElement,Instant> apiCombinedExternalFeedsUserTimeline(
			@PathVariable String username,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user) {
		
		return apiCombinedExternalFeedsUserTimeline(username, hashtag, pageSize, 
				PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				user);
	}
	
	@ResponseBody IPage<? extends ITimelineElement,Instant> apiCombinedExternalFeedsUserTimeline(
			@PathVariable String username,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@RequestBody PageRequest<String> prevPage, 
			@AuthenticationPrincipal User user) {
		
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		
		//Only get feeds visible to the WORLD:
		List<AbstractFeed> feeds = feedRepository.findByUserAndVisibilityIn(profileUser.get(), 
				Collections.singleton(VisibilityType.WORLD));

		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		return feedManager.getUserTimeline(feeds, 
				hashtag.length()>0?hashtag:null, prevPage);
	}
		
	public class ExternalFeed {
		
		private String name;
		private INetworkInfoSmall feedInfo;
		private List<ExternalFeeds.AbstractFeed> accounts;
		private Collection<String> compatibleMimeTypes;
		
		public ExternalFeed(INetworkInfoSmall feedInfo, List<ExternalFeeds.AbstractFeed> accounts,
				Collection<String> compatibleMimeTypes) {
			super();
			this.name = feedInfo.getId();
			this.accounts = accounts;
			this.feedInfo = feedInfo;
			this.compatibleMimeTypes = compatibleMimeTypes;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<ExternalFeeds.AbstractFeed> getAccounts() {
			return accounts;
		}
		public void setAccounts(List<ExternalFeeds.AbstractFeed> accounts) {
			this.accounts = accounts;
		}
		public INetworkInfoSmall getFeedInfo() {
			return feedInfo;
		}
		public void setFeedInfo(INetworkInfoSmall feedInfo) {
			this.feedInfo = feedInfo;
		}
		public Collection<String> getCompatibleMimeTypes() {
			return compatibleMimeTypes;
		}
		public void setCompatibleMimeTypes(Collection<String> compatibleMimeTypes) {
			this.compatibleMimeTypes = compatibleMimeTypes;
		}

	}
	
	public static class ExternalFeedsList extends ApiList<ExternalFeed> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2314764021162994219L;

		public ExternalFeedsList(List<ExternalFeed> list) {
			super("externalFeeds", list);
		}
		
	}
	
}
