package net.mixednutz.app.server.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;

public interface ExternalFeedManager {
	
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUser(User user);
	
	public Map<INetworkInfoSmall, Collection<String>> getCompatibleFeedsForCrossposting();
	
	public Collection<String> getCompatibleFeedsForCrossposting(INetworkInfoSmall networkInfo);
	
}
