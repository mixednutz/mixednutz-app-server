package net.mixednutz.app.server;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("db-local-hsqldb-file")
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class})
public class HsqldbLocalFileConfig {
	
	@Bean
	public DataSource dataSource() {
		return DataSourceBuilder.create()
				.url("jdbc:hsqldb:file:data/testdb")
				.username("SA")
				.password("test")
				.build();
	}

}
