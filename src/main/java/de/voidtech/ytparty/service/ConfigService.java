package main.java.de.voidtech.ytparty.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
public class ConfigService {
	private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());

	private final Properties config = new Properties();

	//PRIVATE FOR SINGLETON
	public ConfigService() {

		File configFile = new File("config.properties");
		if (configFile.exists()) {
			try (FileInputStream fis = new FileInputStream(configFile)){
				config.load(fis);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "an error has occurred while reading the config\n" + e.getMessage());
			}	
		} else {
			LOGGER.log(Level.SEVERE, "There is no config file. You need a file called config.properties at the root of the project!");
		}
	}

	public String getHibernateDialect()
	{
		String dialect = config.getProperty("hibernate.Dialect");
		return dialect != null ? dialect : "org.hibernate.dialect.PostgreSQLDialect";
	}
	
	public String getDriver()
	{
		String driver = config.getProperty("hibernate.Driver");
		return driver != null ? driver : "org.postgresql.Driver";
	}
	
	public String getDBUser()
	{
		String user = config.getProperty("hibernate.User");
		return user != null ? user : "postgres";
	}
	
	public String getDBPassword()
	{
		String pass = config.getProperty("hibernate.Password");
		return pass != null ? pass : "root";
	}
	
	public String getConnectionURL()
	{
		String dbURL = config.getProperty("hibernate.ConnectionURL");
		return dbURL != null ? dbURL : "jdbc:postgresql://localhost:5432/YTParty";
	}
	

	public String getHttpPort() {
		String port = config.getProperty("http.port");
		return port != null ? port : "6969";
	}
	
	public boolean textCacheEnabled() {
		String cacheEnabled = config.getProperty("cache.TextIsEnabled");
		return cacheEnabled != null ? Boolean.parseBoolean(cacheEnabled) : true;	
	}
	
	public boolean binaryCacheEnabled() {
		String cacheEnabled = config.getProperty("cache.BinaryIsEnabled");
		return cacheEnabled != null ? Boolean.parseBoolean(cacheEnabled) : true;	
	}
	
	public String getHCaptchaToken() {
		return config.getProperty("hcaptcha.Token");
	}
}