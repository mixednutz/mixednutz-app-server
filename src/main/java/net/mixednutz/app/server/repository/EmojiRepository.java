package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.Emoji;

@Repository
public interface EmojiRepository extends CrudRepository<Emoji, String> {

	Iterable<Emoji> findAllByOrderBySortId();
	
}
