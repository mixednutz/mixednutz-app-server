package net.mixednutz.app.server.controller.web;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.app.server.controller.BaseFollowerController;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.Follower;
import net.mixednutz.app.server.entity.User;

@Controller
public class FollowerController extends BaseFollowerController {
	
	@RequestMapping(value="/{username}/followers", method = RequestMethod.GET)
	public String friends(@PathVariable String username, 
			@AuthenticationPrincipal User user, Model model) {	
		if (user==null) throw new AuthenticationCredentialsNotFoundException("Must be logged in to do this!");
				
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		model.addAttribute("profileUser", profileUser.get());
		
		List<Follower> followers = getFollowers(username);
		model.addAttribute("followers", followers);
		
		List<Follower> following = getFollowing(username);
		model.addAttribute("following", following);
				
		return "followers/followers";
	}

}
