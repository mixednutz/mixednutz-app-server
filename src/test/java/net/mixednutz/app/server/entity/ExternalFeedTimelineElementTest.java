package net.mixednutz.app.server.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.mixednutz.api.core.model.Action;
import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.TimelineElement;
import net.mixednutz.api.core.model.UserSmall;
import net.mixednutz.api.twitter.model.TweetElement;
import net.mixednutz.app.server.repository.ExternalFeedTimelineElementRepository;
import twitter4j.Status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("jpa-dev")
//@DataJpaTest
@Tag("IntegrationTest")
public class ExternalFeedTimelineElementTest {
	
//	@Autowired
	ExternalFeedTimelineElementRepository repository;
	
//	@PersistenceContext
	EntityManager em;
	
	@Disabled
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
		
		
		ExternalFeedTimelineElement entity = new ExternalFeedTimelineElement(element);
		
		//Test Save
		repository.save(entity);
		em.flush();
		em.clear();
		
		
		//Test Load
		Optional<ExternalFeedTimelineElement> entity2 = repository.findById(element.getUri());
		assertTrue(entity2.isPresent());
		
	}

	@Disabled
	@Test
	public void testTwitterSerialization() {
		twitter4j.User user = Mockito.mock(twitter4j.User.class);
		Mockito.when(user.getScreenName()).thenReturn("postlocal");
		Mockito.when(user.getName()).thenReturn("Post Local");
		Mockito.when(user.getId()).thenReturn(14345759L);
		Mockito.when(user.getURL()).thenReturn("http://t.co/CkmtO4bbR1");
		Status status = Mockito.mock(Status.class);
		Mockito.when(status.getUser()).thenReturn(user);
		Mockito.when(status.getId()).thenReturn(1086342924055519232L);
		Mockito.when(status.getCreatedAt()).thenReturn(new Date());
		Mockito.when(status.getText()).thenReturn("Trump and Pence give surprise addresses at anti-abortion March for Life https://t.co/ncyYTFKftX");
		Mockito.when(status.getRetweetCount()).thenReturn(0);
		Mockito.when(status.getFavoriteCount()).thenReturn(2);
		
		TweetElement element = new TweetElement(status);
				
		ExternalFeedTimelineElement entity = new ExternalFeedTimelineElement(element);
		
		//Test Save
		repository.save(entity);
		em.flush();
		em.clear();
		
		
		//Test Load
		Optional<ExternalFeedTimelineElement> entity2 = repository.findById(element.getUri());
		assertTrue(entity2.isPresent());
		
		ExternalFeedTimelineElement element2 = entity2.get();
		
		element2.serializeElement();
		System.out.println(element2.getElementJson());
		
		assertEquals(Long.toString(1086342924055519232L), element.getProviderId());
	}

}
