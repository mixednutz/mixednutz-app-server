package net.mixednutz.app.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.manager.UserEmailAddressVerificationTokenManager;


@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

	@Autowired
	private UserEmailAddressVerificationTokenManager verificationTokenManager;
	
	@Override
	public void onApplicationEvent(OnRegistrationCompleteEvent event) {
		this.confirmRegistration(event);
	}

	private void confirmRegistration(OnRegistrationCompleteEvent event) {
		UserEmailAddress userEmailAddress = event.getUserEmailAddress();
       
        UserEmailAddressVerificationToken token = verificationTokenManager.createVerificationToken(userEmailAddress);
        //TODO Create Email abstraction
        //verificationTokenManager.send(token, verifyEmailFormatter);
    }
	
}
