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
import org.w3c.activitypub.client.ActivityPubClient;
import org.w3c.activitystreams.model.ActorImpl;

import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.util.HttpSignaturesUtil;

public class SignedHttpHeaderAuthenticationProvider implements AuthenticationProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(SignedHttpHeaderAuthenticationProvider.class);

	private ActivityPubClient activityPubClient;
	
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
					ActorImpl actor = activityPubClient.getActor(actorURI);
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
	
//	private Authentication getValidationToken(String signature) {
//		// call auth service to check validity of token
//		// keeping boolean flag for simplicity
//		boolean isValid = true;
//		if (isValid)
//			return new PreAuthenticatedAuthenticationToken("AuthenticatedUser", "ROLE_ADMIN");
//		else
//			throw new AccessDeniedException("Invalid authetication token");
//
//	}

	@Override
	public boolean supports(Class<?> authentication) {
		return SignedHttpHeaderToken.class.equals(authentication);
	}
	
//	/**
//	 * TODO move to HttpSignaturesUtil
//	 * 
//	 * @param destination
//	 * @param method
//	 * @param headers
//	 * @param publicKeyForKeyId
//	 */
//	public static void verifyRequest(
//			URI destination,
//			HttpMethod method, 
//			HttpHeaders headers, 
//			Function<String,ActorImpl> actorForKeyId) {
//		
//		String sigHeader = headers.getFirst("Signature");
//		if(sigHeader==null) {
//			throw new RuntimeException("Request is missing Signature header");
//		}
//		String[] parts = sigHeader.split(",");
//		if(parts.length<=1) {
//			throw new RuntimeException("Signature header has invalid format");
//		}
//		
//		Map<String, String> values = new HashMap<>();
//		for (String part: parts) {
//			String[] pair = part.split("=");
//			String value = pair[1].replace("\"", "");
//			values.put(pair[0], value);
//		}
//		
//		if(!values.containsKey("algorithm") ||
//				!"rsa-sha256".equalsIgnoreCase(values.get("algorithm"))) {
//			throw new RuntimeException("Signature header is missing algorithm");
//		}
//		if(!values.containsKey("keyId"))
//			throw new RuntimeException("Signature header is missing keyId field");
//		if(!values.containsKey("signature"))
//			throw new RuntimeException("Signature header is missing signature field");
//		if(!values.containsKey("headers"))
//			throw new RuntimeException("Signature header is missing headers field");
//		
//		//Get Public Key
//		final ActorImpl actor = actorForKeyId.apply(values.get("keyId"))
//		final PublicKey publicKey = publicKeyForKeyId.apply(values.get("keyId"));
//		
//		byte[] signature=Base64.getDecoder().decode(values.get("signature"));
//		List<String> sigHeaders = Arrays.asList(values.get("headers").split(" "));
//		
//		if(!sigHeaders.contains("(request-target)"))
//			throw new RuntimeException("(request-target) is not in signed headers");
//		if(!sigHeaders.contains("date"))
//			throw new RuntimeException("date is not in signed headers");
//		if(!sigHeaders.contains("host"))
//			throw new RuntimeException("host is not in signed headers");
//		
//		long unixtime=headers.getDate();
//		long now=Instant.now().toEpochMilli();
//		long diff=now-unixtime;
//		if(diff>30000L)
//			throw new RuntimeException("Date is too far in the future (difference: "+diff+"ms)");
//		if(diff<-30000L)
//			throw new RuntimeException("Date is too far in the past (difference: "+diff+"ms)");
//
//		//TODO get live actor
//		
//		List<String> sigParts=new ArrayList<>();
//		for(String header:sigHeaders){
//			String value;
//			if(header.equals("(request-target)")){
//				value=method.name().toLowerCase()+" "+destination.getPath();
//			}else{
//				value=headers.getFirst(header);
//			}
//			sigParts.add(header+": "+value);
//		}
//		String sigStr=String.join("\n", sigParts);
//		Signature sig;
//		try {
//			sig = Signature.getInstance("SHA256withRSA");
//			sig.initVerify(publicKey);
//			sig.update(sigStr.getBytes(StandardCharsets.UTF_8));
//		} catch (Exception e) {
//			throw new RuntimeException("Enable to create signature from PublicKey and header",e);
//		}
//		try {
//			if(!sig.verify(signature)){
//				LOG.info("Failed signature header: {}", sigHeader);
//				LOG.info("Failed signature string: \n{}", sigStr);
//				throw new RuntimeException("Signature failed to verify");
//			}
//		} catch (SignatureException e) {
//			throw new RuntimeException("something is not configured properly with the signature", e);
//		}
//	}

	public ActivityPubClient getActivityPubClient() {
		return activityPubClient;
	}

	public void setActivityPubClient(ActivityPubClient activityPubClient) {
		this.activityPubClient = activityPubClient;
	}

	public UserProfileRepository getUserProfileRepository() {
		return userProfileRepository;
	}

	public void setUserProfileRepository(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

}
