package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.OembedFilterAllowlist;

@Repository
public interface OembedFilterAllowlistRepository extends CrudRepository<OembedFilterAllowlist, String> {

}
