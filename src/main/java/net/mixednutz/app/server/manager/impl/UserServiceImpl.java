package net.mixednutz.app.server.manager.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.UserService;
import net.mixednutz.app.server.repository.UserRepository;

@Transactional
@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByUsername(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User "+username+"not found");
		}
		return user.get();
	}

	@Override
	public void encryptPassword(User user) {
		if (user.getPasswordRaw() != null && !user.getPasswordRaw().equals("")) {
			user.setPassword(passwordEncoder.encode(user.getPasswordRaw()));
		}
	}


}
