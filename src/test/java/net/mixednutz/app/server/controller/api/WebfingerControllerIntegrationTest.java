package net.mixednutz.app.server.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import net.mixednutz.app.server.entity.Role;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.repository.UserRepository;

@ExtendWith(SpringExtension.class)
@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"jpa-dev","db-local-hsqldb","ssl","aws-local"})
@TestPropertySource(properties= {
		"spring.mail.host=smtp.mail.yahoo.com",
		
})
@Disabled
public class WebfingerControllerIntegrationTest {
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SiteSettingsManager siteSettingsManager;
	
	
	@Transactional
	@Test
	public void webfinger() throws Exception {
		setupAdminUser();
		
		mockMvc.perform(get("/.well-known/webfinger?resource=acct:admin@andrewfesta.com")
				.with(csrf())
				.secure(true))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/jrd+json"))
			.andExpect(jsonPath("subject").value("acct:admin@andrewfesta.com"))
			.andExpect(jsonPath("links[0].href").value("https://andrewfesta.com/activitypub/admin"))
			.andDo(print());
		
		//bad request
		mockMvc.perform(get("/.well-known/webfinger?resource=admin@andrewfesta.com")
				.with(csrf())
				.secure(true))
			.andExpect(status().isBadRequest())
			.andDo(print());
		
		//user not found
		mockMvc.perform(get("/.well-known/webfinger?resource=acct:andy@andrewfesta.com")
				.with(csrf())
				.secure(true))
			.andExpect(status().isNotFound())
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
