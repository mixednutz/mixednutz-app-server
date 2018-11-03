package net.mixednutz.app.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import net.mixednutz.app.server.entity.User;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Category(IntegrationTest.class)
public class SampleControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {
		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().findAny().orElse(null);
		assertNotNull("the json message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Test
  public void test_create() throws Exception {
	 User user = null;
	 MediaType contentType = null;
    String jsonBody = json(new User());
    
    mockMvc.perform(post("/rest/user")
      .secure(true)
      .with(auth(user))
      .contentType(contentType)
      .content(jsonBody))
      .andExpect(status().isCreated())
      .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
      .andExpect(jsonPath("$.url", is("https://...")))
      .andExpect(jsonPath("$._links.self.href", startsWith("https://...")))
      .andDo(print());
  }

	@SuppressWarnings("unchecked")
	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

	static RequestPostProcessor auth(final User user) {
		return new RequestPostProcessor() {
			public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
						user.getPassword(), user.getAuthorities());
				return authentication(authentication).postProcessRequest(request);
			}
		};
	}
}
