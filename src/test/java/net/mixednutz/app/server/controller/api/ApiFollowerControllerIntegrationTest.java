package net.mixednutz.app.server.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.EmojiManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.repository.MenuItemRepository;
import net.mixednutz.app.server.repository.UserRepository;
import net.mixednutz.app.server.security.LastonlineFilter;

@ExtendWith(SpringExtension.class)
@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"jpa-dev","db-local-hsqldb","ssl","aws-local"})
@TestPropertySource(properties= {
		"spring.mail.host=smtp.mail.yahoo.com",
		
})
/*
 * debug=true --spring.profiles.active=jpa-dev,db-local-hsqldb-file,twitter,discord,ssl,aws-local --PHOTO_DIRECTORY=/home/apfesta/workspace-mixednutz/photos/ --KEYSTORE_FILE=/home/apfesta/keystore.p12 --KEYSTORE_TYPE=PKCS12 --KEYSTORE_PASS=@lias1098 --TWITTER_CONSUMER_KEY=ErPS3Yil7Jisa44fEYOZFBTJz --TWITTER_CONSUMER_SECRET=oFpCkrOEkW0x3DkVbi4rCoUMQ8vuKYEXKJBA603iG9Tpaz7Szp --DISCORD_TOKEN=ODE2ODgwNDkzOTA2NjI0NTEz.YEBZPg.Cvv7KrUJxptS7kdJhItR8Gu1tEU --DISCORD_CHANNEL_ID=816882378085367818 --AWS_ACCESS_KEY_ID=AKIATME23SLKRPS3QEVF --AWS_SECRET_ACCESS_KEY=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 --SMTP_HOST=smtp.mail.yahoo.com --SMTP_USERNAME=tfes8@yahoo.com --SMTP_PASSWORD=wywxxthxvblapwyt --server.port=8443 --applicationUrl=https://127.0.0.1:8443 --externalTemplateFolder=/home/apfesta/git/mixednutz/tfemily.com/templates/
 */
public class ApiFollowerControllerIntegrationTest {
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SiteSettingsManager siteSettingsManager;
	
	// Used by WebControllers
	@MockBean
	NotificationApiController notificationApiController;	
	@MockBean
	EmojiManager emojiManager;
	@MockBean
	MenuItemRepository menuItemRepository;	
	@MockBean
	LastonlineFilter lastonlineFilter;
	
	//Used by ApiManagerImpl
	@MockBean
	TagManager tagManager;
	@MockBean
	ReactionManager reactionManager;
	
	@Transactional
	@Test
	public void followAdmin() throws Exception {
				
		setupAdminUser();
		
		User testFollower = new User();
		testFollower.getRoles().add(new Role(testFollower, "ROLE_USER"));
		testFollower.setUsername("test");
		userRepository.save(testFollower);
		
		em.flush();	
		
		mockMvc.perform(post("/api/admin/follow")
				.with(csrf())
				.secure(true)
				.with(user(testFollower)))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/plain;charset=UTF-8"))
			.andDo(print());
		
		em.flush();
		em.clear();
		
		mockMvc.perform(get("/api/following")
				.with(csrf())
				.secure(true)
				.with(user(testFollower)))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			//.andExpect(jsonPath("following$", hasSize(1)));
			.andExpect(jsonPath("following[0].username").value("admin"))
			.andDo(print());
		
		
	}
	
	@Transactional
	@Test
	public void followNonAdmin() throws Exception {
				
		setupAdminUser();
		
		User testUser = new User();
		testUser.getRoles().add(new Role(testUser, "ROLE_USER"));
		testUser.setUsername("test1");
		userRepository.save(testUser);
		
		em.flush();	
		
		User testFollower = new User();
		testFollower.getRoles().add(new Role(testFollower, "ROLE_USER"));
		testFollower.setUsername("test2");
		userRepository.save(testFollower);
		
		em.flush();	
		
		mockMvc.perform(post("/api/test1/follow")
				.with(csrf())
				.secure(true)
				.with(user(testFollower)))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/plain;charset=UTF-8"))
			.andDo(print());
		
		em.flush();
		em.clear();
		
		mockMvc.perform(get("/api/following")
				.with(csrf())
				.secure(true)
				.with(user(testFollower)))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			//.andExpect(jsonPath("following$", hasSize(1)));
			//.andExpect(jsonPath("following[0].username").value("admin"))
			.andDo(print());
		
		
	}
	
	private void setupAdminUser() {
		User user = new User();
		user.setUsername("admin");
		user.getRoles().add(new Role(user, "ROLE_ADMIN"));
		
		userRepository.save(user);
		
		SiteSettings siteSettings = siteSettingsManager.createSiteSettings(user);
		siteSettings.setNewUsersAutoFollowAdminUser(true);
		siteSettingsManager.save(siteSettings);
		
		em.flush();	
	}

}
