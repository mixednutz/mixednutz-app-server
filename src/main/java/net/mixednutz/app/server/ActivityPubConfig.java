package net.mixednutz.app.server;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.w3c.activitypub.util.ProblemHandler;

import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
@ComponentScan("org.w3c.activitypub")
public class ActivityPubConfig {
	
	

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer customizer() {
	    return new Jackson2ObjectMapperBuilderCustomizer() {
	        @Override
	        public void customize(Jackson2ObjectMapperBuilder builder) {
	            builder.modulesToInstall(new ProblemHandlerModule());
	        }
	    };
	}
	
//	 @Bean
//	    public Module javaTimeModule() {
//	        JavaTimeModule module = new JavaTimeModule();
//	        module.addSerializer(LOCAL_DATETIME_SERIALIZER);
//	        return module;
//	    }
	
	public class ProblemHandlerModule extends SimpleModule {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4530145040806033395L;

		@Override
	    public void setupModule(SetupContext context) {
	        // Required, as documented in the Javadoc of SimpleModule
	        super.setupModule(context);
	        context.addDeserializationProblemHandler(new ProblemHandler());
	    } 
		
	}
	
}
