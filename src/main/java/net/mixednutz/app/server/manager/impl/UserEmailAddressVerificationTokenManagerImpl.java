package net.mixednutz.app.server.manager.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.controller.web.UserEmailAddressController;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.manager.EmailMessageManager;
import net.mixednutz.app.server.manager.EmailMessageManager.EmailMessage;
import net.mixednutz.app.server.manager.TokenGenerator;
import net.mixednutz.app.server.manager.UserEmailAddressVerificationTokenManager;
import net.mixednutz.app.server.repository.UserEmailAddressVerificationTokenRepository;


@Service
public class UserEmailAddressVerificationTokenManagerImpl implements UserEmailAddressVerificationTokenManager{

	@Autowired
	private UserEmailAddressVerificationTokenRepository verificationTokenRepository;
	
	@Autowired
	private EmailMessageManager emailManager;
	
	@Autowired
	private TokenGenerator tokenGenerator;
	
	@Value("${mixednutz.email.name}")
	private String siteEmailName;
	
	private static NetworkInfo networkInfo;
	

	@Autowired
	public void setNetworkInfo(NetworkInfo networkInfo) {
		UserEmailAddressVerificationTokenManagerImpl.networkInfo = networkInfo;
	}
	
	@Override
	public void send(UserEmailAddressVerificationToken token) {
		
		EmailMessage msg = new EmailMessage();
		msg.setTo(Collections.singleton(token.getEmailAddress()));
		msg.setSubject(siteEmailName+" Registration Confirmation");
		
		Map<String, Object> model = new HashMap<>();
		model.put("token", token);
		
		String url = UriComponentsBuilder
			.fromHttpUrl(networkInfo.getBaseUrl())
			.path(UserEmailAddressController.REGISTRATION_CONFIRMATION_URL)
			.queryParam("token", token.getToken())
			.build().toUriString();
		model.put("verificationUrl", url);
				
		emailManager.send("html/verification", msg, model);
	}

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
