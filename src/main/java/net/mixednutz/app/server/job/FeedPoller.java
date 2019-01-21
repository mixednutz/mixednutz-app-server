package net.mixednutz.app.server.job;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;

@Component
public class FeedPoller {
	
	private static final Logger LOG = LoggerFactory.getLogger(FeedPoller.class);
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedManager externalFeedManager;
	
	@PersistenceContext
	private EntityManager em;
	
	private Map<AbstractFeed, IPageRequest<Object>> nextPages = new HashMap<>();

	@Scheduled(cron="0 0/5 0 ? * *")
	public void poll() {
		for (AbstractFeed feed: getActiveFeeds()) {
			try {
				if (!nextPages.containsKey(feed)) {
					LOG.info("Polling Feed:{}", feed.getFeedId());
					IPage<?,Object> page = externalFeedManager.pollTimeline(feed);
					LOG.info("Feed:{}, Found {} items", feed.getFeedId(), page.getItems().size());
					nextPages.put(feed, page.getPrevPage());
				} else {
					IPageRequest<Object> nextPage = nextPages.get(feed);
					IPageRequest<String> nextPageStr;
					if (nextPage.getStart()!=null) {
						nextPageStr = PageRequest.next(nextPage.getStart().toString(), nextPage.getPageSize(), 
								nextPage.getDirection());
					} else {
						nextPageStr = PageRequest.first(nextPage.getPageSize(), 
								nextPage.getDirection(), String.class);
					}
					LOG.info("Polling Feed:{} With Page:{}", feed.getFeedId(), nextPageStr);
					IPage<?,Object> page = externalFeedManager.pollTimeline(feed, nextPageStr);
					LOG.info("Feed:{}, Found {} items", feed.getFeedId(), page.getItems().size());
					if (!page.getItems().isEmpty()) {
						nextPages.put(feed, page.getPrevPage());	
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				LOG.debug("Clearing JPA session");
				em.flush();
				em.clear();
			}
		}
	}
	
	protected Iterable<AbstractFeed> getActiveFeeds() {
		return externalFeedRepository.findAll();
	}
	
}
