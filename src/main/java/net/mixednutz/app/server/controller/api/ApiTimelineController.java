package net.mixednutz.app.server.controller.api;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.core.model.Page;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.core.model.TimelineElement;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController.ExternalFeedsList;
import net.mixednutz.app.server.controller.exception.BadParametersException;
import net.mixednutz.app.server.entity.User;


@Controller
@RequestMapping({"/api","/internal"})
public class ApiTimelineController {
	
	public static final String HOME_TIMELINE_ENDPOINT = 
			"/nutsterz/timeline";
	public static final String HOME_TIMELINE_NEXTPAGE_ENDPOINT = 
			"/nutsterz/timeline/nextpage";
	
	public static final String PAGE_SIZE_STR = "20";
	public static final int PAGE_SIZE = Integer.parseInt(PAGE_SIZE_STR);
	
	@Autowired
	private ExternalFeedApiController externalFeedApi;
		
	@RequestMapping(value="/timeline/bundle", method = RequestMethod.GET)
	public @ResponseBody TimelineBundle apiGetTimelineBundle(Authentication auth) {
		User user = (User) auth.getPrincipal();

		return new TimelineBundle()
//				.addFollowingList(friendsApi.apiGetFollowing(user))
				.addExternalFeedsList(externalFeedApi.externalFeeds(user))
//				.addFriendgroups(friendsApi.apiGetCategories(user))
				.addUser(user);
//				.addProfile(profileApi.getProfile(user))
//				.addSettings(settingsApi.getPushSettings("", user));
	}
	
	@RequestMapping(value={HOME_TIMELINE_ENDPOINT}, 
			method = RequestMethod.GET)
	public @ResponseBody TimelinePage getHomeTimeline(Authentication auth) {
//		User user = (User) auth.getPrincipal();
		
		TimelinePage page = new TimelinePage(
				stubData());
//				timelineManager.getNutsterzTimeline(account, PAGE_SIZE, null));
		
		return page;
	}
	
	@RequestMapping(value={HOME_TIMELINE_NEXTPAGE_ENDPOINT}, 
			method = RequestMethod.POST)
	public @ResponseBody TimelinePage getHomeTimeline(Authentication auth, 
			@RequestBody PageRequest<Date> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {
		checkValidPagination(prevPage);
//		User user = (User) auth.getPrincipal();

		TimelinePage page = new TimelinePage(
				stubData());
//				timelineManager.getNutsterzTimeline(account, pageSize, prevPage));
		
		return page;
	}
	
	private static Page<TimelineElement, Date> stubData() {
		Page<TimelineElement, Date> stubData = new Page<>();
		stubData.setItems(Collections.emptyList());
		return stubData;
	}
	
	private void checkValidPagination(PageRequest<?> prevPage) {
		if (prevPage!=null && prevPage.getStart()==null && prevPage.getEnd()==null) {
			throw new BadParametersException("Both start and end cannot be null");
		}
	}
	
	public static class TimelineBundle extends TreeMap<String, Object> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 392022507032141882L;
				
		private TimelineBundle addBundle(ApiList<?> list) {
			for (java.util.Map.Entry<String, ?> e: list.entrySet()) {
				if (containsKey(e.getKey())) {
					throw new RuntimeException("Bundle already has key "+e.getKey());
				}
			}
			this.putAll(list);
			return this;
		}
//		TimelineBundle addFollowingList(FollowingList following) {
//			return addBundle(following);
//		}
		TimelineBundle addExternalFeedsList(ExternalFeedsList feeds) {
			return addBundle(feeds);
		}
//		TimelineBundle addFriendgroups(FriendGroupList fgroups) {
//			return addBundle(fgroups);
//		}
		TimelineBundle addUser(IUserSmall user) {
			this.put("user", user);
			return this;
		}
//		TimelineBundle addProfile(UserProfile profile) {
//			this.put("profile", profile);
//			return this;
//		}
//		TimelineBundle addSettings(UserSettings settings) {
//			this.put("settings", settings);
//			return this;
//		}
	}
	
	public static class TimelinePage extends Page<TimelineElement, Date> {
		private Page<TimelineElement, Date> delegate = new Page<TimelineElement, Date>();

		public TimelinePage(Page<TimelineElement, Date> delegate) {
			super();
			this.delegate = delegate;
		}

		public List<TimelineElement> getItems() {
			return delegate.getItems();
		}

		public void setItems(List<TimelineElement> items) {
			delegate.setItems(items);
		}

		public PageRequest<Date> getNextPage() {
			return delegate.getNextPage();
		}

		public void setNextPage(PageRequest<Date> nextPage) {
			delegate.setNextPage(nextPage);
		}

		@Override
		public PageRequest<Date> getPageRequest() {
			return delegate.getPageRequest();
		}

		@Override
		public void setPageRequest(PageRequest<Date> currentPage) {
			delegate.setPageRequest(currentPage);
		}
	}

}
