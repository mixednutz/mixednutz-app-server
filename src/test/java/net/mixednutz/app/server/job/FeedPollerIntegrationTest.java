package net.mixednutz.app.server.job;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.social.mixednutz.v1_9.connect.MixednutzConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.app.server.IntegrationTest;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth2AuthenticatedFeed;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;
import net.mixednutz.app.server.repository.ExternalFeedContentRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.ExternalFeedTimelineElementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class FeedPollerIntegrationTest {
	
	private static final String ACCESS_TOKEN = "8f99e835-5c14-4ed9-bf6a-09bc22176e3a";
	private static final String REFRESH_TOKEN = "dea76449-91c9-40c1-bdc9-89669d734bb9";
	private static final long EXPIRES_IN = 1572616916130L;
	
	@Autowired
	ExternalFeedTimelineElementRepository externalFeedTimelineElementRepository;
	
	@Autowired
	ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	ExternalFeedContentRepository repository;
	
	@Autowired
	ExternalCredentialsRepository credentialsRepository;
	
	@Autowired
	ApiProviderRegistry apiProviderRegistry;
			
	@Autowired
	MixednutzConnectionFactory mnConnFactory;
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	FeedPoller poller;
	
	private AbstractFeed createFeed() {
		//Save credentials
		Oauth2Credentials creds = new Oauth2Credentials();
		creds.setProviderId(mnConnFactory.getProviderId());
		creds.setAuthCode(ACCESS_TOKEN);
		creds.setRefreshToken(REFRESH_TOKEN);
		creds.setExpireTime(EXPIRES_IN);
		creds = credentialsRepository.save(creds);
				
		Oauth2AuthenticatedFeed feed = new Oauth2AuthenticatedFeed();
		feed.setProviderId(mnConnFactory.getProviderId());
		feed.setCredentials(creds);
		return externalFeedRepository.save(feed);
	}
	
	@Transactional
	@Test
	@Ignore
	public void test() {
		createFeed();
		
		System.out.println(apiProviderRegistry.getProviders());
		em.flush();
		em.clear();
		
		//First poll
		poller.poll();
		
		//Second poll
		poller.poll();
	}

}
