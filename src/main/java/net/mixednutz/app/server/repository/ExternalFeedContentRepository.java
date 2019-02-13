package net.mixednutz.app.server.repository;

import java.time.ZonedDateTime;
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

	@Query(value="select c from ExternalFeedContent c where c.type=:timelineType and c.id.feedId=:feedId order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimeline(
			@Param("feedId")Long feedId, @Param("timelineType")ExternalFeedContent.TimelineType timelineType, Pageable pageable);
	
	@Query(value="select c from ExternalFeedContent c where c.type=:timelineType and c.id.feedId=:feedId and c.element.providerPostedOnDate < :date order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimelineMore(
			@Param("feedId")Long feedId, @Param("timelineType")ExternalFeedContent.TimelineType timelineType, @Param("date")ZonedDateTime date, Pageable pageable);
	
	@Query(value="select c from ExternalFeedContent c where c.type=:timelineType and c.id.feedId=:feedId and c.element.providerPostedOnDate > :date order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimelineSince(
			@Param("feedId")Long feedId, @Param("timelineType")ExternalFeedContent.TimelineType timelineType, @Param("date")ZonedDateTime date, Pageable pageable);
	
	
	@Query(value="select c from ExternalFeedContent c where c.type=:timelineType and c.id.feedId in :feedIds order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimeline(
			@Param("feedIds")Long[] feedIds, @Param("timelineType")ExternalFeedContent.TimelineType timelineType, Pageable pageable);
	
	@Query(value="select c from ExternalFeedContent c where c.type=:timelineType and c.id.feedId in :feedIds and c.element.providerPostedOnDate < :date order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimelineMore(
			@Param("feedIds")Long[] feedIds, @Param("timelineType")ExternalFeedContent.TimelineType timelineType, @Param("date")ZonedDateTime date, Pageable pageable);
	
	@Query(value="select c from ExternalFeedContent c where c.type=:timelineType and c.id.feedId in :feedIds and c.element.providerPostedOnDate > :date order by c.element.providerPostedOnDate desc")
	public List<ExternalFeedContent> findTimelineSince(
			@Param("feedIds")Long[] feedIds, @Param("timelineType")ExternalFeedContent.TimelineType timelineType, @Param("date")ZonedDateTime date, Pageable pageable);
	
}
