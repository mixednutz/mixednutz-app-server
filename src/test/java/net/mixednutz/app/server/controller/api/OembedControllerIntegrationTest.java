package net.mixednutz.app.server.controller.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import net.mixednutz.app.server.NetworkInfoConfig;
import net.mixednutz.app.server.entity.OembedFilterAllowlist;
import net.mixednutz.app.server.entity.Oembeds;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.manager.EmojiManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.manager.UserService;
import net.mixednutz.app.server.manager.impl.ApiManagerImpl;
import net.mixednutz.app.server.manager.impl.ExternalContentManagerImpl;
import net.mixednutz.app.server.manager.impl.ThemoviedbApi;
import net.mixednutz.app.server.manager.post.journal.impl.JournalEntityConverter;
import net.mixednutz.app.server.repository.JournalRepository;
import net.mixednutz.app.server.repository.MenuItemRepository;
import net.mixednutz.app.server.repository.OembedFilterAllowlistRepository;
import net.mixednutz.app.server.repository.UserRepository;
import net.mixednutz.app.server.security.LastonlineFilter;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value=OembedController.class)
@Import({
	ApiManagerImpl.class, NetworkInfoConfig.class, JournalEntityConverter.class,
	Oembeds.class, ExternalContentManagerImpl.class, ThemoviedbApi.class})
@TestPropertySource(properties="themoviedb.apikey=test")
public class OembedControllerIntegrationTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	UserService userService;
	
	// Used by WebControllers
	@MockBean
	NotificationApiController notificationApiController;	
	@MockBean
	EmojiManager emojiManager;
	@MockBean
	MenuItemRepository menuItemRepository;	
	@MockBean
	LastonlineFilter lastonlineFilter;
	
	// Used by JournalEntityConverter
	@MockBean
	JournalRepository journalRepository;
	@MockBean
	UserRepository userRepository;
	
	//Used by ApiManagerImpl
	@MockBean
	TagManager tagManager;
	@MockBean
	ReactionManager reactionManager;
	
	//Used by OembedFilterWhitelistManagerImpl
	@Autowired
	ExternalContentManagerImpl oembedFilterWhitelistManager;
	@MockBean
	OembedFilterAllowlistRepository oembedFilterWhitelistRepository;
	
		
	@BeforeEach
	public void setup() {
				
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
						"https://mixednutz.net/oembed?url={url}",
						ThemoviedbApi.class)
		);
		
		when(oembedFilterWhitelistRepository.findAll()).thenReturn(whitelist);

		oembedFilterWhitelistManager.loadWhitelist();
	}
	

	@Test
	public void test_internal() throws Exception {
		
		User testUser = new User();
		testUser.setUsername("andy");
		when(userRepository.findByUsername(eq("andy")))
			.thenReturn(Optional.of(testUser));
		
		LocalDate testDate = LocalDate.of(2022, 10, 18);
		Journal testJournal = new Journal();
		testJournal.setSubject("Test Subject");
		testJournal.setSubjectKey("test");
		testJournal.setAuthor(testUser);
		when(journalRepository.findByOwnerAndPublishDateKeyAndSubjectKey(
				eq(testUser), eq(testDate), eq("test")))
			.thenReturn(Optional.of(testJournal));
		
		mockMvc.perform(get("/oembed")
					.param("url", "https://andrewfesta.com/andy/journal/2022/10/18/test")
					.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("type").value("rich"))
				.andExpect(jsonPath("title").value("Test Subject : AndrewFesta.com"))
				.andExpect(jsonPath("provider_name").value("AndrewFesta.com"))
				.andExpect(jsonPath("provider_url").value("https://andrewfesta.com"))
				.andDo(print());
	}
	
	@Disabled
	@Test
	public void test_external_imdb() throws Exception {
						
		mockMvc.perform(get("/oembed")
					.param("url", "http://www.imdb.com/title/tt0796366/")
					.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("type").value("rich"))
				.andExpect(jsonPath("title").value("Star Trek (2009) - TMDB"))
				.andExpect(jsonPath("provider_name").value("The Movie Database (TMDB)"))
				.andExpect(jsonPath("provider_url").value("https://andrewfesta.com"))
				.andExpect(jsonPath("thumbnail_url").value("https://www.themoviedb.org/t/p/w600_and_h900_bestv2/9vaRPXj44Q2meHgt3VVfQufiHOJ.jpg"))
				.andDo(print());
	}
	
}
