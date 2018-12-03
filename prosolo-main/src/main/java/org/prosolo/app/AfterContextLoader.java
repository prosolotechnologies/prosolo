package org.prosolo.app;

import com.mysql.jdbc.Driver;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.prosolo.app.bc.BusinessCase0_Blank;
import org.prosolo.app.bc.BusinessCase4_EDX;
import org.prosolo.app.bc.BusinessCase5_UniSA;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.config.observation.ObservationConfigLoaderService;
import org.prosolo.config.security.SecurityService;
import org.prosolo.core.persistance.PersistenceManager;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.importing.DataGenerator;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.messaging.rabbitmq.impl.DefaultMessageWorker;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.util.roles.SystemRoleNames;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

public class AfterContextLoader implements ServletContextListener {

	private static Logger logger = Logger.getLogger(AfterContextLoader.class.getName());

	ReliableConsumer systemConsumer =null;
	ReliableConsumer sessionConsumer =null;
	ReliableConsumer broadcastConsumer=null;

	/* Application Startup Event */
	public void contextInitialized(ServletContextEvent ce) {

		// read settings from config.xml
		final Settings settings = Settings.getInstance();
			logger.debug("Initialized settings");

		if (settings.config.init.formatDB) {
			try {
				Flyway flyway = new Flyway();
				DataSource dataSource;
				/*
				get DataSource already configured for Hibernate if available
				and if not, create DataSource by using the same method that is being
				user for Hibernate
				 */
				Optional<DataSource> ds =  Optional.empty();
						//ServiceLocator.getInstance().getService(PersistenceManager.class).getDataSource();
				dataSource = ds.isPresent() ? ds.get() : HibernateUtil.dataSource();
				flyway.setDataSource(dataSource);
				flyway.setLocations("classpath:dbscripts/init");
				/*
				all migrations up to baseline version will be ignored and all
				migrations after this version will be executed
				 */
				flyway.setBaselineVersionAsString("0.0");
				flyway.setBaselineDescription("DB Init state");
				/*
				baseline should be used when introducing Flyway to existing database
				 */
				flyway.setBaselineOnMigrate(true);
				flyway.migrate();
			} catch (FlywayException fe) {
				logger.error("Error", fe);
				throw new RuntimeException("Erorr during application init");
			}
		}

		if (settings.config.init.formatDB || settings.config.init.importCapabilities){
			try{
				ServiceLocator.getInstance().getService(SecurityService.class).initializeRolesAndCapabilities();
			}catch(Exception e){
			    logger.error(e);
			}
		}
		
		if(settings.config.init.formatDB) {
			try {
				ServiceLocator.getInstance().getService(ObservationConfigLoaderService.class)
					.initializeObservationConfig();
			} catch(DbConnectionException e) {
				logger.error(e);
			}
		}

		if (settings.config.init.formatDB) {
			try {
				logger.debug("initialize elasticsearch indexes");
				initElasticSearchIndexes();
			} catch (IndexingServiceNotAvailable e1) {
				logger.error("Error", e1);
			}

			logger.debug("Initializing static data!");
			boolean oldEmailNotifierVal = CommonSettings.getInstance().config.emailNotifier.activated;
			CommonSettings.getInstance().config.emailNotifier.activated = false;
			
			initStaticData();
			logger.debug("Static data initialised!");
		
			if (settings.config.init.bc != 0) {
				logger.debug("Starting repository initialization");
				initRepository(settings.config.init.bc);
				logger.debug("Repository initialised!");
			}
			
			// initializing admin settings
			ServiceLocator.getInstance().getService(ResourceSettingsManager.class).createResourceSettings(
					Settings.getInstance().config.admin.selectedUsersCanDoEvaluation, 
					Settings.getInstance().config.admin.userCanCreateCompetence,
					Settings.getInstance().config.admin.individualCompetencesCanNotBeEvaluated);
			settings.config.init.formatDB = false;
			
			CommonSettings.getInstance().config.emailNotifier.activated = oldEmailNotifierVal;
		} else {
			/*
			if we are not formatting the database, create indexes with data not dependent on mysql db if they don't exist
			 */
			ESAdministration esAdmin = ServiceLocator.getInstance().getService(ESAdministration.class);
			try {
				esAdmin.createNonrecreatableSystemIndexesIfNotExist();
			} catch (IndexingServiceNotAvailable e) {
				logger.warn("Warning", e);
			}
		}
	
		if (Settings.getInstance().config.init.importData) {
			logger.info("Importing external data");
			logger.info("External data import finished!");
			ServiceLocator.getInstance().getService(DataGenerator.class).populateDBWithTestData();
		}
		
		if (settings.config.init.indexTrainingSet) {
			ServiceLocator.getInstance().getService(ESAdministration.class).indexTrainingSet();
		}

		//init ES client if not initialized
		initESClient();

		logger.debug("initialize Application services");
		
		initApplicationServices();
		logger.debug("Services initialized");
	}

