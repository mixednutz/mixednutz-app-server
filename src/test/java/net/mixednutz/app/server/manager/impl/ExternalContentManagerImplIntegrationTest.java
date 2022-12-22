package net.mixednutz.app.server.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import net.mixednutz.app.server.entity.OembedFilterAllowlist;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedRich;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedOembedHtml;
import net.mixednutz.app.server.repository.OembedFilterAllowlistRepository;

public class ExternalContentManagerImplIntegrationTest {
	
	private RestTemplate defaultRestTemplate = new RestTemplate();
	private RestTemplate mockRestTemplate;
	private OembedFilterAllowlistRepository oembedFilterWhitelistRepository;
	
	@BeforeEach
	public void setup() {
		oembedFilterWhitelistRepository = mock(OembedFilterAllowlistRepository.class);
		
		List<OembedFilterAllowlist> whitelist = List.of(
				new OembedFilterAllowlist(
						"twitter","Twitter",
						"^https?:\\/\\/?(www\\.)?twitter.com\\/(?!i)(?<username>.*)\\/status\\/(?<id>[0-9]*)",
						"^(http|https):\\/\\/?(publish\\.)?twitter.com\\/oembed.*",
						"https://publish.twitter.com/oembed?url={url}",null),
				new OembedFilterAllowlist(
						"instagram","Instagram",
						"^(http|https):\\/\\/?(www\\.)?instagram.com\\/p.*",
						"^(http|https):\\/\\/?(api\\.)?instagram.com\\/oembed.*",
						"https://api.instagram.com/oembed?url={url}",null),
				new OembedFilterAllowlist(
						"flickr","Flickr",
						"^(http|https):\\/\\/?(www\\.)?flickr.com\\/photos\\/.*",
						"^(http|https):\\/\\/?www.flickr.com\\/services\\/oembed.*",
						"https://www.flickr.com/services/oembed?url={url}&format=json&maxwidth=620",null),
				new OembedFilterAllowlist(
						"youtube","YouTube",
						"^(http|https):\\/\\/?(www\\.)?youtube.com\\/watch.*",
						"^(http|https):\\/\\/?www.youtube.com\\/oembed.*",
						"http://www.youtube.com/oembed?url={url}&format=json",null),
				new OembedFilterAllowlist(
						"imgur","Imgur",
						"^(http|https):\\/\\/?(www\\.)?imgur.com\\/a.*",
						"^(http|https):\\/\\/?api.imgur.com\\/oembed\\.json.*",
						"http://api.imgur.com/oembed.json?url={url}",null),
				new OembedFilterAllowlist(
						"imdb","Imdb",
						"^(http|https):\\/\\/?(www\\.)?imdb.com\\/title\\/.*",
						"^https:\\/\\/(www\\.)?mixednutz.net\\/oembed\\?url=(http|https):\\/\\/?(www\\.)?imdb.com\\/title\\/.*",
						"https://mixednutz.net/oembed?url={url}",
						ThemoviedbApi.class),
				new OembedFilterAllowlist(
						"tmdb","TMDB",
						"^(http|https):\\/\\/?(www\\.)?themoviedb.org\\/movie\\/.*",
						"^https:\\/\\/(www\\.)?mixednutz.net\\/oembed\\?url=(http|https):\\/\\/?(www\\.)?themoviedb.org\\/movie\\/.*",
						"https://mixednutz.net/oembed?url={url}",
						ThemoviedbApi.class)
		);
		
		when(oembedFilterWhitelistRepository.findAll()).thenReturn(whitelist);
		
		mockRestTemplate = new RestTemplate();
	}

