package org.w3c.activitypub.security;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SignedHttpHeaderAuthenticationFilter extends AbstractAuthenticationProcessingFilter {


	public SignedHttpHeaderAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher,
			AuthenticationManager authenticationManager) {
		super(requiresAuthenticationRequestMatcher);
		//Set authentication manager
		setAuthenticationManager(authenticationManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
	
		//Extract necessary request properties for verifying signatures
		URI destination = URI.create(request.getRequestURL().toString());
		HttpMethod method = HttpMethod.resolve(request.getMethod());
		HttpHeaders headers = new HttpHeaders();
		for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext();) {
			String headerName = it.next();
			for (Iterator<String> values = request.getHeaders(headerName).asIterator(); values.hasNext();) {
				headers.add(headerName, values.next());
			}
		}
		
		// Create a token object to pass to Authentication Provider
		SignedHttpHeaderToken token = new SignedHttpHeaderToken(destination, method, headers);
		return getAuthenticationManager().authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		// Save user principle in security context
		SecurityContextHolder.getContext().setAuthentication(authResult);
		chain.doFilter(request, response);
	}

	

}