	private void initESClient() {
		logger.debug("Initialize ES client");
		ElasticSearchConnector.initializeESClientIfNotInitialized();
		logger.debug("Finished ES client initialization");
	}

	private void initElasticSearchIndexes() throws IndexingServiceNotAvailable {
		ESAdministration esAdmin = ServiceLocator.getInstance().getService(ESAdministration.class);
		esAdmin.deleteAllIndexes();
		esAdmin.createAllIndexes();
	}
	
	private void initApplicationServices(){
		System.out.println("Init application services...");

		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {

			systemConsumer = new ReliableConsumerImpl();
			systemConsumer.setWorker(new DefaultMessageWorker());
			systemConsumer.setQueue(QueueNames.SYSTEM.name().toLowerCase());
			systemConsumer.StartAsynchronousConsumer();
			sessionConsumer = new ReliableConsumerImpl();
			sessionConsumer.setWorker(new DefaultMessageWorker());
			sessionConsumer.setQueue(QueueNames.SESSION.name().toLowerCase());
			sessionConsumer.StartAsynchronousConsumer();

			broadcastConsumer = new ReliableConsumerImpl();
			broadcastConsumer.setWorker(new DefaultMessageWorker());
			broadcastConsumer.setQueue(QueueNames.BROADCAST.name().toLowerCase());
			broadcastConsumer.StartAsynchronousConsumer();

			if (CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
				System.out.println("Init MasterNodeReliableConsumer...");
			}
		}
	}
	
	private void initStaticData() {
		Long superAdminRoleId = ServiceLocator.getInstance().getService(RoleManager.class).getRoleIdByName(SystemRoleNames.SUPER_ADMIN);

		try {
			ServiceLocator.getInstance().getService(UserManager.class).createNewUser(
                    0,
                    Settings.getInstance().config.init.defaultUser.name,
                    Settings.getInstance().config.init.defaultUser.lastname,
                    Settings.getInstance().config.init.defaultUser.email,
                    true,
                    Settings.getInstance().config.init.defaultUser.pass,
                    null,
                    null,
                    null,
                    Arrays.asList(superAdminRoleId),
                    true);
		} catch (IllegalDataStateException e) {
			logger.error(e);
		}
	}

	/* Application Shutdown Event */
	public void contextDestroyed(ServletContextEvent ce) {
		 systemConsumer.StopAsynchronousConsumer();
		 sessionConsumer.StopAsynchronousConsumer();
		 broadcastConsumer.StopAsynchronousConsumer();

	}
	
	void initRepository(int bc) {
		switch (bc) {
		
		case 0:
			ServiceLocator.getInstance().getService(BusinessCase0_Blank.class).initRepository();
			break;
		case 4:
			ServiceLocator.getInstance().getService(BusinessCase4_EDX.class).initRepository();
			break;
		case 5:
			ServiceLocator.getInstance().getService(BusinessCase5_UniSA.class).initRepository();
			break;
		default:
			break;
		}
	}

}