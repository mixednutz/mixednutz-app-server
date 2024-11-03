package net.mixednutz.app.server.controller.api;

import static net.mixednutz.api.activitypub.ActivityPubManager.URI_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.w3c.activitypub.util.ProblemHandler;
import org.w3c.activitystreams.model.ActivityImpl;
import org.w3c.activitystreams.model.ActorImpl;
import org.w3c.activitystreams.model.LinkImpl;
import org.w3c.activitystreams.model.Note;
import org.w3c.activitystreams.model.PublicKey;
import org.w3c.activitystreams.model.activity.Accept;
import org.w3c.activitystreams.model.activity.Create;
import org.w3c.activitystreams.model.activity.Follow;
import org.w3c.activitystreams.model.actor.Person;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.activitypub.client.ActivityPubClientManager;
import net.mixednutz.app.server.entity.Follower;
import net.mixednutz.app.server.entity.Lastonline;
import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.manager.FollowerManager;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.manager.impl.UserKeyManagerImpl;
import net.mixednutz.app.server.repository.LastonlineRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
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
	private UserProfileRepository userProfileRepository;
	
	@Autowired
	UserKeyManagerImpl userKeyManager;
	
	@Autowired
	ActivityPubManager activityPubManager;
	
	@MockBean
	private ActivityPubClientManager activityPubClientManager;
		
	@Autowired
	FollowerManager followerManager;
	
	@Autowired
	LastonlineRepository lastonlineRepository;
	
	User adminUser;
	
	
	@Transactional
	@Test
	public void testFollowNewUser() throws Exception {
		
		setupAdminUser();
				
		ActorImpl remoteActor = remoteActor();
		KeyPair remoteUserKeyPair = HttpSignaturesUtil.generateKeyPair();
		remoteActor.setPublicKey(new PublicKey("main-key", remoteActor.getId(),
				HttpSignaturesUtil.publicKeyToPem(remoteUserKeyPair.getPublic())));
		when(activityPubClientManager.getActor(eq(remoteActor.getId()))).thenReturn(remoteActor);
		
		
		activityPubManager.getActorUri("admin");
		
		/*
		 * {"
		 * @context":"https://www.w3.org/ns/activitystreams",
		 * "id":"https://mstdn.social/9c5d807b-4659-4414-bf93-ecd06f55fa58",
		 * "type":"Follow",
		 * "actor":"https://mstdn.social/users/tfemilytest",
		 * "object":"https://tfemily.com/activitypub/Emily"}
		 */
		Follow follow = new Follow();
		activityPubManager.initRoot(follow);
		follow.setId(URI.create("https://example.com/fake-id"));
		follow.setSummary("Sally follows Admin");
		follow.setActor(new LinkImpl(remoteActor.getId()));
		follow.setObject(new LinkImpl(activityPubManager.getActorUri("admin")));
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new ProblemHandler());
		System.out.println("BODY:");
		System.out.println(
				mapper.writerWithDefaultPrettyPrinter().writeValueAsString(follow));
			
		
		/*
		 * host:"tfemily.com", 
		 * x-request-id:"5abb448066284d4ab30368926232f2a4", 
		 * x-real-ip:"88.198.169.70", 
		 * x-forwarded-host:"tfemily.com", 
		 * x-forwarded-port:"443", 
		 * x-forwarded-proto:"https", 
		 * x-forwarded-scheme:"https", 
		 * x-scheme:"https", 
		 * content-length:"231", 
		 * user-agent:"http.rb/5.1.1 (Mastodon/4.1.0; +https://mstdn.social/)", 
		 * date:"Sat, 18 Mar 2023 13:53:23 GMT", 
		 * accept-encoding:"gzip", 
		 * digest:"SHA-256=gaeQk40Yk1CHFvsX3GInsRKq5jnnhdzVSk3iZqkJh2g=", 
		 * content-type:"application/activity+json", 
		 * signature:"keyId="https://mstdn.social/users/tfemilytest#main-key",algorithm="rsa-sha256",headers="(request-target) host date digest content-type",signature="Iy6Tg58od+9N8I5w0VUXm/oIfO4tdUEc8Svxig1F8RdYoySOwZ8FWFoR0FXTwOsWMRhOy1bOvUAXOv53RtgyDj16hXYUwlLtPH/DvsOrqzY87x78Eebff2zabMDaPLiKWfxqnEVLvVxYKw6tB/wfKKTFsvvIHnQ8T/G/q1kWg75jq2gIbFsqtyReTv7dcuYpomtAP4D7ACfq5OVShbWsT4dx3fEhKtFIDBR1ZLNRXoYR4poWsswusTn4smeem/d6NOHAnftU3252VCr5UHQwIyeooAGDqYOLrWnr5JIM1lL3GcidrfplbIvxd/rwN5CVi+sWd7PnkJbKwS4Ix/ifzg==""
		 */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(ActivityImpl.APPLICATION_ACTIVITY);
		HttpSignaturesUtil.signRequest(
				URI.create("https://andrewfesta.com"+URI_PREFIX+"/admin/inbox"), 
				HttpMethod.POST, headers, mapper.writeValueAsBytes(follow), 
				remoteUserKeyPair.getPrivate(), "https://example.com/sally#main-key");
		
		headers.setHost(new InetSocketAddress(InetAddress.getByName("andrewfesta.com"),0));
		
		List<Accept> acceptHandler = new ArrayList<>();
		doAnswer(inv->{
			acceptHandler.add(inv.getArgument(1, Accept.class));
			return null;
		}).when(activityPubClientManager).sendActivity(any(), any(Accept.class), any());
		
		mockMvc.perform(post(URI_PREFIX+"/admin/inbox")
				.content(mapper.writeValueAsBytes(follow))
				.headers(headers)
				.contentType(ActivityImpl.APPLICATION_ACTIVITY)
				.secure(true))
			.andExpect(status().isOk())
			.andDo(print());
		
		em.flush();
		em.clear();
		
		assertEquals(1, followerManager.countFollowers(adminUser));
		List<Follower> followers = followerManager.getAllFollowers(adminUser);
		Follower follower = followers.get(0);
		assertFalse(follower.isPending()); 
		assertEquals("@Sally@example.com",follower.getFollower().getUsername());
		
		//Test Accept
		Accept accept = acceptHandler.get(0);
		assertNotNull(accept.getObject());
		assertTrue(accept.getObject() instanceof Follow);
		Follow object = (Follow) accept.getObject();
		assertEquals("https://example.com/fake-id", object.getId().toString());
		assertEquals("https://example.com/sally", object.getActor().getHref().toString());
		assertEquals("https://andrewfesta.com/activitypub/admin", object.getObject().getHref().toString());
		assertEquals("https://andrewfesta.com/activitypub/admin", accept.getActor().getHref().toString());
		assertEquals("https://example.com/sally", accept.getTo().get(0).getHref().toString());
		assertEquals("https://andrewfesta.com/activitypub/Accept/fake-id", accept.getId().toString());
	}
	
	@Transactional
	@Test
	public void testFollowExistingUser() throws Exception {
		setupAdminUser();
		setupRemoteUser();
		
		ActorImpl remoteActor = remoteActor();
		KeyPair remoteUserKeyPair = HttpSignaturesUtil.generateKeyPair();
		remoteActor.setPublicKey(new PublicKey("main-key", remoteActor.getId(),
				HttpSignaturesUtil.publicKeyToPem(remoteUserKeyPair.getPublic())));
		when(activityPubClientManager.getActor(eq(remoteActor.getId()))).thenReturn(remoteActor);
	
		
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
		
		em.flush();
		em.clear();
		
		assertEquals(1, followerManager.countFollowers(adminUser));
		List<Follower> followers = followerManager.getFollowers(adminUser);
		Follower follower = followers.get(0);
		assertFalse(follower.isPending()); 
		assertEquals("sally",follower.getFollower().getUsername());
	}
	
	@Transactional
	@Test
	public void testGoneUser() throws Exception {
		setupAdminUser();
				
		ActorImpl remoteActor = remoteActor();
		KeyPair remoteUserKeyPair = HttpSignaturesUtil.generateKeyPair();
		remoteActor.setPublicKey(new PublicKey("main-key", remoteActor.getId(),
				HttpSignaturesUtil.publicKeyToPem(remoteUserKeyPair.getPublic())));
		when(activityPubClientManager.getActor(eq(remoteActor.getId()))).thenThrow(
				HttpClientErrorException.create(HttpStatus.GONE, URI_PREFIX, null, null, null));
		
		
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
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
	
	@Transactional
	@Test
	public void testBadServer() throws Exception {
		setupAdminUser();
				
		ActorImpl remoteActor = remoteActor();
		KeyPair remoteUserKeyPair = HttpSignaturesUtil.generateKeyPair();
		remoteActor.setPublicKey(new PublicKey("main-key", remoteActor.getId(),
				HttpSignaturesUtil.publicKeyToPem(remoteUserKeyPair.getPublic())));
		when(activityPubClientManager.getActor(eq(remoteActor.getId()))).thenThrow(
				HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, URI_PREFIX, null, null, null));
		
		
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
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
	
	@Transactional
	@Test
	public void testCreateReply() throws Exception {
		
		setupAdminUser();
		
		ActorImpl remoteActor = remoteActor();
		KeyPair remoteUserKeyPair = HttpSignaturesUtil.generateKeyPair();
		remoteActor.setPublicKey(new PublicKey("main-key", remoteActor.getId(),
				HttpSignaturesUtil.publicKeyToPem(remoteUserKeyPair.getPublic())));
		when(activityPubClientManager.getActor(eq(remoteActor.getId()))).thenReturn(remoteActor);
		
		
		activityPubManager.getActorUri("admin");
		
		/*
		 * {
		 * "@context":["https://www.w3.org/ns/activitystreams",{"ostatus":"http://ostatus.org#","atomUri":"ostatus:atomUri","inReplyToAtomUri":"ostatus:inReplyToAtomUri","conversation":"ostatus:conversation","sensitive":"as:sensitive","toot":"http://joinmastodon.org/ns#","votersCount":"toot:votersCount"}],
		 * "id":"https://mastodon.andrewfesta.com/users/admin/statuses/111659789080001617/activity",
		 * "type":"Create",
		 * "actor":"https://mastodon.andrewfesta.com/users/admin",
		 * "published":"2023-12-28T19:52:48Z",
		 * "to":["https://www.w3.org/ns/activitystreams#Public"],
		 * "cc":[
		 *  "https://mastodon.andrewfesta.com/users/admin/followers",
		 *  "https://tfemily.com/activitypub/Emily"
		 *  ],
		 *  "object":{
		 *  	"id":"https://mastodon.andrewfesta.com/users/admin/statuses/111659789080001617",
		 *  	"type":"Note",
		 *  	"summary":"Cate Fox and the Case of the Fading Magic - Chapter 24",
		 *  "inReplyTo":"https://tfemily.com/activitypub/Note/Emily/series/8/cate-fox-and-the-case-of-the-fading-magic/chapter/149/chapter-24",
		 *  "published":"2023-12-28T19:52:48Z",
		 *  "url":"https://mastodon.andrewfesta.com/@admin/111659789080001617",
		 *  "attributedTo":"https://mastodon.andrewfesta.com/users/admin",
		 *  "to":["https://www.w3.org/ns/activitystreams#Public"],
		 *  "cc":["https://mastodon.andrewfesta.com/users/admin/followers","https://tfemily.com/activitypub/Emily"],
		 *  "sensitive":true,
		 *  "atomUri":"https://mastodon.andrewfesta.com/users/admin/statuses/111659789080001617",
		 *  "inReplyToAtomUri":"https://tfemily.com/activitypub/Note/Emily/series/8/cate-fox-and-the-case-of-the-fading-magic/chapter/149/chapter-24",
		 *  "conversation":"tag:mastodon.andrewfesta.com,2023-12-25:objectId=6:objectType=Conversation",
		 *  "content":"\u003cp\u003e\u003cspan class=\"h-card\"\u003e\u003ca href=\"https://tfemily.com/Emily\" class=\"u-url mention\"\u003e@\u003cspan\u003eEmily\u003c/span\u003e\u003c/a\u003e\u003c/span\u003e test reply\u003c/p\u003e","contentMap":{"en":"\u003cp\u003e\u003cspan class=\"h-card\"\u003e\u003ca href=\"https://tfemily.com/Emily\" class=\"u-url mention\"\u003e@\u003cspan\u003eEmily\u003c/span\u003e\u003c/a\u003e\u003c/span\u003e test reply\u003c/p\u003e"},
		 *  "attachment":[],
		 *  "tag":[
		 *   {
		 *    "type":"Mention",
		 *    "href":"https://tfemily.com/activitypub/Emily",
		 *    "name":"@Emily@tfemily.com"}],
		 *  "replies":{
		 *  	"id":"https://mastodon.andrewfesta.com/users/admin/statuses/111659789080001617/replies",
		 *  	"type":"Collection",
		 *    	"first":{"type":"CollectionPage","next":"https://mastodon.andrewfesta.com/users/admin/statuses/111659789080001617/replies?only_other_accounts=true\u0026page=true","partOf":"https://mastodon.andrewfesta.com/users/admin/statuses/111659789080001617/replies","items":[]}}},"signature":{"type":"RsaSignature2017","creator":"https://mastodon.andrewfesta.com/users/admin#main-key","created":"2023-12-28T19:52:48Z","signatureValue":"AvMhScjKB31VlF7k4+eI9HaugYJ74NDi1uFLDHOm9M2Vw7nhm1OFGZkVFUHcmdOHvXEnrVkLdbFPUmANUbwY8I2wjecIFCm0byt/cjoReVRJ36v1c0ELVsy/IFVNafg686NQaH231lHQWtBTMbFsaidLqxOIynBM4ReALI9Xhga8qKQbaDpnS1SEqOuQlyLdVu99HAILXDMei7VBUmj//Ha/OsKyiYkrlFlB9se33QC7GDmuZ5KvDHTnJPAEt0+aIHyftcL75ERJP1fh0uyMh7BkITVuTRksqHAJ0ivVzCYC9FDjEI1fNSBFjio1s/oCVPU7nupD4jtryn2UU2vekw=="}}
		 */
		
		Create create = new Create();
		activityPubManager.initRoot(create);
		create.setSummary("Sally follows Admin");
		create.setActor(new LinkImpl(remoteActor.getId()));
		create.setTo(List.of(new LinkImpl(Create.PUBLIC)));
		create.setCc(List.of(
				new LinkImpl(remoteActor.getFollowers()),
				new LinkImpl(activityPubManager.getActorUri("admin"))));
		
		Note reply = new Note();
		reply.setSummary("summary of original note");
		reply.setInReplyTo(new LinkImpl(URI.create("https://tfemily.com/activitypub/Create/Emily/journal/2023/2/8/tv-review-quantum-leap-2022-let-them-play")));
		reply.setPublished(ZonedDateTime.now());
		reply.setAttributedTo(create.getActor());
		reply.setTo(create.getTo());
	    reply.setCc(create.getCc());
	    reply.setContent("test reply");
	    
	    ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new ProblemHandler());
		System.out.println(
				mapper.writerWithDefaultPrettyPrinter().writeValueAsString(create));
			
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(ActivityImpl.APPLICATION_ACTIVITY);
		HttpSignaturesUtil.signRequest(
				URI.create("https://andrewfesta.com"+URI_PREFIX+"/admin/inbox"), 
				HttpMethod.POST, headers, mapper.writeValueAsBytes(create), 
				remoteUserKeyPair.getPrivate(), "https://example.com/sally#main-key");
		
		headers.setHost(new InetSocketAddress(InetAddress.getByName("andrewfesta.com"),0));
		
		mockMvc.perform(post(URI_PREFIX+"/admin/inbox")
				.content(mapper.writeValueAsBytes(create))
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
		adminUser = user;
		
		SiteSettings siteSettings = siteSettingsManager.createSiteSettings(user);
		siteSettings.setNewUsersAutoFollowAdminUser(true);
		siteSettingsManager.save(siteSettings);
		
		em.flush();	
		
		userKeyManager.generateKeyPair(user);
		em.flush();	
	}
	
	private void setupRemoteUser() {
		User user = new User();
		user.setUsername("sally");
		user.getRoles().add(new Role(user, "ROLE_USER"));
		
		user = userRepository.save(user);
		
		em.flush();	
		
		UserProfile userProfile = new UserProfile();
		userProfile.setUser(user);
		userProfile.setUserId(user.getUserId());
		userProfile.setActivityPubActorUri("https://example.com/sally");
		userProfileRepository.save(userProfile);
		
		em.flush();	
		
		Lastonline lastonline = new Lastonline();
		lastonline.setUserId(user.getUserId());
		lastonlineRepository.save(lastonline);
		
		em.flush();	
	}
	
	private ActorImpl remoteActor() {
		Person remoteActor = new Person();
		remoteActor.setId(URI.create("https://example.com/sally"));
		remoteActor.setPreferredUsername("Sally");
		remoteActor.setInbox(URI.create("https://example.com/sally/inbox"));
		remoteActor.setFollowers(URI.create("https://example.com/sally/followers"));
		return remoteActor;
	}
	
}
