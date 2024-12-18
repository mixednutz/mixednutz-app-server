package net.mixednutz.app.server.manager.impl;

import static net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials.OAUTH1;
import static net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials.OAUTH2;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.UriComponentsBuilder;

import net.mixednutz.api.client.MixednutzClient;
import net.mixednutz.api.client.PostClient;
import net.mixednutz.api.client.TimelineClient;
import net.mixednutz.api.client.UserClient;
import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.IPost;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.api.model.IUserSmall;
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
import net.mixednutz.app.server.entity.VisibilityType;
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
			if (provider!=null) {
				newMap.put(provider.getNetworkInfo(), entry.getValue());
			} else {
				LOG.warn("Unable to find provider for {}", entry.getKey());
			}
			
		}
		return newMap;
	}

	@Override
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUserVisibleToWorld(User user) {
		final Map<String, List<AbstractFeed>> map = collate(
				externalFeedRepository.findByUserAndVisibilityIn(user, 
						Collections.singleton(VisibilityType.WORLD)));
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
	
	@Cacheable(value="externalHomeFeed", 
			key="T(net.mixednutz.app.server.manager.impl.ExternalFeedManagerImpl).getTimelineHash(#feed, #hashtag, #paging, T(net.mixednutz.app.server.entity.ExternalFeedContent.TimelineType).HOME)")
	public IPage<? extends ITimelineElement,Instant> getTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging) {
		return this.getTimelineInternal(feed, hashtag, 
				ExternalFeedContent.TimelineType.HOME, paging);
	}
	
	@Cacheable(value="externalUserFeed", 
			key="T(net.mixednutz.app.server.manager.impl.ExternalFeedManagerImpl).getTimelineHash(#feed, #hashtag, #paging, T(net.mixednutz.app.server.entity.ExternalFeedContent.TimelineType).USER)")
	public IPage<? extends ITimelineElement, Instant> getUserTimeline(AbstractFeed feed, String hashtag,
			IPageRequest<String> paging) {
		return this.getTimelineInternal(feed, hashtag, 
				ExternalFeedContent.TimelineType.USER, paging);
	}

	@Cacheable(value="externalUserFeeds", 
			key="T(net.mixednutz.app.server.manager.impl.ExternalFeedManagerImpl).getTimelineHash(#feeds, #hashtag, #paging, T(net.mixednutz.app.server.entity.ExternalFeedContent.TimelineType).USER)")
	public IPage<? extends ITimelineElement, Instant> getUserTimeline(Iterable<AbstractFeed> feeds, String hashtag,
			IPageRequest<String> paging) {
		return this.getTimelineInternal(feeds, hashtag, 
				ExternalFeedContent.TimelineType.USER, paging);
	}

	protected IPage<? extends ITimelineElement,Instant> getTimelineInternal(AbstractFeed feed, 
			String hashtag, ExternalFeedContent.TimelineType timelineType, IPageRequest<String> paging) {
					
		List<ExternalFeedContent> contents = null;
		final net.mixednutz.api.core.model.PageRequest<Instant> pageRequest = net.mixednutz.api.core.model.PageRequest
				.convert(paging, Instant.class, (str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		if (paging.getStart()==null) {
			contents = externalFeedContentRepository.findTimeline(feed.getFeedId(), 
					timelineType, PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = pageRequest.getStart().atZone(ZoneId.systemDefault());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = externalFeedContentRepository.findTimelineMore(feed.getFeedId(), 
						timelineType, start, PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = externalFeedContentRepository.findTimelineSince(feed.getFeedId(), 
						timelineType, start, PageRequest.of(0, paging.getPageSize()));
			}
		}
		List<ExternalFeedTimelineElement> elements = new ArrayList<>();
		for (ExternalFeedContent content: contents) {
			elements.add(content.getElement());
		}
		
		return new PageBuilder<ExternalFeedTimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTokenCallback((item) -> {
					return item.getProviderPostedOnDate().toInstant();
				})
			.build();
	}
	
	protected IPage<? extends ITimelineElement,Instant> getTimelineInternal(Iterable<AbstractFeed> feeds, 
			String hashtag, ExternalFeedContent.TimelineType timelineType, IPageRequest<String> paging) {
			
		final List<Long> feedIdList = new ArrayList<Long>();
		for (AbstractFeed feed: feeds) {
			if (!VisibilityType.PRIVATE.equals(feed.getVisibility())) {
				feedIdList.add(feed.getFeedId());
			}
		}
		final Long[] feedIdArray = new Long[feedIdList.size()];
		feedIdList.toArray(feedIdArray);
				
		List<ExternalFeedContent> contents = null;
		final net.mixednutz.api.core.model.PageRequest<Instant> pageRequest = net.mixednutz.api.core.model.PageRequest
				.convert(paging, Instant.class, (str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		if (paging.getStart()==null) {
			contents = externalFeedContentRepository.findTimeline(feedIdArray, 
					timelineType, PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = pageRequest.getStart().atZone(ZoneId.systemDefault());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = externalFeedContentRepository.findTimelineMore(feedIdArray, 
						timelineType, start, PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = externalFeedContentRepository.findTimelineSince(feedIdArray, 
						timelineType, start, PageRequest.of(0, paging.getPageSize()));
			}
		}
		List<ExternalFeedTimelineElement> elements = new ArrayList<>();
		for (ExternalFeedContent content: contents) {
			elements.add(content.getElement());
		}
		
		return new PageBuilder<ExternalFeedTimelineElement, Instant>()
			.setItems(elements)
			.setPageRequest(pageRequest)
			.setTrimToPageSize(true)
			.setReSortComparator(new Comparator<ExternalFeedTimelineElement>(){
				@Override
				public int compare(ExternalFeedTimelineElement o1, ExternalFeedTimelineElement o2) {
					return -o1.getProviderPostedOnDate().compareTo(o2.getProviderPostedOnDate());
				}})
			.setTokenCallback((item)->item.getProviderPostedOnDate().toInstant())
			.build();
	}

	public static int getTimelineHash(AbstractFeed feed, String hashtag, IPageRequest<Object> paging,
			ExternalFeedContent.TimelineType timelineType) {
		int hash = feed.getFeedId().hashCode();
		if (hashtag!=null) {
			hash += hashtag.hashCode();
		}
		if (paging!=null) {
			hash += paging.hashCode();
		}
		return hash;
	}
	
	public static int getTimelineHash(Iterable<AbstractFeed> feeds, String hashtag, IPageRequest<Object> paging,
			ExternalFeedContent.TimelineType timelineType) {
		int hash = feeds.hashCode();
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
	
	@SuppressWarnings("unchecked" )
	private <Token> UserClient<Token> getUserClient(MixednutzClient api, Class<Token> tokenClass) {
		return (UserClient<Token>) api.getUserClient();
	}
	
	
	
	protected IPage<? extends ITimelineElement,Object> loadLiveTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging) {
		if (OAUTH1.equals(feed.getType())) {
			return getOauth1Timeline((Oauth1AuthenticatedFeed)feed, hashtag, paging,
					new GetLiveTimelineCallback());
		} else if (OAUTH2.equals(feed.getType())) {
			return getOauth2Timeline((Oauth2AuthenticatedFeed)feed, hashtag, paging,
					new GetLiveTimelineCallback());
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected IPage<? extends ITimelineElement,Object> loadLiveUserTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging) {
		if (OAUTH1.equals(feed.getType())) {
			return getOauth1Timeline((Oauth1AuthenticatedFeed)feed, hashtag, paging,
					new GetLiveTimelineCallback());
		} else if (OAUTH2.equals(feed.getType())) {
			return getOauth2Timeline((Oauth2AuthenticatedFeed)feed, hashtag, paging,
					new GetLiveTimelineCallback());
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected IPage<? extends ITimelineElement,Object> pollLiveTimeline(AbstractFeed feed) {
		if (OAUTH1.equals(feed.getType())) {
			return pollOauth1Timeline((Oauth1AuthenticatedFeed)feed,
					new PollLiveTimelineCallback());
		} else if (OAUTH2.equals(feed.getType())) {
			return pollOauth2Timeline((Oauth2AuthenticatedFeed)feed,
					new PollLiveTimelineCallback());
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected IPage<? extends ITimelineElement,Object> pollLiveUserTimeline(AbstractFeed feed) {
		if (OAUTH1.equals(feed.getType())) {
			return pollOauth1Timeline((Oauth1AuthenticatedFeed)feed,
					new PollLiveUserTimelineCallback());
		} else if (OAUTH2.equals(feed.getType())) {
			return pollOauth2Timeline((Oauth2AuthenticatedFeed)feed,
					new PollLiveUserTimelineCallback());
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected void saveTimeline(AbstractFeed feed,
			IPage<? extends ITimelineElement,Object> timeline,
			ExternalFeedContent.TimelineType timelineType) {
		for (ITimelineElement timelineElement: timeline.getItems()) {
			ExternalFeedContent content = new ExternalFeedContent(feed,
					externalFeedTimelineElementRepository.save(
							new ExternalFeedTimelineElement(timelineElement)), 
					timelineType);
			externalFeedContentRepository.save(content);
		}
	}
	
	public IPage<? extends ITimelineElement,Object> pollTimeline(AbstractFeed feed) {
		//Get Live Content
		ZonedDateTime crawledTime = ZonedDateTime.now();
		
		IPage<? extends ITimelineElement,Object> timeline =
				pollLiveTimeline(feed);
		
		//Save Live Content
		feed.setLastCrawled(crawledTime);
		
		if (timeline!=null) {
			saveTimeline(feed, timeline, ExternalFeedContent.TimelineType.HOME);
			
			if (!timeline.getItems().isEmpty()) {
				feed.setLastCrawledHomeTimelineKey(timeline.getNextPage().getStart().toString());
			}
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
		feed.setLastCrawled(crawledTime);
		
		if (timeline!=null) {
			saveTimeline(feed, timeline, ExternalFeedContent.TimelineType.HOME);
			
			if (!timeline.getItems().isEmpty()) {
				feed.setLastCrawledHomeTimelineKey(timeline.getNextPage().getStart().toString());
			}
		}
				
		return timeline;
	}
	
	@Override
	public IPage<? extends ITimelineElement, Object> pollUserTimeline(AbstractFeed feed) {
		//Get Live Content
		ZonedDateTime crawledTime = ZonedDateTime.now();
		
		IPage<? extends ITimelineElement,Object> timeline =
				pollLiveUserTimeline(feed);
		
		//Save Live Content
		feed.setLastCrawled(crawledTime);
		
		if (timeline!=null) {
			saveTimeline(feed, timeline, ExternalFeedContent.TimelineType.USER);
			
			if (!timeline.getItems().isEmpty()) {
				feed.setLastCrawledUserTimelineKey(timeline.getNextPage().getStart().toString());
			}
			
		}
		
		return timeline;
	}

	@Override
	public IPage<? extends ITimelineElement, Object> pollUserTimeline(AbstractFeed feed, IPageRequest<String> paging) {
		//Get Live Content
		ZonedDateTime crawledTime = ZonedDateTime.now();
		
		IPage<? extends ITimelineElement,Object> timeline =
				loadLiveUserTimeline(feed, null, paging);
		
		//Save Live Content
		feed.setLastCrawled(crawledTime);
		
		if (timeline!=null) {
			saveTimeline(feed, timeline, ExternalFeedContent.TimelineType.USER);
			
			if (!timeline.getItems().isEmpty()) {
				feed.setLastCrawledUserTimelineKey(timeline.getNextPage().getStart().toString());
			}
		}
		
		return timeline;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <P extends IPost> Optional<P> instantiatePost(AbstractFeed feed) {
		if (OAUTH1.equals(feed.getType())) {
			return instantiatePost((Oauth1AuthenticatedFeed)feed, (api) -> {
				if (api.getPostClient()!=null) {
					return Optional.of((P)api.getPostClient().create());
				}
				return Optional.empty();
			});
		} else if (OAUTH2.equals(feed.getType())) {
			return instantiatePost((Oauth2AuthenticatedFeed)feed, (api) -> {
				if (api.getPostClient()!=null) {
					return Optional.of((P)api.getPostClient().create());
				}
				return Optional.empty();
			});
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	public <P extends IPost> ITimelineElement post(AbstractFeed feed, P post) {
		return withPostClient(feed, postClient->postClient.postToTimeline(post));
	}
		
	@Override
	public Optional<ExternalFeedContent> crosspost(AbstractFeed feed, 
			String text, String url, String[] tags, ExternalFeedContent inReplyTo, 
			HttpServletRequest request) {
		if (request!=null) {
			ExternalFeedContent content = crosspost(feed, text, url, tags, inReplyTo,
					new ServletRequestParameterPropertyValues(request));
			if (content!=null) {
				return Optional.of(content);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<ExternalFeedContent> crosspost(AbstractFeed feed, 
			String text, String url, String[] tags, ExternalFeedContent inReplyTo, 
			Map<String,Object> additionalValues) {
		if (additionalValues!=null) {
			ExternalFeedContent content = crosspost(feed, text, url, tags, inReplyTo,
					new MutablePropertyValues(additionalValues));
			if (content!=null) {
				return Optional.of(content);
			}
		}
		return Optional.empty();
	}
	
	protected ExternalFeedContent crosspost(AbstractFeed feed, String text, 
			String url, String[] tags, ExternalFeedContent inReplyTo, PropertyValues additionalValues) {
		IPost ipost = instantiatePost(feed)
				.orElseThrow(() -> new IllegalArgumentException("Feed doesn't support posting"));
		
		//Copy Request into post
		if (additionalValues!=null) {
			final WebDataBinder binder = new WebDataBinder(ipost);
			binder.bind(additionalValues);
		}
		
		if (url!=null) {
			url = UriComponentsBuilder
				.fromHttpUrl(url)
				.queryParam("utm_source",feed.getProviderId())
				.queryParam("utm_medium","social")
				.queryParam("utm_campaign","crosspost")
				.build().toUriString();
		}
		
		ipost.setText(text);
		ipost.setUrl(url);
		ipost.setTags(tags);
		if (inReplyTo!=null) {
			ipost.setInReplyTo(inReplyTo.getElement().getReference());
		}
		
		ITimelineElement timelineElement = post(feed, ipost);
		
		if (timelineElement!=null) {
			ExternalFeedTimelineElement persisted = externalFeedTimelineElementRepository.save(
					new ExternalFeedTimelineElement(timelineElement));
			if (persisted!=null) {
				ExternalFeedContent content = new ExternalFeedContent(feed, persisted, 
						ExternalFeedContent.TimelineType.CROSSPOST);
				return externalFeedContentRepository.save(content);
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> referenceData(AbstractFeed feed) {
		return withPostClient(feed, client->client.referenceDataForPosting());
	}

	protected IPage<? extends ITimelineElement, Object> getOauth1Timeline(
			Oauth1AuthenticatedFeed feed, String hashtag, IPageRequest<String> prevPage,
			GetTimelineCallback callback) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);
		
		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		IUserSmall user = api.getUserClient().getUser();
		if (user.isPrivate()) {
			feed.setVisibility(VisibilityType.PRIVATE);
			return null;
			//TODO  If private maybe we should consider deleting everything we have saved
		}
		
		//TODO add in hashtag for search
		
		IPage<? extends ITimelineElement, Object> page;
		if (prevPage!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), prevPage.getStart(), prevPage.getPageSize()});
			page = callback.getTimelineStringToken(api, prevPage);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = callback.getTimeline(api);
		}
		
		return page;
	}
	
	protected IPage<? extends ITimelineElement, Object> getOauth2Timeline(
			Oauth2AuthenticatedFeed feed, String hashtag, IPageRequest<String> prevPage,
			GetTimelineCallback callback) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		if (provider==null) {
			LOG.warn("Provider for {} not found",creds.getProviderId());
			return null;
		}
		
		MixednutzClient api = provider.getApi(creds);
		IUserSmall user = api.getUserClient().getUser();
		if (user.isPrivate()) {
			feed.setVisibility(VisibilityType.PRIVATE);
			return null;
			//TODO  If private maybe we should consider deleting everything we have saved
		}
		//TODO add in hashtag for search

		
		IPage<? extends ITimelineElement, Object> page;
		if (prevPage!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), prevPage.getStart(), prevPage.getPageSize()});
			page = callback.getTimelineStringToken(api, prevPage);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = callback.getTimeline(api);
		}
		
		return page;
	}
	
	protected IPage<? extends ITimelineElement, Object> pollOauth1Timeline(
			Oauth1AuthenticatedFeed feed, PollTimelineCallback callback) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		if (provider==null) {
			LOG.warn("Provider for {} not found",creds.getProviderId());
			return null;
		}
		
		MixednutzClient api = provider.getApi(creds);
		if (api.getUserClient()==null) {
			return null;
		}
		IUserSmall user = api.getUserClient().getUser();
		if (user.isPrivate()) {
			feed.setVisibility(VisibilityType.PRIVATE);
			return null;
			//TODO  If private maybe we should consider deleting everything we have saved
		}
		
		IPageRequest<String> pagination = 
				callback.getTimelinePollRequest(api, callback.getLastCrawledKey(feed));
		
		IPage<? extends ITimelineElement, Object> page;
		if (pagination!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), pagination.getStart(), pagination.getPageSize()});
			page = callback.getTimelineStringToken(api, pagination);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = callback.getTimeline(api);
		}
		
		return page;
	}
	
	protected IPage<? extends ITimelineElement, Object> pollOauth2Timeline(
			Oauth2AuthenticatedFeed feed, PollTimelineCallback callback) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		IUserSmall user = api.getUserClient().getUser();
		if (user.isPrivate()) {
			feed.setVisibility(VisibilityType.PRIVATE);
			return null;
			//TODO  If private maybe we should consider deleting everything we have saved
		}
		
		IPageRequest<String> pagination = 
				callback.getTimelinePollRequest(api, callback.getLastCrawledKey(feed));

		IPage<? extends ITimelineElement, Object> page;
		if (pagination!=null) {
			LOG.debug("Querying {}. Start:{} PageSize:{}", new Object[]{
					feed.getProviderId(), pagination.getStart(), pagination.getPageSize()});
			page = callback.getTimelineStringToken(api, pagination);
		} else {
			LOG.debug("Querying {}. No bounds", feed.getProviderId());
			page = callback.getTimeline(api);
		}
		
		return page;
	}
	
	protected <P extends IPost> Optional<P> instantiatePost(Oauth1AuthenticatedFeed feed, 
			Function<MixednutzClient, Optional<P>> function) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		return function.apply(api);
	}
	
	protected <P extends IPost> Optional<P> instantiatePost(Oauth2AuthenticatedFeed feed, 
			Function<MixednutzClient, Optional<P>> function) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		return function.apply(api);
	}
	
//	protected <P extends IPost> void postOauth1(Oauth1AuthenticatedFeed feed, P post, PostCallback callback) {
//		Oauth1Credentials creds = feed.getCredentials();
//		
//		//Ensure connection is up to date.
//		creds = externalAccountCredentialsManager.refresh(creds);
//
//		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
//				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
//		
//		MixednutzClient api = provider.getApi(creds);
//		@SuppressWarnings("unchecked")
//		PostClient<P> postClient = (PostClient<P>) api.getPostClient();
//		if (postClient!=null) {
//			callback.post(api, postClient, post);
//		}
//	}
//	
//	protected <P extends IPost> void postOauth2(Oauth2AuthenticatedFeed feed, P post, PostCallback callback) {
//		Oauth2Credentials creds = feed.getCredentials();
//		
//		//Ensure connection is up to date.
//		creds = externalAccountCredentialsManager.refresh(creds);
//
//		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
//				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
//		
//		MixednutzClient api = provider.getApi(creds);
//		@SuppressWarnings("unchecked")
//		PostClient<P> postClient = (PostClient<P>) api.getPostClient();
//		if (postClient!=null) {
//			callback.post(api, postClient, post);
//		}
//	}
	
		
	protected <P extends IPost> void consumeMixednutzClient(AbstractFeed feed,
			Consumer<MixednutzClient> consumer) {
		if (OAUTH1.equals(feed.getType())) {
			consumeMixednutzClient((Oauth1AuthenticatedFeed)feed, consumer);
		} else if (OAUTH2.equals(feed.getType())) {
			consumeMixednutzClient((Oauth2AuthenticatedFeed)feed, consumer);
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected <P extends IPost> void consumeMixednutzClient(Oauth1AuthenticatedFeed feed,
			Consumer<MixednutzClient> consumer) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		consumer.accept(api);
	}
		
	protected <P extends IPost> void consumeMixednutzClient(Oauth2AuthenticatedFeed feed,
			Consumer<MixednutzClient> consumer) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		consumer.accept(api);
	}
	
	protected <P extends IPost> void consumePostClient(AbstractFeed feed,
			Consumer<PostClient<P>> consumer) {
		consumeMixednutzClient(feed, (api)->{
			@SuppressWarnings("unchecked")
			PostClient<P> postClient = (PostClient<P>) api.getPostClient();
			if (postClient!=null) {
				consumer.accept(postClient);
			}
		});
	}
	
	protected <T> T withMixednutzClient(AbstractFeed feed,
			Function<MixednutzClient, T> function) {
		if (OAUTH1.equals(feed.getType())) {
			return withMixednutzClient((Oauth1AuthenticatedFeed)feed, function);
		} else if (OAUTH2.equals(feed.getType())) {
			return withMixednutzClient((Oauth2AuthenticatedFeed)feed, function);
		} else {
			throw new RuntimeException("Invalid feed type: "+feed.getType());
		}
	}
	
	protected <T> T withMixednutzClient(Oauth1AuthenticatedFeed feed,
			Function<MixednutzClient, T> function) {
		Oauth1Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth1Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth1Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		return function.apply(api);
	}
	
	protected <T> T withMixednutzClient(Oauth2AuthenticatedFeed feed,
			Function<MixednutzClient, T> function) {
		Oauth2Credentials creds = feed.getCredentials();
		
		//Ensure connection is up to date.
		creds = externalAccountCredentialsManager.refresh(creds);

		ApiProvider<MixednutzClient,IOauth2Credentials> provider = 
				this.getProvider(creds, MixednutzClient.class, IOauth2Credentials.class);
		
		MixednutzClient api = provider.getApi(creds);
		return function.apply(api);
	}
	
	protected <P extends IPost, T> T withPostClient(AbstractFeed feed, Function<PostClient<P>, T> function) {
		return withMixednutzClient(feed, (api)->{
			@SuppressWarnings("unchecked")
			PostClient<P> postClient = (PostClient<P>) api.getPostClient();
			if (postClient!=null) {
				return function.apply(postClient);
			}
			return null;
		});
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
	
	interface GetTimelineCallback {
		IPage<? extends ITimelineElement, Object> getTimelineStringToken(
				MixednutzClient api, IPageRequest<String> prevPage);
		IPage<? extends ITimelineElement, Object> getTimeline(
				MixednutzClient api);
	}
	interface PollTimelineCallback extends GetTimelineCallback {
		IPageRequest<String> getTimelinePollRequest(
				MixednutzClient api, String start);
		String getLastCrawledKey(AbstractFeed feed);
	}	
	
	class GetLiveTimelineCallback implements GetTimelineCallback {

		@Override
		public IPage<? extends ITimelineElement, Object> getTimelineStringToken(MixednutzClient api,
				IPageRequest<String> prevPage) {
			return getTimelineClient(api, Object.class).getTimelineStringToken(prevPage);
		}

		@Override
		public IPage<? extends ITimelineElement, Object> getTimeline(MixednutzClient api) {
			return getTimelineClient(api, Object.class).getTimeline();
		}

	}
	
	class PollLiveTimelineCallback extends GetLiveTimelineCallback implements PollTimelineCallback {

		@Override
		public IPageRequest<String> getTimelinePollRequest(MixednutzClient api, String start) {
			return getTimelineClient(api, Object.class)
					.getTimelinePollRequest(start);
		}
		
		@Override
		public String getLastCrawledKey(AbstractFeed feed) {
			return feed.getLastCrawledHomeTimelineKey();
		}
		
	}
	
	class GetLiveUserTimelineCallback implements GetTimelineCallback {

		@Override
		public IPage<? extends ITimelineElement, Object> getTimelineStringToken(MixednutzClient api,
				IPageRequest<String> prevPage) {
			return getUserClient(api, Object.class).getUserTimelineStringToken(prevPage);
		}

		@Override
		public IPage<? extends ITimelineElement, Object> getTimeline(MixednutzClient api) {
			return getUserClient(api, Object.class).getUserTimeline();
		}
		
	}
	
	class PollLiveUserTimelineCallback extends GetLiveUserTimelineCallback implements PollTimelineCallback {

		@Override
		public IPageRequest<String> getTimelinePollRequest(MixednutzClient api, String start) {
			return getUserClient(api, Object.class)
					.getUserTimelinePollRequest(start);
		}
		
		@Override
		public String getLastCrawledKey(AbstractFeed feed) {
			return feed.getLastCrawledUserTimelineKey();
		}
	}
	
}
