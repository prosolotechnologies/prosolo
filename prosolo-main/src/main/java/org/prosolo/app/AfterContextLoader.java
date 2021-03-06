package org.prosolo.app;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.*;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.config.observation.ObservationConfigLoaderService;
import org.prosolo.config.security.SecurityService;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.datainit.StaticDataInitManager;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.importing.DataGenerator;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.messaging.rabbitmq.impl.AppEventMessageWorker;
import org.prosolo.services.messaging.rabbitmq.impl.DefaultMessageWorker;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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

			try {
				ServiceLocator.getInstance().getService(StaticDataInitManager.class).initStaticData();
			} catch (Exception e) {
				logger.error("error", e);
				throw new RuntimeException("Error initializing static data");
			}
			logger.debug("Static data initialised!");
		
			if (settings.config.init.bc != null) {
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
			systemConsumer.setWorker(new AppEventMessageWorker(ServiceLocator.getInstance().getService(EventFactory.class)));
			systemConsumer.setQueue(QueueNames.APP_EVENT.name().toLowerCase());
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

	/* Application Shutdown Event */
	public void contextDestroyed(ServletContextEvent ce) {
		 systemConsumer.StopAsynchronousConsumer();
		 sessionConsumer.StopAsynchronousConsumer();
		 broadcastConsumer.StopAsynchronousConsumer();

	}
	
	void initRepository(String bc) {
		if (bc.startsWith("test/")) {
			String test = bc.substring(5);
			test = test.replace('.', '_');
			try {
				((BaseBusinessCase) Class.forName("org.prosolo.app.bc.test.BusinessCase_Test_" + test).getConstructor().newInstance()).initRepository();
			} catch (ClassNotFoundException e) {
				logger.error("Test business case class not found for business case " + bc);
			} catch (NoSuchMethodException e) {
				logger.error("There is no default constructor in test business case " + bc);
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
		switch (bc) {
			case "0":
				ServiceLocator.getInstance().getService(BusinessCase0_Blank.class).initRepository();
				break;
			case "4":
				ServiceLocator.getInstance().getService(BusinessCase4_EDX.class).initRepository();
				break;
			case "5":
				new BusinessCase5_Demo().initRepository();
				break;
			case "tutorial":
				new BusinessCase5_Tutorial().initRepository();
				break;
			default:
				break;
		}
	}

}