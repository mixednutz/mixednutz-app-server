package net.mixednutz.app.server.job;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.post.AbstractScheduledPost;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.repository.ScheduledPostRepository;

@Component
@Transactional
public class PublishPostJob {

	@Autowired
	private ScheduledPostRepository scheduledPostRepository;

	@Scheduled(cron="0 0/5 * * * ?")
	public void publish() {

		for (AbstractScheduledPost scheduledPost : scheduledPostRepository
				.findByPublishDateLessThanEqualAndPublishedFalse(ZonedDateTime.now())) {

			Post<?> post = scheduledPost.post();
			post.setDatePublished(ZonedDateTime.now());
			scheduledPost.setPublished(true);
			//TODO crosspost here
			
		}
	}

}
