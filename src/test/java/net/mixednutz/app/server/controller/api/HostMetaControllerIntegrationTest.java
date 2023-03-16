package net.mixednutz.app.server.controller.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

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

@ExtendWith(SpringExtension.class)
@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"jpa-dev","db-local-hsqldb","ssl","aws-local"})
@TestPropertySource(properties= {
		"spring.mail.host=smtp.mail.yahoo.com",
		
})
@Disabled
public class HostMetaControllerIntegrationTest {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	private MockMvc mockMvc;
	
	
	@Transactional
	@Test
	public void hostMetaRequest() throws Exception {
		mockMvc.perform(get("/.well-known/host-meta")
				.secure(true))
			.andExpect(status().isOk())
			.andExpect(content()
					.contentType("application/xrd+xml;charset=UTF-8"))
			.andExpect(xpath("/XRD/Link[@rel='lrdd']/@template")
					.string("http://andrewfesta.com/.well-known/webfinger?resource={uri}"))
			.andDo(print());
	}
	
	
}
