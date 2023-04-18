package org.w3c.activitypub;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import org.w3c.activitystreams.model.BaseObjectOrLink;
import org.w3c.activitystreams.model.activity.Accept;
import org.w3c.activitystreams.model.activity.Delete;
import org.w3c.activitystreams.model.activity.Follow;
import org.w3c.activitystreams.model.activity.Undo;
import org.w3c.activitystreams.model.activity.Update;

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
	
	@PostMapping(value={USER_INBOX_ENDPOINT},consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> handleInboxJson(
			@PathVariable String username, 
			@RequestBody String activityStr, 
			@RequestHeader HttpHeaders httpHeaders,
			Authentication auth) throws JsonMappingException, JsonProcessingException {
		LOG.warn("Invalid ContentType. {}. Processing anyway", httpHeaders.getContentType());
		return handleInbox(username, activityStr,httpHeaders, auth);
	}
	
	@PostMapping(value={USER_INBOX_ENDPOINT},consumes = {
			ActivityImpl.APPLICATION_ACTIVITY_VALUE,
			"application/ld+json"})
	public ResponseEntity<String> handleInbox(
			@PathVariable String username, 
			@RequestBody String activityStr, 
			@RequestHeader HttpHeaders httpHeaders,
			Authentication auth) throws JsonMappingException, JsonProcessingException {
		
		//LOG
		Inbox loggedActivity = new Inbox();
		loggedActivity.setRecievedDate(ZonedDateTime.now());
		loggedActivity.setHeaders(HttpHeaders.formatHeaders(httpHeaders));
		loggedActivity.setPayload(activityStr);
		
		LOG.info("Recieved activity in {}'s inbox. Attempting to parse.",
				username);
		
		ActivityImpl activity = objectMapper.readValue(activityStr, ActivityImpl.class);
		if (activity.getId()!=null) {
			loggedActivity.setActivityId(activity.getId().toString());
		} else {
			LOG.warn("{} activity does not have an ID. Source String: {}", activity.getType(), activityStr);
		}
		loggedActivity.setType(activity.getType());
		
		URI actorUri = null;
		if (activity.getActor() instanceof Link) {
			actorUri = ((Link)activity.getActor()).getHref();
		} else if (activity.getActor() instanceof ActorImpl) {
			actorUri = ((ActorImpl)activity.getActor()).getId();
		}
		loggedActivity.setActor(actorUri.toString());
		
		inboxRepository.save(loggedActivity);
		LOG.info("Recieved activity in {}'s inbox: {} from {}",
				username, activity.getType().toUpperCase(), actorUri);
				
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		/* We do delete first because it's entirely possible this actor doesn't 
		 * exist and we can't retrieve the actor and verify its signature.
		 */
		if (activity instanceof Delete) {
			status = handleDelete((Delete)activity, actorUri);
			return new ResponseEntity<String>(status);
		}
		
		//Check if authenticated (DELETEs wont get this far)
		if (auth.getAuthorities().stream()
				.noneMatch(role->role.getAuthority().equals("USER_ROLE"))) {
			return new ResponseEntity<String>("Not authenticated",status);
		}
		
		final ActorImpl actor = apClient.getActor(actorUri);
		
		User remoteUser = null;			
		if (activity instanceof Follow) {
			
			if (auth.getPrincipal() instanceof User) {
				remoteUser = (User)auth.getPrincipal();
			} else {
				//We need to create a user now
				remoteUser = createNewUser(actor).getUser();
			}
			
			status = handleFollow(username, (Follow) activity, profileUser, remoteUser, actor);
		} 
		
		
		if (activity instanceof Undo) {
			status = handleUndo(username, (Undo)activity, profileUser, remoteUser, actor);
		} else if (activity instanceof Update) {
			status = handleUpdate((Update)activity);
		} else {
			LOG.warn("Unhandled Activity Type: {}", activity.getType());
		}
		
		return new ResponseEntity<String>(status);
	}
	
	private UserProfile createNewUser(ActorImpl actor) {
		User user = new User();
		user.setUsername('@'+actor.getPreferredUsername()+'@'+actor.getId().getHost());
		if (StringUtils.hasText(actor.getName())) user.setDisplayName(actor.getName());
		LOG.info("Creating user {} for {}", user.getUsername(), actor.getId().toString());
		user = userRepository.save(user);
		UserProfile up = new UserProfile(user);
		up.setUser(user);
		up.setActivityPubActorUri(actor.getId().toString());
		up.setFediverseUsername('@'+actor.getPreferredUsername()+'@'+actor.getId().getHost());
		return userProfileRepository.save(up);
	}
	
	protected HttpStatus handleFollow(String username, Follow follow, User localUser, User remoteUser, ActorImpl actor) {
		
		if (Set.of(ApiFollowerController.AUTO_ACCEPTED, ApiFollowerController.ALREADY_ACCEPTED)
				.contains(followerController.follow(username, remoteUser))) {
			
			URI inbox = actor.getInbox();
			Accept accept = apManager.toAccept(username, follow);
			
			apClient.sendActivity(inbox, accept, localUser);
			
			//Accepted
			return HttpStatus.OK;
		}
		//Processing
		return HttpStatus.ACCEPTED;
	}
	
	protected HttpStatus handleUndo(String username, Undo undo, User localUser, User remoteUser, ActorImpl actor) {
		URI inbox = actor.getInbox();
		
		BaseObjectOrLink object = undo.getObject();
		if (object instanceof Link) {
			LOG.error("Not Implemented. Undo from Link.");
		}
		if (object instanceof Follow) {
			followerController.unfollow(username, remoteUser);
			
			Accept accept = apManager.toAccept(username, undo);
			
			apClient.sendActivity(inbox, accept, localUser);
			
			//Accepted
			return HttpStatus.OK;
		}
		return HttpStatus.ACCEPTED;
	}
	
	protected HttpStatus handleDelete(Delete delete, URI actorUri) {
		URI objectUri = null;
		if (delete.getObject() instanceof Link) {
			objectUri = ((Link)delete.getObject()).getHref();
		} else if (delete.getActor() instanceof ActorImpl) {
			objectUri = ((ActorImpl)delete.getObject()).getId();
		}
		
		if (objectUri.equals(actorUri)) {
			//we could search and chose between GONE and NOT_FOUND
			return HttpStatus.NOT_FOUND;
		}
		
		return HttpStatus.NOT_FOUND;
	}
	protected HttpStatus handleUpdate(Update update) {
		// Not implemented yet
		return HttpStatus.OK;
	}
	

}
