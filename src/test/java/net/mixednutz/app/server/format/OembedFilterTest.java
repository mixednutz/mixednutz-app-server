package net.mixednutz.app.server.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.mixednutz.app.server.entity.OembedFilterAllowlist;
import net.mixednutz.app.server.format.AbstractUrlFilter.UrlEntity;
import net.mixednutz.app.server.format.OembedFilter.OembedEntity;
import net.mixednutz.app.server.manager.impl.ExternalContentManagerImpl;
import net.mixednutz.app.server.repository.OembedFilterAllowlistRepository;

public class OembedFilterTest {
	
	private OembedFilterAllowlistRepository oembedFilterWhitelistRepository;
	private ExternalContentManagerImpl oembedFilterWhitelistManager;
	
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
		
		oembedFilterWhitelistManager = new ExternalContentManagerImpl(oembedFilterWhitelistRepository, Optional.empty());
		oembedFilterWhitelistManager.loadWhitelist();
	}
	
	@Test
	public void findUrls() {
		final String html = "http://www.google.com<br>"
				+ "This is a test<br>"
				+ "http://www.yahoo.com<br/>"
				+ "Testing http://t.co/<br >"
				+ "https://www.mixednutz.net/photo/_id/1<br>"
				+ "Testing<br />"
				+ "http://youtube.com?watch=asdfafd<br>\n"
				+ "https://www.mixednutz.net/photo/_id/1<br>\n"
				+ "https://www.amazon.com/dp/B0BMXYLXW1<br>"
				+ "<a href=\"www.flickr.com\">Flicker</a><br>"
				+ "<a href='www.picasa.com'>Picasa</a>"
				+ "<div>https://www.imdb.com/title/tt0160862</div>";
				
		
		OembedFilter filter = new OembedFilter(null);
		List<UrlEntity> urlEntities = filter.findUrls(html);
		for (UrlEntity entity: urlEntities) {
			System.out.println(entity.text+" "+entity.start+" "+entity.end);
		}
		assertEquals("http://www.google.com", urlEntities.get(0).text);
		assertEquals("http://www.yahoo.com", urlEntities.get(1).text);
		assertEquals("https://www.mixednutz.net/photo/_id/1", urlEntities.get(2).text);
		assertEquals("http://youtube.com?watch=asdfafd", urlEntities.get(3).text);
		assertEquals("https://www.mixednutz.net/photo/_id/1", urlEntities.get(4).text);
		assertEquals("https://www.amazon.com/dp/B0BMXYLXW1", urlEntities.get(5).text);
		assertEquals("https://www.imdb.com/title/tt0160862", urlEntities.get(6).text);
	}
	
	@Test
	public void lookupOembeds() {
		List<UrlEntity> urlEntities = new ArrayList<UrlEntity>();
		urlEntities.add(new UrlEntity("https://stopdst.com",0,0));
		urlEntities.add(new UrlEntity("https://twitter.com/klingershow/status/770399624695775236",0,0));
		urlEntities.add(new UrlEntity("https://www.instagram.com/p/BOtT5WdhDZB/",0,0));
		urlEntities.add(new UrlEntity("https://www.flickr.com/photos/andy_festa/6274209706/",0,0));
		urlEntities.add(new UrlEntity("https://www.youtube.com/watch?v=DOd3d9q6wuY",0,0));
		urlEntities.add(new UrlEntity("https://imgur.com/a/NFO8l",0,0));
		urlEntities.add(new UrlEntity("http://www.imdb.com/title/tt0796366/",0,0));
		
		OembedFilter filter = new OembedFilter(oembedFilterWhitelistManager);
		
		List<UrlEntity> oembedEntities = filter.lookupOembeds(urlEntities);
		for (UrlEntity entity: oembedEntities) {
			if (entity instanceof OembedEntity) {
				OembedEntity oEntity = (OembedEntity) entity;
				System.out.println(entity.text+" "+entity.start+" "+entity.end+" "+oEntity.type);
			} else {
				System.out.println(entity.text+" "+entity.start+" "+entity.end);
			}
			
		}
	}
	
	@Test
	public void filter() {
		final OembedFilter filter = new OembedFilter(oembedFilterWhitelistManager);
		
		String html = "http://www.google.com<br>"
				+ "This is a test<br>"
				+ "https://www.instagram.com/p/BOtT5WdhDZB/<br/>"
				+ "Testing http://t.co/<br >"
				+ "https://www.flickr.com/photos/andy_festa/6274209706/<br>"
				+ "Testing<br />"
				+ "https://www.youtube.com/watch?v=DOd3d9q6wuY<br>\n"
				+ "http://imgur.com/a/NFO8l<br>"
				+ "<a href=\"www.flickr.com\">Flicker</a><br>"
				+ "<a href='www.picasa.com'>Picasa</a><br>\n"
				+ "http://www.imdb.com/title/tt0796366/<br/>\n"
				+ "https://www.amazon.com/dp/B0BMXYLXW1<br/>";
		String newHtml = filter.filter(html);
		System.out.println("\nFILTER TAKE #1");
		System.out.println(newHtml);
		
		html = "This is a sample blog that demonstrates dynamic embedding capability. &nbsp;All you have to do is put a URL on a blank line."
				+ "<div><br></div>"
				+ "<div>Here's a YouTube Example: (https://www.youtube.com/watch?v=DOd3d9q6wuY)</div>"
				+ "<div>https://www.youtube.com/watch?v=DOd3d9q6wuY<br></div>"
				+ "<div><br></div>"
				+ "<div>Here's a Flickr Example: (https://www.flickr.com/photos/andy_festa/5273551501)</div>"
				+ "<div>https://www.flickr.com/photos/andy_festa/5273551501</div>"
				+ "<div><br></div>"
				+ "<div>Here's a Instagram Example: (https://www.instagram.com/p/BOa169LBGaA/)</div>"
				+ "<div>https://www.instagram.com/p/BOa169LBGaA/</div>"
				+ "<div><br></div>"
				+ "<div>Here's a Twitter Example: (https://twitter.com/andrewpfesta/status/760781827309121537)</div>"
				+ "<div>https://twitter.com/andrewpfesta/status/760781827309121537</div>"
				+ "<div><br></div>"
				+ "<div>Here's a IMDB Example: (http://www.imdb.com/title/tt0796366/)</div>"
				+ "<div>http://www.imdb.com/title/tt0796366/</div>"
				+ "<div>Here's an example of Just a URL that doesn't have an Oembed:</div>"
				+ "<div>https://www.amazon.com/dp/B0BMXYLXW1</div>";
		System.out.println("\nFILTER TAKE #2");
		System.out.println("=Original Text:");
		System.out.println(html);
		newHtml = filter.filter(html);
		System.out.println("=Filtered Text:");
		System.out.println(newHtml);	

	}

}
