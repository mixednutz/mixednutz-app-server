package net.mixednutz.app.server.controller.web;

import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController.ExternalFeedsList;
import net.mixednutz.app.server.entity.User;


@Controller
public class TimelineController {
	
	@Autowired
	private ExternalFeedApiController externalFeedApi;
	
	@RequestMapping(value="/api/timeline/bundle", method = RequestMethod.GET)
	public @ResponseBody TimelineBundle apiGetTimelineBundle(Authentication auth) {
		User user = (User) auth.getPrincipal();

		return new TimelineBundle()
//				.addFollowingList(friendsApi.apiGetFollowing(user))
				.addExternalFeedsList(externalFeedApi.externalFeeds(user));
//				.addFriendgroups(friendsApi.apiGetCategories(user))
//				.addUser(userApi.loggedInUser(user))
//				.addProfile(profileApi.getProfile(user))
//				.addSettings(settingsApi.getPushSettings("", user));
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
//		TimelineBundle addUser(UserSmall user) {
//			this.put("user", user);
//			return this;
//		}
//		TimelineBundle addProfile(UserProfile profile) {
//			this.put("profile", profile);
//			return this;
//		}
//		TimelineBundle addSettings(UserSettings settings) {
//			this.put("settings", settings);
//			return this;
//		}
	}
	
}
