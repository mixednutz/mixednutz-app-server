package net.mixednutz.app.server.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.app.server.IntegrationTest;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.repository.JournalRepository;
import net.mixednutz.app.server.repository.UserRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles("jpa-dev")
@DataJpaTest
@Category(IntegrationTest.class)
public class JournalTest {
	
	@Autowired
	private JournalRepository repository;
	
	@Autowired
	private UserRepository userRepository;
	
	@PersistenceContext
	private EntityManager em;
	
	User user;
	User user2;
	
	@Before
	public void setup() {
		user = new User();
		user.setUsername("andy");
		user = userRepository.save(user);
		
		user2 = new User();
		user2.setUsername("axthelm");
		user2 = userRepository.save(user2);
	}
	
	@Test
	public void findById() {
		
		
		Journal journal = new Journal();
		journal.setSubject("Test Journal");
		journal.setSubjectKey("test-journal");
		journal.setDescription("Description");
		journal.setBody("Body");
		journal.setAuthor(user);
		journal.setAuthorId(user.getUserId());
		journal.setOwner(user);
		journal.setOwnerId(user.getUserId());
		{
			HashSet<User> users = new HashSet<>();
			users.add(user);
			journal.setVisibility(Visibility.toSelectFollowers(users));
		}
		journal = repository.save(journal);
		assertNotNull(journal.getId());
		
		em.flush();
		em.clear();
		
		journal = repository.findById(journal.getId()).get();
		assertNotNull(journal.getId());		
		System.out.println(journal.getUri());
		
		em.flush();
		em.clear();
		
		System.out.println(journal.getPublishDateKey());
		Optional<Journal> result = repository.findByOwnerAndPublishDateKeyAndSubjectKey(
				user, journal.getPublishDateKey(), journal.getSubjectKey());
		assertTrue(result.isPresent());
	}
	
	@Test
	public void getMyPosts() {
		
		ZonedDateTime beginTime = ZonedDateTime.now();
		System.out.println(beginTime);
		
		Journal journal = new Journal();
		journal.setSubject("Test Journal");
		journal.setSubjectKey("test-journal");
		journal.setDescription("Description");
		journal.setBody("Body");
		journal.setAuthor(user);
		journal.setAuthorId(user.getUserId());
		journal.setOwner(user);
		journal.setOwnerId(user.getUserId());
		journal.setVisibility(Visibility.asPrivate());
		journal = repository.save(journal);
		assertNotNull(journal.getId());
		
		em.flush();
		em.clear();
		
		System.out.println(ZonedDateTime.now());
		List<Journal> results = repository.getMyPostsLessThan(
				user, ZonedDateTime.now(), PageRequest.of(0, 20));
		assertFalse(results.isEmpty());
		for (Journal j: results) {
			System.out.println(j.getSubject()+" ["+j.getUri()+"]");
		}
	
		results = repository.getMyPostsGreaterThan(
				user, beginTime, PageRequest.of(0, 20));
		assertFalse(results.isEmpty());
		for (Journal j: results) {
			System.out.println(j.getSubject()+" ["+j.getUri()+"]");
		}
		
	}

