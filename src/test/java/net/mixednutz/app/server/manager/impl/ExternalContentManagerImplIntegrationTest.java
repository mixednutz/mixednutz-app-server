package net.mixednutz.app.server.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.mixednutz.app.server.entity.OembedFilterAllowlist;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;
import net.mixednutz.app.server.repository.OembedFilterAllowlistRepository;

public class ExternalContentManagerImplIntegrationTest {
	
	private OembedFilterAllowlistRepository oembedFilterWhitelistRepository;
	
	@BeforeEach
	public void setup() {
		oembedFilterWhitelistRepository = mock(OembedFilterAllowlistRepository.class);
		
		List<OembedFilterAllowlist> whitelist = List.of(
				new OembedFilterAllowlist(
						"twitter","Twitter",
						"^https?:\\/\\/?(www\\.)?twitter.com\\/(?!i)(?<username>.*)\\/status\\/(?<id>[0-9]*)",
						"^(http|https):\\/\\/?(publish\\.)?twitter.com\\/oembed.*",
						"https://publish.twitter.com/oembed?url={url}"),
				new OembedFilterAllowlist(
						"instagram","Instagram",
						"^(http|https):\\/\\/?(www\\.)?instagram.com\\/p.*",
						"^(http|https):\\/\\/?(api\\.)?instagram.com\\/oembed.*",
						"https://api.instagram.com/oembed?url={url}"),
				new OembedFilterAllowlist(
						"flickr","Flickr",
						"^(http|https):\\/\\/?(www\\.)?flickr.com\\/photos\\/.*",
						"^(http|https):\\/\\/?www.flickr.com\\/services\\/oembed.*",
						"https://www.flickr.com/services/oembed?url={url}&format=json&maxwidth=620"),
				new OembedFilterAllowlist(
						"youtube","YouTube",
						"^(http|https):\\/\\/?(www\\.)?youtube.com\\/watch.*",
						"^(http|https):\\/\\/?www.youtube.com\\/oembed.*",
						"http://www.youtube.com/oembed?url={url}&format=json"),
				new OembedFilterAllowlist(
						"imgur","Imgur",
						"^(http|https):\\/\\/?(www\\.)?imgur.com\\/a.*",
						"^(http|https):\\/\\/?api.imgur.com\\/oembed\\.json.*",
						"http://api.imgur.com/oembed.json?url={url}"),
				new OembedFilterAllowlist(
						"imdb","Imdb",
						"^(http|https):\\/\\/?(www\\.)?imdb.com\\/title\\/.*",
						"^https:\\/\\/(www\\.)?mixednutz.net\\/oembed\\?url=(http|https):\\/\\/?(www\\.)?imdb.com\\/title\\/.*",
						"https://mixednutz.net/oembed?url={url}")
		);
		
		when(oembedFilterWhitelistRepository.findAll()).thenReturn(whitelist);
		
	}

	@Test
	public void testDerviceSourceType() {
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository);
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
		
		//assertEquals("mixednutz", manager.deriveSourceType("https://mixednutz.net/photo/id/719"));
	}
	
	/**
	 * Reads a URL and returns the metadata
	 */
	@Disabled
	@Test
	public void testMetadataLookup() {
//		new SslProperties(KEYSTORE_PATH, 
//				KEYSTORE_PASS, KEYSTORE_TYPE);
		
		ExternalContentManagerImpl manager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository);
		manager.loadWhitelist();
				
		// Imdb
		ExtractedMetadata url = manager.lookupMetadata(
				"http://www.imdb.com/title/tt0796366/"
				);
		printExternalUrl(url);
		assertNull(url.getOembedUrl()); //Null because IMDB doesn't have oembed.
				
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
