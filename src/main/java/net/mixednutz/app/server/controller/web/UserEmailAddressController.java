package net.mixednutz.app.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.manager.VerificationTokenManager;
import net.mixednutz.app.server.repository.UserEmailAddressRepository;
import net.mixednutz.app.server.security.OnRegistrationCompleteEvent;

@Controller
public class UserEmailAddressController {

	@Autowired
	private UserEmailAddressRepository emailAddressRepository;
	
	@Autowired
	protected VerificationTokenManager verificationTokenManager;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
		
	private String emailForm(@AuthenticationPrincipal User user, Model model) {
		final UserEmailAddress emailAddress =
				emailAddressRepository.findByUser(user).orElse(new UserEmailAddress());
		
		EmailAddressForm form = new EmailAddressForm(emailAddress);
		model.addAttribute("form", form);
						
		return "profile/edit";
	}
	
	@RequestMapping(value="/{username}/editEmailAddress", method = RequestMethod.GET)
	public String editEmailAddress(@PathVariable String username, @AuthenticationPrincipal User user, Model model) {				
		if (!username.equals(user.getUsername())) {
			throw new AccessDeniedException("That's not yours to edit!");
		}
		
		return emailForm(user, model);
	}
	
	@RequestMapping(value="/{username}/edit", method = RequestMethod.POST)
	public String saveEmailAddress(EmailAddressForm form, Errors errors,
			@PathVariable String username,
			@AuthenticationPrincipal final User currentUser, Model model) {			
		
		if (!username.equals(currentUser.getUsername())) {
			throw new AccessDeniedException("That's not yours to edit!");
		}
		
		if (errors.hasErrors()) {
			return emailForm(currentUser, model);
		}
		
		// Load entities so we can change them:
		
		//FIX THIS SINCE EMAILADDRESS HAS ITS OWN ID NOW
		UserEmailAddress emailAddress = emailAddressRepository.findById(currentUser.getUserId())
				.orElse(new UserEmailAddress());
		

		if (notEquals(form.getEmailAddress(), emailAddress.getEmailAddress())) {
			emailAddress.setEmailAddress(form.getEmailAddress());
		}
		
		emailAddress = emailAddressRepository.save(emailAddress);
			
		eventPublisher.publishEvent(new OnRegistrationCompleteEvent(emailAddress));
		
		return "redirect:/"+currentUser.getUsername();
	}
	
	/**
	 * Null-safe notEquals method
	 * 
	 * @param value1
	 * @param value2
	 * @return
	 */
	private <O> boolean notEquals(O value1, O value2) {
		return (value1!=null && !value1.equals(value2)) ||
				(value2!=null && !value2.equals(value1));
	}
	
	public static class EmailAddressForm {
		String emailAddress;
		
		public EmailAddressForm() {
			super();
		}
		public EmailAddressForm(UserEmailAddress emailAddressEntity) {
			super();
			emailAddress = emailAddressEntity.getEmailAddress();
		}
		public String getEmailAddress() {
			return emailAddress;
		}
		public void setEmailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
		}
	}
	
}
