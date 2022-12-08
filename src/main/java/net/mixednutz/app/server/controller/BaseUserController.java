package net.mixednutz.app.server.controller;

import org.springframework.beans.factory.annotation.Autowired;

import net.mixednutz.app.server.entity.Follower.FollowerPK;
import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.FollowerManager;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.manager.UserService;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseUserController {
		
	protected static final String USER_ROLE = "ROLE_USER";
	
	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	private SiteSettingsManager siteSettingsManager;
	
	@Autowired
	private FollowerManager followerManager;
	
	
	protected User saveNewUser(User user) {
		userService.encryptPassword(user);
		user.setEnabled(true);
		user.getRoles().add(new Role(user, USER_ROLE));
		user = userRepository.save(user);
		
		SiteSettings siteSettings = siteSettingsManager.getSiteSettings();
		if (siteSettings.getNewUsersAutoFollowAdminUser()) {
			followerManager.autoAcceptFollow(
					new FollowerPK(siteSettings.getAdminUserId(), user.getUserId()));
		}
		
		return user;
	}
	
	protected User encryptPassword(User user) {
		userService.encryptPassword(user);
		return userRepository.save(user);
	}
	
}