	@Test
	public void testDerviceSourceType() {
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository, Optional.empty(), List.of());
		manager.loadWhitelist();
		assertTrue(manager.deriveSourceType("https://stopdst.com").isEmpty());
		assertEquals("twitter", manager.deriveSourceType("https://twitter.com/klingershow/status/770399624695775236").get());
		assertEquals("twitter", manager.deriveSourceType("http://www.twitter.com/klingershow/status/770399624695775236").get());
		assertEquals("twitter", manager.deriveSourceType("https://www.twitter.com/klingershow/status/770399624695775236").get());
		assertEquals("twitter", manager.deriveSourceType("https://www.Twitter.com/klingershow/status/770399624695775236").get());
		assertTrue(manager.deriveSourceType("https://twitter.com/i/web/status/786363977890721792").isEmpty());
		assertEquals("twitter_oembed", manager.deriveSourceType("https://publish.twitter.com/oembed?url=https://twitter.com/klingershow/status/770399624695775236").get());
		assertEquals("instagram", manager.deriveSourceType("https://www.instagram.com/p/BOtT5WdhDZB/").get());
		assertEquals("instagram_oembed", manager.deriveSourceType("https://api.instagram.com/oembed?url=https://www.instagram.com/p/BOtT5WdhDZB/").get());
		assertEquals("flickr", manager.deriveSourceType("https://www.flickr.com/photos/andy_festa/6274209706/").get());
		assertEquals("flickr_oembed", manager.deriveSourceType("https://www.flickr.com/services/oembed?url=https://www.flickr.com/photos/andy_festa/6274209706/").get());
		assertEquals("youtube", manager.deriveSourceType("https://www.youtube.com/watch?v=DOd3d9q6wuY").get());
		assertEquals("youtube_oembed", manager.deriveSourceType("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=DOd3d9q6wuY").get());
		assertEquals("imgur", manager.deriveSourceType("https://imgur.com/a/NFO8l").get());
		assertEquals("imgur_oembed", manager.deriveSourceType("https://api.imgur.com/oembed.json?url=http://imgur.com/a/NFO8l").get());
		assertEquals("imdb", manager.deriveSourceType("http://www.imdb.com/title/tt0796366/").get());
		assertEquals("imdb_oembed", manager.deriveSourceType("https://mixednutz.net/oembed?url=http://www.imdb.com/title/tt0796366/").get());
		assertEquals("tmdb", manager.deriveSourceType("https://www.themoviedb.org/movie/13475-star-trek").get());
		
