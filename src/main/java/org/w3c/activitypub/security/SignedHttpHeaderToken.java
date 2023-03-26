package org.w3c.activitypub.security;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class SignedHttpHeaderToken extends AbstractAuthenticationToken {
	
	private static final long serialVersionUID = 3239057082217304054L;
	
	final String signature;
	final URI destination;
	final HttpMethod method;
	final HttpHeaders headers;

	public SignedHttpHeaderToken(URI destination,
			HttpMethod method, HttpHeaders headers) {
		super(null);
		this.destination = destination;
		this.method = method;
		this.headers = headers;
		this.signature = headers.getFirst("Signature");		
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return signature;
	}

	public URI getDestination() {
		return destination;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}


}
