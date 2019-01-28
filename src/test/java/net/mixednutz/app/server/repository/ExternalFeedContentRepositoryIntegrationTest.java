package net.mixednutz.app.server.repository;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

import net.mixednutz.api.core.model.Action;
import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.TimelineElement;
import net.mixednutz.api.core.model.UserSmall;
import net.mixednutz.app.server.IntegrationTest;
import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ExternalFeedTimelineElement;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth2AuthenticatedFeed;

@RunWith(SpringRunner.class)
@ActiveProfiles("jpa-dev")
@DataJpaTest
@Category(IntegrationTest.class)
public class ExternalFeedContentRepositoryIntegrationTest {
	
	@Autowired
	ExternalFeedTimelineElementRepository externalFeedTimelineElementRepository;
	
	@Autowired
	ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	ExternalFeedContentRepository repository;
	
	@PersistenceContext
	EntityManager em;
	
	@Before
	public void setup() {
		
	}
	
	private AbstractFeed createFeed() {
		Oauth2AuthenticatedFeed feed = new Oauth2AuthenticatedFeed();
		feed.setProviderId("mixednutz");
		return feed;
	}
	
	private TimelineElement createTimelineElement() {
		TimelineElement element = new TimelineElement();
		element.setProviderId("838");
		element.setUri("/api/photo/id/838");
		element.setUrl("https://mixednutz.net/photo/id/838");
		element.setActions(new ArrayList<>());
		{
			Action tag = new Action(
					new Link("https://mixednutz.net/photo/id/838/tag"), 
					"tag", "New Tag");
			tag.setGlyphiconIconName("tag");
			element.getActions().add(tag);
		}
		{
			Action reaction = new Action(
					new Link("https://mixednutz.net/photo/id/838/reaction"), 
					"reaction", "New Reaction");
			reaction.setFontAwesomeIconName("smile-o");
			element.getActions().add(reaction);
		}
		element.setType(new TimelineElement.Type("Photo", "mixednutz.net", "mixednutz_Photo"));
		{
			UserSmall postedByUser = new UserSmall();
			postedByUser.setProviderId("1059");
			postedByUser.setUri("/api/user/shiltner");
			postedByUser.setUrl("https://mixednutz.net/shiltner");
			postedByUser.setUsername("shiltner");
			postedByUser.setAvatar(new Image("https://mixednutz.net/img/nophoto.gif"));
			element.setPostedByUser(postedByUser);
		}
		element.setPostedOnDate(ZonedDateTime.now());
		element.setDescription("Fur babies am I right. ");
			
		return element;
	}

	@Test
	public void test() {
		AbstractFeed feed = externalFeedRepository.save(createFeed());
		
		ExternalFeedContent content = new ExternalFeedContent(feed,
				externalFeedTimelineElementRepository.save(
						new ExternalFeedTimelineElement(createTimelineElement())), 
				ExternalFeedContent.TimelineType.HOME);
		
		repository.save(content);
		
		em.flush();
		em.clear();
		
		List<ExternalFeedContent> results = null;
		
		//Find first 10
		results = repository.findTimeline(feed.getFeedId(), 
				ExternalFeedContent.TimelineType.HOME, PageRequest.of(0, 10));
		assertEquals(1, results.size());
		
		results = repository.findTimelineMore(feed.getFeedId(), 
				ExternalFeedContent.TimelineType.HOME,
				ZonedDateTime.now(), PageRequest.of(0, 10));
		assertEquals(1, results.size());
		
		results = repository.findTimelineSince(feed.getFeedId(), 
				ExternalFeedContent.TimelineType.HOME,
				ZonedDateTime.now(), PageRequest.of(0, 10));
		assertEquals(0, results.size());
	}
	
}
