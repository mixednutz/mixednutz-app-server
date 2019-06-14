package net.mixednutz.app.server.controller.api;

import java.util.TreeMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController.ExternalFeedsList;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
@RequestMapping({"/api","/internal"})
public class ApiUserController {
	
	public static final String USER_PROFILE_ENDPOINT = 
			"/loggedin/user";
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private ExternalFeedApiController externalFeedApi;
	
	@Autowired
	private ApiManager apiManager;
	
	@RequestMapping(value="/{username}/bundle", method = RequestMethod.GET)
	public @ResponseBody UserProfileBundle getUserTimelineBundle(
			@PathVariable String username,
			@AuthenticationPrincipal User user) {
		
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		UserProfile profileData = profileRepository.findById(profileUser.getUserId()).orElse(null);
				
		UserProfileBundle bundle = new UserProfileBundle()
				.addUser(apiManager.toUser(profileUser, profileData))
				.addExternalFeedsList(externalFeedApi.externalFeeds(username));
//				.addFollowerCount(friendManager.countFollowers(account, false))
//				.addFollowingCount(friendManager.countFollowing(account, false));
				
//		if (user !=null) {
//			bundle
//				.addBlockByMe(blockeduserManager.get(
//					new BlockeduserPK(user.getId(), account.getId()))!=null)
//				.addFriendPath(friendsApi.apiGetUserPath(username, user))
//				.addFriendStatus(friendManager.get(new FriendPK(user.getId(), account.getId())));
//		}
//		if (account instanceof User) {
//			bundle
//				.addProfile(profileApi.getProfile((User)account));
//		}
//		
		return bundle;
	}
	
	/**
	 * Lookup current user
	 * @param model
	 * @return
	 */
	@RequestMapping(value=USER_PROFILE_ENDPOINT, method = RequestMethod.GET)
	public @ResponseBody IUserSmall loggedInUser(@AuthenticationPrincipal User user) {
		return apiManager.toUser(user);
	}
	
	public static class UserProfileBundle extends TreeMap<String, Object> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4514253806502880482L;
		
		private UserProfileBundle addBundle(ApiList<?> list) {
			for (java.util.Map.Entry<String, ?> e: list.entrySet()) {
				if (containsKey(e.getKey())) {
					throw new RuntimeException("Bundle already has key "+e.getKey());
				}
			}
			this.putAll(list);
			return this;
		}
//		UserProfileBundle addFollowerCount(int followerCount) {
//			this.put("followerCount", followerCount);
//			return this;
//		}
//		UserProfileBundle addFollowingCount(int followingCount) {
//			this.put("followingCount", followingCount);
//			return this;
//		}
		UserProfileBundle addUser(IUserSmall user) {
			this.put("user", user);
			return this;
		}
		UserProfileBundle addExternalFeedsList(ExternalFeedsList feeds) {
			return addBundle(feeds);
		}
//		UserProfileBundle addFriendStatus(Friend friendStatus) {
//			if (friendStatus!=null) {
//				//status whether current user has permission to follow this account
//				this.put("pending", friendStatus.isPending());
//				this.put("following", !friendStatus.isPending());
//			} else {
//				this.put("following", false);
//			}
//			return this;
//		}
//		UserProfileBundle addBlockByMe(boolean blockByMe) {
//			if (blockByMe) {
//				this.put("blockByMe", blockByMe);
//			}
//			return this;
//		}
//		UserProfileBundle addFriendPath(FriendPath friendPath) {
//			return addBundle(friendPath);
//		}
		
	}

}
