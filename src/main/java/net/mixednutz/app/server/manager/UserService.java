package net.mixednutz.app.server.manager;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import net.mixednutz.app.server.entity.User;

public interface UserService extends UserDetailsService {
	
	User loadUserByUsername(String username) throws UsernameNotFoundException;
	
	void encryptPassword(User user);

}
