package net.mixednutz.app.server.manager.impl;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.manager.EmailMessageManager;

@Service
@ConfigurationProperties(prefix="mixednutz")
public class EmailMessageManagerImpl implements EmailMessageManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailMessageManagerImpl.class);
	
	@Autowired
	private JavaMailSender mailSender;
			
	private SpringTemplateEngine emailTemplateEngine;
	
	private EmailProperties email = new EmailProperties();
	
	@Autowired
	public void setEmailTemplateEngine(SpringTemplateEngine emailTemplateEngine) {
		this.emailTemplateEngine = emailTemplateEngine;
	}
		
	@Override
	public void send(String templateName, EmailMessage message, Map<String, Object> model) {

		try {
			MimeMessage msg = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg);
			
			//TO
			setTo(helper, message);
			
			//BCC
			setLogAsBcc(helper, message);
			
			//FROM
			if (message.getFrom()!=null) {
				setNoReply(helper);
			} else {
//				setUserAsFrom(helper);
				setDefaultAsFrom(helper);
			}
			
			//Subject
			LOG.info("Subject: {}",message.getSubject());
			helper.setSubject(message.getSubject());
			
			//Body
			String body = processTemplate(templateName, model);
			LOG.info("Body: {}",body);
			helper.setText(body, true);
						
			LOG.info("Sending msg");
			mailSender.send(msg);
		} catch (MailException me) {
			LOG.error("Error sending email", me);
			// remove this after testing
			throw me;
		} catch (UnsupportedEncodingException | MessagingException e) {
			LOG.error("Error build email message", e);
			// remove this after testing
			throw new RuntimeException("Error build email message", e);
		}
	}
	
	protected String processTemplate(String templateName, Map<String, Object> model) {
		IContext context = new Context(Locale.getDefault(), model);
		return emailTemplateEngine.process(templateName, context);
	}
	
	protected void setTo(MimeMessageHelper helper, EmailMessage message) 
			throws UnsupportedEncodingException, MessagingException {
		
		for (UserEmailAddress email: message.getTo()) {
			InternetAddress to = new InternetAddress(email.getEmailAddress());
			if (email.getDisplayName()!=null) {
				to.setPersonal(email.getUser().getDisplayName());
				LOG.info("To: {}<{}>", email.getEmailAddress(), email.getDisplayName());
			} else {
				LOG.info("To: {}", email.getEmailAddress());
			}
			helper.addTo(to);
		}
	}
	
	protected void setDefaultAsFrom(MimeMessageHelper helper) throws MessagingException {
		InternetAddress from2 = new InternetAddress("tfes8@yahoo.com");
		LOG.info("From: {}", from2.getAddress());
		helper.setFrom(from2);
	}
	
	protected void setUserAsFrom(MimeMessageHelper helper) {
//			User user = (User) CurrentUserHolder.getUser();
//			String email = makeUsernameEmail(user.getUsername());
//			InternetAddress from = new InternetAddress(email);
//			String personalName = user.getFirstname()+" "+user.getLastname()+ 
//					" (via "+siteEmailName+")";
//			from.setPersonal(personalName);
//			LOG.info("From: "+personalName+"<"+email+">");
//			helper.setFrom(from);			
	}
	
	protected void setNoReply(MimeMessageHelper helper) throws UnsupportedEncodingException, MessagingException {
		InternetAddress from2 = new InternetAddress(email.getNoReply());
		if (email.getDisplayName()!=null) {
			from2.setPersonal(email.getDisplayName());
			LOG.info("From: {}<{}>", email.getDisplayName(), email.getNoReply());
		} else {
			LOG.info("From: {}", email.getNoReply());
		}
		helper.setFrom(from2);
	}
	
	public void setLogAsBcc(MimeMessageHelper helper, EmailMessage message) throws MessagingException {
		List<String> emails = StreamSupport
			.stream(message.getTo().spliterator(), true)
			.map((uea)->uea.getEmailAddress())
			.collect(Collectors.toList());
		if (!emails.contains(email.getLogTo())) {
			helper.setBcc(email.getLogTo());
			LOG.info("Bcc: {}", email.getLogTo());
		}		
	}
	
	public EmailProperties getEmail() {
		return email;
	}

	public void setEmail(EmailProperties email) {
		this.email = email;
	}
	
	public static class EmailProperties {
		
		private String displayName;
		private String noReply;
		private String logTo;
		
		public String getDisplayName() {
			return displayName;
		}
		public void setDisplayName(String name) {
			this.displayName = name;
		}
		public String getNoReply() {
			return noReply;
		}
		public void setNoReply(String noReply) {
			this.noReply = noReply;
		}
		public String getLogTo() {
			return logTo;
		}
		public void setLogTo(String logTo) {
			this.logTo = logTo;
		}
		
	}

}
