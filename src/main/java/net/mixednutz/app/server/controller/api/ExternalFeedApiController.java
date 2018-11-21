package net.mixednutz.app.server.controller.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.app.server.entity.ExternalFeeds;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ExternalFeedManager;


@Controller
@RequestMapping(value="/api")
public class ExternalFeedApiController {
	
	@Autowired
	private ExternalFeedManager feedManager;
	
	@RequestMapping(value="/feeds", method = RequestMethod.GET)
	public @ResponseBody ExternalFeedsList externalFeeds(@AuthenticationPrincipal User user) {
		final Map<INetworkInfoSmall, List<AbstractFeed>> externalFeeds = 
				feedManager.feedsForUser(user);
		
		ExternalFeedsList feeds = new ExternalFeedsList();
		for (Entry<INetworkInfoSmall, List<AbstractFeed>> entry: externalFeeds.entrySet()) {
			feeds.add(new ExternalFeed(entry.getKey(), entry.getValue(), feedManager.getCompatibleFeedsForCrossposting(entry.getKey())));
		}
		return feeds;
	}
		
	public class ExternalFeed {
		
		private INetworkInfoSmall feedInfo;
		private List<ExternalFeeds.AbstractFeed> accounts;
		private Collection<String> compatibleMimeTypes;
		
		public ExternalFeed(INetworkInfoSmall feedInfo, List<ExternalFeeds.AbstractFeed> accounts,
				Collection<String> compatibleMimeTypes) {
			super();
			this.accounts = accounts;
			this.feedInfo = feedInfo;
			this.compatibleMimeTypes = compatibleMimeTypes;
		}
		public List<ExternalFeeds.AbstractFeed> getAccounts() {
			return accounts;
		}
		public void setAccounts(List<ExternalFeeds.AbstractFeed> accounts) {
			this.accounts = accounts;
		}
		public INetworkInfoSmall getFeedInfo() {
			return feedInfo;
		}
		public void setFeedInfo(INetworkInfoSmall feedInfo) {
			this.feedInfo = feedInfo;
		}
		public Collection<String> getCompatibleMimeTypes() {
			return compatibleMimeTypes;
		}
		public void setCompatibleMimeTypes(Collection<String> compatibleMimeTypes) {
			this.compatibleMimeTypes = compatibleMimeTypes;
		}

	}
	
	public static class ExternalFeedsList extends ApiList<ExternalFeed> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2314764021162994219L;

		public ExternalFeedsList() {
			super("externalFeeds");
		}
		
	}

}
