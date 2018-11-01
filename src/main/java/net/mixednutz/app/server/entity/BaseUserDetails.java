package net.mixednutz.app.server.entity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class BaseUserDetails implements UserDetails {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6973664070957516202L;
	
	private String password;
    private String username;
    /** Persisted roles */
    protected Set<GrantedAuthority> roles = new LinkedHashSet<GrantedAuthority>(); //Hibernate does not agree with arrays
    /** Manual temporary roles */
    protected Set<GrantedAuthority> additionalRoles = new LinkedHashSet<GrantedAuthority>();
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Set<GrantedAuthority> getRoles() {
		return roles;
	}
	public void setRoles(Set<GrantedAuthority> roles) {
		this.roles = roles;
	}
	public Set<GrantedAuthority> getAdditionalRoles() {
		return additionalRoles;
	}
	public void setAdditionalRoles(Set<GrantedAuthority> additionalRoles) {
		this.additionalRoles = additionalRoles;
	}
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new LinkedHashSet<GrantedAuthority>();
		authorities.addAll(roles);
		authorities.addAll(additionalRoles);
		return authorities;
	}
	
	
}
