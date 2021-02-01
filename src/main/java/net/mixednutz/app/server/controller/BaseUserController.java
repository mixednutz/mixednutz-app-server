package net.mixednutz.app.server.controller;

import org.springframework.beans.factory.annotation.Autowired;

import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.UserService;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseUserController {
		
	protected static final String USER_ROLE = "ROLE_USER";
	
	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	protected UserService userService;
	
	protected User save(User user) {
		userService.encryptPassword(user);
		user.setEnabled(true);
		user.getRoles().add(new Role(user, USER_ROLE));
		return userRepository.save(user);
	}

}
