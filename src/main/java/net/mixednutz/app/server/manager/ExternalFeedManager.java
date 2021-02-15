package net.mixednutz.app.server.manager;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.api.model.IPost;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;

public interface ExternalFeedManager {
	
	public Map<String, INetworkInfoSmall> getProviders();
	
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUser(User user);
	
	public Map<INetworkInfoSmall, List<AbstractFeed>> feedsForUserVisibleToWorld(User user);
	
	/**
	 * Retrieves a user's feed timeline that has previously been polled.
	 * 
	 * @param feed
	 * @param hashtag
	 * @param paging
	 * @return
	 */
	public IPage<? extends ITimelineElement,Instant> getTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging);
	
	/**
	 * Retrieves a user's feed timeline that has previously been polled.
	 * 
	 * @param feed
	 * @param hashtag
	 * @param paging
	 * @return
	 */
	public IPage<? extends ITimelineElement,Instant> getUserTimeline(AbstractFeed feed, 
			String hashtag, IPageRequest<String> paging);
	
	/**
	 * Retrieves a user's feed timeline that has previously been polled.
	 * 
	 * @param feed
	 * @param hashtag
	 * @param paging
	 * @return
	 */
	public IPage<? extends ITimelineElement,Instant> getUserTimeline(Iterable<AbstractFeed> feeds, 
			String hashtag, IPageRequest<String> paging);
	
	/**
	 * Polls a user's feed timeline and persists the results.
	 * Poll Request go in reverse to a normal timeline request.  This request goes
	 * forward in time and returns newer items.
	 * 
	 * @param feed
	 * @return
	 */
	public IPage<? extends ITimelineElement,Object> pollTimeline(AbstractFeed feed);
	
	/**
	 * Polls a user's feed timeline and persists the results.  Pagination enabled.
	 * 
	 * @param feed
	 * @param paging
	 * @return
	 */
	public IPage<? extends ITimelineElement,Object> pollTimeline(AbstractFeed feed, 
			IPageRequest<String> paging);
	
	/**
	 * Polls a user's feed timeline and persists the results.
	 * Poll Request go in reverse to a normal timeline request.  This request goes
	 * forward in time and returns newer items.
	 * 
	 * @param feed
	 * @return
	 */
	public IPage<? extends ITimelineElement,Object> pollUserTimeline(AbstractFeed feed);
	
	/**
	 * Polls a user's feed timeline and persists the results.  Pagination enabled.
	 * 
	 * @param feed
	 * @param paging
	 * @return
	 */
	public IPage<? extends ITimelineElement,Object> pollUserTimeline(AbstractFeed feed, 
			IPageRequest<String> paging);
	
	/**
	 * Instantiates a IPost object to the client can fill in the ncessary fields
	 * to send to {@link #post(AbstractFeed, IPost)}
	 * 
	 * @param <P>
	 * @param feed
	 * @return
	 */
	public <P extends IPost> Optional<P> instantiatePost(AbstractFeed feed);
	
	/**
	 * Posts a new element to the external feed.  Get an instance of IPost from
	 * {@link #instantiatePost()}
	 * 
	 * @param feed
	 * @param post
	 */
	public <P extends IPost> void post(AbstractFeed feed, P post);
	
	public void crosspost(AbstractFeed feed, String text, String url, String[] tags);
		
	public Map<INetworkInfoSmall, Collection<String>> getCompatibleFeedsForCrossposting();
	
	public Collection<String> getCompatibleFeedsForCrossposting(INetworkInfoSmall networkInfo);
	
	
}