	@Test
	public void getUsersPosts() {
		
		ZonedDateTime beginTime = ZonedDateTime.now();
		
		Collection<Journal> posts = new ArrayList<Journal>();
		//User's Private Post (SHOULD NOT show up)
		{
			Journal journal = new Journal();
			journal.setSubject("Test Private Journal");
			journal.setSubjectKey("test-private-journal");
			journal.setDescription("Private Journal");
			journal.setBody("Body");
			journal.setAuthor(user2);
			journal.setAuthorId(user2.getUserId());
			journal.setOwner(user2);
			journal.setOwnerId(user2.getUserId());
			journal.setVisibility(Visibility.asPrivate());
			posts.add(journal);
		}
		//Author is Viewer (SHOULD show up)
		{
			Journal journal = new Journal();
			journal.setSubject("Test Non-Author Journal");
			journal.setSubjectKey("test-non-author-journal");
			journal.setDescription("Journal where author is not owner");
			journal.setBody("Body");
			journal.setAuthor(user2);
			journal.setAuthorId(user2.getUserId());
			journal.setOwner(user2);
			journal.setOwnerId(user2.getUserId());
			journal.setVisibility(Visibility.toAllUsers());
			posts.add(journal);
		}
		//User's Public Post (SHOULD)
		{
			Journal journal = new Journal();
			journal.setSubject("Test Public Journal");
			journal.setSubjectKey("test-public-journal");
			journal.setDescription("Public Journal");
			journal.setBody("Body");
			journal.setAuthor(user2);
			journal.setAuthorId(user2.getUserId());
			journal.setOwner(user2);
			journal.setOwnerId(user2.getUserId());
			journal.setVisibility(Visibility.toWorld());
			posts.add(journal);
		}
		//User's Private Select Post - includes viewer (SHOULD)
		{
			Journal journal = new Journal();
			journal.setSubject("Test Journal Select Followers 1");
			journal.setSubjectKey("test-journal-select-followers-1");
			journal.setDescription("Test Journal Select followers includes user1");
			journal.setBody("Body");
			journal.setAuthor(user2);
			journal.setAuthorId(user2.getUserId());
			journal.setOwner(user2);
			journal.setOwnerId(user2.getUserId());
			HashSet<User> users = new HashSet<>();
			users.add(user);
			journal.setVisibility(Visibility.toSelectFollowers(users));
			posts.add(journal);
		}
		//User's Private Select Post - does not include viewer (SHOULD NOT)
		{
			Journal journal = new Journal();
			journal.setSubject("Test Journal Select Followers 2");
			journal.setSubjectKey("test-journal-select-followers-2");
			journal.setDescription("Test Journal Select followers includes user2");
			journal.setBody("Body");
			journal.setAuthor(user2);
			journal.setAuthorId(user2.getUserId());
			journal.setOwner(user2);
			journal.setOwnerId(user2.getUserId());
			HashSet<User> users = new HashSet<>();
			users.add(user2);
			journal.setVisibility(Visibility.toSelectFollowers(users));
			posts.add(journal);
		}
		//User's All Users Post (SHOULD)
		{
			Journal journal = new Journal();
			journal.setSubject("Test All Users");
			journal.setSubjectKey("test-all-users");
			journal.setDescription("Test Journal All Authenticated Users");
			journal.setBody("Body");
			journal.setAuthor(user2);
			journal.setAuthorId(user2.getUserId());
			journal.setOwner(user2);
			journal.setOwnerId(user2.getUserId());
			HashSet<User> users = new HashSet<>();
			users.add(user);
			journal.setVisibility(Visibility.toAllUsers());
			posts.add(journal);
		}
		
		repository.saveAll(posts);
		
		em.flush();
		em.clear();
		
		System.out.println(ZonedDateTime.now());
		List<Journal> results = repository.getUsersPostsByDateCreatedLessThanEquals(
				user2, user, ZonedDateTime.now(), PageRequest.of(0, 20));
		assertFalse(results.isEmpty());
		assertEquals(4, results.size());
		for (Journal j: results) {
			System.out.println(j.getSubject()+" ["+j.getUri()+"]");
		}
		
		results = repository.getUsersPostsByDateCreatedGreaterThan(
				user2, user, beginTime, PageRequest.of(0, 20));
		assertFalse(results.isEmpty());
		assertEquals(4, results.size());
		for (Journal j: results) {
			System.out.println(j.getSubject()+" ["+j.getUri()+"]");
		}
		
		
		//viewer not authenticated
		results = repository.getUsersPostsByDateCreatedLessThanEquals(
				user2, null, ZonedDateTime.now(), PageRequest.of(0, 20));
		assertFalse(results.isEmpty());
		assertEquals(1, results.size());
		for (Journal j: results) {
			System.out.println(j.getSubject());
		}
		
		results = repository.getUsersPostsByDateCreatedGreaterThan(
				user2, null, beginTime, PageRequest.of(0, 20));
		assertFalse(results.isEmpty());
		assertEquals(1, results.size());
		for (Journal j: results) {
			System.out.println(j.getSubject()+" ["+j.getUri()+"]");
		}
	}
}
