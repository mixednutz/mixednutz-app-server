package net.mixednutz.app.server.controller.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
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
import net.mixednutz.app.server.entity.ExternalFeeds;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;


@Controller
@RequestMapping(value="/internal")
public class ExternalFeedApiController {
	
	public static final String PAGE_SIZE_STR = "20";
	public static final int PAGE_SIZE = Integer.parseInt(PAGE_SIZE_STR);
	
	@Autowired
	private ExternalFeedManager feedManager;
	
	@Autowired
	private ExternalFeedRepository feedRepository;
	

	
	@RequestMapping(value="/feeds", method = RequestMethod.GET)
	public @ResponseBody ExternalFeedsList externalFeeds(@AuthenticationPrincipal User user) {
		final Map<INetworkInfoSmall, List<AbstractFeed>> externalFeeds = 
				feedManager.feedsForUser(user);
		
		ExternalFeedsList feeds = new ExternalFeedsList();
		for (Entry<INetworkInfoSmall, List<AbstractFeed>> entry: externalFeeds.entrySet()) {
			feeds.add(new ExternalFeed(entry.getKey(), entry.getValue(), feedManager.getCompatibleFeedsForCrossposting(entry.getKey())));
		}
		return feeds;
	}
	
	@RequestMapping(value="/feeds/timeline", method = RequestMethod.GET)
	public @ResponseBody IPage<? extends ITimelineElement,Object> apiExternalFeedTimeline(
			@RequestParam("feedId") Long feedId, Authentication auth,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {
		
		return apiExternalFeedTimeline(feedId, auth,
				hashtag, PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				pageSize);
	}
		
	@RequestMapping(value="/feeds/timeline/nextpage", method = RequestMethod.POST)
	public @ResponseBody IPage<? extends ITimelineElement,Object> apiExternalFeedTimeline(
			@RequestParam("feedId") Long feedId, Authentication auth,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestBody PageRequest<String> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {
		User user = (User) auth.getPrincipal();
		Optional<AbstractFeed> feed = feedRepository.findById(feedId);
		if (!feed.isPresent()) {
			throw new ResourceNotFoundException("Feed not found");
		}
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

		public ExternalFeedsList() {
			super("externalFeeds");
		}
		
	}
	
}
