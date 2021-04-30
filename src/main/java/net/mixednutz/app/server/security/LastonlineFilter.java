package net.mixednutz.app.server.security;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.Lastonline;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.repository.LastonlineRepository;

@Component
public class LastonlineFilter {

	private static final Logger logger = LoggerFactory.getLogger(LastonlineFilter.class);
	
	@Autowired
	LastonlineRepository lastonlineRepository;
		
	//Roles to log the last online timestamp
	private String[] rolesToLog = new String[] {"ROLE_ADMIN","ROLE_USER"};
	
	public Lastonline doFilter() {
		
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        	if (logger.isDebugEnabled()) {
        		logger.debug("SecurityContextHolder is populated with '"+
        				auth+"'");
        	}
        	if (isAuthorized(auth) && auth.getPrincipal() instanceof User) {
        		return onAuthorized(auth, (User)auth.getPrincipal());
        	}
        }
        return null;
	}
	
	protected boolean isAuthorized(Authentication auth) {
		for (GrantedAuthority grantedAuthority: auth.getAuthorities()) {
    		for (String role: rolesToLog) {
    			if (grantedAuthority.getAuthority().equals(role)) {
    				return true;
    			}
    		}
    	}
		return false;
	}
	
	protected Lastonline onAuthorized(Authentication auth, User user) {
		Lastonline lastonline = lastonlineRepository.findById(user.getUserId())
				.orElse(new Lastonline(user));
		
		lastonline.setTimestamp(ZonedDateTime.now());
		lastonline = lastonlineRepository.save(lastonline);
		
		if (logger.isDebugEnabled()) {
    		logger.debug("User '{}' is active at {}", user.getUsername(),lastonline.getTimestamp());
    	}
		
		return lastonline;
	}

}
