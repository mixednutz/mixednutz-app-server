package net.mixednutz.app.server.controller.api;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.core.model.Page;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController.ExternalFeedsList;
import net.mixednutz.app.server.controller.exception.NotAuthenticatedException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserSettings;
import net.mixednutz.app.server.repository.UserRepository;
import net.mixednutz.app.server.repository.UserSettingsRepository;


@Controller
@RequestMapping({"/api","/internal"})
public class ApiTimelineController {
	
	public static final String HOME_TIMELINE_ENDPOINT = 
			"/nutsterz/timeline";
	public static final String HOME_TIMELINE_NEXTPAGE_ENDPOINT = 
			"/nutsterz/timeline/nextpage";
	public static final String USER_TIMELINE_ENDPOINT = 
			"/{username}/timeline";
	public static final String USER_TIMELINE_NEXTPAGE_ENDPOINT = 
			"/{username}/timeline/nextpage";
	
	public static final String PAGE_SIZE_STR = "20";
	public static final int PAGE_SIZE = Integer.parseInt(PAGE_SIZE_STR);
	
	@Autowired
	private ExternalFeedApiController externalFeedApi;
	
	@Autowired
	private UserSettingsRepository settingsRepository;
	
	@Autowired
	UserRepository userRepository;
		
	@RequestMapping(value="/timeline/bundle", method = RequestMethod.GET)
	public @ResponseBody TimelineBundle apiGetTimelineBundle(@AuthenticationPrincipal User user) {
		if (user==null) {
			throw new NotAuthenticatedException("Not logged in");
		}
		
		return new TimelineBundle()
//				.addFollowingList(friendsApi.apiGetFollowing(user))
				.addExternalFeedsList(externalFeedApi.externalFeeds(user))
//				.addFriendgroups(friendsApi.apiGetCategories(user))
				.addUser(user)
//				.addProfile(profileApi.getProfile(user))
				.addSettings(settingsRepository.findById(user.getUserId()).
						orElse(new UserSettings()));
	}
	
	@RequestMapping(value={HOME_TIMELINE_ENDPOINT}, 
			method = RequestMethod.GET)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> getHomeTimeline(@AuthenticationPrincipal User user,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {

		return getHomeTimeline(
				PageRequest.first(pageSize, Direction.LESS_THAN, Date.class), 
				pageSize, user);
	}
	
	@RequestMapping(value={HOME_TIMELINE_NEXTPAGE_ENDPOINT}, 
			method = RequestMethod.POST)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> getHomeTimeline( 
			@RequestBody PageRequest<Date> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user) {
		if (user==null) {
			throw new NotAuthenticatedException("Not logged in");
		}

		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		return stubData();
	}
	
	@RequestMapping(value={USER_TIMELINE_ENDPOINT}, 
			method = RequestMethod.GET)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> getUserTimeline(
			@PathVariable String username,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user) {
		
		return getUserTimeline(username, hashtag, 
				PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				pageSize, user);
	}
	
	@RequestMapping(value={USER_TIMELINE_NEXTPAGE_ENDPOINT}, 
			method = RequestMethod.POST)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> getUserTimeline(
			@PathVariable String username,
			@RequestParam(value="hashtag", defaultValue="") String hashtag,
			@RequestBody PageRequest<String> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user) {
		
		return externalFeedApi.apiCombinedExternalFeedsUserTimeline(username, hashtag, pageSize, 
				prevPage, user);
	}
	
	private static IPage<? extends ITimelineElement,Instant> stubData() {
		Page<? extends ITimelineElement,Instant> stubData = new Page<>();
		stubData.setItems(Collections.emptyList());
		return stubData;
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
		TimelineBundle addSettings(UserSettings settings) {
			this.put("settings", settings);
			return this;
		}
	}

}
