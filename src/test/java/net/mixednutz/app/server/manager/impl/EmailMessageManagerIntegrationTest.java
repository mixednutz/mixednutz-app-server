package net.mixednutz.app.server.manager.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.app.server.IntegrationTest;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.manager.UserEmailAddressVerificationTokenManager;
import net.mixednutz.app.server.repository.UserEmailAddressRepository;
import net.mixednutz.app.server.repository.UserRepository;

/**
 * Test real yahoo settings
 * 
 * @author apfesta
 *
 */
@ExtendWith(SpringExtension.class)
@Tag("IntegrationTest")
@SpringBootTest
@ActiveProfiles({"jpa-dev","db-local-hsqldb","ssl","aws-local"})
@Disabled
public class EmailMessageManagerIntegrationTest {
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserEmailAddressRepository userEmailAddressRepository;
		
	@Autowired
	private UserEmailAddressVerificationTokenManager UserEmailAddressVerificationTokenManager;
	
	@Transactional
	@Test
	public void test() {
		
		User user = new User();
		user.setUsername("test");
		userRepository.save(user);
		
		em.flush();
		
		UserEmailAddress emailAddress =
				userEmailAddressRepository.findByUserAndPrimaryTrue(user).orElseGet(()->{
					UserEmailAddress newEmailAddress = new UserEmailAddress();
					newEmailAddress.setEmailAddress("andy_festa@yahoo.com");
					newEmailAddress.setUser(user);
					return newEmailAddress;
				});
		emailAddress = userEmailAddressRepository.save(emailAddress);
		
		em.flush();
		
		UserEmailAddressVerificationToken token = 
				UserEmailAddressVerificationTokenManager.createVerificationToken(emailAddress);
				
		em.flush();
		
		UserEmailAddressVerificationTokenManager.send(token);
		
	}

}
