package net.mixednutz.app.server.controller.api;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.controller.BaseFollowerController;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.Follower;
import net.mixednutz.app.server.entity.Follower.FollowerPK;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.repository.UserRepository;

@RestController
@RequestMapping(value="/api")
public class ApiFollowerController extends BaseFollowerController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SiteSettingsManager siteSettingsManager;
	
	@Autowired
	private ApiManager apiManager;
	
	@RequestMapping(value="/following", method = RequestMethod.GET)
	public FollowingList apiGetFollowing(@AuthenticationPrincipal User user) {
		FollowingList following = new FollowingList();

		followerManager.getFollowing(user)
			.stream()
			.map(f->apiManager.toUser(f.getUser()))
			.forEach(u->following.add(u));
		return following;
	}
	
	@RequestMapping(value="/followers", method = RequestMethod.GET)
	public FollowersList apiGetFollowers(@AuthenticationPrincipal User user) {
		FollowersList followers = new FollowersList();

		followerManager.getFollowers(user)
			.stream()
			.map(f->apiManager.toUser(f.getUser()))
			.forEach(u->followers.add(u));
		return followers;
	}
	
	@RequestMapping(value="/{username}/following", method = RequestMethod.GET)
	public FollowingList apiGetFollowing(@PathVariable String username, 
			@AuthenticationPrincipal User user) {
		FollowingList following = new FollowingList();

		getFollowing(username)
			.stream()
			.map(f->apiManager.toUser(f.getUser()))
			.forEach(u->following.add(u));
		return following;
	}
	
	@RequestMapping(value="/{username}/followers", method = RequestMethod.GET)
	public FollowersList apiGetFollowers(@PathVariable String username, 
			@AuthenticationPrincipal User user) {
		FollowersList followers = new FollowersList();

		getFollowers(username)
			.stream()
			.map(f->apiManager.toUser(f.getUser()))
			.forEach(u->followers.add(u));
		return followers;
	}
	
	@RequestMapping(value="/{username}/follow", method = RequestMethod.POST)
	public String follow(@PathVariable String username, 
			@AuthenticationPrincipal User user) {
		User userToFollow = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		FollowerPK followerId = new FollowerPK(userToFollow.getUserId(), user.getUserId());
		Optional<Follower> existing = followerManager.get(followerId);
		
		SiteSettings siteSettings = siteSettingsManager.getSiteSettings();
		
		if (existing.isEmpty()) {
			
			followerManager.requestFollow(followerId, x->{});
			
			if (siteSettings.getNewUsersAutoFollowAdminUser() && 
					userToFollow.getUserId().equals(siteSettings.getAdminUserId())) {
				followerManager.acceptFollow(followerId, x->{});
				
				return "Auto accepted";
			}
			
			//TODO Send follow request
			
			return "Follow request sent";
		}
		
		return "Follow request already sent";
	}
	
	public static class FollowingList extends ApiList<IUserSmall> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4328947544853436214L;

		public FollowingList() {
			super("following");
		}
		
	}
	
	public static class FollowersList extends ApiList<IUserSmall> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7694872710549850417L;

		public FollowersList() {
			super("followers");
		}
		
	}

}
