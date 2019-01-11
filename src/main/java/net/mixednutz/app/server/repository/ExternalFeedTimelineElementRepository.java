package net.mixednutz.app.server.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.ExternalFeedTimelineElement;

@Repository
public interface ExternalFeedTimelineElementRepository
	extends PagingAndSortingRepository<ExternalFeedTimelineElement, String> {

}
