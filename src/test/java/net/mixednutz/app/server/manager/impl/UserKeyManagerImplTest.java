package net.mixednutz.app.server.manager.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.w3c.activitystreams.model.ActorImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserKey;
import net.mixednutz.app.server.repository.UserKeyRepository;

public class UserKeyManagerImplTest {

	@Test
	public void test() throws Exception {
		UserKeyManagerImpl manager = new UserKeyManagerImpl();
		UserKeyRepository userKeyRepository = mock(UserKeyRepository.class);
		manager.userKeyRepository = userKeyRepository;
		manager.init();
		
		
		Map<Long, UserKey> saved = new HashMap<>();
		when(userKeyRepository.save(any()))
			.thenAnswer(inv->{
				UserKey e = inv.getArgument(0, UserKey.class);
				return saved.put(e.getUserId(), e);
			});
		when(userKeyRepository.findById(anyLong()))
			.thenAnswer(inv->
				Optional.ofNullable(saved.get(inv.getArgument(0, Long.class))));
		
		User user = new User();
		user.setUserId(1L);
		
		manager.generateKeyPair(user);
		
		assertFalse(saved.isEmpty());
		
		// SHOW PUBLIC KEY PEM
		URI actorUri = URI.create("https://andrewfesta.com/activitypub/admin");
		ActorImpl actor = new ActorImpl();
		actor.setId(actorUri);
		manager.setPublicKeyPem(user, actor);
		System.out.println(actor.getPublicKey());
		
		// WRITE ACTOR JSON
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(actor));
		
		// SIGN REQUEST
//		RestTemplate rest = new RestTemplateBuilder()
//				.additionalInterceptors((request, body, execution)->{
//					manager.signRequest(request, user, actorUri, body);
//					return null;
////					return execution.execute(request, body);
//				}).build();
//		
//		rest.exchange("https://mastodon.social/inbox", HttpMethod.POST, 
//				new HttpEntity<String>(""), String.class);
		
		HttpHeaders headers = new HttpHeaders();
		URI destination = URI.create("https://mastodon.social/inbox");
		manager.signRequest(user, HttpMethod.POST, headers, actorUri, 
				destination, "".getBytes());
		
		System.out.println(headers);
		
		headers.setHost(new InetSocketAddress(InetAddress.getByName("mastodon.social"),0));
		
		// VERFIY
		manager.verfiyRequest(HttpMethod.POST, headers, actor, destination);
	}
	
	public void testRestTemplate() {
		RestTemplate rest = new RestTemplate();
		rest.getInterceptors();
	}
	
}
