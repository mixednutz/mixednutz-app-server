package net.mixednutz.app.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ExternalFeedContent.ExternalFeedContentPK;

@Repository
public interface ExternalFeedContentRepository
	extends PagingAndSortingRepository<ExternalFeedContent, ExternalFeedContentPK> {

	public Page<ExternalFeedContent> findByIdFeedId(Long feedId, Pageable pageable);
	
}
