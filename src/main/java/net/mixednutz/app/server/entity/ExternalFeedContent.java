package net.mixednutz.app.server.entity;

import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;

/**
 * Associates a Feed to a Saved element.  This enforces the idea that
 * users can only see content that they subscribe to.
 * 
 * @author apfesta
 *
 */
public class ExternalFeedContent {

	private AbstractFeed feed;
	private ExternalFeedTimelineElement element;
	
}
