package net.mixednutz.app.server.manager.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.mixednutz.app.server.ThymeleafEmailConfig;
import net.mixednutz.app.server.manager.ResetPasswordTokenManager;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= {ThymeleafEmailConfig.class, EmailMessageManagerImpl.class, ResetPasswordTokenManagerImpl.class})
@Disabled
public class ResetPasswordTokenManagerImplTest {
	
	@Autowired
	private ResetPasswordTokenManager manager;
	
	@MockBean
	JavaMailSender mailSender;
	
	@Test
	public void test() {
		
	}

}
