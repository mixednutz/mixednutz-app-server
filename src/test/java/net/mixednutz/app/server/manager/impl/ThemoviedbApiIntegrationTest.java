package net.mixednutz.app.server.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;

public class ThemoviedbApiIntegrationTest {

	private static final String API_KEY = "TEST";
	
	@Disabled
	@Test
	public void test() {
		ThemoviedbApi api = new ThemoviedbApi(API_KEY);
		
		ExtractedMetadata metadata = api.lookupContent("http://www.imdb.com/title/tt0796366/");
		assertEquals("Star Trek (2009) - TMDB",metadata.getTitle());
		assertEquals("https://www.themoviedb.org/movie/13475",metadata.getUrl());
		
		metadata = api.lookupContent("https://www.themoviedb.org/movie/13475-star-trek");
		assertEquals("Star Trek (2009) - TMDB",metadata.getTitle());
		assertEquals("https://www.themoviedb.org/movie/13475-star-trek",metadata.getUrl());
	}
	
}
