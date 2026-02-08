package net.mixednutz.app.server;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${photoDirectory:#{null}}")
	String photoDirectory;
		
	@Value("${ads-txt:#{null}}")
	Resource adsTxtLocation;
	
	@Autowired
	ApplicationContext ctx;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		URI photoDirectoryUri = Paths.get(photoDirectory!=null?photoDirectory:"").toUri();
		//This might not be necessary.  addResourceLocations might take as adsTxtLocation is. 
		URI adsTxtUri;
		try {
			adsTxtUri = adsTxtLocation!=null?adsTxtLocation.getURI():Paths.get("").toUri();
		} catch (IOException e) {
			throw new RuntimeException("Unable to load Resource Handler for ads-txt.", e);
		}
		
		/*
		 * In order to get both /** Controllers and root files /ad.txt to work
		 * we need to disable Spring boot's spring.resources.addMappings property.
		 * That means we need to redefine /css,/img,/js,/webjars manually.
		 */
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registry
			.addResourceHandler("/photos/**")
	        .addResourceLocations(photoDirectoryUri.toString())
	    	.setCacheControl(CacheControl.maxAge(Duration.ofHours(1))); 
		registry.
			addResourceHandler("/css/**")
	    	.addResourceLocations("classpath:/static/css/")
	    	.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
		registry
			.addResourceHandler("/img/**")
	    	.addResourceLocations("classpath:/static/img/");
		registry
			.addResourceHandler("/js/**")
	    	.addResourceLocations("classpath:/static/js/")
	    	.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
		if (!registry.hasMappingForPattern("/webjars/**")) {
			registry
				.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/")
		    	.setCacheControl(CacheControl.maxAge(Duration.ofDays(1)))
				.resourceChain(false);
		}
		registry
			.addResourceHandler("/ads.txt*")
			.addResourceLocations(adsTxtUri.toString())
	    	.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
	}

}
