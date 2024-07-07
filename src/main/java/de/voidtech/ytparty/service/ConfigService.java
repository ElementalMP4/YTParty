package main.java.de.voidtech.ytparty.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigService {
	private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());

	private static final Properties config = new Properties();

	static {
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
	
	public static String getDBUser()
	{
		String user = config.getProperty("hibernate.User");
		return user != null ? user : "postgres";
	}
	
	public static String getDBPassword()
	{
		String pass = config.getProperty("hibernate.Password");
		return pass != null ? pass : "root";
	}
	
	public static String getConnectionURL()
	{
		String dbURL = config.getProperty("hibernate.ConnectionURL");
		return dbURL != null ? dbURL : "jdbc:postgresql://localhost:5432/YTParty";
	}
	

	public static String getHttpPort() {
		String port = config.getProperty("http.port");
		return port != null ? port : "6969";
	}
	
	public static String getCaptchaToken() {
		return config.getProperty("captcha.Token");
	}

}