package net.mixednutz.app.server.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;

public interface ExternalFeedManager {
	
	public Map<String, INetworkInfoSmall> getProviders();
	
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUser(User user);
	
	public IPage<? extends ITimelineElement,Object> getTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<Object> paging);
	
	public Map<INetworkInfoSmall, Collection<String>> getCompatibleFeedsForCrossposting();
	
	public Collection<String> getCompatibleFeedsForCrossposting(INetworkInfoSmall networkInfo);
	
}
