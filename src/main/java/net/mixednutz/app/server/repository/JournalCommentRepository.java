package net.mixednutz.app.server.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.journal.JournalComment;

@Repository
public interface JournalCommentRepository extends CommentRepository<JournalComment> {

	@Query("select c from #{#entityName} c"
			+" left join c.journal p"
			+" left join p.visibility.selectFollowers vsf"
			+ " where c.authorId = :authorId"+
			  " and (c.authorId = :viewerId"
			  + " or p.visibility.visibilityType = 'WORLD'"
			  + " or (p.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (p.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and c.dateCreated > :dateCreated")
	List<JournalComment> queryAuthorPostsByDateCreatedGreaterThan(
			Long authorId, 
			Long viewerId, 
			ZonedDateTime dateCreated, Pageable pageRequest);
	
	@Query("select c from #{#entityName} c"
			+" left join c.journal p"
			+" left join p.visibility.selectFollowers vsf"
			+ " where c.authorId = :authorId"+
			  " and (c.authorId = :viewerId"
			  + " or p.visibility.visibilityType = 'WORLD'"
			  + " or (p.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (p.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and c.dateCreated <= :dateCreated")
	List<JournalComment> queryAuthorPostsByDateCreatedLessThanEquals(
			@Param("authorId")Long authorId, 
			@Param("viewerId")Long viewerId, 
			@Param("dateCreated")ZonedDateTime dateCreated, Pageable pageRequest);
	
}
