package net.mixednutz.app.server.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ExternalFeedContent.ExternalFeedContentPK;

@Repository
public interface ExternalFeedContentRepository
	extends CrudRepository<ExternalFeedContent, ExternalFeedContentPK> {

	@Query(value="select c from ExternalFeedContent c where c.id.feedId=:feedId order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimeline(
			@Param("feedId")Long feedId, Pageable pageable);
	
	@Query(value="select c from ExternalFeedContent c where c.id.feedId=:feedId and c.element.providerPostedOnDate < :date order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimelineMore(
			@Param("feedId")Long feedId, @Param("date")Instant date, Pageable pageable);
	
	@Query(value="select c from ExternalFeedContent c where c.id.feedId=:feedId and c.element.providerPostedOnDate > :date order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimelineSince(
			@Param("feedId")Long feedId, @Param("date")Instant date, Pageable pageable);
	
}
