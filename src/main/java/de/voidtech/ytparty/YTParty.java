package main.java.de.voidtech.ytparty;

import main.java.de.voidtech.ytparty.service.ConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class YTParty {
	
	public static void main(String[] args) {
		SpringApplication springApp = new SpringApplication(YTParty.class);
		Properties props = new Properties();

		props.setProperty("server.port", ConfigService.getHttpPort());
		props.put("spring.datasource.url", ConfigService.getConnectionURL());
		props.put("spring.datasource.username", ConfigService.getDBUser());
		props.put("spring.datasource.password", ConfigService.getDBPassword());
		springApp.setDefaultProperties(props);
		springApp.run(args);
	}
}