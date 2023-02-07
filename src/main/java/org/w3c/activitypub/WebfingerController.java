package org.w3c.activitypub;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
public class WebfingerController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private ActivityPubManager activityPubManager;
	
	private final ObjectMapper mapper = new ObjectMapper();

	@GetMapping(path="/.well-known/webfinger",produces={"application/jrd+json"})
	public ResponseEntity<String> webfinger(@RequestParam("resource")URI resource) throws JsonProcessingException {
		String scheme = resource.getScheme();
		URI actorUri = null;
		if ("acct".equals(scheme)) {
			String acc = resource.getSchemeSpecificPart();
			String[] part = acc.split("@");
			if (part.length!=2) {
				return new ResponseEntity<String>(
						"{\"error\":\"Resource expected to be acct:username@host: "+resource.toString()+"\"}", 
						HttpStatus.BAD_REQUEST);
			}
			final String username = part[0];
			final String host = part[1];
			
			if (!networkInfo.getHostName().equalsIgnoreCase(host)) {
				return new ResponseEntity<String>(
						"{\"error\":\"Resource host not found on this server: "+host+"\"}", 
						HttpStatus.BAD_REQUEST);
			}
			
			User profileUser = userRepository.findByUsername(username)
					.orElseThrow(new Supplier<UserNotFoundException>(){
						@Override
						public UserNotFoundException get() {
							throw new UserNotFoundException("User "+username+" not found");
						}});
			
			resource = URI.create("acct:"+profileUser.getUsername()+"@"+networkInfo.getHostName());
			actorUri = activityPubManager.getActorUri(profileUser.getUsername());
		} else {
			return new ResponseEntity<String>(
					"{\"error\":\"Resource expected to be acct:username@host: "+resource.toString()+"\"}", 
					HttpStatus.BAD_REQUEST);
		}
		
		WebfingerResponse response = new WebfingerResponse(resource, List.of(
				new Link("self","application/activity+json",actorUri.toString())));
		
		String t =  mapper.writeValueAsString(response);
		return new ResponseEntity<String>(t, HttpStatus.OK);
	}
	
	public static class WebfingerResponse {
		URI subject;
		List<Link> links;
		
		public WebfingerResponse(URI subject, List<Link> links) {
			super();
			this.subject = subject;
			this.links = links;
		}

		public URI getSubject() {
			return subject;
		}

		public void setSubject(URI subject) {
			this.subject = subject;
		}

		public List<Link> getLinks() {
			return links;
		}

		public void setLinks(List<Link> links) {
			this.links = links;
		}
	}
	
	public static class Link {
		String rel;
		String type;
		String href;
		
		public Link(String rel, String type, String href) {
			super();
			this.rel = rel;
			this.type = type;
			this.href = href;
		}

		public String getRel() {
			return rel;
		}

		public void setRel(String rel) {
			this.rel = rel;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}
	}
	
}
