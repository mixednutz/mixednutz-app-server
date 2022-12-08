package org.w3c.activitypub;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.activitystreams.model.ActivityImpl;
import org.w3c.activitystreams.model.activity.Follow;

import net.mixednutz.app.server.controller.api.ApiFollowerController;
import net.mixednutz.app.server.entity.User;

@RestController
@RequestMapping(URI_PREFIX)
public class InboxController {
	
	public static final String USER_INBOX_ENDPOINT = 
			"/{username}/inbox";

	ApiFollowerController followerController;
	
	@RequestMapping(value={USER_INBOX_ENDPOINT}, 
			method = RequestMethod.POST)
	public void handleInbox(
			@PathVariable String username, 
			@RequestBody ActivityImpl activity, 
			@AuthenticationPrincipal User user) {
		
		if (activity instanceof Follow) {
			handleFollow(username, user);
		}
		
	}
	
	protected void handleFollow(String username, User currentUser) {
		//async
		followerController.follow(username, currentUser);
		
		//get follower's inbbox
		//send accept
		
		//return 200-series
	}
	

}
