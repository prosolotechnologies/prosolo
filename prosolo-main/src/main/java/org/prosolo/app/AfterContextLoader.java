package org.prosolo.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.prosolo.app.bc.BusinessCase;
import org.prosolo.app.bc.BusinessCase1_DL;
import org.prosolo.app.bc.BusinessCase2_AU;
import org.prosolo.app.bc.BusinessCase3_Statistics;
import org.prosolo.app.bc.BusinessCase4_EDX;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.evaluation.BadgeType;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.importing.DataGenerator;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.indexing.impl.ESAdministrationImpl;
import org.prosolo.services.messaging.rabbitmq.impl.DefaultMessageWorker;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

public class AfterContextLoader implements ServletContextListener {

	private static Logger logger = Logger.getLogger(AfterContextLoader.class.getName());

	/* Application Startup Event */
	public void contextInitialized(ServletContextEvent ce) {

		// read settings from config.xml
		final Settings settings = Settings.getInstance();

		if (settings.config.init.formatDB) {
			logger.debug("Initializing static data!");
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
			settings.config.init.formatDB=false;
		}

		if (Settings.getInstance().config.init.importData) {
			logger.info("Importing external data");
			// ServiceLocator.getInstance().getService(ResourcesImporter.class).batchImportExternalCompetences();
			logger.info("External data import finished!");
			ServiceLocator.getInstance().getService(DataGenerator.class)
					.populateDBWithTestData();

		}
		if (settings.config.init.indexTrainingSet){
			ESAdministration esAdmin=new ESAdministrationImpl();
			esAdmin.indexTrainingSet();
		}

		new Thread(new Runnable() {
			@SuppressWarnings("unused")
			@Override
			public void run() {
				try {
					Client client = ElasticSearchFactory.getClient();
				} catch (IndexingServiceNotAvailable e) {
					logger.error(e);
				}
				System.out.println("Finished ElasticSearch initialization:" + CommonSettings.getInstance().config.rabbitMQConfig.distributed + " .."
						+ CommonSettings.getInstance().config.rabbitMQConfig.masterNode);
				if (!CommonSettings.getInstance().config.rabbitMQConfig.distributed || CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
					System.out.println("Initializing Twitter Streams Manager here. REMOVED");
				//	ServiceLocator.getInstance().getService(TwitterStreamsManager.class).start();
				}
			
			}
		}).start();
		initApplicationServices();
		//ServiceLocator.getInstance().getService(AnalyticalServiceCollector.class).testCreateTargetCompetenceActivitiesAnalyticalData();
		
	}
	
