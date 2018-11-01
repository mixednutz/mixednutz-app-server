package net.mixednutz.app.server;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
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
	        			"/loggedin/**",
	        			"/event/**",
	        			"/privacy/**").authenticated()
//	        	.hasRole("MIXEDNUTZ")
	        	.anyRequest().permitAll();
//	        	.and()
//	        .requiresChannel().anyRequest().requiresSecure();
    }

}
