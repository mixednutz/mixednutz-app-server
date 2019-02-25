package net.mixednutz.app.server.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;

@Repository
public interface ExternalFeedRepository extends CrudRepository<AbstractFeed, Long> {

	public List<AbstractFeed> findByUser(User user);
	
	public List<AbstractFeed> findByUserAndVisibilityIn(User user, Collection<VisibilityType> visibilities);
	
}
