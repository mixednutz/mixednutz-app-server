package net.mixednutz.app.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class MixednutzAppServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MixednutzAppServerApplication.class, args);
	}
}
