package net.mixednutz.app.server.manager.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.app.server.ThymeleafEmailConfig;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.repository.UserEmailAddressVerificationTokenRepository;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={
		ThymeleafEmailConfig.class, 
		EmailMessageManagerImpl.class, 
		UserEmailAddressVerificationTokenManagerImpl.class,
		DefaultKeyGenerator.class})
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
