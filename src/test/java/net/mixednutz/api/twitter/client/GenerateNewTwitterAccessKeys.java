package net.mixednutz.api.twitter.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter4j.connect.TwitterConnectionFactory;
import org.springframework.util.LinkedMultiValueMap;

import twitter4j.Twitter;

public class GenerateNewTwitterAccessKeys {
	
	public static final String CONSUMER_KEY = getArugment("Twitter Consumer Key: ");
	public static final String CONSUMER_SECRET = getArugment("Twitter Consumer Secret: ");
				
	private static final Logger log = LoggerFactory.getLogger(GenerateNewTwitterAccessKeys.class);
	
	public void createNewKeys() {
		
		
		TwitterConnectionFactory connectionFactory= 
				new TwitterConnectionFactory(CONSUMER_KEY, CONSUMER_SECRET);
		
		OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
		
		OAuthToken requestToken = oauthOperations.fetchRequestToken("https://127.0.0.1:8443/connect/twitter", new LinkedMultiValueMap<>());
		System.out.println(requestToken);
		String authUrl = oauthOperations.buildAuthenticateUrl(requestToken.getValue(), OAuth1Parameters.NONE);		
		
		System.out.println("Log in as your twitter account and go to this URL:");
		System.out.println(authUrl);
				
		String verifier = getArugment("Paste 'verifier' param from post-authenticated URL: ");
		
		AuthorizedRequestToken art = new AuthorizedRequestToken(requestToken, verifier);
		OAuthToken newAccessCodes = oauthOperations.exchangeForAccessToken(art, null);
		System.out.println("value:   "+newAccessCodes.getValue());
		System.out.println("secret:  "+newAccessCodes.getSecret());
		
		Connection<Twitter> conn = connectionFactory.createConnection(newAccessCodes);
		
		log.info("Logged in as {}",conn.getDisplayName());
	}
	
	static String getArugment(String prompt) {
		System.out.print(prompt);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			return in.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Unable to request user input", e);
		} 
	}
	
	public static void main(String[] args) throws Exception {
		GenerateNewTwitterAccessKeys testClass = new GenerateNewTwitterAccessKeys();
		testClass.createNewKeys();
	}
	
	
	
}
