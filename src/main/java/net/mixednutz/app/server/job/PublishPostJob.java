package net.mixednutz.app.server.job;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.api.activitypub.client.ActivityPubClientManager;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.CrosspostsAware;
import net.mixednutz.app.server.entity.ExternalFeedContent;
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
	
	@Autowired
	protected ActivityPubManager activityPubManager;
	@Autowired
	protected ActivityPubClientManager activityPubClient;
	
	private static NetworkInfo networkInfo;
	
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	
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
			
			try {
				activityPubClient.sendActivity(post.getAuthor(), activityPubManager.toCreateNote(
						exportableEntity, post.getAuthor().getUsername()));
			} catch (Exception e) {
				LOGGER.error("Unable to send activity",e);
			}
			
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
					
					// Get in-reply-to crosspost element
					ExternalFeedContent inReplyToCrosspost=null;
					if (scheduledPost.inReplyTo()!=null && scheduledPost.inReplyTo() instanceof CrosspostsAware) {
						Optional<ExternalFeedContent> first = 
								((CrosspostsAware) scheduledPost.inReplyTo())
								.getCrossposts().stream()
									.filter(cp->cp.getFeed().equals(feed)).findFirst();
						if (first.isPresent()) {
							inReplyToCrosspost = first.get();
							LOGGER.info("Found {} crosspost to reply to: {}", 
									feed.getProviderId(),
									inReplyToCrosspost.getId());
						}
					}	
					final String[] finalTags = tags;
					final ExternalFeedContent finalInReplyToCrosspost=null;
					
					executor.submit(()->{
						try {
							//Sleep 2sec before crossposting
							Thread.sleep(2000);
							
							externalFeedManager.crosspost(feed, 
									exportableEntity.getTitle(), 
									exportableEntity.getUrl(), 
									finalTags, finalInReplyToCrosspost,
									scheduledPost.getExternalFeedData())
							.ifPresent(crosspost->{
								if (post instanceof CrosspostsAware) {
									CrosspostsAware crosspostAware = (CrosspostsAware) post;
									if (crosspostAware.getCrossposts()==null) {
										crosspostAware.setCrossposts(new HashSet<>());
									}
									crosspostAware.getCrossposts().add(crosspost);
								} else {
									LOGGER.warn("Unable to crosspost to {} {} because {} doesn't implement CrosspostsAware",
											feed.getType(),feed.getFeedId(),post.getClass());
								}
							});
						} catch (Throwable e) {
							// Log and swallow error
							LOGGER.error("Unable to crosspost to "+feed.getType()+" "+feed.getFeedId(), e);
						}
					});
					
					
				}
			}
			
		}
	}

}
