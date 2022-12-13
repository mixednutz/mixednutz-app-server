package net.mixednutz.app.server.controller;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;

import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.Follower;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.FollowerManager;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseFollowerController {
	
	@Autowired
	protected FollowerManager followerManager;

	@Autowired
	protected UserRepository userRepository;
	
	protected List<Follower> getFollowers(@PathVariable String username) {
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		return this.followerManager.getFollowers(profileUser);
	}
	
	protected List<Follower> getFollowing(@PathVariable String username) {
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		return this.followerManager.getFollowing(profileUser);
	}
	
}
