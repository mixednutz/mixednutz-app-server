package net.mixednutz.app.server.entity;

import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.api.core.model.Action;
import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.TimelineElement;
import net.mixednutz.api.core.model.UserSmall;
import net.mixednutz.app.server.IntegrationTest;
import net.mixednutz.app.server.repository.ExternalFeedTimelineElementRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles("jpa-dev")
@DataJpaTest
@Category(IntegrationTest.class)
public class ExternalFeedTimelineElementTest {
	
	@Autowired
	ExternalFeedTimelineElementRepository repository;
	
	@PersistenceContext
	EntityManager em;
	
	@Test
	public void testSerialization() {
		
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
		element.setType(new TimelineElement.Type("Photo", "mixednutz.net"));
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
		
		
		ExternalFeedTimelineElement entity = new ExternalFeedTimelineElement(element);
		
		//Test Save
		repository.save(entity);
		em.flush();
		em.clear();
		
		
		//Test Load
		Optional<ExternalFeedTimelineElement> entity2 = repository.findById(element.getUri());
		assertTrue(entity2.isPresent());
	}

}
