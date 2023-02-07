package org.w3c.activitypub;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.activitystreams.model.actor.Person;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.controller.BaseUserController;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
@RequestMapping(URI_PREFIX)
public class ActorController extends BaseUserController {
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private UserRepository userRepository;
		
	@Autowired
	private ApiManager apiManager;
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private ActivityPubManager activityPubManager;
	

	
	@RequestMapping(value=ActivityPubManager.USER_ACTOR_ENDPOINT, method = RequestMethod.GET)
	public @ResponseBody Actor getActor(@PathVariable String username) {
		
		return userRepository.findByUsername(username)
		.map(user->{
			URI id = activityPubManager.getActorUri(username);
			URI outbox = UriComponentsBuilder
					.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+OutboxController.USER_OUTBOX_ENDPOINT)
					.buildAndExpand(Map.of("username",username)).toUri();
			URI inbox = UriComponentsBuilder
					.fromHttpUrl(networkInfo.getBaseUrl()+URI_PREFIX+InboxController.USER_INBOX_ENDPOINT)
					.buildAndExpand(Map.of("username",username)).toUri();
			
			Person person = activityPubManager.toPerson(apiManager.toUser(user), user, request, id, outbox, inbox, true);
			return person;
		})
		.orElseThrow(new Supplier<UserNotFoundException>() {
			@Override
			public UserNotFoundException get() {
				throw new UserNotFoundException("User " + username + " not found");
			}
		});
	}
		
}
