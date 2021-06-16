package net.mixednutz.app.server.manager.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.controller.web.UserController;
import net.mixednutz.app.server.entity.ResetPasswordToken;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.EmailMessageManager;
import net.mixednutz.app.server.manager.EmailMessageManager.EmailMessage;
import net.mixednutz.app.server.manager.ResetPasswordTokenManager;
import net.mixednutz.app.server.manager.TokenGenerator;
import net.mixednutz.app.server.repository.ResetPasswordTokenRepository;
import net.mixednutz.app.server.repository.UserEmailAddressRepository;


@Service
public class ResetPasswordTokenManagerImpl implements ResetPasswordTokenManager{

	@Autowired
	private ResetPasswordTokenRepository verificationTokenRepository;
	
	@Autowired
	private EmailMessageManager emailManager;
	
	@Autowired
	private UserEmailAddressRepository emailAddressRepository;
	
	@Autowired
	private TokenGenerator tokenGenerator;
	
	@Value("${mixednutz.email.display-name}")
	private String siteEmailName;
	
	private static NetworkInfo networkInfo;
	

	@Autowired
	public void setNetworkInfo(NetworkInfo networkInfo) {
		ResetPasswordTokenManagerImpl.networkInfo = networkInfo;
	}
	
	@Override
	public void send(ResetPasswordToken token) {
		
		EmailMessage msg = new EmailMessage();
		//This might be null, so its likely this email wont get sent
		msg.setTo(token.getEmailAddress()!=null?Collections.singleton(token.getEmailAddress()):Collections.emptyList());
		msg.setSubject(siteEmailName+" Password Reset");
		
		Map<String, Object> model = new HashMap<>();
		model.put("token", token);
		
		String url = UriComponentsBuilder
			.fromHttpUrl(networkInfo.getBaseUrl())
			.path(UserController.RESET_PASSWORD_URL)
			.queryParam("token", token.getToken())
			.build().toUriString();
		model.put("resetPasswordUrl", url);
				
		emailManager.send("html/resetpassword", msg, model);
	}

	@Override
	public ResetPasswordToken createVerificationToken(User user) {
		ResetPasswordToken token = new ResetPasswordToken();
		token.setUser(user);
		token.setEmailAddress(
				emailAddressRepository.findByUserAndPrimaryTrue(user).orElse(null));
		return save(token);
	}
	
	protected ResetPasswordToken save(ResetPasswordToken token) {
		token.setToken(tokenGenerator.generate());
		return verificationTokenRepository.save(token);
	}

	@Override
	public ResetPasswordToken getVerificationToken(String tokenString) {
		return verificationTokenRepository.findById(tokenString).get();
	}

	@Override
	public void delete(ResetPasswordToken token) {
		verificationTokenRepository.delete(token);
	}
	
}
