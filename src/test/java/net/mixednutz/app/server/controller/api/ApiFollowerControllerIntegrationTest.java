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

import org.junit.jupiter.api.Disabled;
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
@Disabled
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
