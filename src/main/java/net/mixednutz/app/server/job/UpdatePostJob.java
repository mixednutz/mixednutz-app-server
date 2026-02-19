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

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.CrosspostsAware;
import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.Visibility;
import net.mixednutz.app.server.entity.post.AbstractScheduledPostUpdate;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.ScheduledPostUpdateRepository;

@Component
@Transactional
public class UpdatePostJob {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePostJob.class);

	@Autowired
	private ScheduledPostUpdateRepository scheduledPostUpdateRepository;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedManager externalFeedManager;
	
	@Autowired
	private ApiManager apiManager;
	
	private static NetworkInfo networkInfo;
	
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	
	@Autowired
	public void setNetworkInfo(NetworkInfo networkInfo) {
		UpdatePostJob.networkInfo = networkInfo;
	}
	
	@Scheduled(cron="0 0/5 * * * ?")
	public void update() {
		
		for (AbstractScheduledPostUpdate scheduledUpdate : scheduledPostUpdateRepository
				.findByEffectiveDateLessThanEqualAndExecutedFalse(ZonedDateTime.now())) {
			
			Post<?> post = scheduledUpdate.post();
			LOGGER.info("Updating Post: {} URI:{}", post.getId(), post.getUri());
			
			doUpdate(post, scheduledUpdate);
			
			scheduledUpdate.setExecuted(true);
			
			this.doExport(post, scheduledUpdate);
		}
	}
	
	private void doUpdate(Post<?> post, AbstractScheduledPostUpdate scheduledUpdate) {
		if (scheduledUpdate.getVisibility()!=null) {
			post.setVisibility(new Visibility(scheduledUpdate.getVisibility()));
		}
		post.setDatePublished(ZonedDateTime.now());
	}
	
	private void doExport(Post<?> post, AbstractScheduledPostUpdate scheduledUpdate) {
		InternalTimelineElement exportableEntity = 
				apiManager.toTimelineElement(post, null, networkInfo.getBaseUrl());
		
//		activityPubClient.sendActivity(post.getAuthor(), activityPubManager.toCreateNote(
//				exportableEntity, post.getAuthor().getUsername()));
		
		String[] tags = null;
		if (exportableEntity.getTags()!=null) {
			tags = exportableEntity.getTags().stream()
					.map((t->t.getName())).toArray(size->new String[size]);	
		}
		if (scheduledUpdate.getExternalFeedId()!=null) {
			for (Long feedId: scheduledUpdate.getExternalFeedId()) {
				AbstractFeed feed= externalFeedRepository.findById(feedId).get();
				LOGGER.info("Crossposting {} to {}", 
						exportableEntity.getTitle(), 
						feed.getProviderId());
				
				// Get in-reply-to crosspost element
				ExternalFeedContent inReplyToCrosspost=null;
				if (scheduledUpdate.inReplyTo()!=null && scheduledUpdate.inReplyTo() instanceof CrosspostsAware) {
					Optional<ExternalFeedContent> first = 
							((CrosspostsAware) scheduledUpdate.inReplyTo())
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
								scheduledUpdate.getExternalFeedData())
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
