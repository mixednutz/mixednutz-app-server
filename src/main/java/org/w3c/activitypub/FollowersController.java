package org.w3c.activitypub;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.activitystreams.model.BaseObjectOrLink;
import org.w3c.activitystreams.model.LinkImpl;
import org.w3c.activitystreams.model.OrderedCollectionImpl;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.Follower;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.manager.FollowerManager;
import net.mixednutz.app.server.repository.UserProfileRepository;

@Controller
@RequestMapping(URI_PREFIX)
public class FollowersController {
	
	public static final String USER_FOLLOWERS_ENDPOINT = 
			"/{username}/followers";
	
	@Autowired
	private FollowerManager followerManager;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private ActivityPubManager activityPubManager;
	
	@RequestMapping(value=USER_FOLLOWERS_ENDPOINT, method = RequestMethod.GET)
	public OrderedCollectionImpl getFollowers(@PathVariable String username, 
			@AuthenticationPrincipal User user) {
		
		return toOrderedCollection(followerManager.getFollowers(user), username);
	}
	
	protected OrderedCollectionImpl toOrderedCollection(
			List<Follower> followers, 
			String username) {
		OrderedCollectionImpl orderedcollection = new OrderedCollectionImpl();
		activityPubManager.initRoot(orderedcollection);
		List<BaseObjectOrLink> followerIds = followers.stream()
			.map(f->profileRepository.findById(f.getId().getFollowerId()).orElse(new UserProfile()))
			.filter(p->p.getActivityPubActorUri()!=null)
			.map(p->new LinkImpl(p.getActivityPubActorUri()))
			.collect(Collectors.toList());
		orderedcollection.setTotalItems((long) followerIds.size()); 
		orderedcollection.setItems(followerIds);
		
		orderedcollection.setId(UriComponentsBuilder
				.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+USER_FOLLOWERS_ENDPOINT)
				.buildAndExpand(Map.of("username",username)).toUri());
		
		return orderedcollection;
	}
	
}
