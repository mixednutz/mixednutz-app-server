package net.mixednutz.app.server;

import java.net.URI;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${photoDirectory:#{null}}")
	String photoDirectory;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		URI photoDirectoryUri = Paths.get(photoDirectory!=null?photoDirectory:"").toUri();
		
		registry
			.addResourceHandler("/photos/**")
	        .addResourceLocations(photoDirectoryUri.toString()); 
	}

}
