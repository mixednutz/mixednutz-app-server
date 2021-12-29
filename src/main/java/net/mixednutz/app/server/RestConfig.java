package net.mixednutz.app.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

import net.mixednutz.app.server.entity.User;

@Configuration
public class RestConfig {

	@Bean
	public RepresentationModelProcessor<EntityModel<User>> userProcessor() {
		return new RepresentationModelProcessor<EntityModel<User>>() {
			public EntityModel<User> process(EntityModel<User> resource) {
				// TODO add actions
				// Link link = linkTo(methodOn(Controller.class)
				// .controllerMethod();
				// resource.add(link);
				return resource;
			}
		};
	}

}
