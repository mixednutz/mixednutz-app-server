package net.mixednutz.app.server.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;

@Repository
public interface ExternalFeedRepository extends CrudRepository<AbstractFeed, Long> {

	public List<AbstractFeed> findByUser(User user);
	
}
