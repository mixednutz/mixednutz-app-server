package net.mixednutz.app.server.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.manager.TokenGenerator;
import net.mixednutz.app.server.manager.UserEmailAddressVerificationTokenManager;
import net.mixednutz.app.server.repository.UserEmailAddressVerificationTokenRepository;


@Service
public class UserEmailAddressVerificationTokenManagerImpl implements UserEmailAddressVerificationTokenManager{

	@Autowired
	private UserEmailAddressVerificationTokenRepository verificationTokenRepository;
	
	@Autowired
	private TokenGenerator tokenGenerator;
	
	@Override
	public UserEmailAddressVerificationToken createVerificationToken(UserEmailAddress emailAddress) {
		UserEmailAddressVerificationToken token = new UserEmailAddressVerificationToken();
		token.setEmailAddress(emailAddress);
		return save(token);
	}
	
	protected UserEmailAddressVerificationToken save(UserEmailAddressVerificationToken token) {
		token.setToken(tokenGenerator.generate());
		return verificationTokenRepository.save(token);
	}

	@Override
	public UserEmailAddressVerificationToken getVerificationToken(String tokenString) {
		return verificationTokenRepository.findById(tokenString).get();
	}

	@Override
	public void delete(UserEmailAddressVerificationToken token) {
		verificationTokenRepository.delete(token);
	}

}
