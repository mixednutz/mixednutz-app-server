package net.mixednutz.app.server.controller.api;

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
public class NodeinfoControllerIntegrationTest {
	
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
	public void nodeinfoRequest() throws Exception {
		mockMvc.perform(get("/.well-known/nodeinfo")
				.secure(true))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			.andExpect(jsonPath("links[0].rel").value("http://nodeinfo.diaspora.software/ns/schema/2.1"))
			.andExpect(jsonPath("links[0].href").value("http://andrewfesta.com/nodeinfo/2.1"))
			.andDo(print());
	}
	
	@Transactional
	@Test
	public void nodeinfoSchema() throws Exception {
		setupAdminUser();
		
		mockMvc.perform(get("/nodeinfo/2.1")
				.secure(true))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			.andExpect(jsonPath("software.repository").value("https://github.com/mixednutz/mixednutz-app-server"))
			.andExpect(jsonPath("protocols[0]").value("activitypub"))
			.andExpect(jsonPath("services.inbound[0]").value("twitter"))
			.andExpect(jsonPath("services.outbound[0]").value("twitter"))
			.andExpect(jsonPath("usage.users.total").value(1L))
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
