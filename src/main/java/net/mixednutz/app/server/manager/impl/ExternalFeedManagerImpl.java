package net.mixednutz.app.server.manager.impl;

import static net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials.OAUTH1;
import static net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials.OAUTH2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.client.MixednutzClient;
import net.mixednutz.api.client.TimelineClient;
import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.model.PageBuilder.GetTokenCallback;
import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.api.provider.ApiProvider;
import net.mixednutz.api.provider.IOauth1Credentials;
import net.mixednutz.api.provider.IOauth2Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.ExternalAccountCredentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;
import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ExternalFeedTimelineElement;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth1AuthenticatedFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth2AuthenticatedFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ExternalAccountCredentialsManager;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedContentRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.ExternalFeedTimelineElementRepository;

@Service
@Transactional
public class ExternalFeedManagerImpl implements ExternalFeedManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExternalFeedManagerImpl.class);

	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedTimelineElementRepository externalFeedTimelineElementRepository;
	
	@Autowired
	private ExternalFeedContentRepository externalFeedContentRepository;
	
	@Autowired
	private ApiProviderRegistry apiProviderRegistry;
	
	@Autowired
	private ExternalAccountCredentialsManager externalAccountCredentialsManager;
		
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUser(User user) {
		final Map<String, List<AbstractFeed>> map = collate(externalFeedRepository.findByUser(user));
		final Map<INetworkInfoSmall, List<AbstractFeed>> newMap = new LinkedHashMap<>();
		for (Entry<String, List<AbstractFeed>> entry: map.entrySet()) {
			ApiProvider<?, ?> provider = apiProviderRegistry.getSocialNetworkClient(entry.getKey());
			newMap.put(provider.getNetworkInfo(), entry.getValue());
		}
		return newMap;
	}

	private Map<String, List<AbstractFeed>> collate(Iterable<AbstractFeed> feeds) {
		final Map<String, List<AbstractFeed>> map = new LinkedHashMap<>();
		for (AbstractFeed feed: feeds) {
			String providerId = feed.getProviderId();
			if (!map.containsKey(providerId)) {
				map.put(providerId, new ArrayList<AbstractFeed>());
			}
			map.get(providerId).add(feed);
		}
		return map;
	}
	
	@Cacheable(value="externalFeed", 
			key="T(net.mixednutz.app.server.manager.impl.ExternalFeedManagerImpl).getTimelineHash(#feed, #hashtag, #paging)")
	public IPage<? extends ITimelineElement,Instant> getTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging) {
			
		List<ExternalFeedContent> contents = null;
		net.mixednutz.api.core.model.PageRequest<Instant> pageRequest;
		if (paging.getStart()==null) {
			pageRequest = net.mixednutz.api.core.model.PageRequest.first(
					paging.getPageSize(), paging.getDirection(), Instant.class);
			contents = externalFeedContentRepository.findTimeline(feed.getFeedId(), 
					PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = ZonedDateTime.parse(paging.getStart());
			pageRequest = net.mixednutz.api.core.model.PageRequest.next(
					start.toInstant(), paging.getPageSize(), paging.getDirection());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = externalFeedContentRepository.findTimelineMore(feed.getFeedId(), 
						start, PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = externalFeedContentRepository.findTimelineSince(feed.getFeedId(), 
						start, PageRequest.of(0, paging.getPageSize()));
			}
		}
		List<ExternalFeedTimelineElement> elements = new ArrayList<>();
		for (ExternalFeedContent content: contents) {
			elements.add(content.getElement());
		}
		
		return new PageBuilder<ExternalFeedTimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTokenCallback(new GetTokenCallback<ExternalFeedTimelineElement, Instant>(){
				@Override
				public Instant getToken(ExternalFeedTimelineElement item) {
					return item.getProviderPostedOnDate().toInstant();
				}})
			.build();
	}
	
	public static int getTimelineHash(AbstractFeed feed, String hashtag, IPageRequest<Object> paging) {
		int hash = feed.getFeedId().hashCode();
		if (hashtag!=null) {
			hash += hashtag.hashCode();
		}
		if (paging!=null) {
			hash += paging.hashCode();
		}
		return hash;
	}
	
	@SuppressWarnings("unchecked")
	private <Api, Credentials> ApiProvider<Api, Credentials> getProvider(ExternalAccountCredentials creds, 
			Class<Api> apiClass, Class<Credentials> credentialsClass) {
		return (ApiProvider<Api, Credentials>) apiProviderRegistry
				.getSocialNetworkClient(creds.getProviderId());
	}
	
	@SuppressWarnings("unchecked" )
	private <Token> TimelineClient<Token> getTimelineClient(MixednutzClient api, Class<Token> tokenClass) {
		return (TimelineClient<Token>) api.getTimelineClient();
	}
	
	protected IPage<? extends ITimelineElement,Object> loadLiveTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging) {
		if (OAUTH1.equals(feed.getType())) {
			return getOauth1Timeline((Oauth1AuthenticatedFeed)feed, hashtag, paging);
		} else if (OAUTH2.equals(feed.getType())) {
			return getOauth2Timeline((Oauth2AuthenticatedFeed)feed, hashtag, paging);
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected IPage<? extends ITimelineElement,Object> pollLiveTimeline(AbstractFeed feed) {
		if (OAUTH1.equals(feed.getType())) {
			return pollOauth1Timeline((Oauth1AuthenticatedFeed)feed);
		} else if (OAUTH2.equals(feed.getType())) {
			return pollOauth2Timeline((Oauth2AuthenticatedFeed)feed);
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected void saveTimeline(AbstractFeed feed,
			IPage<? extends ITimelineElement,Object> timeline) {
		for (ITimelineElement timelineElement: timeline.getItems()) {
			ExternalFeedContent content = new ExternalFeedContent(feed,
					externalFeedTimelineElementRepository.save(
							new ExternalFeedTimelineElement(timelineElement)));
			externalFeedContentRepository.save(content);
		}
	}
	
	public IPage<? extends ITimelineElement,Object> pollTimeline(AbstractFeed feed) {
		//Get Live Content
		ZonedDateTime crawledTime = ZonedDateTime.now();
		
		IPage<? extends ITimelineElement,Object> timeline =
				pollLiveTimeline(feed);
		
		//Save Live Content
		saveTimeline(feed, timeline);
		
		feed.setLastCrawled(crawledTime);
		if (!timeline.getItems().isEmpty()) {
			feed.setLastCrawledKey(timeline.getReversePage().getStart().toString());
		}
		
		return timeline;
	}
	
	public IPage<? extends ITimelineElement,Object> pollTimeline(AbstractFeed feed, 
			IPageRequest<String> paging) {
		//Get Live Content
		ZonedDateTime crawledTime = ZonedDateTime.now();
		
		IPage<? extends ITimelineElement,Object> timeline =
				loadLiveTimeline(feed, null, paging);
		
		//Save Live Content
		saveTimeline(feed, timeline);
		
		feed.setLastCrawled(crawledTime);
		if (!timeline.getItems().isEmpty()) {
			feed.setLastCrawledKey(timeline.getReversePage().getStart().toString());
		}
				
		return timeline;
	}
		
	protected IPage<? extends ITimelineElement, Object> getOauth1Timeline(
			Oauth1AuthenticatedFeed feed, String hashtag, IPageRequest<String> prevPage) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		
		//TODO add in hashtag for search

		
		IPage<? extends ITimelineElement, Object> page;
		if (prevPage!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), prevPage.getStart(), prevPage.getPageSize()});
			page = getTimelineClient(api, Object.class).getTimelineStringToken(prevPage);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = getTimelineClient(api, Object.class).getTimeline();
		}
		
		return page;
	}
	

	protected IPage<? extends ITimelineElement, Object> getOauth2Timeline(
			Oauth2AuthenticatedFeed feed, String hashtag, IPageRequest<String> prevPage) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		
		//TODO add in hashtag for search

		
		IPage<? extends ITimelineElement, Object> page;
		if (prevPage!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), prevPage.getStart(), prevPage.getPageSize()});
			page = getTimelineClient(api, Object.class).getTimelineStringToken(prevPage);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = getTimelineClient(api, Object.class).getTimeline();
		}
		
		return page;
	}
	
	protected IPage<? extends ITimelineElement, Object> pollOauth1Timeline(
			Oauth1AuthenticatedFeed feed) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		
		IPageRequest<String> pagination = 
				getTimelineClient(api, Object.class).getTimelinePollRequest(
						feed.getLastCrawledKey());
		
		IPage<? extends ITimelineElement, Object> page;
		if (pagination!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), pagination.getStart(), pagination.getPageSize()});
			page = getTimelineClient(api, Object.class).getTimelineStringToken(pagination);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = getTimelineClient(api, Object.class).getTimeline();
		}
		
		return page;
	}
	

	protected IPage<? extends ITimelineElement, Object> pollOauth2Timeline(
			Oauth2AuthenticatedFeed feed) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		
		IPageRequest<String> pagination = 
				getTimelineClient(api, Object.class).getTimelinePollRequest(
						feed.getLastCrawledKey());

		IPage<? extends ITimelineElement, Object> page;
		if (pagination!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), pagination.getStart(), pagination.getPageSize()});
			page = getTimelineClient(api, Object.class).getTimelineStringToken(pagination);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = getTimelineClient(api, Object.class).getTimeline();
		}
		
		return page;
	}
	

	@Override
	public List<String> getCompatibleFeedsForCrossposting(INetworkInfoSmall networkInfo) {
		return Arrays.asList(networkInfo.compatibleMimeTypes());
	}


	@Override
	public Map<INetworkInfoSmall, Collection<String>> getCompatibleFeedsForCrossposting() {
		final Map<INetworkInfoSmall, Collection<String>> mimeTypes = new LinkedHashMap<>();
		for (ApiProvider<?,?> provider: apiProviderRegistry.getProviders()) {
			INetworkInfoSmall key = provider.getNetworkInfo();
			mimeTypes.put(key, this.getCompatibleFeedsForCrossposting(key));
		}
		return mimeTypes;
	}
	
	public Map<String, INetworkInfoSmall> getProviders() {
		final Map<String, INetworkInfoSmall> map = new LinkedHashMap<>();
		for (ApiProvider<?,?> provider: apiProviderRegistry.getProviders()) {
			map.put(provider.getProviderId(), provider.getNetworkInfo());
		}
		return map;
	}
	
	
}