	private void initApplicationServices(){
		System.out.println("Init application services...");
		ServiceLocator.getInstance().getService(CollaboratorsRecommendation.class).initializeMostActiveRecommendedUsers();
	//	Settings settings = Settings.getInstance(); 
		if(CommonSettings.getInstance().config.rabbitMQConfig.distributed){
			
		
		ReliableConsumer systemConsumer=new ReliableConsumerImpl();
		systemConsumer.setWorker(new DefaultMessageWorker());
	//	systemConsumer=new ReliableConsumerImpl();
		systemConsumer.setQueue(QueueNames.SYSTEM.name().toLowerCase());
		systemConsumer.StartAsynchronousConsumer();
	//	systemConsumer.init(QueueNames.SYSTEM);
		//ServiceLocator.getInstance().getService(ReliableConsumer.class).init(QueueNames.SYSTEM);
		ReliableConsumer sessionConsumer=new ReliableConsumerImpl();
		sessionConsumer.setWorker(new DefaultMessageWorker());
		sessionConsumer.setQueue(QueueNames.SESSION.name().toLowerCase());
		sessionConsumer.StartAsynchronousConsumer();
	//	sessionConsumer.init(QueueNames.SESSION);
		//ServiceLocator.getInstance().getService(ReliableConsumer.class).init(QueueNames.SESSION);
		
		//ServiceLocator.getInstance().getService(ReliableProducer.class).init();
		if(CommonSettings.getInstance().config.rabbitMQConfig.masterNode){
			System.out.println("Init MasterNodeReliableConsumer...");
			
			//ServiceLocator.getInstance().getService(MasterNodeReliableConsumer.class).init();
		} 
		}
		//ServiceLocator.getInstance().getService(AnalyticalServiceCollector.class).testCreateActivityInteractionData();
	}
 
	
	private void initStaticData() {
		try {
			User adminUser = ServiceLocator.getInstance().getService(ResourceFactory.class)
					.createNewUser(
							Settings.getInstance().config.init.defaultUser.name,
							Settings.getInstance().config.init.defaultUser.lastname,
							Settings.getInstance().config.init.defaultUser.email,
							true,
							Settings.getInstance().config.init.defaultUser.pass,
							null, 
							null, 
							true);
			
			Organization organization = ServiceLocator.getInstance().getService(OrganizationManager.class)
					.createNewOrganization(adminUser, "", "", "");
	
			adminUser.setOrganization(organization);
			adminUser = ServiceLocator.getInstance().getService(UserManager.class)
					.saveEntity(adminUser);
	
			OrganizationalUnit headOfficeOrgUnit = ServiceLocator.getInstance().getService(OrganizationManager.class)
					.createNewOrganizationalUnit(
							organization,
							"Head Office",
							"Default inital organizational Unit for: " + organization.getTitle(), 
							false);
			
			OrganizationalUnit systemOrgUnit = ServiceLocator.getInstance().getService(OrganizationManager.class)
					.createNewOrganizationalUnit(
							organization, 
							"System",
							"System unit", 
							true);
	
			organization.addOrgUnit(headOfficeOrgUnit);
			organization.addOrgUnit(systemOrgUnit);
			ServiceLocator.getInstance().getService(OrganizationManager.class).saveEntity(organization);
	
			String roleUserTitle = "User";
			String roleManagerTitle = "Manager";
			String roleAdminTitle = "Admin";
	
			ServiceLocator.getInstance().getService(RoleManager.class).createNewRole(
							roleUserTitle, 
							"Regular user", 
							false);
			
			ServiceLocator.getInstance().getService(RoleManager.class).createNewRole(
							roleManagerTitle, 
							"Manage learning artifacts",
							false);
			
			Role adminRole = ServiceLocator.getInstance().getService(RoleManager.class)
					.createNewRole(roleAdminTitle,
							"Administrator has maximumum priviledges", 
							true);
	
	//		ServiceLocator
	//		.getInstance()
	//		.getService(RoleManager.class)
	//		.assignRoleToUser(adminRole, adminUser, systemOrgUnit,
	//				"System administrator");
			adminUser = ServiceLocator.getInstance().getService(RoleManager.class)
					.assignRoleToUser(
							adminRole, 
							adminUser);
	
			// instantiate badges
			ServiceLocator.getInstance().getService(BadgeManager.class)
					.createBadge(BadgeType.STAR, "Excellence Badge");
			
		
		} catch (EventException e) {
			logger.error(e);
		}
	}

	/* Application Shutdown Event */
	public void contextDestroyed(ServletContextEvent ce) { }
	
	void initRepository(int bc) {
		switch (bc) {

		case BusinessCase.DL_TEST:
			try {
				ServiceLocator.getInstance().getService(BusinessCase1_DL.class)
						.initRepository();
			} catch (Exception e) {
				logger.error("Could not initialise Repository for BC DL_TEST:", e);
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
			ServiceLocator.getInstance().getService(BusinessCase3_Statistics.class)
					.initRepository();
			break;
		case BusinessCase.EXD:
			ServiceLocator.getInstance().getService(BusinessCase4_EDX.class)
			.initRepository();
	break;
		default:
			break;
		}
	}

}