import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Category(IntegrationTest.class)
public class SampleControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  
  private HttpMessageConverter mappingJackson2HttpMessageConverter;
  
  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {
    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
      .findAny()
      .orElse(null);
    assertNotNull("the json message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }
  
  public void test_create() throws Exception {
    String jsonBody = json(new User());
    
    mockMvc.perform(post("/rest/user")
      .secure(true)
      .with(auth(user))
      .contentType(contentType)
      .content(jsonBody)
      .andExpect(status().isCreated())
      .andExpect(content().contentTypeComatibleWith(MediaTypes.HAL_JSON))
      .andExpect(jsonPath("$.url", is("https://...")))
      .andExpect(jsonPath("$._links.self.href", startsWith("https://...")))
      .andDo(print());
  }

  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }
  
  static RequestPostProcessor auth(final user user) {
    return new RequestPostProcessor() {
      public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new ...(user, user.getPassword, user.getAuthorities());
        return authentication(authentication).postProcessRequest(request);
      }
    };
  }
}
