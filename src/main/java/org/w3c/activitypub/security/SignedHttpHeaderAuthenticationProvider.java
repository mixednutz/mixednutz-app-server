package org.w3c.activitypub.security;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.w3c.activitystreams.model.ActorImpl;

import net.mixednutz.api.activitypub.client.ActivityPubClientManager;
import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.util.HttpSignaturesUtil;

public class SignedHttpHeaderAuthenticationProvider implements AuthenticationProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(SignedHttpHeaderAuthenticationProvider.class);

	private ActivityPubClientManager activityPubClientManager;
	
	private UserProfileRepository userProfileRepository;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (authentication.getPrincipal()==null) {
			throw new AccessDeniedException("Missing Signature header");
		}
		SignedHttpHeaderToken token = (SignedHttpHeaderToken)authentication;
		
		Context ctx = new Context();
		
		LOG.info("Verifying Signature for {}",authentication.getPrincipal());
		HttpSignaturesUtil.verifyRequest(
				token.getDestination(), token.getMethod(), token.getHeaders(),
				keyId->{
					String[] parts = keyId.split("#");
					URI actorURI = URI.create(parts[0]);
					
					ActorImpl actor = activityPubClientManager.getActor(actorURI);
					ctx.actor = actor;
					
					if (!actor.getPublicKey().getId().toString().equals(keyId)) {
						throw new RuntimeException("Cannot find key "+keyId);
					}
					return HttpSignaturesUtil.getPublicKeyFromPem(
							actor.getPublicKey().getPublicKeyPem());
				});
		
		LOG.info("Signature verified for {}",authentication.getPrincipal());
		Optional<UserProfile> userProfile = userProfileRepository.
				findOneByActivityPubActorUri(ctx.actor.getId().toString());
					
		if (userProfile.isPresent()) {
			LOG.info("User {} exists for {}",
					userProfile.get().getUser().getUsername(), authentication.getPrincipal());
			return this.createSuccessAuthentication(ctx.actor.getId().toString(), 
					authentication, userProfile.get().getUser());
		}
		return new AnonymousAuthenticationToken(ctx.actor.getId().toString(), ctx.actor, List.of(new Role(null, "USER_ROLE")));
		
	}
	
	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
			User user) {
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(user,
				authentication.getCredentials(), List.of(new Role(user, "USER_ROLE")));
		result.setDetails(authentication.getDetails());
		LOG.debug("Authenticated user");
		return result;
	}
	
	
	class Context {
		ActorImpl actor;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return SignedHttpHeaderToken.class.equals(authentication);
	}
	
	public ActivityPubClientManager getActivityPubClientManager() {
		return activityPubClientManager;
	}

	public void setActivityPubClientManager(ActivityPubClientManager activityPubClientManager) {
		this.activityPubClientManager = activityPubClientManager;
	}

	public UserProfileRepository getUserProfileRepository() {
		return userProfileRepository;
	}

	public void setUserProfileRepository(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

}
