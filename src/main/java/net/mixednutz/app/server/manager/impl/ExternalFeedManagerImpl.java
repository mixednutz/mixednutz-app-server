package net.mixednutz.app.server.manager.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.provider.ApiProvider;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;

@Service
@Transactional
public class ExternalFeedManagerImpl implements ExternalFeedManager {

	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ApiProviderRegistry registry;
	
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUser(User user) {
		final Map<Class<? extends INetworkInfoSmall>, List<AbstractFeed>> map = collate(externalFeedRepository.findByUser(user));
		final Map<INetworkInfoSmall, List<AbstractFeed>> newMap = new LinkedHashMap<>();
		for (Entry<Class<? extends INetworkInfoSmall>, List<AbstractFeed>> entry: map.entrySet()) {
			newMap.put(getInstance(entry.getKey()), entry.getValue());
		}
		return newMap;
	}
	

	@SuppressWarnings("unchecked")
	private <T> T getInstance(Class<? extends T> clazz) {
		try{
			Method method = clazz.getMethod("getInstance");
			return (T) method.invoke(null);
		} catch (Exception e) {
			throw new RuntimeException("Class "+clazz.toString()+" needs to implement getInstance()");
		}
	}
	
	private Map<Class<? extends INetworkInfoSmall>, List<AbstractFeed>> collate(Iterable<AbstractFeed> feeds) {
		final Map<Class<? extends INetworkInfoSmall>, List<AbstractFeed>> map = new LinkedHashMap<>();
		for (AbstractFeed feed: feeds) {
			if (!map.containsKey(feed.getFeedInfoClass())) {
				map.put(feed.getFeedInfoClass(), new ArrayList<AbstractFeed>());
			}
			map.get(feed.getType()).add(feed);
		}
		return map;
	}


	@Override
	public List<String> getCompatibleFeedsForCrossposting(INetworkInfoSmall networkInfo) {
		return Arrays.asList(networkInfo.compatibleMimeTypes());
	}


	@Override
	public Map<INetworkInfoSmall, Collection<String>> getCompatibleFeedsForCrossposting() {
		final Map<INetworkInfoSmall, Collection<String>> mimeTypes = new LinkedHashMap<>();
		for (ApiProvider<?,?> provider: registry.getProviders()) {
			INetworkInfoSmall key = provider.getNetworkInfo();
			mimeTypes.put(key, this.getCompatibleFeedsForCrossposting(key));
		}
		return mimeTypes;
	}
}
