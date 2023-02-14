package net.mixednutz.app.server.controller.api;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyPair;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.w3c.activitypub.util.ProblemHandler;
import org.w3c.activitystreams.model.ActivityImpl;
import org.w3c.activitystreams.model.ActorImpl;
import org.w3c.activitystreams.model.LinkImpl;
import org.w3c.activitystreams.model.PublicKey;
import org.w3c.activitystreams.model.activity.Follow;
import org.w3c.activitystreams.model.actor.Person;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.activitypub.client.ActivityPubClientManager;
import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.manager.impl.UserKeyManagerImpl;
import net.mixednutz.app.server.repository.UserRepository;
import net.mixednutz.app.server.util.HttpSignaturesUtil;

@ExtendWith(SpringExtension.class)
@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"jpa-dev","db-local-hsqldb","ssl","aws-local"})
@TestPropertySource(properties= {
		"spring.mail.host=smtp.mail.yahoo.com",
		
})
@Disabled
public class InboxControllerIntegrationTest {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SiteSettingsManager siteSettingsManager;
	
	@Autowired
	UserKeyManagerImpl userKeyManager;
	
	@Autowired
	ActivityPubManager activityPubManager;
	
	@MockBean
	private ActivityPubClientManager activityPubClient;

	
	@Transactional
	@Test
	public void testFollow() throws Exception {
		setupAdminUser();
		
		ActorImpl remoteActor = remoteActor();
		KeyPair remoteUserKeyPair = HttpSignaturesUtil.generateKeyPair();
		remoteActor.setPublicKey(new PublicKey("main-key", remoteActor.getId(),
				HttpSignaturesUtil.publicKeyToPem(remoteUserKeyPair.getPublic())));
		when(activityPubClient.getActor(eq(remoteActor.getId()))).thenReturn(remoteActor);
		
		activityPubManager.getActorUri("admin");
		
		Follow follow = new Follow();
		activityPubManager.initRoot(follow);
		follow.setSummary("Sally follows Admin");
		follow.setActor(new LinkImpl(remoteActor.getId()));
		follow.setObject(new LinkImpl(activityPubManager.getActorUri("admin")));
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new ProblemHandler());
		System.out.println(
				mapper.writerWithDefaultPrettyPrinter().writeValueAsString(follow));
			
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(ActivityImpl.APPLICATION_ACTIVITY);
		HttpSignaturesUtil.signRequest(
				URI.create("https://andrewfesta.com"+URI_PREFIX+"/admin/inbox"), 
				HttpMethod.POST, headers, mapper.writeValueAsBytes(follow), 
				remoteUserKeyPair.getPrivate(), "https://example.com/sally#main-key");
		
		headers.setHost(new InetSocketAddress(InetAddress.getByName("andrewfesta.com"),0));
		
		mockMvc.perform(post(URI_PREFIX+"/admin/inbox")
				.content(mapper.writeValueAsBytes(follow))
				.headers(headers)
				.contentType(ActivityImpl.APPLICATION_ACTIVITY)
				.secure(true))
			.andExpect(status().isOk())
			.andDo(print());
	}
	
	private void setupAdminUser() {
		User user = new User();
		user.setUsername("admin");
		user.getRoles().add(new Role(user, "ROLE_ADMIN"));
		
		user = userRepository.save(user);
		
		SiteSettings siteSettings = siteSettingsManager.createSiteSettings(user);
		siteSettings.setNewUsersAutoFollowAdminUser(true);
		siteSettingsManager.save(siteSettings);
		
		em.flush();	
		
		userKeyManager.generateKeyPair(user);
		em.flush();	
	}
	
	private ActorImpl remoteActor() {
		Person remoteActor = new Person();
		remoteActor.setId(URI.create("https://example.com/sally"));
		remoteActor.setPreferredUsername("Sally");
		remoteActor.setInbox(URI.create("https://example.com/sally/inbox"));
		return remoteActor;
	}
	
}