		//assertEquals("mixednutz", manager.deriveSourceType("https://mixednutz.net/photo/id/719"));
	}
	
	@Disabled
	@Test
	public void testTwitterOembedLookup() {
//		new SslProperties(KEYSTORE_PATH, 
//				KEYSTORE_PASS, KEYSTORE_TYPE);
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository, Optional.empty(), List.of());
		manager.loadWhitelist();
		
		ExternalContentManagerImpl.RestTemplateUrlLookup lookup = 
				manager.new RestTemplateUrlLookup();
		
		ExtractedMetadata url= lookup.lookupContent(
				"https://twitter.com/i/web/status/786363977890721792"
				);
		System.out.println("URL:         "+url.getUrl());
		System.out.println("ContentType: "+url.getContentType());
		System.out.println("Title:       "+url.getTitle());
		System.out.println("oEmbed:        "+url.getOembedUrl());
		System.out.println("oEmbed Title:  "+url.getOembedTitle());
		/*
		 * Twitter doesn't put oembed in that page anymore!!!
		 */
		assertNotNull(url.getOembedUrl());
		
		//Lookup actual OEMBED endpoint
		ExternalContentManagerImpl.AllowlistOembedLookup oembedLookup = 
				manager.new AllowlistOembedLookup();
		Oembed oembed = oembedLookup.lookupContent(url.getOembedUrl());
		if (oembed instanceof OembedRich) {
			System.out.println("HTML:        "+((OembedRich) oembed).getHtml());			
		}

	}
	
	
	/**
	 * Reads a URL and returns the metadata
	 */
	@Disabled
	@Test
	public void testMetadataLookup_imdb() {
//		new SslProperties(KEYSTORE_PATH, 
//				KEYSTORE_PASS, KEYSTORE_TYPE);
		
		ThemoviedbApi customLookup = new ThemoviedbApi("test");
				
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(
				oembedFilterWhitelistRepository, 
				Optional.of(defaultRestTemplate), 
				List.of(customLookup));
		manager.loadWhitelist();
				
		// Imdb
		ExtractedMetadata url = manager.lookupMetadata(
				"http://www.imdb.com/title/tt0796366"
				);
		printExternalUrl(url);
		assertNull(url.getOembedUrl()); //Null because IMDB doesn't have oembed.
		assertEquals("Star Trek (2009) - TMDB", url.getTitle());	
	}
	
	@Disabled
	@Test
	public void testLookupContent_External() {

		String response = "{\"type\":\"rich\",\"version\":\"1.0\",\"title\":\"Star Trek (2009) - IMDb\","
				+ "\"html\":\"<iframe height=\\\"270\\\" src=\\\"https://andrewfesta.com/embed/lookup/url/https://www.imdb.com/title/tt0796366/\\\" style=\\\"max-width: 658px; width: calc(100% - 2px);\\\" frameborder=\\\"0\\\"></iframe>\","
				+ "\"width\":0,\"height\":0,\"provider_name\":\"IMDb\",\"provider_url\":\"https://andrewfesta.com\",\"thumbnail_url\":\"https://m.media-amazon.com/images/M/MV5BMjE5NDQ5OTE4Ml5BMl5BanBnXkFtZTcwOTE3NDIzMw@@._V1_FMjpg_UX1000_.jpg\",\"thumbnail_width\":1000,\"thumbnail_height\":1482}";		
		
		MockRestServiceServer mockServer =
				  MockRestServiceServer.bindTo(mockRestTemplate).build();
		
		String url = "https://mixednutz.net/oembed?url=http://www.imdb.com/title/tt0796366/";
		mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
		  .andRespond(withSuccess(response, MediaType.parseMediaType("application/json;charset=UTF-8")));
		
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository, Optional.of(mockRestTemplate), List.of());
		manager.loadWhitelist();
				
		Optional<ExtractedOembedHtml> content = manager.lookupContent("imdb", "http://www.imdb.com/title/tt0796366/");
		assertTrue(content.isPresent());
		content.ifPresent(obj->{
			OembedRich rich = (OembedRich) obj.getOembed();
			System.out.println(rich.getHtml());
		});
						
	}
	
//	@Disabled
	@Test
	public void testLookupContent_Internal() {

		String response = "{\"type\":\"rich\",\"version\":\"1.0\",\"title\":\"Test Subject : AndrewFesta.com\",\"html\":\"<iframe height=\\\"270\\\" src=\\\"https://andrewfesta.com/embed/journal/id/null\\\" style=\\\"max-width: 658px; width: calc(100% - 2px);\\\" frameborder=\\\"0\\\"></iframe>\",\"width\":658,\"height\":270,\"provider_name\":\"AndrewFesta.com\",\"provider_url\":\"https://andrewfesta.com\",\"author_name\":\"andy\"}";		
		
		MockRestServiceServer mockServer =
				  MockRestServiceServer.bindTo(mockRestTemplate).build();
		
		String url = "https://mixednutz.net/oembed?url=https://andrewfesta.com/andy/journal/2022/10/18/test";
		mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
		  .andRespond(withSuccess(response, MediaType.parseMediaType("application/json;charset=UTF-8")));
		
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository, Optional.of(mockRestTemplate), List.of());
		manager.loadWhitelist();
				
		Optional<ExtractedOembedHtml> content = manager.lookupContent("imdb", "https://andrewfesta.com/andy/journal/2022/10/18/test");
		assertTrue(content.isPresent());
		content.ifPresent(obj->{
			OembedRich rich = (OembedRich) obj.getOembed();
			System.out.println(rich.getHtml());
		});
						
	}
	

	protected void printExternalUrl(ExtractedMetadata url) {
		System.out.println("URL:         "+url.getUrl());
		System.out.println("ContentType: "+url.getContentType());
		System.out.println("SiteName:    "+url.getSiteName());
		System.out.println("Title:       "+url.getTitle());
		System.out.println("oEmbed:        "+url.getOembedUrl());
		System.out.println("oEmbed Title:  "+url.getOembedTitle());
	}
	
}
