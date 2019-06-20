package net.mixednutz.app.server.controller.api;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
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
import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController.ExternalFeedsList;
import net.mixednutz.app.server.controller.exception.NotAuthenticatedException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserSettings;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.TimelineManager;
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
	private TimelineManager timelineManager;
	
	@Autowired
	private UserSettingsRepository settingsRepository;
	
	@Autowired
	private ApiManager apiManager;
	
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
				.addUser(apiManager.toUser(user))
//				.addProfile(profileApi.getProfile(user))
				.addSettings(settingsRepository.findById(user.getUserId()).
						orElse(new UserSettings()));
	}
	
	@RequestMapping(value={HOME_TIMELINE_ENDPOINT}, 
			method = RequestMethod.GET)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> getHomeTimeline(@AuthenticationPrincipal User user,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {

		return getHomeTimeline(
				PageRequest.first(pageSize, Direction.LESS_THAN, String.class), 
				pageSize, user);
	}
	
	@RequestMapping(value={HOME_TIMELINE_NEXTPAGE_ENDPOINT}, 
			method = RequestMethod.POST)
	public @ResponseBody IPage<? extends ITimelineElement,Instant> getHomeTimeline( 
			@RequestBody PageRequest<String> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user) {
		if (user==null) {
			throw new NotAuthenticatedException("Not logged in");
		}

		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		//Get internal data
		IPage<? extends ITimelineElement,Instant> internalContent = 
				timelineManager.getHomeTimeline(user, prevPage);
		
		return internalContent;
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
		
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		
		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
		
		UserSettings settings = settingsRepository.findById(profileUser.get().getUserId())
				.orElse(new UserSettings());
		
		//Get local data
		final IPage<? extends ITimelineElement,Instant> internalContent = 
				timelineManager.getUserTimeline(profileUser.get(), user, prevPage);
		
		if (settings.isShowCombinedExternalFeedsOnProfile()) {
			
			//Get external data
			final IPage<? extends ITimelineElement,Instant> externalContent = externalFeedApi.apiCombinedExternalFeedsUserTimeline(username, hashtag, pageSize, 
					prevPage, user);
			
			PageRequest<Instant> pageRequest = PageRequest.convert(prevPage, Instant.class,
					(str) -> {
						return ZonedDateTime.parse(str).toInstant();
					});

			return new PageBuilder<ITimelineElement, Instant>()
					.addItems(internalContent.getItems())
					.addItems(externalContent.getItems())
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
		return internalContent;
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
