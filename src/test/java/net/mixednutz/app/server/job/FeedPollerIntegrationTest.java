package net.mixednutz.app.server.job;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.social.mixednutz.v1_9.connect.MixednutzConnectionFactory;
import org.springframework.social.twitter4j.connect.TwitterConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.app.server.IntegrationTest;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth1AuthenticatedFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth2AuthenticatedFeed;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;
import net.mixednutz.app.server.repository.ExternalFeedContentRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.ExternalFeedTimelineElementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
@Transactional
public class FeedPollerIntegrationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(FeedPollerIntegrationTest.class);
	
	private static final String MIXEDNUTZ_ACCESS_TOKEN = "8f99e835-5c14-4ed9-bf6a-09bc22176e3a";
	private static final String MIXEDNUTZ_REFRESH_TOKEN = "dea76449-91c9-40c1-bdc9-89669d734bb9";
	private static final long MIXEDNUTZ_EXPIRES_IN = 1572616916130L;
//	private static final String MIXEDNUTZ_ACCESS_TOKEN = "854ca18e-ff14-46d1-b1f2-e3bb2c3582c9";
//	private static final String MIXEDNUTZ_REFRESH_TOKEN = "3ff63145-d9d3-4294-b566-35ac8f25e8d5";
//	private static final long MIXEDNUTZ_EXPIRES_IN = 1570740494868L;
	
	private static final String TWITTER_ACCESS_ID = "228538942-7bjeI60YMfW5fVeAJLpRLeEPrOtXZUt4v3ENhiZm";
	private static final String TWITTER_ACCESS_SECRET = "YdHzmeIHQnoQLE3Bx5WIvX0Ubu6LGnvgB1f9oZRuhDCmQ";
	
	
	
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
	
	@Autowired
	TwitterConnectionFactory twitterFactory;
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	FeedPoller poller;
	
	private AbstractFeed createMixedNutzFeed() {
		
		LOG.info("Creating feed for {}", mnConnFactory.getProviderId());
		//Save credentials
		Oauth2Credentials creds = new Oauth2Credentials();
		creds.setProviderId(mnConnFactory.getProviderId());
		creds.setAuthCode(MIXEDNUTZ_ACCESS_TOKEN);
		creds.setRefreshToken(MIXEDNUTZ_REFRESH_TOKEN);
		creds.setExpireTime(MIXEDNUTZ_EXPIRES_IN);
		creds = credentialsRepository.save(creds);
				
		Oauth2AuthenticatedFeed feed = new Oauth2AuthenticatedFeed();
		feed.setProviderId(mnConnFactory.getProviderId());
		feed.setCredentials(creds);
		return externalFeedRepository.save(feed);
	}
	
	private AbstractFeed createTwitterFeed() {
		
		LOG.info("Creating feed for {}", twitterFactory.getProviderId());
		//Save credentials
		Oauth1Credentials creds = new Oauth1Credentials();
		creds.setProviderId(twitterFactory.getProviderId());
		creds.setAccessToken(TWITTER_ACCESS_ID);
		creds.setSecret(TWITTER_ACCESS_SECRET);
		creds = credentialsRepository.save(creds);
				
		Oauth1AuthenticatedFeed feed = new Oauth1AuthenticatedFeed();
		feed.setProviderId(twitterFactory.getProviderId());
		feed.setCredentials(creds);
		return externalFeedRepository.save(feed);
	}
	
	
	@Test
	@Ignore
	public void testPollMixedNutz() {
		createMixedNutzFeed();

		em.flush();
		em.clear();
		
		//First poll
		poller.poll();
		
		//Second poll
		poller.poll();
	}
	
	@Test
//	@Ignore
	public void testPollTwitter() {
		createTwitterFeed();
		
		em.flush();
		em.clear();
		
		//First poll
		poller.poll();
		
		System.out.println("Sleeping for "+ (1000*60*5)+"ms");
		try {
			Thread.sleep(1000*60*5); //5 minutes
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Second poll
		poller.poll();
	}
	
	static {
	    //for localhost testing only
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){
 
	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return false;
	        }
	    });
	}

}
