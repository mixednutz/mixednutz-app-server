package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.ExternalCredentials.ExternalAccountCredentials;

@Repository
public interface ExternalCredentialsRepository extends CrudRepository<ExternalAccountCredentials, Integer> {

}
