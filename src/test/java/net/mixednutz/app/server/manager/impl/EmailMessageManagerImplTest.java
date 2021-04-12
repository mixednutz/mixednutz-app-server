package net.mixednutz.app.server.manager.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import net.mixednutz.app.server.ThymeleafEmailConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes= {ThymeleafEmailConfig.class, EmailMessageManagerImpl.class})
public class EmailMessageManagerImplTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailMessageManagerImplTest.class);
		
	@Autowired
	private EmailMessageManagerImpl manager;
	
	@MockBean
	JavaMailSender mailSender;
	
	@Ignore
	@Test
	public void processTemplate() {
		Map<String,Object>model = new HashMap<>();
		model.put("message", new Message("world"));
		
		String result = manager.processTemplate("html/test_template", model);
		LOG.debug(result);
	}
	
	public static class Message {
		String hello;

		public Message(String hello) {
			super();
			this.hello = hello;
		}

		public String getHello() {
			return hello;
		}

		public void setHello(String hello) {
			this.hello = hello;
		}
	}
}
