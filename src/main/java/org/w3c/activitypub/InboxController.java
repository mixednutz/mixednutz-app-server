package org.w3c.activitypub;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.activitystreams.Link;
import org.w3c.activitystreams.model.ActivityImpl;
import org.w3c.activitystreams.model.ActorImpl;
import org.w3c.activitystreams.model.activity.Accept;
import org.w3c.activitystreams.model.activity.Follow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.activitypub.client.ActivityPubClientManager;
import net.mixednutz.api.webfinger.client.WebfingerClient;
import net.mixednutz.app.server.controller.api.ApiFollowerController;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.entity.activitypub.Inbox;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;
import net.mixednutz.app.server.repository.activitypub.InboxRepository;
import net.mixednutz.app.server.util.HttpSignaturesUtil;

@Controller
@RequestMapping(URI_PREFIX)
public class InboxController {
	
	private static final Logger LOG = LoggerFactory.getLogger(InboxController.class);
	
	public static final String USER_INBOX_ENDPOINT = 
			"/{username}/inbox";

	@Autowired
	ApiFollowerController followerController;
	
	@Autowired
	private UserProfileRepository userProfileRepository;
	
	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	private InboxRepository inboxRepository;
	
	@Autowired
	ActivityPubClientManager apClient;
	
	@Autowired
	WebfingerClient wfClient;
	
	@Autowired
	ActivityPubManager apManager;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@PostMapping(value={USER_INBOX_ENDPOINT},consumes = {ActivityImpl.APPLICATION_ACTIVITY_VALUE,"application/ld+json"})
	public ResponseEntity<String> handleInbox(
			@PathVariable String username, 
			@RequestBody String activityStr, 
			@RequestHeader HttpHeaders httpHeaders,
			HttpServletRequest request) throws JsonMappingException, JsonProcessingException {
		
		//LOG
		Inbox loggedActivity = new Inbox();
		loggedActivity.setRecievedDate(ZonedDateTime.now());
		loggedActivity.setHeaders(HttpHeaders.formatHeaders(httpHeaders));
		loggedActivity.setPayload(activityStr);
		
		LOG.info("Recieved activity in {}'s inbox. Attempting to parse.",
				username);
		
		ActivityImpl activity = objectMapper.readValue(activityStr, ActivityImpl.class);
		loggedActivity.setActivityId(activity.getId());
		loggedActivity.setType(activity.getType());
		
		URI actorUri = null;
		if (activity.getActor() instanceof Link) {
			actorUri = ((Link)activity.getActor()).getHref();
		} else if (activity.getActor() instanceof ActorImpl) {
			actorUri = ((ActorImpl)activity.getActor()).getId();
		}
		loggedActivity.setActor(actorUri);
		
		inboxRepository.save(loggedActivity);
		LOG.info("Recieved activity in {}'s inbox: {} from {}",
				username, activity.getType().toUpperCase(), actorUri);
		
		
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		//get live version of actor
		final ActorImpl actor = apClient.getActor(actorUri);
		
		//verify
		HttpSignaturesUtil.verifyRequest(URI.create(request.getRequestURI()),
				HttpMethod.valueOf(request.getMethod()), httpHeaders, 
				keyId->{
					if (keyId.equals(actor.getPublicKey().getId().toString())) {
						return HttpSignaturesUtil.getPublicKeyFromPem(
								actor.getPublicKey().getPublicKeyPem());
					}
					throw new RuntimeException("Unable to find key "+keyId);
				});
				
		//find actor in db
		UserProfile userProfile = 
				userProfileRepository.findOneByActivityPubActorUri(actorUri)
					.orElseGet(()->createNewUser(actor));
		User remoteUser = userProfile.getUser();
						
		HttpStatus status = HttpStatus.BAD_REQUEST;
		if (activity instanceof Follow) {
			status = handleFollow(username, (Follow) activity, profileUser, remoteUser, actor);
		}
		
		return new ResponseEntity<String>(status);
	}
	
	private UserProfile createNewUser(ActorImpl actor) {
		User user = new User();
		user.setUsername('@'+actor.getPreferredUsername()+'@'+actor.getId().getHost());
		if (StringUtils.hasText(actor.getName())) user.setDisplayName(actor.getName());
		user = userRepository.save(user);
		UserProfile up = new UserProfile(user);
		up.setUser(user);
		up.setActivityPubActorUri(actor.getId());
		up.setFediverseUsername('@'+actor.getPreferredUsername()+'@'+actor.getId().getHost());
		return userProfileRepository.save(up);
	}
	
	protected HttpStatus handleFollow(String username, Follow follow, User localUser, User remoteUser, ActorImpl actor) {
		
		if (ApiFollowerController.AUTO_ACCEPTED.equals(
				followerController.follow(username, remoteUser))) {
			
			URI inbox = actor.getInbox();
			Accept accept = apManager.toAccept(username, follow);
			
			apClient.sendActivity(inbox, accept, localUser);
			
			//Accepted
			return HttpStatus.OK;
		}
		//Processing
		return HttpStatus.ACCEPTED;
	}
	

}
