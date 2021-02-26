package net.mixednutz.app.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration
public class ThymeleafConfig {

	@Autowired
	ThymeleafProperties properties;
	
	@Bean
	public SpringResourceTemplateResolver defaultTemplateResolver() {
	    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
	    templateResolver.setPrefix("classpath:/templates/");
	    templateResolver.setSuffix(".html");
	    templateResolver.setTemplateMode(TemplateMode.HTML);
	    templateResolver.setCharacterEncoding("UTF-8");
	    templateResolver.setOrder(0);
	    templateResolver.setCacheable(this.properties.isCache());
	    templateResolver.setCheckExistence(true);

	    return templateResolver;
	}

	@Bean
	public FileTemplateResolver fileTemplateResolver(
			@Value("${externalTemplateFolder:#{null}}") String prefix) {
		if (prefix==null) {
			return null;
		}
		FileTemplateResolver templateResolver = new FileTemplateResolver();
	    templateResolver.setPrefix(prefix);
	    templateResolver.setSuffix(".html");
	    templateResolver.setTemplateMode(TemplateMode.HTML);
	    templateResolver.setCharacterEncoding("UTF-8");
	    templateResolver.setOrder(1);
	    templateResolver.setCacheable(this.properties.isCache());
	    templateResolver.setCheckExistence(true);

	    return templateResolver;
	}
	
}
