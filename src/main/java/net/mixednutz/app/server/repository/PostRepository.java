/**
 * 
 */
package net.mixednutz.app.server.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;

/**
 * @author Andy
 *
 */
@NoRepositoryBean
public interface PostRepository<P extends Post<C>, C extends PostComment> extends CrudRepository<P, Long> {

	List<P> findByOwnerAndDateCreatedGreaterThanOrderByDateCreatedDesc(User owner, ZonedDateTime dateCreated, Pageable pageRequest);
	
	default List<P> getMyPostsGreaterThan(User owner, ZonedDateTime dateCreated, Pageable pageRequest) {
		return findByOwnerAndDateCreatedGreaterThanOrderByDateCreatedDesc(owner, dateCreated, pageRequest);
	}
	
	List<P> findByOwnerAndDateCreatedLessThanEqualOrderByDateCreatedDesc(User owner, ZonedDateTime dateCreated, Pageable pageRequest);
	
	default List<P> getMyPostsLessThan(User owner, ZonedDateTime dateCreated, Pageable pageRequest) {
		return findByOwnerAndDateCreatedLessThanEqualOrderByDateCreatedDesc(owner, dateCreated, pageRequest);
	}
	

	@Query("select p from #{#entityName} p"
			+" left join p.visibility.selectFollowers vsf"
			+ " where p.ownerId = :ownerId"+
			  " and (p.ownerId = :viewerId"
			  + " or p.authorId = :viewerId"
			  + " or p.visibility.visibilityType = 'WORLD'"
			  + " or (p.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (p.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and p.dateCreated > :dateCreated")
	List<P> queryUsersPostsByDateCreatedGreaterThan(
			@Param("ownerId")Long ownerId, 
			@Param("viewerId")Long viewerId, 
			@Param("dateCreated")ZonedDateTime dateCreated, Pageable pageRequest);
	
	default List<P> getUsersPostsByDateCreatedGreaterThan(User owner, User viewer, ZonedDateTime dateCreated, Pageable pageRequest) {
		return queryUsersPostsByDateCreatedGreaterThan(owner.getUserId(), viewer!=null?viewer.getUserId():null, dateCreated, pageRequest);
	}
	

	@Query("select p from #{#entityName} p"
			+" left join p.visibility.selectFollowers vsf"
			+ " where p.ownerId = :ownerId"+
			  " and (p.ownerId = :viewerId"
			  + " or p.authorId = :viewerId"
			  + " or p.visibility.visibilityType = 'WORLD'"
			  + " or (p.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (p.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and p.dateCreated <= :dateCreated")
	List<P> queryUsersPostsByDateCreatedLessThanEquals(
			@Param("ownerId")Long ownerId, 
			@Param("viewerId")Long viewerId, 
			@Param("dateCreated")ZonedDateTime dateCreated, Pageable pageRequest);
	
	default List<P> getUsersPostsByDateCreatedLessThanEquals(User owner, User viewer, ZonedDateTime dateCreated, Pageable pageRequest) {
		return queryUsersPostsByDateCreatedLessThanEquals(owner.getUserId(), viewer!=null?viewer.getUserId():null, dateCreated, pageRequest);
	}
	
}
