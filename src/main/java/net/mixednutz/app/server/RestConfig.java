package net.mixednutz.app.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import net.mixednutz.app.server.entity.User;

@Configuration
public class RestConfig {

	@Bean
	public ResourceProcessor<Resource<User>> userProcessor() {
		return new ResourceProcessor<Resource<User>>() {
			public Resource<User> process(Resource<User> resource) {
				// TODO add actions
				// Link link = linkTo(methodOn(Controller.class)
				// .controllerMethod();
				// resource.add(link);
				return resource;
			}
		};
	}

}
