package org.prosolo.app;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.prosolo.app.bc.*;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.config.observation.ObservationConfigLoaderService;
import org.prosolo.config.security.SecurityService;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.importing.DataGenerator;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.indexing.impl.ESAdministrationImpl;
import org.prosolo.services.messaging.rabbitmq.impl.DefaultMessageWorker;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;

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
			//initialize ES indexes
			try {
				logger.debug("initialize elasticsearch indexes");
				initElasticSearchIndexes();
			} catch (IndexingServiceNotAvailable e1) {
				logger.error(e1);
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
		}
	
		if (Settings.getInstance().config.init.importData) {
			logger.info("Importing external data");
			logger.info("External data import finished!");
			ServiceLocator.getInstance().getService(DataGenerator.class).populateDBWithTestData();
		}
		
		if (settings.config.init.indexTrainingSet) {
			ESAdministration esAdmin = new ESAdministrationImpl();
			esAdmin.indexTrainingSet();
		}
		
		logger.debug("Initialize thread to start elastic search");
		new Thread(new Runnable() {
			@SuppressWarnings("unused")
			@Override
			public void run() {
				try {
					Client client = ElasticSearchFactory.getClient();
				} catch (NoNodeAvailableException e) {
					logger.error(e);
				}
				
				System.out.println("Finished ElasticSearch initialization:" + CommonSettings.getInstance().config.rabbitMQConfig.distributed + " .."
						+ CommonSettings.getInstance().config.rabbitMQConfig.masterNode);
				if (!CommonSettings.getInstance().config.rabbitMQConfig.distributed || CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
					System.out.println("Initializing Twitter Streams Manager here. REMOVED");
				}
			}
		}).start();
		logger.debug("initialize Application services");
		
		initApplicationServices();
		logger.debug("Services initialized");
	}

	private void initElasticSearchIndexes() throws IndexingServiceNotAvailable {
		ESAdministration esAdmin = ServiceLocator.getInstance().getService(ESAdministration.class);
		esAdmin.deleteIndexes();
		esAdmin.createIndexes();
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
		try {
			User adminUser = ServiceLocator.getInstance().getService(UserManager.class)
					.createNewUser(
							0,
							Settings.getInstance().config.init.defaultUser.name,
							Settings.getInstance().config.init.defaultUser.lastname,
							Settings.getInstance().config.init.defaultUser.email,
							true,
							Settings.getInstance().config.init.defaultUser.pass,
							null, 
							null,
							null,
							null,
							true);
			
			// allow user to be added to indexes in the previous method
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e);
			}
	
			String roleAdminTitle = "Admin";
			
			Role adminRole = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleAdminTitle);
			
			adminUser = ServiceLocator.getInstance().getService(RoleManager.class)
					.assignRoleToUser(
							adminRole, 
							adminUser.getId());
			
			ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(
					EventType.Edit_Profile, UserContextData.ofActor(adminUser.getId()), adminUser, null, null, null);
	
//			// instantiate badges
//			ServiceLocator.getInstance().getService(BadgeManager.class)
//					.createBadge(BadgeType.STAR, "Excellence Badge");
		} catch (EventException e) {
			logger.error(e);
		} catch (UserAlreadyRegisteredException e) {
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
		
		case BusinessCase.BLANK:
			try {
				ServiceLocator.getInstance().getService(BusinessCase0_Blank.class).initRepository();
			} catch (Exception e) {
				logger.error("Could not initialise Repository for BC BLANK:", e);
			}
			break;

		case BusinessCase.AU_TEST:
			try {
				BusinessCase2_AU.initRepository();
			} catch (Exception e) {
				logger.error("Could not initialise Repository for BC AU_TEST:", e);
			}
			break;
		case BusinessCase.STATISTICS:
			ServiceLocator.getInstance().getService(BusinessCase3_Statistics.class).initRepository();
			break;
		case BusinessCase.EDX:
			ServiceLocator.getInstance().getService(BusinessCase4_EDX.class).initRepository();
	break;
		default:
			break;
		}
	}

}