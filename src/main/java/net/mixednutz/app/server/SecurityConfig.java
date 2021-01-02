package net.mixednutz.app.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
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
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.manager.UserService;


@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	UserService userService;
	
	@Autowired(required=false)
	SslConfigurer sslConfigurer;
	
	@Bean
	public DaoAuthenticationProvider daoProvider() {
		DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
	    daoProvider.setUserDetailsService(userService);
	    daoProvider.setPasswordEncoder(passwordEncoder());
	    return daoProvider;
	}
	public RememberMeAuthenticationProvider rememberMeProvider() {
		return new RememberMeAuthenticationProvider("mixedNutzRox");
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(14);
	}
	
	@Autowired
	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
	
	    auth
	    	.authenticationProvider(daoProvider())
	    	.authenticationProvider(rememberMeProvider());
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
//	        	.rememberMeServices(chainedRememberMeServices())
	        	.and()
	        .httpBasic()
    			.and()
	        .logout()
	        	.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	        	.and()
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
	        			"/v1/lounge").permitAll()
	        	.antMatchers(
	        			"/main/**",
	        			"/v1/main/**",
	        			"/search/**",
	        			"/v1/search/**",
	        			"/settings/**",
	        			"/**/loggedin/**",
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

}
