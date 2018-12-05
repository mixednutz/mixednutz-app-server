package net.mixednutz.app.server.manager.impl;

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
		final Map<String, List<AbstractFeed>> map = collate(externalFeedRepository.findByUser(user));
		final Map<INetworkInfoSmall, List<AbstractFeed>> newMap = new LinkedHashMap<>();
		for (Entry<String, List<AbstractFeed>> entry: map.entrySet()) {
			ApiProvider<?, ?> provider = registry.getSocialNetworkClient(entry.getKey());
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
	
	public Map<String, INetworkInfoSmall> getProviders() {
		final Map<String, INetworkInfoSmall> map = new LinkedHashMap<>();
		for (ApiProvider<?,?> provider: registry.getProviders()) {
			map.put(provider.getProviderId(), provider.getNetworkInfo());
		}
		return map;
	}
}
