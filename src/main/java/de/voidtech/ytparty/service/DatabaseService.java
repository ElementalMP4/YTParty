package main.java.de.voidtech.ytparty.service;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Service //Tell spring that this bean is a Service
@EnableTransactionManagement
@org.springframework.context.annotation.Configuration
@Order(1) //Initialise this Service before all others
public class DatabaseService 
{
	private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName()); //Get the logger for this class
	
	@Autowired
	private ConfigService config; //Inject the configuration
	
	@Bean("sessionFactory") //Tell spring that this bean is called "sessionFactory"
	public SessionFactory getSessionFactory() 
	{
		SessionFactory sessionFactory = null;
			try {
				exportSchema();
				Properties hibernateProperties = getHibernateProperties();
				Configuration hibernateConfig = new Configuration();
				getAllEntities().forEach(hibernateConfig::addAnnotatedClass); //Get all the database entities. 
																			  //Add all the entities to the hibernate config
				hibernateConfig.setProperties(hibernateProperties);
				sessionFactory = hibernateConfig.buildSessionFactory(); //Create the SessionFactory
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "An error occurred while setting up Hibernate SessionFactory:\n" + e.getMessage());
			}

		return sessionFactory; //When we use this bean, the sessionFactory from before will be returned.
	}
	
	private void exportSchema() {
		Properties hbnProperties = getHibernateProperties();
		MetadataSources metadataSources = new MetadataSources(
				new StandardServiceRegistryBuilder()
					.applySettings(hbnProperties)
					.build()
				); //Pass the database properties into Hibernate
		
		Set<Class<?>> annotated = getAllEntities(); //Get all the database entities
		annotated.forEach(metadataSources::addAnnotatedClass); //Add all the entities into Hibernate
		
		new SchemaUpdate()
			.setFormat(true)
			.execute(EnumSet.of(TargetType.DATABASE), metadataSources.buildMetadata()); //Configure the Schema
	}
	
	private Properties getHibernateProperties()
	{
		Properties properties = new Properties();
		properties.put(Environment.DRIVER, config.getDriver()); //Set the database driver (which database we are using)
		properties.put(Environment.URL, config.getConnectionURL()); //Set the Connection URL
		properties.put(Environment.USER, config.getDBUser()); //Set the Username
		properties.put(Environment.PASS, config.getDBPassword()); //Set the Password
		properties.put(Environment.DIALECT, config.getHibernateDialect()); //Set the dialect (what type of SQL should be produced)
		
		return properties;
	}
	
	private Set<Class<?>> getAllEntities()
	{
		return new Reflections("main.java.de.voidtech.ytparty").getTypesAnnotatedWith(Entity.class); 
		//Get every class in the project which is a database entity 
	}
}