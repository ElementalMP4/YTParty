package main.java.de.voidtech.ytparty.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigService {
	private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());

	private final Properties config = new Properties();

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
		return config.getProperty("captcha.Token");
	}

	public String getMailUser() {
		return config.getProperty("mail.User");
	}

	public String getMailPassword() {
		return config.getProperty("mail.Password");
	}

	public String getMailHost() {
		return config.getProperty("mail.Host");
	}

	public int getMailPort() {
		return Integer.parseInt(config.getProperty("mail.Port"));
	}

	public String getMailAddress() {
		return config.getProperty("mail.Address");
	}
	
	public String getParticleMode() {
		String mode = config.getProperty("particles.Mode");
		return (mode == null ? "regular" : mode);
	}

	public String getLogWebhookURL() {
		return config.getProperty("logging.Webhook");
	}
}