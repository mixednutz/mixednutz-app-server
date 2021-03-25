package net.mixednutz.app.server.manager.impl;

import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
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
			mailSender.send(new MimeMessagePreparator[] {
					setTo(message),
					setLogAsBcc(),
					message.getFrom()!=null ? setNoReply() : setUserAsFrom(),
					(msg)->{
						MimeMessageHelper helper = new MimeMessageHelper(msg);
						
						//Subject
						helper.setSubject(message.getSubject());
						
						//Body
						helper.setText(processTemplate(templateName, model), true);
					}
			});
		} catch (MailException me) {
			LOG.error("Error sending email", me);
		}
	}
	
	protected String processTemplate(String templateName, Map<String, Object> model) {
		IContext context = new Context(Locale.getDefault(), model);
		return emailTemplateEngine.process(templateName, context);
	}
	
	protected MimeMessagePreparator setTo(EmailMessage message) {
		return (msg)->{
			MimeMessageHelper helper = new MimeMessageHelper(msg);
			for (UserEmailAddress email: message.getTo()) {
				InternetAddress to = new InternetAddress(email.getEmailAddress());
				if (email.getDisplayName()!=null) {
					to.setPersonal(email.getUser().getDisplayName());
				} 
				helper.addTo(to);
			}
		};
	}
	
	protected MimeMessagePreparator setUserAsFrom() {
		return (msg)->{
//			User user = (User) CurrentUserHolder.getUser();
//			String email = makeUsernameEmail(user.getUsername());
//			InternetAddress from = new InternetAddress(email);
//			String personalName = user.getFirstname()+" "+user.getLastname()+ 
//					" (via "+siteEmailName+")";
//			from.setPersonal(personalName);
//			LOG.info("From: "+personalName+"<"+email+">");
//			helper.setFrom(from);			
		};
	}
	
	protected MimeMessagePreparator setNoReply() {
		return (msg)->{
			InternetAddress from2 = new InternetAddress(email.getNoReply());
			if (email.getName()!=null) {
				from2.setPersonal(email.getName());
				LOG.info("From: {}<{}>", email.getName(), email.getNoReply());
			} else {
				LOG.info("From: {}", email.getNoReply());
			}
			msg.setFrom(from2);
		};
	}
	
	public MimeMessagePreparator setLogAsBcc() {
		return (msg)->{
			MimeMessageHelper helper = new MimeMessageHelper(msg);
			helper.setBcc(email.getLogTo());
			LOG.info("Bcc: {}", email.getLogTo());
		};
	}
	
	public EmailProperties getEmail() {
		return email;
	}

	public void setEmail(EmailProperties email) {
		this.email = email;
	}
	
	public static class EmailProperties {
		
		private String host;
		private String name;
		private String noReply;
		private String logTo;
		
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
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
