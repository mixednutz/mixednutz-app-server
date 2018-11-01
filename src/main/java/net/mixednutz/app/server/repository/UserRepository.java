package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
