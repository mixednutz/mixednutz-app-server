package net.mixednutz.app.server.controller.web;

import java.time.ZonedDateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.UserEmailAddressVerificationToken;
import net.mixednutz.app.server.manager.UserEmailAddressVerificationTokenManager;
import net.mixednutz.app.server.repository.UserEmailAddressRepository;
import net.mixednutz.app.server.security.OnRegistrationCompleteEvent;

@Controller
public class UserEmailAddressController {
	
	public static final String REGISTRATION_CONFIRMATION_URL = "/registrationConfirmation";

	@Autowired
	private UserEmailAddressRepository emailAddressRepository;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
		
	@Autowired
	private UserEmailAddressVerificationTokenManager userEmailAddressVerificationTokenManager;
	
	@PersistenceContext
    EntityManager entityManager;
	
	@Autowired
    protected AuthenticationManager authenticationManager;
	
	@Autowired
	protected UserDetailsService userDetailsService;
	
	private String emailForm(@AuthenticationPrincipal User user, Model model) {
		final UserEmailAddress emailAddress =
				emailAddressRepository.findByUserAndPrimaryTrue(user).orElse(new UserEmailAddress());
		
		EmailAddressForm form = new EmailAddressForm(emailAddress);
		model.addAttribute("form", form);
						
		return "profile/editEmailAddress";
	}
	
	@RequestMapping(value="/{username}/editEmailAddress", method = RequestMethod.GET)
	public String editEmailAddress(@PathVariable String username, @AuthenticationPrincipal User user, Model model) {				
		if (!username.equals(user.getUsername())) {
			throw new AccessDeniedException("That's not yours to edit!");
		}
		
		return emailForm(user, model);
	}
	
	@RequestMapping(value="/{username}/editEmailAddress", method = RequestMethod.POST)
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
				.orElseGet(()->{
					UserEmailAddress uea = new UserEmailAddress();
					uea.setUserId(currentUser.getUserId());
					return uea;
				});
		

		if (notEquals(form.getEmailAddress(), emailAddress.getEmailAddress())) {
			emailAddress.setEmailAddress(form.getEmailAddress());
			emailAddress.setVerified(false);
			//TODO create a new email address entity instead
		}
		
		emailAddress = emailAddressRepository.save(emailAddress);
			
		eventPublisher.publishEvent(new OnRegistrationCompleteEvent(emailAddress));
		
		return "redirect:/"+currentUser.getUsername();
	}
	
	@RequestMapping(value = REGISTRATION_CONFIRMATION_URL, method = RequestMethod.GET)
	public String confirmRegistration(HttpServletRequest request, Model model, 
			@RequestParam("token") String token) {
	  
	    UserEmailAddressVerificationToken verificationToken = userEmailAddressVerificationTokenManager.getVerificationToken(token);
	    if (verificationToken == null) {
	        return "redirect:/badToken";
	    }
	     
	    UserEmailAddress userEmailAddress = verificationToken.getEmailAddress();
	    
	    ZonedDateTime now = ZonedDateTime.now();
	    if (verificationToken.getExpiryDate().isBefore(now)) {
	    	return "redirect:/expiredToken";
	    }
	    
	    userEmailAddress.setVerified(true);
	    userEmailAddress.setPrimary(true);
	    emailAddressRepository.save(userEmailAddress);
	    userEmailAddressVerificationTokenManager.delete(verificationToken);
	    
	    //Get Others
	    for (UserEmailAddress inactiveEmailAddress : emailAddressRepository.findByUser(userEmailAddress.getUser())) {
	    	if (!inactiveEmailAddress.equals(userEmailAddress) && 
	    			inactiveEmailAddress.isPrimary()) {
	    		inactiveEmailAddress.setPrimary(false);
	    		emailAddressRepository.save(inactiveEmailAddress);	    
	    	}
	    }

	    login(userEmailAddress.getUser(), request);
	    return "redirect:/main";
	}
	
	protected void login(User user, HttpServletRequest request) {
	    // generate session if one doesn't exist
	    request.getSession();
	    
	    //clear this so that when login occurs a fresh User object is created
	    entityManager.clear();
	    
	    //reload user
	    user = (User) userDetailsService.loadUserByUsername(user.getUsername());
	    //create authentication
	    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
	            user, user.getPassword(), user.getAuthorities());
	    token.setDetails(new WebAuthenticationDetails(request));
//	    Authentication authenticatedUser = authenticationManager.authenticate(token);
	
	    //put authentication
	    SecurityContextHolder.getContext().setAuthentication(token);
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
