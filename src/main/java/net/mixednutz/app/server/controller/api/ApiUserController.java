package net.mixednutz.app.server.controller.api;

import java.util.TreeMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.controller.api.ExternalFeedApiController.ExternalFeedsList;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.Lastonline;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.FollowerManager;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

@RestController
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
	private FollowerManager followerManager;
	
	@Autowired
	private ApiManager apiManager;
	
	/**
	 * Simple endpoint to keep the authentication session alive
	 */
	@RequestMapping("/keepAlive")
	@ResponseStatus(value=HttpStatus.NO_CONTENT)
	public String keepAlive() {
		return null;
	}
	
	@RequestMapping(value = "/{username}/exists", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public String userExists(@PathVariable String username) {
		userRepository.findByUsername(username).orElseThrow(new Supplier<UserNotFoundException>() {
			@Override
			public UserNotFoundException get() {
				throw new UserNotFoundException("User " + username + " not found");
			}
		});

		return null;
	}
	
	@RequestMapping(value="/{username}/bundle", method = RequestMethod.GET)
	public UserProfileBundle getUserTimelineBundle(
			@PathVariable String username,
			@AuthenticationPrincipal User user) {
		
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		UserProfile profileData = profileRepository.findById(profileUser.getUserId()).orElse(null);
				
		Lastonline lastonline = profileUser.getLastonline();

		UserProfileBundle bundle = new UserProfileBundle()
				.addUser(apiManager.toUser(profileUser, profileData))
				.addExternalFeedsList(externalFeedApi.externalFeeds(username))
				.addLastonline(lastonline)
				.addFollowerCount(followerManager.countFollowers(profileUser))
				.addFollowingCount(followerManager.countFollowing(profileUser));
				
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
	public IUserSmall loggedInUser(@AuthenticationPrincipal User user) {
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
		UserProfileBundle addFollowerCount(long followerCount) {
			this.put("followerCount", followerCount);
			return this;
		}
		UserProfileBundle addFollowingCount(long followingCount) {
			this.put("followingCount", followingCount);
			return this;
		}
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
		UserProfileBundle addLastonline(Lastonline lastonline) {
			if (lastonline!=null) {
				this.put("lastOnline", lastonline.getTimestamp().toLocalDate());
			}
			return this;
		}
		
	}

}
