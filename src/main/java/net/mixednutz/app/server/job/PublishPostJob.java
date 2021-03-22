package net.mixednutz.app.server.job;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.post.AbstractScheduledPost;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.ScheduledPostRepository;

@Component
@Transactional
public class PublishPostJob {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PublishPostJob.class);

	@Autowired
	private ScheduledPostRepository scheduledPostRepository;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedManager externalFeedManager;
	
	@Autowired
	private ApiManager apiManager;
	
	private static NetworkInfo networkInfo;
	
	@Autowired
	public void setNetworkInfo(NetworkInfo networkInfo) {
		PublishPostJob.networkInfo = networkInfo;
	}

	@Scheduled(cron="0 0/5 * * * ?")
	public void publish() {

		for (AbstractScheduledPost scheduledPost : scheduledPostRepository
				.findByPublishDateLessThanEqualAndPublishedFalse(ZonedDateTime.now())) {

			Post<?> post = scheduledPost.post();
			LOGGER.info("Publishing Post: {} URI:{}", post.getId(), post.getUri());
			post.setDatePublished(ZonedDateTime.now());
			scheduledPost.setPublished(true);
			
			InternalTimelineElement exportableEntity = 
					apiManager.toTimelineElement(post, null, networkInfo.getBaseUrl());
			String[] tags = null;
			if (exportableEntity.getTags()!=null) {
				tags = exportableEntity.getTags().stream()
						.map((t->t.getName())).toArray(size->new String[size]);	
			}
			if (scheduledPost.getExternalFeedId()!=null) {
				for (Long feedId: scheduledPost.getExternalFeedId()) {
					AbstractFeed feed= externalFeedRepository.findById(feedId).get();
					LOGGER.info("Crossposting {} to {}", 
							exportableEntity.getTitle(), 
							feed.getProviderId());
					try {
						externalFeedManager.crosspost(feed, 
								exportableEntity.getTitle(), 
								exportableEntity.getUrl(), 
								tags, 
								scheduledPost.getExternalFeedData());
					} catch (Exception e) {
						// Log and swallow error
						LOGGER.error("Unable to crosspost to "+feed.getType()+" "+feed.getFeedId(), e);
					}
					
				}
			}
			
		}
	}

}
