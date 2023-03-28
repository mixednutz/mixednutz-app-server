package net.mixednutz.app.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.w3c.activitypub.security.SignedHttpHeaderAuthenticationFilter;
import org.w3c.activitypub.security.SignedHttpHeaderAuthenticationProvider;

import net.mixednutz.api.activitypub.client.ActivityPubClientManager;
import net.mixednutz.app.server.controller.web.AuthController;
import net.mixednutz.app.server.controller.web.UserEmailAddressController;
import net.mixednutz.app.server.manager.UserService;
import net.mixednutz.app.server.repository.UserProfileRepository;


@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value("${rememberMe.key}")
	private String rememberMeKey;
	
	@Value("${rememberMe.tokenValiditySeconds}")
	private int rememberMeTokenValiditySeconds;
	
	@Autowired
	UserService userService;
	
	@Autowired(required=false)
	SslConfigurer sslConfigurer;
	
	@Autowired
	ActivityPubClientManager activityPubClientManager;
	
	@Autowired
	UserProfileRepository userProfileRepository;
	
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
	public DaoAuthenticationProvider daoProvider() {
		DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
	    daoProvider.setUserDetailsService(userService);
	    daoProvider.setPasswordEncoder(passwordEncoder());
	    return daoProvider;
	}
	
	public SignedHttpHeaderAuthenticationProvider signedHttpHeaderProvider() {
		SignedHttpHeaderAuthenticationProvider signedHttpHeaderProvider = new SignedHttpHeaderAuthenticationProvider();
		signedHttpHeaderProvider.setActivityPubClientManager(activityPubClientManager);
		signedHttpHeaderProvider.setUserProfileRepository(userProfileRepository);
		return signedHttpHeaderProvider;
	}
		
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(14);
	}
	
	@Autowired
	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
	    auth.authenticationProvider(daoProvider());
	    auth.authenticationProvider(signedHttpHeaderProvider());
	}
	
	@Bean
	protected TokenBasedRememberMeServices rememberMeServices() {
		//TODO - replace with PersistentTokenBasedRememberMeServices and add a table for the token
		TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
				"mixedNutzRox", userService);
		services.setParameter("setcookie"); //checkbox param
		services.setCookieName("mixednutzcookie"); //cookie name
		services.setUseSecureCookie(true);
		return services;
	}
	
	@Bean
	protected SignedHttpHeaderAuthenticationFilter signedHttpHeaderAuthenticationFilter() throws Exception {
		return new SignedHttpHeaderAuthenticationFilter(
				new OrRequestMatcher(new AntPathRequestMatcher("/activitypub/**")), 
				authenticationManager());
	}
	
	@Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
                .antMatchers("/css/**","/js/**","/images/**","/rss/**","/webjars/**");
    }
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
    	/*
    	 * This header writer says to apply XFrame Sameorigion for everything 
    	 * EXCEPT /embed/** urls.
    	 */
//    	DelegatingRequestMatcherHeaderWriter headerWriter = new DelegatingRequestMatcherHeaderWriter(
//			new NegatedRequestMatcher(new AntPathRequestMatcher("/embed/**")),
//			new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN));

    	http
    		.headers().frameOptions().disable()
//    		.headers()
//    			.addHeaderWriter(headerWriter)
    			.and()
    		.csrf()
    			.ignoringAntMatchers(new String[]{
    					"/api/**",
    					"/activitypub/**"})
    			.and()
    		.sessionManagement()
    			.enableSessionUrlRewriting(false)
    			.and()
        	.formLogin()
	        	.loginPage("/login")
	        	.loginProcessingUrl("/j_spring_security_check")
//	        	.successHandler(new SavedRequestExceptPhotosSuccessHandler())
	        	.permitAll()
	        	.and()
	        .rememberMe()
				.key(rememberMeKey).tokenValiditySeconds(rememberMeTokenValiditySeconds)
				.userDetailsService(userService)
//	        	.rememberMeServices(chainedRememberMeServices())
	        	.and()
	        .httpBasic()
    			.and()
	        .logout()
	        	.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	        	.deleteCookies("JSESSIONID")
	        	.and()
//	        .addFilterBefore(signedHttpHeaderAuthenticationFilter(), AnonymousAuthenticationFilter.class)
//	        .addFilterAfter(lastonlineFilter, AnonymousAuthenticationFilter.class)
        	.authorizeRequests()
	        	.antMatchers(
	        			"/admin_signup",
	        			"/signup",
	        			"/about",
	        			"/chromecast",
	        			"/oauth/signup**",
	        			"/social-network-info",
	        			"/network-info",
	        			"/mixednutz-info",
	        			"/lounge",
	        			"/v1/lounge",
	        			UserEmailAddressController.REGISTRATION_CONFIRMATION_URL
	        			).permitAll()
	        	.antMatchers(
	        			"/main/**",
	        			"/v1/main/**",
	        			"/search/**",
	        			"/v1/search/**",
	        			"/settings/**",
	        			"/**/loggedin/**",
	        			AuthController.AUTH_THEN_REDIRECT_PREFIX,
	        			"/**/edit",
	        			"/event/**",
	        			"/privacy/**").authenticated()
//	        	.hasRole("MIXEDNUTZ")
	        	.anyRequest().permitAll();
    	if (sslConfigurer!=null) {
    		http.apply(sslConfigurer);
    	}    	
    }
	
	@Component
	@Profile("ssl")
	public class SslConfigurer extends AbstractHttpConfigurer<SslConfigurer, HttpSecurity> {

		@Override
		public void init(HttpSecurity http) throws Exception {
			http.requiresChannel().anyRequest().requiresSecure();
		}

	}
	
	@Component
	public class ActivityPubConfigurer extends AbstractHttpConfigurer<ActivityPubConfigurer, HttpSecurity> {
		
		@Override
		public void init(HttpSecurity http) throws Exception {
			http.addFilterBefore(signedHttpHeaderAuthenticationFilter(), AnonymousAuthenticationFilter.class);
		}
		
	}

}
