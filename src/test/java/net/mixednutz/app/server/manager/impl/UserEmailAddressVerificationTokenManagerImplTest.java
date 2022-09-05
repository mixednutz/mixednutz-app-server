package net.mixednutz.app.server.manager.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.mixednutz.app.server.ThymeleafConfig;
import net.mixednutz.app.server.ThymeleafEmailConfig;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.repository.UserEmailAddressVerificationTokenRepository;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={
		ThymeleafConfig.class,
		ThymeleafEmailConfig.class, 
		EmailMessageManagerImpl.class, 
		UserEmailAddressVerificationTokenManagerImpl.class,
		DefaultKeyGenerator.class})
@Disabled
public class UserEmailAddressVerificationTokenManagerImplTest {

	@Autowired
	UserEmailAddressVerificationTokenManagerImpl tokenManager;
	
	@MockBean
	JavaMailSender mailSender;
	
	@MockBean
	UserEmailAddressVerificationTokenRepository repoisitory;
	
	@Test
	public void test() {
		
		when(repoisitory.save(any(UserEmailAddressVerificationToken.class)))
			.thenAnswer((inv)->{
				return inv.getArgument(0);
			});
		
		UserEmailAddress emailAddress = new UserEmailAddress();
		
		UserEmailAddressVerificationToken token = tokenManager.createVerificationToken(emailAddress);
	
		tokenManager.send(token);
	}
	
}
