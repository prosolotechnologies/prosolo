package org.prosolo.app.bc;

import java.util.ArrayList;
import java.util.Collection;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.annotation.UserRating;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.Scale;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.DefaultManager;

public class BusinessCase2_AU extends BusinessCase {
	public static Collection<Tag> allTags = new ArrayList<Tag>();
	public static Collection<User> allUsers = new ArrayList<User>();
//	private static Collection<Competence> allCompetences = new ArrayList<Competence>();
//	private static Date currentDate = new Date();
	
	public static void createTags() throws Exception{
		//Tags - BEGINS
		String[] tags={"OWL", "RDF", "Semantic Web", "Domain modeling", "Linked Data", "ontology", "methodology","ontology engineering","Conceptual Modeling","Reasoning","Protege","ontology development",
				"java","PHP","Web services","SCA","Tuscany" , "Software engineering", "Development", "Programming","Security assurance", "Application frameworks","Formal specification","Component composition","Service engineering","Software modelling","Design Strategies","Software requirements",
				"Real-tim operating systems","Operating systems","Distributed systems","Process analysis","Software quality",
				"Software pricing", "Risk management","Managing people","Team work","Version management"};
		for(int i=0;i<tags.length;i++){
			Tag t = createTag(tags[i]);
			t = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(t);
			allTags.add(t);
		}
	}
	
//	private static Annotation getTag(String tagString) throws Exception {
//		Annotation tag = null;
//		
//		for (Annotation curtag : allTags) {
//			if (curtag.getTitle().equalsIgnoreCase(tagString)) {
//				tag = curtag;
//			}
//		}
//		if (tag == null) {
//			tag = createTag(tagString);
//		}
//		return tag;
//	}
	
//	private static void addRandomTag(Node cu,int tagNumb) throws Exception{
//		Random randomGenerator = new Random();
//		
//		for (int idx = 0; idx < tagNumb; idx++){
//			int randomInt = randomGenerator.nextInt(allTags.size());
//			int i=0;
//			
//			for(Annotation t:allTags){
//				if(i==randomInt){
//					cu.addTag(t);
//				}
//				i++;
//			}
//			
//			cu = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(cu);
//		}
//	}
	
//	private static Annotation getRandomTag(){
//		Random randomGenerator = new Random();
//		
//		int randomInt = randomGenerator.nextInt(allTags.size());
//		int i=0;
//		
//		for(Annotation c:allTags){
//			if(i==randomInt){
//				return c;
//			}
//			i++;
//		}
//		return null;
//	}
//	
//	private static Competence getRandomCompetence(){
//		Random randomGenerator = new Random();
//		
//		int randomInt = randomGenerator.nextInt(allCompetences.size());
//		int i=0;
//		
//		for(Competence c:allCompetences){
//			if(i==randomInt){
//				return c;
//			}
//			i++;
//		}
//		return null;
//	}
	
//	private static void addIniUsers(OrganizationalUnit iniSWDevOrgUnit, String orgPos) throws Exception{
//		User nikolaDamjanovic = ServiceLocator1.getInstance().getService(UserManager.class).
//				createNewUser("Richard","Rodrigues", "nikola.damjanovic@ini.rs", iniSWDevOrgUnit, orgPos);
//		User aleksandraManic = ServiceLocator1.getInstance().getService(UserManager.class).
//				createNewUser("Joseph","Garcia", "aleksandra.manic@ini.rs", iniSWDevOrgUnit, orgPos);
//		User radmilaStojmenovic = ServiceLocator1.getInstance().getService(UserManager.class).
//				createNewUser("Charles","Green", "radmila.stojmenovic@ini.rs", iniSWDevOrgUnit, orgPos);
//		
//		allUsers.add(nikolaDamjanovic);
//		allUsers.add(aleksandraManic);
//		allUsers.add(radmilaStojmenovic);
//		createUserAccount("r.rodrigues", "pass", null,nikolaDamjanovic);
//		createUserAccount("j.garcia", "pass", null,aleksandraManic);
//		createUserAccount("c.green", "pass", null, radmilaStojmenovic);
//		for(User us:allUsers){
//			//User us=zoranJeremic;
//			SocialStream sStream=new SocialStream();
//			sStream = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(sStream);
//			us.setSocialStream(sStream);
//			us = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(us);
//			// addRandomFollowedEntity(us,4);
//			// addEventsExamples(us,org);
//			addUserPreferences(us);
//		}
//
//	}
	
//	private static void addUserPreferences(User user) throws Exception{
//		TopicPreference tPreference=new TopicPreference();
//		CompetencePreference cPreference=new CompetencePreference();
//		for (int i=0;i<6;i++){
//			tPreference.addPreferredKeyword(getRandomTag());
//			cPreference.addPreferredCompetence(getRandomCompetence());
//		}
//		tPreference.addPreferredKeyword(getTag("OWL"));
//		tPreference.addPreferredKeyword(getTag("Software engineering"));
//		tPreference.addPreferredKeyword(getTag("Software quality"));
//		tPreference = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(tPreference);
//		cPreference = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(cPreference);
//		
//		EmailPreferences ePreference = new EmailPreferences();
//		ePreference.addNotificationFrequency(TimeFrame.DAILY);
//		ePreference = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(ePreference);
//		user.addPreference(ePreference);
// 
//		user.addPreference(tPreference);
//		user.addPreference(cPreference);
//		user=ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(user);
//	}
//	
// 
//	
//	private static Date setPreviousDate() throws Exception{
////		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
//		GregorianCalendar gc = new GregorianCalendar();
//		// java.util.Date d = sdf.parse("12/12/2003");
//		gc.setTime(currentDate);
//	 int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
//		gc.roll(Calendar.DAY_OF_YEAR, -1);
//		int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
//		
//		if(dayAfter > dayBefore) {
//		    gc.roll(Calendar.YEAR, -1);
//		}
//		
//		gc.get(Calendar.DATE);
//		java.util.Date yesterday = gc.getTime();
//	 	currentDate=yesterday;
//		
//		return currentDate;
//	}
	
 
	
	public static void initRepository() throws Exception {
//		System.out.println("BusinessCaseINI - initRepository()");
//		createTags();
//		
//		try {
//			Organization flexoOrg = new Organization();
//			flexoOrg.setTitle("Flexo Digital");
//			flexoOrg = ums.saveResource(flexoOrg, false);
//			
//			Organization fos = new Organization();
//			fos.setTitle("FOS");
//			fos = ums.saveResource(fos, false);
//			
//			IntelLEO bc2IntelLEO = new IntelLEO();
//			bc2IntelLEO.addOrganization(flexoOrg);
//			bc2IntelLEO.addOrganization(fos);
//			bc2IntelLEO = ums.saveResource(bc2IntelLEO, false);
//			
//			OrganizationalUnit iniSWDevOrgUnit = new OrganizationalUnit();
//			iniSWDevOrgUnit.setTitle("Semantic Web Dev Unit");
//			iniSWDevOrgUnit.setUserOrganization(flexoOrg);
//			iniSWDevOrgUnit = ums.saveResource(iniSWDevOrgUnit, false);
//			
//			flexoOrg.addOrgUnit(iniSWDevOrgUnit);
//			flexoOrg = ums.saveResource(flexoOrg, false);
//			
//			OrganizationalUnit fosGoodOldAiResearchNetworkOrgUnit = new OrganizationalUnit();
//			fosGoodOldAiResearchNetworkOrgUnit.setTitle("FOS GOOD OLD AI");
//			fosGoodOldAiResearchNetworkOrgUnit.setUserOrganization(fos);
//			fosGoodOldAiResearchNetworkOrgUnit = ums.saveResource(fosGoodOldAiResearchNetworkOrgUnit, false);
//			
//			fos.addOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
//			fos = ums.saveResource(fos, false);
//			
//			OrganizationalPosition iniSeniorProgrammerOrgPosition = new OrganizationalPosition();
//			iniSeniorProgrammerOrgPosition.setTitle("Senior Programmer");
//			iniSeniorProgrammerOrgPosition.setAllocatedToOrgUnit(iniSWDevOrgUnit);
//			iniSeniorProgrammerOrgPosition = ums.saveResource(iniSeniorProgrammerOrgPosition, false);
//			
//			OrganizationalPosition fosSeniorProgrammerOrgPosition = new OrganizationalPosition();
//			fosSeniorProgrammerOrgPosition.setTitle("Senior Programmer");
//			fosSeniorProgrammerOrgPosition.setAllocatedToOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
//			fosSeniorProgrammerOrgPosition = ums.saveResource(fosSeniorProgrammerOrgPosition, false);
//			
//			OrganizationalPosition fosAssistantProfessorPosition = new OrganizationalPosition();
//			fosAssistantProfessorPosition.setTitle("Assistant Professor");
//			fosAssistantProfessorPosition.setAllocatedToOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
//			fosAssistantProfessorPosition = ums.saveResource(fosAssistantProfessorPosition, false);
//			
//			OrganizationalPosition fosTeachingAssistantPosition = new OrganizationalPosition();
//			fosTeachingAssistantPosition.setTitle("Teaching Assistant");
//			fosTeachingAssistantPosition.setAllocatedToOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
//			fosTeachingAssistantPosition = ums.saveResource(fosTeachingAssistantPosition, false);
//			
//			User paulEdwards = createUser("Paul Edwards", "paul.edwards@fon.rs", fos, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
//			User stevenTurner = createUser("Steven Turner", "steven.turner@fon.rs", fos, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
//			User georgeYoung = createUser("George Young", "george.young@fon.rs", fos, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
//			User kevinHall = createUser("Kevin Hall", "kevin.hall@gmail.com", fos, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
//			User kennethCarter = createUser("Kenneth Carter", "kenneth.carter@fon.rs", fos, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
//			User karenWhite = createUser("Karen White", "karen.whitea@fon.rs", fos, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
//			User helenCampbell = createUser("Helen Campbell", "helen.campbell@gmail.com", fos, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
//			User anthonyMoore = createUser("Anthony Moore", "anthony.moore@gmail.com", fos, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
//			allUsers.add(paulEdwards);
//			allUsers.add(stevenTurner);
//			allUsers.add(georgeYoung);
//			allUsers.add(anthonyMoore);
//			allUsers.add(kevinHall);
//			allUsers.add(kennethCarter);
//			allUsers.add(karenWhite);
//			allUsers.add(helenCampbell);
//			TopicPreference zjPreference=new TopicPreference();
//			zjPreference.addPreferredKeyword(getTag("Semantic Web"));
//			zjPreference.addPreferredKeyword(getTag("RDF"));
//			zjPreference.addPreferredKeyword(getTag("Ontology development"));
//			zjPreference = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(zjPreference, false);
//			
//			anthonyMoore.addPreference(zjPreference);
// 
//			TopicPreference nmPreference=new TopicPreference();
//			nmPreference.addPreferredKeyword(getTag("Semantic Web"));
//			nmPreference.addPreferredKeyword(getTag("RDF"));
//			nmPreference.addPreferredKeyword(getTag("Ontology development"));
//			nmPreference.addPreferredKeyword(getTag("domain modelling"));
//			nmPreference.addPreferredKeyword(getTag("Ontology modelling"));
//			nmPreference.addPreferredKeyword(getTag("Ontology extraction"));
//			nmPreference.addPreferredKeyword(getTag("Data mining"));
//			nmPreference.addPreferredKeyword(getTag("Text mining"));
//			nmPreference.addPreferredKeyword(getTag("Reasoning"));
//			nmPreference = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(nmPreference, false);
//			
//			anthonyMoore.addPreference(nmPreference);
//			
//			//C1: Ability to create a domain model at conceptual level - BEGINS
//			Competence domainModelingComp = new  Competence();
//			domainModelingComp.setTitle("Ability to create a domain model at conceptual level");
//			domainModelingComp.setMaker(flexoOrg);
//			domainModelingComp.addTag(getTag("OWL"));
//			domainModelingComp.addTag(getTag("Domain modeling"));
//			domainModelingComp.addTag(getTag("Semantic Web"));
//			domainModelingComp.addTag(getTag("Linked Data"));
//			domainModelingComp.addTag(getTag("ontology engineering"));
//			domainModelingComp.setVisibility(createPublicVisibility());
//			domainModelingComp = ums.saveResource(domainModelingComp, false);
//			
//			CompetenceRequirement domModelingCompRequirement = new CompetenceRequirement();
//			domModelingCompRequirement.setCompetence(domainModelingComp);
//			domModelingCompRequirement.setCompetenceLevel(CompetenceLevel.ADVANCED);
//			domModelingCompRequirement.setVisibility(createPublicVisibility());
//			domModelingCompRequirement.setMaker(flexoOrg);
//			domModelingCompRequirement = ums.saveResource(domModelingCompRequirement, false);
//			
//			
//			LearningObjective domainModelingObjective = new LearningObjective();
//			domainModelingObjective.setTargetCompetence(domainModelingComp);
//			//domainModelingObjective.setOrganization(ini);
//			domainModelingObjective = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingObjective, false);
//			
//			ContentUnit modelingIntroContent = new ContentUnit();
//			modelingIntroContent.setTitle("Conceptual Modeling for Absolute Beginners");
//			modelingIntroContent.addTag(getTag("Conceptual Modeling"));
//			modelingIntroContent.addTag(getTag("Domain Modeling"));
//			modelingIntroContent.setHref(URI.create("http://www.slideshare.net/signer/introduction-and-conceptual-modelling"));
//			modelingIntroContent.setVisibility(createPublicVisibility());
//			modelingIntroContent = ums.saveResource(modelingIntroContent, false);
//			
//			KnowledgeAsset modelingIntroKnowledgeAsset = new KnowledgeAsset();
//			modelingIntroKnowledgeAsset.setTitle(modelingIntroContent.getTitle());
//			modelingIntroKnowledgeAsset.setReferenceToContent(modelingIntroContent);
//			modelingIntroKnowledgeAsset.addTag(getTag("Ontology"));
//			modelingIntroKnowledgeAsset.setVisibility(createPublicVisibility());
//			modelingIntroKnowledgeAsset = ums.saveResource(modelingIntroKnowledgeAsset, false);
//			
//			Activity learnModelingBasics = new Activity();
//			learnModelingBasics.setTitle("Read the introductory content on Conceptual Modeling");
//			learnModelingBasics.addContent(modelingIntroContent);
//			learnModelingBasics.addRequiresKA(modelingIntroKnowledgeAsset);
//			learnModelingBasics.setVisibility(createPublicVisibility());
//			learnModelingBasics.addTag(getTag("Conceptual Modeling"));
//			learnModelingBasics.addTag(getTag("Domain Modeling"));
//			learnModelingBasics = ums.saveResource(learnModelingBasics, false);
//			
//			modelingIntroKnowledgeAsset.setActivity(learnModelingBasics);
//			modelingIntroKnowledgeAsset = ums.saveResource(modelingIntroKnowledgeAsset, false);
//			
//			Activity createDomainModel = new Activity();
//			createDomainModel.setTitle("Create your own domain model for a domain of choice");
//			////createDomainModel.setPrecedingActivity(learnModelingBasics);
//			createDomainModel.setVisibility(createPublicVisibility());
//			createDomainModel.addTag(getTag("Domain Modeling"));
//			createDomainModel = ums.saveResource(createDomainModel, false);
//			
//			LearningTask modelingLP = new LearningTask();
//			modelingLP.setTitle("How to Model Things");
//			modelingLP.addLearningActivity(learnModelingBasics);
//			modelingLP.addLearningActivity(createDomainModel);
////			modelingLP.addObjective(domainModelingObjective);
//			modelingLP.setVisibility(createPublicVisibility());
//			modelingLP.setMaker(anthonyMoore);
//			modelingLP.addTag(getTag("OWL"));
//			modelingLP.addTag(getTag("RDF"));
//			modelingLP.addTag(getTag("Semantic Web"));
//			modelingLP = ums.saveResource(modelingLP, false);	
//			
//			//C1: Ability to create a domain model at conceptual level - ENDS
//			
//			//C1.1: Ability to develop complex software systems - BEGINS
//			Competence softwEngComp = new  Competence();
//			softwEngComp.setTitle("Ability to develop complex software systems");
//			softwEngComp.setMaker(flexoOrg);
//			softwEngComp.addTag(getTag("Software engineering"));
//			softwEngComp.setVisibility(createPublicVisibility());
//			softwEngComp = ums.saveResource(softwEngComp, false);
//			
//			CompetenceRequirement softwEngCompRequirement = new CompetenceRequirement();
//			softwEngCompRequirement.setCompetence(softwEngComp);
//			softwEngCompRequirement.setCompetenceLevel(CompetenceLevel.ADVANCED);
//			softwEngCompRequirement.setVisibility(createPublicVisibility());
//			softwEngCompRequirement.setMaker(flexoOrg);
//			softwEngCompRequirement = ums.saveResource(softwEngCompRequirement, false);
//			
//			
//			LearningObjective softwEngObjective = new LearningObjective();
//			softwEngObjective.setTargetCompetence(softwEngComp);
//			//domainModelingObjective.setOrganization(ini);
//			softwEngObjective.setVisibility(createPublicVisibility());
//			softwEngObjective = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(softwEngObjective, false);
//			
//			
//			Activity softwConstrBasicsz = new Activity();
//			softwConstrBasicsz.setTitle("Read about the software design strategies");
//			//softwConstrBasicsz.addContent(softwConstrIntroContentz);
//			//softwConstrBasicsz.addRequiresKA(softwConstrIntroAsset);
//			softwConstrBasicsz.setVisibility(createPublicVisibility());
//			//softwConstrIntroAssetz.addTag(getTag("Design Strategies"));
//			//softwConstrIntroAsset.addTag(getTag("Software quality"));
//			//softwConstrIntroAsset.addTag(getTag("Process analysis"));
//			softwConstrBasicsz = ums.saveResource(softwConstrBasicsz, false);
//			
//			LearningTask softwEngLPx = new LearningTask();
//			softwEngLPx.setTitle("How to Model software");
//			softwEngLPx.addLearningActivity(softwConstrBasicsz);
//			//softwEngLPx.addLearningActivity(softwEngModel);
//			//softwEngLPx.addObjective(softwEngObjective);
//			softwEngLPx.addTag(getTag("Software modelling"));
//			softwEngLPx.setMaker(kennethCarter);
//			softwEngLPx.addTag(getTag("Application frameworks"));
//			softwEngLPx.setVisibility(createPublicVisibility());
//			softwEngLPx = ums.saveResource(softwEngLPx, false);	
//			
//			ReusedLearningTask scrltvd=DomainModelUtil.cloneToReusedLearningTask(softwEngLPx, karenWhite, null);
//			scrltvd.addTag(getTag("Software quality"));
//			scrltvd.addTag(getTag("reliability"));
//			scrltvd.addTag(getTag("computability theory"));
//			scrltvd.addTag(getTag("requirement analysis"));
//			scrltvd.addTag(getTag("Design objectives"));
//			scrltvd.setTitle("Defining user requirements");
//			scrltvd.setVisibility(createPublicVisibility());
//			scrltvd = ums.saveResource(scrltvd, false);
//			
//			setActivitiesInReusedLTAsPublic(scrltvd, karenWhite);
//			
//			TargetCompetence softwConstrCompTCvd = new TargetCompetence();
//			softwConstrCompTCvd.setCompetence(softwEngComp);
//			softwConstrCompTCvd.setCurrentTask(scrltvd);
//			softwConstrCompTCvd.setTargetLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompTCvd.setCurrentLevel(CompetenceLevel.BEGINER);
//			softwConstrCompTCvd.setVisibility(createPublicVisibility());
//			softwConstrCompTCvd.setMaker(karenWhite);
//			softwConstrCompTCvd = ums.saveResource(softwConstrCompTCvd, false);
//			
//			
//			
//			ContentUnit softwEngIntroContent = new ContentUnit();
//			softwEngIntroContent.setTitle("Software engineering for Absolute Beginners");
//			softwEngIntroContent.setHref(URI.create("http://www.slideshare.net/signer/introduction-and-conceptual-modelling"));
//			softwEngIntroContent.setVisibility(createPublicVisibility());
//			softwEngIntroContent.addTag(getTag("Software engineering"));
//			softwEngIntroContent = ums.saveResource(softwEngIntroContent, false);
//			
//			KnowledgeAsset softwEngIntroAsset = new KnowledgeAsset();
//			softwEngIntroAsset.setTitle(softwEngIntroContent.getTitle());
//			softwEngIntroAsset.setReferenceToContent(softwEngIntroContent);
//			softwEngIntroAsset.setVisibility(createPublicVisibility());
//			softwEngIntroAsset = ums.saveResource(softwEngIntroAsset, false);
//			
//			Activity softwEngBasics = new Activity();
//			softwEngBasics.setTitle("Read the introductory content on Software engineering");
//			softwEngBasics.addContent(softwEngIntroContent);
//			softwEngBasics.addRequiresKA(softwEngIntroAsset);
//			softwEngBasics.setVisibility(createPublicVisibility());
//			softwEngBasics.addTag(getTag("Software engineering"));
//			softwEngBasics = ums.saveResource(softwEngBasics, false);
//			
//			softwEngIntroAsset.setActivity(softwEngBasics);
//			softwEngIntroAsset = ums.saveResource(softwEngIntroAsset, false);
//			
//			Activity softwEngModel = new Activity();
//			softwEngModel.setTitle("Develop your own application for a domain of choice");
//			////createDomainModel.setPrecedingActivity(learnModelingBasics);
//			softwEngModel.setVisibility(createPublicVisibility());
//			softwEngModel.addTag(getTag("Programming"));
//			softwEngModel.addTag(getTag("Development"));
//			softwEngModel = ums.saveResource(softwEngModel, false);
//			
//			LearningTask softwEngLP = new LearningTask();
//			softwEngLP.setTitle("How to Model software system");
//			softwEngLP.addLearningActivity(softwEngBasics);
//			softwEngLP.addLearningActivity(softwEngModel);
//			//softwEngLP.addObjective(softwEngObjective);
//			softwEngLP.setMaker(kevinHall);
//			softwEngLP.addTag(getTag("Software modelling"));
//			softwEngLP.addTag(getTag("Application frameworks"));
//			softwEngLP.setVisibility(createPublicVisibility());
//			softwEngLP = ums.saveResource(softwEngLP, false);		
//			//C1.1: Ability to develop complex software systems - ENDS
//			
//			ReusedLearningTask scrltDD=DomainModelUtil.cloneToReusedLearningTask(softwEngLP, georgeYoung, null);
//			scrltDD.addTag(getTag("Software quality"));
//			scrltDD.addTag(getTag("reliability"));
//			scrltDD.addTag(getTag("computability theory"));
//			scrltDD.addTag(getTag("automatic programming"));
//			scrltDD.addTag(getTag("Software requirements"));
//			scrltDD.addTag(getTag("rational approximation"));
//			scrltDD.addTag(getTag("Design Strategies"));
//			scrltDD.setTitle("Developing industrial software");
//			scrltDD.setVisibility(createPublicVisibility());
//			setActivitiesInReusedLTAsPublic(scrltDD, georgeYoung);
//			scrltDD = ums.saveResource(scrltDD, false);
//			
//			TargetCompetence softwConstrCompTCSR = new TargetCompetence();
//			softwConstrCompTCSR.setCompetence(softwEngComp);
//			softwConstrCompTCSR.setCurrentTask(scrltDD);
//			softwConstrCompTCSR.setTargetLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompTCSR.setCurrentLevel(CompetenceLevel.BEGINER);
//			//softwConstrCompTCSR.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCSR.setVisibility(createPublicVisibility());
//			//softwConstrCompTCSR.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCSR.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCSR.setMaker(georgeYoung);
//			softwConstrCompTCSR = ums.saveResource(softwConstrCompTCSR, false);
//			
//			
//			//C1.1: Ability to develop complex software systems - BEGINS
//			Competence softwConstrComp = new  Competence();
//			softwConstrComp.setTitle("Ability to construct industrial software systems");
//			softwConstrComp.setMaker(flexoOrg);
//			softwConstrComp.setVisibility(createPublicVisibility());
//			softwConstrComp.addTag(getTag("Software engineering"));
//			softwConstrComp.addTag(getTag("Software quality"));
//			softwConstrComp = ums.saveResource(softwConstrComp, false);
//			
//			CompetenceRequirement softwConstrCompRequirement = new CompetenceRequirement();
//			softwConstrCompRequirement.setCompetence(softwConstrComp);
//			softwConstrCompRequirement.setCompetenceLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompRequirement.setVisibility(createPublicVisibility());
//			softwConstrCompRequirement.setMaker(flexoOrg);
//			softwConstrCompRequirement = ums.saveResource(softwConstrCompRequirement, false);
//			
//			
//			LearningObjective softwConstrObjective = new LearningObjective();
//			softwConstrObjective.setTargetCompetence(softwConstrComp);
//			//domainModelingObjective.setOrganization(ini);
//			softwConstrObjective.setVisibility(createPublicVisibility());
//			softwConstrObjective = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(softwConstrObjective, false);
//			
//			ContentUnit softwConstrIntroContent = new ContentUnit() ;
//			softwConstrIntroContent.setTitle("Software high-level design");
//			softwConstrIntroContent.setHref(URI.create("http://www.slideshare.net/signer/introduction-and-conceptual-modelling"));
//			softwConstrIntroContent.setVisibility(createPublicVisibility());
//			softwConstrIntroContent.addTag(getTag("Software modelling"));
//			softwConstrIntroContent.addTag(getTag("Component composition"));
//			softwConstrIntroContent.addTag(getTag("Formal specification"));
//			softwConstrIntroContent = ums.saveResource(softwConstrIntroContent, false);
//			
//			KnowledgeAsset softwConstrIntroAsset = new KnowledgeAsset();
//			softwConstrIntroAsset.setTitle(softwConstrIntroContent.getTitle());
//			softwConstrIntroAsset.setReferenceToContent(softwConstrIntroContent);
//			softwConstrIntroAsset.addTag(getTag("Software modelling"));
//			softwConstrIntroAsset.setVisibility(createPublicVisibility());
//			softwConstrIntroAsset = ums.saveResource(softwConstrIntroAsset, false);
//			
//			Activity softwConstrBasics = new Activity();
//			softwConstrBasics.setTitle("Read about the software design strategies");
//			softwConstrBasics.addContent(softwConstrIntroContent);
//			softwConstrBasics.addRequiresKA(softwConstrIntroAsset);
//			softwConstrBasics.setVisibility(createPublicVisibility());
//			softwConstrBasics.addTag(getTag("Design Strategies"));
//			softwConstrBasics.addTag(getTag("Software quality"));
//			softwConstrBasics.addTag(getTag("Process analysis"));
//			softwConstrBasics = ums.saveResource(softwConstrBasics, false);
//			
//			softwConstrIntroAsset.setActivity(softwConstrBasics);
//			softwConstrIntroAsset = ums.saveResource(softwConstrIntroAsset, false);
//			
//			Activity softwConstrModel = new Activity();
//			softwConstrModel.setTitle("Develop design documentation: case study");
//			////createDomainModel.setPrecedingActivity(learnModelingBasics);
//			softwConstrModel.setVisibility(createPublicVisibility());
//			softwConstrModel.addTag(getTag("Version management"));
//			softwConstrModel.addTag(getTag("Design Strategies"));
//			softwConstrModel.addTag(getTag("Software quality"));
//			softwConstrModel.addTag(getTag("Formal specification"));
//			softwConstrModel = ums.saveResource(softwConstrModel, false);
//			
//			LearningTask softwConstrLP = new LearningTask();
//			softwConstrLP.setTitle("How to interpret software requirements");
//			softwConstrLP.addLearningActivity(softwConstrBasics);
//			softwConstrLP.addLearningActivity(softwConstrModel);
//			softwConstrLP.setMaker(paulEdwards);
//			//softwConstrLP.addObjective(softwConstrObjective);
//			softwConstrLP.setVisibility(createPublicVisibility());
//			softwConstrLP.addTag(getTag("Software quality"));
//			softwConstrLP.addTag(getTag("Software requirements"));
//			softwConstrLP.addTag(getTag("Design Strategies"));
//			softwConstrLP = ums.saveResource(softwConstrLP, false);		
//			
//			ReusedLearningTask scrltDD2=DomainModelUtil.cloneToReusedLearningTask(softwEngLP, helenCampbell, null);
//			scrltDD2.addTag(getTag("Software quality"));
//			scrltDD2.addTag(getTag("reliability"));
//			scrltDD2.addTag(getTag("computability theory"));
//			scrltDD2.addTag(getTag("Design Strategies"));
//			scrltDD2.setTitle("Developing domain specific applications");
//			scrltDD2.setVisibility(createPublicVisibility());
//			scrltDD2 = ums.saveResource(scrltDD2, false);
//			
//			setActivitiesInReusedLTAsPublic(scrltDD2, helenCampbell);
//			
//			TargetCompetence softwConstrCompTCDD = new TargetCompetence();
//			softwConstrCompTCDD.setCompetence(softwConstrComp);
//			softwConstrCompTCDD.setCurrentTask(scrltDD2);
//			softwConstrCompTCDD.setTargetLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompTCDD.setCurrentLevel(CompetenceLevel.BEGINER);
//			//softwConstrCompTCDD.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCDD.setVisibility(createPublicVisibility());
//			//softwConstrCompTCDD.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCDD.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCDD.setMaker(helenCampbell);
//			softwConstrCompTCDD = ums.saveResource(softwConstrCompTCDD, false);
//			
//			ReusedLearningTask scrltVDx=DomainModelUtil.cloneToReusedLearningTask(softwEngLP, georgeYoung, null);
//			scrltVDx.addTag(getTag("Software quality"));
//			scrltVDx.addTag(getTag("reliability"));
//			scrltVDx.addTag(getTag("Software requirements"));
//			scrltVDx.addTag(getTag("Design Strategies"));
//			scrltVDx.setTitle("Specifying software requirements");
//			scrltVDx.setVisibility(createPublicVisibility());
//			scrltVDx = ums.saveResource(scrltVDx, false);
//			
//			setActivitiesInReusedLTAsPublic(scrltVDx, georgeYoung);
//			
//			TargetCompetence softwConstrCompTCVD = new TargetCompetence();
//			softwConstrCompTCVD.setCompetence(softwConstrComp);
//			softwConstrCompTCVD.setCurrentTask(scrltVDx);
//			softwConstrCompTCVD.setTargetLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompTCVD.setCurrentLevel(CompetenceLevel.BEGINER);
//			//softwConstrCompTCVD.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCVD.setVisibility(createPublicVisibility());
//			//softwConstrCompTCVD.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCVD.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCVD.setMaker(georgeYoung);
//			softwConstrCompTCVD = ums.saveResource(softwConstrCompTCVD, false);
//			
//			Activity softwConstrBasics2 = new Activity();
//			softwConstrBasics2.setTitle("Software design strategies - introduction");
//			softwConstrBasics2.addContent(softwConstrIntroContent);
//			softwConstrBasics2.addRequiresKA(softwConstrIntroAsset);
//			softwConstrBasics2.setVisibility(createPublicVisibility());
//			softwConstrBasics2.addTag(getTag("Design Quality"));
//			softwConstrBasics2.addTag(getTag("Software quality"));
//			softwConstrBasics2.addTag(getTag("Process analysis"));
//			softwConstrBasics2 = ums.saveResource(softwConstrBasics2, false);
//			
//			softwConstrIntroAsset.setActivity(softwConstrBasics);
//			softwConstrIntroAsset = ums.saveResource(softwConstrIntroAsset, false);
//			
//			Activity softwConstrModel2 = new Activity();
//			softwConstrModel2.setTitle("Develop design documentation: practice");
//			////createDomainModel.setPrecedingActivity(learnModelingBasics);
//			softwConstrModel2.setVisibility(createPublicVisibility());
//			softwConstrModel2.addTag(getTag("Version control"));
//			softwConstrModel2.addTag(getTag("Design Strategies"));
//			softwConstrModel2.addTag(getTag("Requirements specification"));
//			softwConstrModel2.addTag(getTag("Formal specification"));
//			
//			softwConstrModel2 = ums.saveResource(softwConstrModel2, false);
//			
//			
//			LearningTask softwConstrLP2 = new LearningTask();
//			softwConstrLP2.setTitle("How to model user requirements");
//			softwConstrLP2.addLearningActivity(softwConstrBasics2);
//			softwConstrLP2.addLearningActivity(softwConstrModel2);
//			//softwConstrLP2.addObjective(softwConstrObjective);
//			softwConstrLP2.setMaker(anthonyMoore);
//			softwConstrLP2.setVisibility(createPublicVisibility());
//			softwConstrLP2.addTag(getTag("Software quality"));
//			softwConstrLP2.addTag(getTag("Software requirements"));
//			softwConstrLP2.addTag(getTag("Design Strategies"));
//			softwConstrLP2 = ums.saveResource(softwConstrLP2, false);	
//			
//			ReusedLearningTask scrltDDj2=DomainModelUtil.cloneToReusedLearningTask(softwConstrLP, karenWhite, null);
//			scrltDDj2.addTag(getTag("Software quality"));
//			scrltDDj2.addTag(getTag("reliability"));
//			scrltDDj2.addTag(getTag("computability theory"));
//			scrltDDj2.addTag(getTag("automatic programming"));
//			scrltDDj2.addTag(getTag("Software requirements"));
//			scrltDDj2.addTag(getTag("rational approximation"));
//			scrltDDj2.addTag(getTag("Design Strategies"));
//			scrltDDj2.setTitle("Most common design strategies");
//			scrltDDj2.setVisibility(createPublicVisibility());
//			scrltDDj2 = ums.saveResource(scrltDDj2, false);
//			
//			setActivitiesInReusedLTAsPublic(scrltDDj2, karenWhite);
//			
//			TargetCompetence softwConstrCompTCDD2 = new TargetCompetence();
//			softwConstrCompTCDD2.setCompetence(softwConstrComp);
//			softwConstrCompTCDD.setCurrentTask(scrltDDj2);
//			softwConstrCompTCDD2.setTargetLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompTCDD2.setCurrentLevel(CompetenceLevel.BEGINER);
//			//softwConstrCompTCDD.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCDD2.setVisibility(createPublicVisibility());
//			//softwConstrCompTCDD.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCDD.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCDD2.setMaker(karenWhite);
//			softwConstrCompTCDD2 = ums.saveResource(softwConstrCompTCDD2, false);
//			
//			
//			ReusedLearningTask scrltDD3=DomainModelUtil.cloneToReusedLearningTask(softwConstrLP, anthonyMoore, null);
//			scrltDD3.addTag(getTag("Software quality"));
//			scrltDD3.addTag(getTag("reliability"));
//			scrltDD3.addTag(getTag("computability theory"));
//			scrltDD3.addTag(getTag("automatic programming"));
//			scrltDD3.addTag(getTag("Software requirements"));
//			scrltDD3.addTag(getTag("rational approximation"));
//			scrltDD3.addTag(getTag("Design objectives"));
//			scrltDD3.setTitle("Defining user requirements");
//			scrltDD3.setVisibility(createPublicVisibility());
//			scrltDD3 = ums.saveResource(scrltDD3, false);
//			
//			setActivitiesInReusedLTAsPublic(scrltDD3, anthonyMoore);
//			
//			TargetCompetence softwConstrCompTCDD3 = new TargetCompetence();
//			softwConstrCompTCDD2.setCompetence(softwConstrComp);
//			softwConstrCompTCDD.setCurrentTask(scrltDD3);
//			softwConstrCompTCDD3.setTargetLevel(CompetenceLevel.ADVANCED);
//			softwConstrCompTCDD3.setCurrentLevel(CompetenceLevel.BEGINER);
//			//softwConstrCompTCDD.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCDD3.setVisibility(createPublicVisibility());
//			//softwConstrCompTCDD.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCDD.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCDD3.setMaker(anthonyMoore);
//			softwConstrCompTCDD3 = ums.saveResource(softwConstrCompTCDD3, false);
//			//C1.1: Ability to develop complex software systems - ENDS
//			
//			//C2: Ability to formalize domain model using OWL ontology language - BEGINS
//			Competence owlModelingComp = new Competence();
//			owlModelingComp.setTitle("Ability to formalize domain model using OWL ontology language");
//			////owlModelingComp.addRequiredCompetence(domainModelingComp);
//			owlModelingComp.setMaker(fos);
//			owlModelingComp.addTag(getTag("Linked Data"));
//			owlModelingComp.addTag(getTag("Semantic Web"));
//			owlModelingComp.addTag(getTag("OWL"));
//			owlModelingComp.addTag(getTag("RDF"));
//			owlModelingComp.addTag(getTag("ontology"));
//			owlModelingComp.setVisibility(createPublicVisibility());
//			owlModelingComp = ums.saveResource(owlModelingComp, false);
//			
//			CompetenceRequirement owlModelingCompRequirement = new CompetenceRequirement();
//			owlModelingCompRequirement.setCompetence(owlModelingComp);
//			owlModelingCompRequirement.setCompetenceLevel(CompetenceLevel.ADVANCED);
//			owlModelingCompRequirement.setVisibility(createPublicVisibility());
//			owlModelingCompRequirement.setMaker(fos);
//			owlModelingCompRequirement = ums.saveResource(owlModelingCompRequirement, false);
//			
//			LearningObjective owlModelingObjective = new LearningObjective();
//			owlModelingObjective.setTargetCompetence(owlModelingComp);
//			//owlModelingObjective.setOrganization(ini);
//			owlModelingObjective.setVisibility(createPublicVisibility());
//			owlModelingObjective = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(owlModelingObjective, false);
//			
//			//LP2.1: OWL Learning Path for Absolute Beginners - BEGINS
//			ContentUnit semwebIntroVideo = new ContentUnit();
//			semwebIntroVideo.setTitle("Video: A short Tutorial on Semantic Web");
//			semwebIntroVideo.setHref(URI.create("http://videolectures.net/training06_sure_stsw/"));
//			 
//			semwebIntroVideo.addTag(getTag("Semantic Web"));
//			semwebIntroVideo.addTag(getTag("ontology engineering"));
//			semwebIntroVideo.addAnnotation(createComment("very useful for getting an overall understanding of the SemWeb technology", paulEdwards));
//			////semwebIntroVideo.addUserRating(rating5);
//			semwebIntroVideo.setVisibility(createPublicVisibility());
//			semwebIntroVideo = ums.saveResource(semwebIntroVideo, false);
//			
//			KnowledgeAsset semwebIntroVideoAsset = new KnowledgeAsset();
//			semwebIntroVideoAsset.setTitle(semwebIntroVideo.getTitle());
//			semwebIntroVideoAsset.setReferenceToContent(semwebIntroVideo);
//			semwebIntroVideoAsset.addTag(getTag("ontology"));
//			semwebIntroVideoAsset.setVisibility(createPublicVisibility());
//			semwebIntroVideoAsset = ums.saveResource(semwebIntroVideoAsset, false);
//			
//			ContentUnit semwebIntro = new ContentUnit();
//			semwebIntro.setTitle("Semantic Web - Introduction and Overview");
//			semwebIntro.setHref(URI.create("http://dret.net/netdret/docs/wilde-einiras2005-semweb/"));
//			addRandomTag(semwebIntro,3);
//			semwebIntro.addAnnotation(createComment("not very useful; hard to follow", stevenTurner));
//			semwebIntro.setVisibility(createPublicVisibility());
//			semwebIntro.addTag(getTag("Semantic Web"));
//			semwebIntro = ums.saveResource(semwebIntro, false);
//			
//			KnowledgeAsset semwebIntroAsset = new KnowledgeAsset();
//			semwebIntroAsset.setTitle(semwebIntro.getTitle());
//			semwebIntroAsset.setReferenceToContent(semwebIntro);
//			semwebIntroAsset.addTag(getTag("Semantic Web"));
//			semwebIntroAsset.setVisibility(createPublicVisibility());
//			semwebIntroAsset = ums.saveResource(semwebIntroAsset, false);
//			
//			Activity learnSemWebBasics = new Activity();
//			learnSemWebBasics.setTitle("Learn the basics of the Semantic Web technologies");
//			addRandomTag(learnSemWebBasics,4);
//			learnSemWebBasics.addContent(semwebIntroVideo);
//			learnSemWebBasics.addRequiresKA(semwebIntroVideoAsset);
//			learnSemWebBasics.addContent(semwebIntro);
//			learnSemWebBasics.addRequiresKA(semwebIntroAsset);
//			learnSemWebBasics.addTag(getTag("OWL"));
//			learnSemWebBasics.addTag(getTag("ontology"));
//			learnSemWebBasics.addTag(getTag("Linked Data"));
//			learnSemWebBasics.addTag(getTag("RDF"));
//			learnSemWebBasics.setVisibility(createPublicVisibility());
//			learnSemWebBasics = ums.saveResource(learnSemWebBasics, false);
//			
//			semwebIntroAsset.setActivity(learnSemWebBasics);
//			semwebIntroAsset = ums.saveResource(semwebIntroAsset, false);
//			
//			ContentUnit owlIntroVideo = new ContentUnit();
//			owlIntroVideo.setTitle("Video: An Introduction to OWL");
//			owlIntroVideo.setHref(URI.create("http://videolectures.net/training06_sure_stsw/"));
//			owlIntroVideo.addTag(getTag("OWL"));
//			addRandomTag(owlIntroVideo,4);
//			owlIntroVideo.addAnnotation(createComment("very interesting and useful - recommend", karenWhite));
//			////owlIntroVideo.addUserRating(rating5);
//			owlIntroVideo.setVisibility(createPublicVisibility());
//			owlIntroVideo = ums.saveResource(owlIntroVideo, false);
//			
//			KnowledgeAsset owlIntroVideoAsset = new KnowledgeAsset();
//			owlIntroVideoAsset.setTitle(owlIntroVideo.getTitle());
//			owlIntroVideoAsset.setReferenceToContent(owlIntroVideo);
//			owlIntroVideoAsset.addTag(getTag("OWL"));
//			owlIntroVideoAsset.setVisibility(createPublicVisibility());
//			owlIntroVideoAsset = ums.saveResource(owlIntroVideoAsset, false);
//			
//			ContentUnit owlPrimer = new ContentUnit();
//			owlPrimer.setTitle("W3C OWL Primer");
//			owlPrimer.setDescription("This primer provides an approachable introduction to OWL 2, including " +
//					"orientation for those coming from other disciplines, a running example showing how OWL 2 " +
//					"can be used to represent first simple information and then more complex information, how " +
//					"OWL 2 manages ontologies, and finally the distinctions between the various sublanguages " +
//					"of OWL 2.");
//			owlPrimer.setHref(URI.create("http://www.w3.org/TR/owl2-primer/"));
//			owlPrimer.addTag(getTag("OWL"));
//			owlPrimer.setDescription("the official, W3C OWL tutorial");
//			owlPrimer.setVisibility(createPublicVisibility());
//			owlPrimer = ums.saveResource(owlPrimer, false);
//			
//			KnowledgeAsset owlPrimerAsset = new KnowledgeAsset();
//			owlPrimerAsset.setTitle(owlPrimer.getTitle());
//			owlPrimerAsset.setReferenceToContent(owlPrimer);
//			owlPrimerAsset.addTag(getTag("OWL"));
//			owlPrimerAsset.addTag(getTag("RDF"));
//			owlPrimerAsset.setVisibility(createPublicVisibility());
//			owlPrimerAsset = ums.saveResource(owlPrimerAsset, false);
//			
//			Activity takeOWLTutorial = new Activity();
//			takeOWLTutorial.setTitle("Take an OWL tutorial");
//			takeOWLTutorial.addTag(getTag("OWL"));
//			addRandomTag(takeOWLTutorial,3);
//			 
//			////takeOWLTutorial.addUserRating(rating4);
//			////takeOWLTutorial.setPrecedingActivity(learnSemWebBasics);
//			takeOWLTutorial.addContent(owlIntroVideo);
//			takeOWLTutorial.addRequiresKA(owlPrimerAsset);
//			takeOWLTutorial.addContent(owlPrimer);
//			takeOWLTutorial.addRequiresKA(owlPrimerAsset);
//			takeOWLTutorial.setVisibility(createPublicVisibility());
//			takeOWLTutorial = ums.saveResource(takeOWLTutorial, false);
//			
//			owlPrimerAsset.setActivity(takeOWLTutorial);
//			owlPrimerAsset = ums.saveResource(owlPrimerAsset, false);
//			
//			owlPrimerAsset.setActivity(takeOWLTutorial);
//			owlPrimerAsset = ums.saveResource(owlPrimerAsset, false);
//			
//			ContentUnit owlExamples = new ContentUnit();
//			owlExamples.setTitle("OWL Reasoning Examples and Hands-On Session");
//			owlExamples.setHref(URI.create("http://owl.cs.manchester.ac.uk/2009/07/sssw/"));
//			addRandomTag(owlExamples,2);
//			owlExamples.setVisibility(createPublicVisibility());
//			owlExamples.addTag(getTag("Reasoning"));
//			owlExamples = ums.saveResource(owlExamples, false);
//			
//			KnowledgeAsset owlExamplesAsset = new KnowledgeAsset();
//			owlExamplesAsset.setTitle(owlExamples.getTitle());
//			owlExamplesAsset.setReferenceToContent(owlExamples);
//			owlExamplesAsset.addTag(getTag("Reasoning"));
//			owlExamplesAsset = ums.saveResource(owlExamplesAsset, false);
//			
//			Activity practiceOWL = new Activity();
//			practiceOWL.setTitle("Practice OWL through examples");
//			practiceOWL.addContent(owlExamples);
//			practiceOWL.addRequiresKA(owlExamplesAsset);
//			practiceOWL.addTag(getTag("ontology"));
//			practiceOWL.addTag(getTag("OWL"));
//			////practiceOWL.setPrecedingActivity(takeOWLTutorial);
//			practiceOWL.setVisibility(createPublicVisibility());
//			practiceOWL = ums.saveResource(practiceOWL, false);
//			
//			owlExamplesAsset.setActivity(practiceOWL);
//			owlExamplesAsset = ums.saveResource(owlExamplesAsset, false);
//			
//			LearningTask owlINILP = new LearningTask();
//			owlINILP.setTitle("OWL for Absolute Beginners");
//			owlINILP.addLearningActivity(learnSemWebBasics);
//			owlINILP.addLearningActivity(takeOWLTutorial);
//			owlINILP.addLearningActivity(practiceOWL);
//			owlINILP.setUserOrganization(flexoOrg);
//			owlINILP.setMaker(helenCampbell);
////			owlINILP.addObjective(owlModelingObjective);
//			owlINILP.addTag(getTag("OWL"));
//			owlINILP.addTag(getTag("Semantic Web"));
//			owlINILP.setVisibility(createPublicVisibility());
//			owlINILP = ums.saveResource(owlINILP, false);
//			//LP2.1: OWL Learning Path for Absolute Beginners - ENDS
//			
//			//LP2.2: Linked Data Learning Path: Learning OWL in Linked Data Way - BEGINS
//			
//			ContentUnit linkedDataIntroVideo = new ContentUnit();
//			linkedDataIntroVideo.setTitle("Video: An Introduction to Linked Data");
//			linkedDataIntroVideo.setDescription("This talk will cover the basic concepts and techniques " +
//					"of publishing and using Linked Data, assuming some familiarity with programming and " +
//					"the Web. No prior knowledge of Semantic Web technologies is required.");
//			linkedDataIntroVideo.setHref(URI.create("http://vimeo.com/12444260"));
//			linkedDataIntroVideo.addTag(getTag("Linked Data"));
//			linkedDataIntroVideo.setVisibility(createPublicVisibility());
//			linkedDataIntroVideo = ums.saveResource(linkedDataIntroVideo, false);
//			
//			KnowledgeAsset linkedDataIntroVideoAsset = new KnowledgeAsset();
//			linkedDataIntroVideoAsset.setTitle(linkedDataIntroVideo.getTitle());
//			linkedDataIntroVideoAsset.setReferenceToContent(linkedDataIntroVideo);
//			linkedDataIntroVideoAsset.addTag(getTag("Linked Data"));
//			linkedDataIntroVideoAsset.setVisibility(createPublicVisibility());
//			linkedDataIntroVideoAsset = ums.saveResource(linkedDataIntroVideoAsset, false);
//			
//			ContentUnit linkedDataSlides = new ContentUnit();
//			linkedDataSlides.setTitle("Slides: Hello Open Data World");
//			linkedDataSlides.setHref(URI.create("http://www.slideshare.net/terraces/hello-open-world-semtech-2009"));
//			linkedDataSlides.addTag(getTag("Linked Data"));
//			linkedDataSlides.addTag(getTag("Semantic Web"));
//			linkedDataSlides.addAnnotation(createComment("Good set of slides - amusing and useful", georgeYoung));
//			////linkedDataSlides.addUserRating(rating5);
//			linkedDataSlides.setVisibility(createPublicVisibility());
//			linkedDataSlides = ums.saveResource(linkedDataSlides, false);
//			
//			KnowledgeAsset linkedDataSlidesAsset = new KnowledgeAsset();
//			linkedDataSlidesAsset.setTitle(linkedDataSlides.getTitle());
//			linkedDataSlidesAsset.setReferenceToContent(linkedDataSlides);
//			linkedDataSlidesAsset.addTag(getTag("Linked Data"));
//			linkedDataSlidesAsset.setVisibility(createPublicVisibility());
//			linkedDataSlidesAsset = ums.saveResource(linkedDataSlidesAsset, false);
//			
//			Activity learnLinkedDataBasics = new Activity();
//			learnLinkedDataBasics.setTitle("Familiarize yourself with Linked Data concepts and principles");
//			learnLinkedDataBasics.addContent(linkedDataIntroVideo);
//			learnLinkedDataBasics.addRequiresKA(linkedDataIntroVideoAsset);
//			learnLinkedDataBasics.addContent(linkedDataSlides);
//			learnLinkedDataBasics.addTag(getTag("Linked Data"));
//			learnLinkedDataBasics.addRequiresKA(linkedDataSlidesAsset);
//			learnLinkedDataBasics.setVisibility(createPublicVisibility());
//			learnLinkedDataBasics = ums.saveResource(learnLinkedDataBasics, false);
//			
//			linkedDataIntroVideoAsset.setActivity(learnLinkedDataBasics);
//			linkedDataIntroVideoAsset = ums.saveResource(linkedDataIntroVideoAsset, false);
//			
//			linkedDataSlidesAsset.setActivity(learnLinkedDataBasics);
//			linkedDataSlidesAsset = ums.saveResource(linkedDataSlidesAsset, false);
//			
//			Activity takeOWLTutorial2 = new Activity();
//			takeOWLTutorial2.setTitle("Take an OWL tutorial");
//			////takeOWLTutorial2.setPrecedingActivity(learnLinkedDataBasics);
//			takeOWLTutorial2.addContent(owlIntroVideo);
//			takeOWLTutorial2.addRequiresKA(owlIntroVideoAsset);
//			takeOWLTutorial2.addContent(owlPrimer);
//			takeOWLTutorial2.addRequiresKA(owlPrimerAsset);
//			takeOWLTutorial2.addTag(getTag("OWL"));
//			takeOWLTutorial2.setVisibility(createPublicVisibility());
//			takeOWLTutorial2 = ums.saveResource(takeOWLTutorial2, false);
//			
//			ContentUnit ontologyReuseVideo = new ContentUnit();
//			ontologyReuseVideo.setTitle("Video: Learning from the Masters: Understanding Ontologies found on the Web ");
//			ontologyReuseVideo.setHref(URI.create("http://videolectures.net/iswc06_parsia_uofw/"));
//			ontologyReuseVideo.addTag(getTag("OWL"));
//			ontologyReuseVideo.addTag(getTag("ontology"));
//			ontologyReuseVideo.addAnnotation(createComment("difficult to follow, requires good understanding of owl and ontology development in general", kevinHall));
//			////ontologyReuseVideo.addUserRating(rating3);
//			ontologyReuseVideo.setVisibility(createPublicVisibility());
//			ontologyReuseVideo = ums.saveResource(ontologyReuseVideo, false);
//			
//			KnowledgeAsset ontologyReuseVideoAsset = new KnowledgeAsset();
//			ontologyReuseVideoAsset.setTitle(ontologyReuseVideo.getTitle());
//			ontologyReuseVideoAsset.setReferenceToContent(ontologyReuseVideo);
//			ontologyReuseVideoAsset.addTag(getTag("ontology"));
//			ontologyReuseVideoAsset = ums.saveResource(ontologyReuseVideoAsset, false);
//			
//			Activity learnToReuseOntologies = new Activity();
//			learnToReuseOntologies.setTitle("Learn how to make use of the existing ontologies on the Web");
//			learnToReuseOntologies.addContent(ontologyReuseVideo);
//			learnToReuseOntologies.addRequiresKA(ontologyReuseVideoAsset);
//			learnToReuseOntologies.addTag(getTag("ontology"));
//			////learnToReuseOntologies.setPrecedingActivity(takeOWLTutorial2);
//			learnToReuseOntologies.setVisibility(createPublicVisibility());
//			learnToReuseOntologies = ums.saveResource(learnToReuseOntologies, false);
//			
//			ontologyReuseVideoAsset.setActivity(learnToReuseOntologies);
//			ontologyReuseVideoAsset = ums.saveResource(ontologyReuseVideoAsset, false);
//			
//			LearningTask linkedDataLP = new LearningTask();
//			linkedDataLP.setTitle("Learning OWL in Linked Data Way");
//			linkedDataLP.addLearningActivity(learnLinkedDataBasics);
//			linkedDataLP.addLearningActivity(takeOWLTutorial2);
//			linkedDataLP.addLearningActivity(learnToReuseOntologies);
//			linkedDataLP.setUserOrganization(fos);
//			linkedDataLP.setMaker(kennethCarter);
//			linkedDataLP.addTag(getTag("ontology"));
//			linkedDataLP.addTag(getTag("Linked Data"));
//			linkedDataLP.addTag(getTag("Semantic Web"));
////			linkedDataLP.addObjective(owlModelingObjective);
//			linkedDataLP.setVisibility(createPublicVisibility());
//			linkedDataLP = ums.saveResource(linkedDataLP, false);
//			
//			//LP2.2: Linked Data Learning Path: Learning OWL in Linked Data Way - ENDS
//			
//			//LP2.3: Personal Learning Path: My Path Towards OWL Mastery - BEGINS
//						
//			ContentUnit linkedData4Dummies = new ContentUnit();
//			linkedData4Dummies.setTitle("Linked Data in Plain English");
//			linkedData4Dummies.setDescription("Tim Berners Lee illustrates Linked Data through an analogy " +
//					"with a bag of potato chips");
//			linkedData4Dummies.setHref(URI.create("http://www.w3.org/QA/2010/05/linked_data_its_is_not_like_th.html"));
//			addRandomTag(linkedData4Dummies,3);
//			linkedData4Dummies.setVisibility(createPublicVisibility());
//			linkedData4Dummies.addTag(getTag("Linked Data"));
//			linkedData4Dummies = ums.saveResource(linkedData4Dummies, false);
//			
//			KnowledgeAsset linkedData4DummiesAsset = new KnowledgeAsset();
//			linkedData4DummiesAsset.setTitle(linkedData4Dummies.getTitle());
//			linkedData4DummiesAsset.setReferenceToContent(linkedData4Dummies);
//			linkedData4DummiesAsset.addTag(getTag("Linked Data"));
//			linkedData4DummiesAsset.setVisibility(createPublicVisibility());
//			linkedData4DummiesAsset = ums.saveResource(linkedData4DummiesAsset, false);
//			
//			Activity learnLinkedDataBasics2 = new Activity();
//			learnLinkedDataBasics2.setTitle("Familiarize yourself with Linked Data concepts and principles");
//			learnLinkedDataBasics2.addContent(linkedDataSlides);
//			learnLinkedDataBasics2.addRequiresKA(linkedDataSlidesAsset);
//			learnLinkedDataBasics2.addContent(linkedData4Dummies);
//			learnLinkedDataBasics2.addRequiresKA(linkedData4DummiesAsset);
//			learnLinkedDataBasics2.addTag(getTag("Linked Data"));
//			learnLinkedDataBasics2.addTag(getTag("Semantic Web"));
//			learnLinkedDataBasics2.setVisibility(createPublicVisibility());
//			learnLinkedDataBasics2 = ums.saveResource(learnLinkedDataBasics2, false);
//			
//			linkedData4DummiesAsset.setActivity(learnLinkedDataBasics2);
//			linkedData4DummiesAsset = ums.saveResource(linkedData4DummiesAsset, false);
//			
//			ContentUnit owlVideo = new ContentUnit();
//			owlVideo.setTitle("Video: OWL");
//			owlVideo.setHref(URI.create("http://videolectures.net/koml04_harmelen_o/"));
//			owlVideo.addTag(getTag("OWL"));
//			owlVideo.setVisibility(createPublicVisibility());
//			owlVideo = ums.saveResource(owlVideo, false);
//			
//			KnowledgeAsset owlVideoAsset = new KnowledgeAsset();
//			owlVideoAsset.setTitle(owlVideo.getTitle());
//			owlVideoAsset.setReferenceToContent(owlVideo);
//			owlVideoAsset.addTag(getTag("OWL"));
//			owlVideoAsset.setVisibility(createPublicVisibility());
//			owlVideoAsset = ums.saveResource(owlVideoAsset, false);
//						
//			Activity learnOWLBasics = new Activity();
//			learnOWLBasics.setTitle("Learn the basics of OWL language");
//			learnOWLBasics.addContent(owlVideo);
//			learnOWLBasics.addRequiresKA(owlVideoAsset);
//			learnOWLBasics.addContent(owlPrimer);
//			learnOWLBasics.addRequiresKA(owlPrimerAsset);
//			learnOWLBasics.addTag(getTag("OWL"));
//			learnOWLBasics.addTag(getTag("RDF"));
//			learnOWLBasics.addTag(getTag("ontology"));
//			////learnOWLBasics.setPrecedingActivity(learnLinkedDataBasics2);
//			learnOWLBasics.setVisibility(createPublicVisibility());
//			learnOWLBasics = ums.saveResource(learnOWLBasics, false);
//			
//			owlVideoAsset.setActivity(learnOWLBasics);
//			owlVideoAsset = ums.saveResource(owlVideoAsset, false);
//			
//			ContentUnit ontMethodologiesVideo = new ContentUnit();
//			ontMethodologiesVideo.setTitle("Video: Ontology Engineering Methodologies");
//			ontMethodologiesVideo.setHref(URI.create("http://videolectures.net/iswc07_perez_oem/"));
//			 
//			ontMethodologiesVideo.addTag(getTag("ontology engineering"));
//			////ontMethodologiesVideo.addUserRating(rating4);
//			ontMethodologiesVideo.setVisibility(createPublicVisibility());
//			ontMethodologiesVideo = ums.saveResource(ontMethodologiesVideo, false);
//			
//			KnowledgeAsset ontMethodologiesVideoAsset = new KnowledgeAsset();
//			ontMethodologiesVideoAsset.setTitle(ontMethodologiesVideo.getTitle());
//			ontMethodologiesVideoAsset.setReferenceToContent(ontMethodologiesVideo);
//			ontMethodologiesVideoAsset.addTag(getTag("ontology engineering"));
//			ontMethodologiesVideoAsset.setVisibility(createPublicVisibility());
//			ontMethodologiesVideoAsset = ums.saveResource(ontMethodologiesVideoAsset, false);
//			
//			Activity learnOntEngMethodlogies = new Activity();
//			learnOntEngMethodlogies.setTitle("Learn about ontology engineering methodologies");
//			learnOntEngMethodlogies.addContent(ontMethodologiesVideo);
//			learnOntEngMethodlogies.addTag(getTag("ontology engineering"));
//			learnOntEngMethodlogies.addRequiresKA(ontMethodologiesVideoAsset);
//			////learnOntEngMethodlogies.setPrecedingActivity(learnOWLBasics);
//			learnOntEngMethodlogies.setVisibility(createPublicVisibility());
//			learnOntEngMethodlogies = ums.saveResource(learnOntEngMethodlogies, false);
//			
//			ontMethodologiesVideoAsset.setActivity(learnOntEngMethodlogies);
//			ontMethodologiesVideoAsset = ums.saveResource(ontMethodologiesVideoAsset, false);
//			
//			ContentUnit owlOnSemOverflow = new ContentUnit();
//			owlOnSemOverflow.setTitle("OWL-related discussions on semanticoveflow.com ");
//			owlOnSemOverflow.setHref(URI.create("http://www.semanticoverflow.com/questions/tagged/owl"));
//			owlOnSemOverflow.setVisibility(createPublicVisibility());
//			owlOnSemOverflow.addTag(getTag("Semantic Web"));
//			owlOnSemOverflow.addTag(getTag("OWL"));
//			owlOnSemOverflow = ums.saveResource(owlOnSemOverflow, false);
//			
//			KnowledgeAsset owlOnSemOverflowAsset = new KnowledgeAsset();
//			owlOnSemOverflowAsset.setTitle(owlOnSemOverflow.getTitle());
//			owlOnSemOverflowAsset.setReferenceToContent(owlOnSemOverflow);
//			owlOnSemOverflowAsset = ums.saveResource(owlOnSemOverflowAsset, false);
//			
//			Activity learnFromPractitioners = new Activity();
//			learnFromPractitioners.setTitle("Check the discussion about OWL at practitioners forums");
//			learnFromPractitioners.addContent(owlOnSemOverflow);
//			learnFromPractitioners.addRequiresKA(owlOnSemOverflowAsset);
//			learnFromPractitioners.addTag(getTag("OWL"));
//			////learnFromPractitioners.setPrecedingActivity(learnOntEngMethodlogies);
//			learnFromPractitioners.setVisibility(createPublicVisibility());
//			learnFromPractitioners = ums.saveResource(learnFromPractitioners, false);
//			
//			owlOnSemOverflowAsset.setActivity(learnFromPractitioners);
//			owlOnSemOverflowAsset = ums.saveResource(owlOnSemOverflowAsset, false);
//			
//			LearningTask personalOWLLP = new LearningTask();
//			personalOWLLP.setTitle("My Path Towards OWL Mastery");
//			personalOWLLP.addLearningActivity(learnLinkedDataBasics2);
//			personalOWLLP.addLearningActivity(learnOWLBasics);
//			personalOWLLP.addLearningActivity(learnOntEngMethodlogies);
//			personalOWLLP.addLearningActivity(learnFromPractitioners);
//			personalOWLLP.setUserOrganization(fos);
//			personalOWLLP.addTag(getTag("OWL"));
//			personalOWLLP.setMaker(karenWhite);
//			 
//			personalOWLLP.setVisibility(createPublicVisibility());
//			//personalOWLLP.setMaker(maker);
//			personalOWLLP = ums.saveResource(personalOWLLP, false);
//			
//			ReusedLearningTask reusLearnTaskVD=DomainModelUtil.cloneToReusedLearningTask(softwEngLPx, kennethCarter, null);
//			reusLearnTaskVD.addTag(getTag("OWL"));
//			reusLearnTaskVD.addTag(getTag("Semantic Web education"));
//			reusLearnTaskVD.addTag(getTag("education"));
//			reusLearnTaskVD.setTitle("Semantic Web in education");
//			reusLearnTaskVD.setVisibility(createPublicVisibility());
//			reusLearnTaskVD = ums.saveResource(reusLearnTaskVD, false);
//			
//			setActivitiesInReusedLTAsPublic(reusLearnTaskVD, kennethCarter);
//			
//			TargetCompetence personalOWLLPVlDev = new TargetCompetence();
//			personalOWLLPVlDev.setCompetence(softwEngComp);
//			personalOWLLPVlDev.setCurrentTask(reusLearnTaskVD);
//			personalOWLLPVlDev.setTargetLevel(CompetenceLevel.ADVANCED);
//			personalOWLLPVlDev.setCurrentLevel(CompetenceLevel.BEGINER);
//			personalOWLLPVlDev.setVisibility(createPublicVisibility());
//			personalOWLLPVlDev.setMaker(kennethCarter);
//			personalOWLLPVlDev = ums.saveResource(personalOWLLPVlDev, false);
//			
//			CompetenceRecord personalOWLCRecord = new CompetenceRecord();
//			personalOWLCRecord.setCompetence(owlModelingComp);
//			personalOWLCRecord.setRecordedDate(new Date());
//			personalOWLCRecord.setRecordedDate(setPreviousDate());
//			personalOWLCRecord.setRecordedLevel(CompetenceLevel.INTERMEDIATE);
//			personalOWLCRecord.setVisibility(createPublicVisibility());
//			personalOWLCRecord = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(personalOWLCRecord, false);
//			
//			Date personalOWLTCDeadline = new Date();
//			Calendar personalOWLTCCal = Calendar.getInstance();
//			personalOWLTCCal.setTime(personalOWLTCDeadline);
//			personalOWLTCCal.add(Calendar.DATE, 10);
//			
//			UserProgress personalOWLProgress = ResourceFactory.createNewUserProgress();
//			
//			TargetCompetence personalOWLTC = new TargetCompetence();
//			personalOWLTC.setCurrentTask(personalOWLLP);
//			personalOWLTC.setCompetence(owlModelingComp);
//			personalOWLTC.setTargetLevel(CompetenceLevel.ADVANCED);
//			personalOWLTC.setCurrentLevel(CompetenceLevel.INTERMEDIATE);
//			personalOWLTC.addCompetenceRecord(personalOWLCRecord);
//			personalOWLTC.setVisibility(createPublicVisibility());
//			personalOWLTC.setDeadline(personalOWLTCCal.getTime());
//			personalOWLTC.setProgress(personalOWLProgress);
//			personalOWLTC = ums.saveResource(personalOWLTC, false);
//			
//			//LP2.3: Personal Learning Path: My Path Towards OWL Mastery - ENDS
//	
//			//C2: Ability to formalize domain model using OWL ontology language - ENDS
//			
//			//C3: Ability to use the Protege ontology editor for ontology development - BEGINS
//			Competence protegeComp = new Competence();
//			protegeComp.setTitle("Ability to use the Protege ontology editor for ontology development");
//			protegeComp.addRequiredCompetence(owlModelingComp);
//			protegeComp.addTag(getTag("Protege"));
//			protegeComp.addTag(getTag("ontology engineering"));
//			protegeComp.addTag(getTag("ontology development"));
//			protegeComp.setVisibility(createPublicVisibility());
//			protegeComp = ums.saveResource(protegeComp, false);
//			
//			CompetenceRequirement protegeCompRequirement = new CompetenceRequirement();
//			protegeCompRequirement.setCompetence(protegeComp);
//			protegeCompRequirement.setCompetenceLevel(CompetenceLevel.ADVANCED);
//			protegeCompRequirement.setVisibility(createPublicVisibility());
//			protegeCompRequirement = ums.saveResource(protegeCompRequirement, false);
//			
//			LearningObjective protegeObjective = new LearningObjective();
//			protegeObjective.setTargetCompetence(protegeComp);
//			protegeObjective.setVisibility(createPublicVisibility());
//			//owlModelingObjective.setOrganization(ini);
//			protegeObjective = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(protegeObjective, false);
//			
//			ContentUnit protegeTutorial = new ContentUnit();
//			protegeTutorial.setTitle("Protege OWL tutorial");
//			protegeTutorial.setHref(URI.create("http://owl.cs.manchester.ac.uk/tutorials/protegeowltutorial/"));
//			protegeTutorial.setVisibility(createPublicVisibility());
//			protegeTutorial = ums.saveResource(protegeTutorial, false);
//			
//			KnowledgeAsset protegeTutorialAsset = new KnowledgeAsset();
//			protegeTutorialAsset.setTitle(protegeTutorial.getTitle());
//			protegeTutorialAsset.setReferenceToContent(protegeTutorial);
//			protegeTutorialAsset.addTag(getTag("Protege"));
//			protegeTutorialAsset.setVisibility(createPublicVisibility());
//			protegeTutorialAsset = ums.saveResource(protegeTutorialAsset, false);
//			
//			Activity takeProtegeTutorial = new Activity();
//			takeProtegeTutorial.setTitle("Take a Protege OWL tutorial");
//			takeProtegeTutorial.addContent(protegeTutorial);
//			takeProtegeTutorial.addRequiresKA(protegeTutorialAsset);
//			takeProtegeTutorial.addTag(getTag("Protege"));
//			takeProtegeTutorial.addTag(getTag("OWL"));
//			takeProtegeTutorial.addAnnotation(createComment("Very useful tutorial both for OWL and Protege", karenWhite));
//			////takeProtegeTutorial.addUserRating(rating5);
//			takeProtegeTutorial.setVisibility(createPublicVisibility());
//			takeProtegeTutorial = ums.saveResource(takeProtegeTutorial, false);
//			
//			protegeTutorialAsset.setActivity(takeProtegeTutorial);
//			protegeTutorialAsset = ums.saveResource(protegeTutorialAsset, false);
//			
//			Activity practiceExamples = new Activity();
//			practiceExamples.setTitle("Do some practice through examples");
//			practiceExamples.addAnnotation(createComment("It was not easy to accomplish", anthonyMoore));
//			////practiceExamples.addUserRating(rating2);
//			practiceExamples.addContent(owlExamples);
//			practiceExamples.addRequiresKA(owlExamplesAsset);
//			practiceExamples.addTag(getTag("Protege"));
//			////practiceExamples.setPrecedingActivity(takeProtegeTutorial);
//			practiceExamples.setVisibility(createPublicVisibility());
//			practiceExamples = ums.saveResource(practiceExamples, false);
//			
//			Activity createOntology = new Activity();
//			createOntology.setTitle("Create in Protege your own OWL ontology for the chosen domain");
//			////createOntology.setPrecedingActivity(practiceExamples);
//			createOntology.setVisibility(createPublicVisibility());
//			createOntology.addTag(getTag("Protege"));
//			createOntology.addTag(getTag("OWL"));
//			createOntology.addTag(getTag("ontology"));
//			createOntology = ums.saveResource(createOntology, false);
//			
//			LearningTask protegeLP = new LearningTask();
//			protegeLP.setTitle("Best Practice: Stanford Guide to Protege and OWL");
//			protegeLP.addLearningActivity(takeProtegeTutorial);
//			protegeLP.addLearningActivity(practiceExamples);
//			protegeLP.addLearningActivity(createOntology);
////			protegeLP.addObjective(protegeObjective);
//			protegeLP.addTag(getTag("Protege"));
//			protegeLP.addTag(getTag("OWL"));
//			protegeLP.setMaker(georgeYoung);
//			protegeLP.setVisibility(createPublicVisibility());
//			protegeLP = ums.saveResource(protegeLP, false);	
//			//C3: Ability to use the Protege ontology editor for ontology development - ENDS
//
//			Duty ontDevDuty = new Duty();
//			ontDevDuty.setTitle("Ontology Development");
//			ontDevDuty.addRequiredCompetence(domModelingCompRequirement);
//			ontDevDuty.addRequiredCompetence(owlModelingCompRequirement);
//			ontDevDuty.addTag(getTag("ontology development"));
//			ontDevDuty.addTag(getTag("ontology engineering"));
//			ontDevDuty.addRequiredCompetence(protegeCompRequirement);
//			ontDevDuty.setVisibility(createPublicVisibility());
//			ontDevDuty = ums.saveResource(ontDevDuty, false);
//			
//			iniSeniorProgrammerOrgPosition.addAssignedDuty(ontDevDuty);
//			iniSeniorProgrammerOrgPosition = ums.saveResource(iniSeniorProgrammerOrgPosition, false);
//			
//			fosSeniorProgrammerOrgPosition.addAssignedDuty(ontDevDuty);
//			fosSeniorProgrammerOrgPosition = ums.saveResource(fosSeniorProgrammerOrgPosition, false);
//			
//			fosTeachingAssistantPosition.addAssignedDuty(ontDevDuty);
//			fosTeachingAssistantPosition = ums.saveResource(fosTeachingAssistantPosition, false);
//			
//			Duty softwEngDevDuty = new Duty();
//			softwEngDevDuty.setTitle("Software Systems Development");
//			softwEngDevDuty.addRequiredCompetence(softwEngCompRequirement);
//			softwEngDevDuty.addRequiredCompetence(softwConstrCompRequirement);
//			 
//			softwEngDevDuty = ums.saveResource(softwEngDevDuty, false);
//			
//			iniSeniorProgrammerOrgPosition.addAssignedDuty(softwEngDevDuty);
//			iniSeniorProgrammerOrgPosition = ums.saveResource(iniSeniorProgrammerOrgPosition, false);
//			
//			fosSeniorProgrammerOrgPosition.addAssignedDuty(softwEngDevDuty);
//			fosSeniorProgrammerOrgPosition = ums.saveResource(fosSeniorProgrammerOrgPosition, false);
//			
//			fosTeachingAssistantPosition.addAssignedDuty(softwEngDevDuty);
//			fosTeachingAssistantPosition = ums.saveResource(fosTeachingAssistantPosition, false);
//			
//			for(User us:allUsers){
//				//User us=zoranJeremic;
//				SocialStream sStream=new SocialStream();
//				sStream = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(sStream, false);
//	 			us.setSocialStream(sStream);
//	 			us = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(us,false);
//	  			// addRandomFollowedEntity(us,4);
//				// addEventsExamples(us,org);
//				addUserPreferences(us);
//			}
//
//			createUserAccount("p.edwards", "pass", null, paulEdwards);
//			createUserAccount("s.turner", "pass", null, stevenTurner);
//			createUserAccount("g.young", "pass", null, georgeYoung);
//			createUserAccount("k.hall", "pass", null, kevinHall);
//			createUserAccount("k.carter", "pass", null, kennethCarter);
//			createUserAccount("k.white", "pass", null, karenWhite);
//			createUserAccount("h.campbell", "pass", null, helenCampbell);
//			createUserAccount("a.noore", "pass", null, anthonyMoore);
//			addIniUsers(flexoOrg, iniSWDevOrgUnit, iniSeniorProgrammerOrgPosition);
//			
//			Project op4lProject = new Project();
//			op4lProject.setTitle("OP4L project");
//			op4lProject.setUserOrganization(fos);
//			op4lProject = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(op4lProject, false);
//			
//			BookmarkEvent rdfW3cBookamarkEvent=new BookmarkEvent();
//			Bookmark rdfW3cBookamark = new Bookmark();
//			rdfW3cBookamark.setAuthor(kevinHall);
//			rdfW3cBookamark.setBookmarkURL("http://www.w3.org/RDF/");
//			rdfW3cBookamark.setTitle("RDF - Semantic Web Standards");
//			rdfW3cBookamark.setDescription("RDF is a standard model for data interchange on the Web.");
//			rdfW3cBookamark.addTag(getTag("rdf"));
//			rdfW3cBookamark.addTag(getTag("w3c"));
//			rdfW3cBookamark.addTag(getTag("semantic web"));
//			rdfW3cBookamark.setVisibility(createPublicVisibility());
//			rdfW3cBookamark = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rdfW3cBookamark, false);
//			
//			rdfW3cBookamarkEvent.setAnnotationRef(rdfW3cBookamark);
//			rdfW3cBookamarkEvent.setPerformedBy(anthonyMoore);
//			rdfW3cBookamarkEvent.setTimeStamp(new Date());
//			rdfW3cBookamarkEvent.setVisibility(createPublicVisibility());
//			rdfW3cBookamarkEvent = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rdfW3cBookamarkEvent, false);
//			
//			
//			BookmarkEvent semanticWebBookamarkEvent=new BookmarkEvent();
//			
//			Bookmark semanticWebBookamark = new Bookmark();
//			semanticWebBookamark.setAuthor(anthonyMoore);
//			semanticWebBookamark.setBookmarkURL("http://semanticweb.org");
//			semanticWebBookamark.setTitle("semanticweb.org");
//			semanticWebBookamark.setDescription("The Semantic Web is the extension of the World Wide Web that enables people to share content beyond the boundaries of applications and websites.");
//			semanticWebBookamark.addTag(getTag("rdf"));
//			semanticWebBookamark.addTag(getTag("semantic web"));
//			semanticWebBookamark.setVisibility(createPublicVisibility());
//			semanticWebBookamark = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(semanticWebBookamark, false);
//			
//			semanticWebBookamarkEvent.setAnnotationRef(semanticWebBookamark);
//			semanticWebBookamarkEvent.setPerformedBy(anthonyMoore);
//			semanticWebBookamarkEvent.setTimeStamp(new Date());
//			semanticWebBookamarkEvent = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(semanticWebBookamarkEvent, false);
//			
//			BookmarkEvent sparqlByExampleBookamarkEvent=new BookmarkEvent();
//			
//			Bookmark sparqlByExampleBookamark = new Bookmark();
//			sparqlByExampleBookamark.setAuthor(anthonyMoore);
//			sparqlByExampleBookamark.setBookmarkURL("http://www.cambridgesemantics.com/2008/09/sparql-by-example/");
//			sparqlByExampleBookamark.setTitle("SPARQL By Example");
//			sparqlByExampleBookamark.setDescription("SPARQL By Example - A Tutorial");
//			sparqlByExampleBookamark.addTag(getTag("sparql"));
//			sparqlByExampleBookamark.addTag(getTag("tutorial"));
//			sparqlByExampleBookamark.addTag(getTag("w3c"));
//			sparqlByExampleBookamark.setVisibility(createPublicVisibility());
//			sparqlByExampleBookamark = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(sparqlByExampleBookamark, false);
//			
//			sparqlByExampleBookamarkEvent.setAnnotationRef(sparqlByExampleBookamark);
//			sparqlByExampleBookamarkEvent.setPerformedBy(anthonyMoore);
//			sparqlByExampleBookamarkEvent.setTimeStamp(new Date());
//			sparqlByExampleBookamarkEvent = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(sparqlByExampleBookamarkEvent, false);
//			BookmarkEvent protegeDocsBookamarkEvent=new BookmarkEvent();
//			
//			Bookmark protegeDocsBookamark = new Bookmark();
//			protegeDocsBookamark.setAuthor(kevinHall);
//			 
//			protegeDocsBookamark.setBookmarkURL("http://protege.stanford.edu/doc/sparql/");
//			protegeDocsBookamark.setTitle("Protege;: Using SPARQL in Protege-OWL");
//			protegeDocsBookamark.addTag(getTag("sparql"));
//			protegeDocsBookamark.addTag(getTag("documentation"));
//			protegeDocsBookamark.addTag(getTag("protege"));
//			protegeDocsBookamark.addTag(getTag("owl"));
//			protegeDocsBookamark.setVisibility(createPublicVisibility());
//			protegeDocsBookamark = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(protegeDocsBookamark, false);
//			
//			protegeDocsBookamarkEvent.setAnnotationRef(protegeDocsBookamark);
//			protegeDocsBookamarkEvent.setPerformedBy(kevinHall);
//			protegeDocsBookamarkEvent.setTimeStamp(new Date()); 
//			protegeDocsBookamarkEvent = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(protegeDocsBookamarkEvent, false);
//			BookmarkEvent fourStoreBookamarkEvent=new BookmarkEvent();
//			
//			BookmarkEvent rdfW3cBookamarkEvent2=new BookmarkEvent();
//			Bookmark rdfW3cBookamark2 = new Bookmark();
//			rdfW3cBookamark2.setAuthor(kevinHall);
//			rdfW3cBookamark2.setBookmarkURL("http://www.w3.org/2001/sw/");
//			rdfW3cBookamark2.setTitle("W3C Semantic Web Activitys");
//			rdfW3cBookamark2.setDescription("The Semantic Web is about two things. It is about common formats for integration and combination of data drawn from diverse sources, where on the original Web mainly concentrated on the interchange of documents. It is also about language for recording how the data relates to real world objects.");
//			rdfW3cBookamark2.addTag(getTag("rdf"));
//			rdfW3cBookamark2.addTag(getTag("w3c"));
//			rdfW3cBookamark2.addTag(getTag("semantic web"));
//			rdfW3cBookamark2.addTag(getTag("semantic web activity"));
//			rdfW3cBookamark2.setVisibility(createPublicVisibility());
//			rdfW3cBookamark2 = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rdfW3cBookamark2, false);
//			
//			rdfW3cBookamarkEvent2.setAnnotationRef(rdfW3cBookamark2);
//			rdfW3cBookamarkEvent2.setPerformedBy(kevinHall);
//			rdfW3cBookamarkEvent2.setTimeStamp(new Date());
//			rdfW3cBookamarkEvent2 = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rdfW3cBookamarkEvent2, false);
//			
//			
//			BookmarkEvent rdfW3cBookamarkEvent3=new BookmarkEvent();
//			Bookmark rdfW3cBookamark3 = new Bookmark();
//			rdfW3cBookamark3.setAuthor(kevinHall);
//			rdfW3cBookamark3.setBookmarkURL("http://semanticarts.com/");
//			rdfW3cBookamark3.setTitle("Semantic Arts - Semantic and Service Oriented Architects");
//			rdfW3cBookamark3.setDescription("In a nutshell, we strive to help our clients remove complexity from existing systems. We do this by first understanding and documenting that complexity.");
//			rdfW3cBookamark3.addTag(getTag("rdf"));
//			rdfW3cBookamark3.addTag(getTag("w3c"));
//			rdfW3cBookamark3.addTag(getTag("semantic web"));
//			rdfW3cBookamark3.addTag(getTag("semantic web activity"));
//			rdfW3cBookamark3.setVisibility(createPublicVisibility());
//			rdfW3cBookamark3 = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rdfW3cBookamark3, false);
//			
//			rdfW3cBookamarkEvent3.setAnnotationRef(rdfW3cBookamark3);
//			rdfW3cBookamarkEvent3.setPerformedBy(kevinHall);
//			rdfW3cBookamarkEvent3.setTimeStamp(new Date());
//			rdfW3cBookamarkEvent3 = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rdfW3cBookamarkEvent3, false);
//			
//			
//			Bookmark fourStoreBookamark = new Bookmark();
//			fourStoreBookamark.setAuthor(anthonyMoore);
//			fourStoreBookamark.setBookmarkURL("http://4store.org/trac/wiki/SparqlServer");
//			fourStoreBookamark.setTitle("SparqlServer 4store");
//			fourStoreBookamark.setDescription("4store includes a SPARQL HTTP protocol server, which can answer SPARQL queries using the standard SPARQL HTTP query protocol among other features.");
//			fourStoreBookamark.addTag(getTag("4store"));
//			fourStoreBookamark.addTag(getTag("sparql"));
//			fourStoreBookamark.addTag(getTag("rdf"));
//			fourStoreBookamark.addTag(getTag("repository"));
//			fourStoreBookamark.setVisibility(createPublicVisibility());
//			fourStoreBookamark = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(fourStoreBookamark, false);
//			
//			fourStoreBookamarkEvent.setAnnotationRef(fourStoreBookamark);
//			 
//			fourStoreBookamarkEvent.setPerformedBy(anthonyMoore);
//			fourStoreBookamarkEvent.setTimeStamp(new Date());
//			fourStoreBookamarkEvent = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(fourStoreBookamarkEvent, false);
//			// adding the same Competence to the couple of users
//			//Zoran Jeremic
//			UserProgress domainModelingProgressZJ = ResourceFactory.createNewUserProgress();
//
//			Date currentDate = new Date();
//			Calendar deadlineCal = Calendar.getInstance();
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, 3);
//			
//			CompetenceRecord domainModelingCompCRecordZJ = new CompetenceRecord();
//			domainModelingCompCRecordZJ.setCompetence(domainModelingComp);
//			domainModelingCompCRecordZJ.setRecordedDate(new Date());
//			domainModelingCompCRecordZJ.setRecordedLevel(CompetenceLevel.INTERMEDIATE);
//			domainModelingCompCRecordZJ = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingCompCRecordZJ, false);
//			
//			ReusedLearningTask zjRLT = DomainModelUtil.cloneToReusedLearningTask(modelingLP, anthonyMoore, null);
//			zjRLT.setTitle("Modeling in OWL");
//			zjRLT.addTag(getTag("Linked Data"));
//			zjRLT.addTag(getTag("Ontology modeling"));
//			zjRLT.addTag(getTag("RDF"));
//			zjRLT.addTag(getTag("OWL"));
//			zjRLT.addTag(getTag("Semantic Web"));
//			zjRLT.setVisibility(createPublicVisibility());
//			zjRLT = ums.saveResource(zjRLT, false);
//			
//			setActivitiesInReusedLTAsPublic(zjRLT, anthonyMoore);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, zjRLT, zjRLT.getVisibility(), null);
//			
//			TargetCompetence domainModelingCompTCZJ = new TargetCompetence();
//			domainModelingCompTCZJ.setCompetence(domainModelingComp);
//			domainModelingCompTCZJ.setCurrentTask(zjRLT);
//			domainModelingCompTCZJ.setTargetLevel(CompetenceLevel.ADVANCED);
//			domainModelingCompTCZJ.setCurrentLevel(CompetenceLevel.INTERMEDIATE);
//			domainModelingCompTCZJ.addCompetenceRecord(domainModelingCompCRecordZJ);
//			domainModelingCompTCZJ.setVisibility(createPublicVisibility());
//			domainModelingCompTCZJ.setDeadline(deadlineCal.getTime());
//			domainModelingCompTCZJ.setProgress(domainModelingProgressZJ);
//			domainModelingCompTCZJ.setMaker(anthonyMoore);
//			domainModelingCompTCZJ = ums.saveResource(domainModelingCompTCZJ, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, domainModelingCompTCZJ, domainModelingCompTCZJ.getVisibility(), null);
//			
//			EventFactory.getInstance().createChangeProgressEvent(anthonyMoore, 
//					domainModelingCompTCZJ, 
//					domainModelingProgressZJ.getUserProgressLevel(), 
//					domainModelingProgressZJ.getProgressScale(),
//					null);
//			
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, 10);
//			
//			LearningGoalLabel knowledgeModelingLGLabel = new LearningGoalLabel();
//			knowledgeModelingLGLabel.setTitle("Knowledge Modeling");
//			knowledgeModelingLGLabel.setMaker(anthonyMoore);
//			knowledgeModelingLGLabel.setVisibility(createPublicVisibility());
//			knowledgeModelingLGLabel = ums.saveResource(knowledgeModelingLGLabel, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, knowledgeModelingLGLabel, knowledgeModelingLGLabel.getVisibility(), null);
//			
//			UserProgress ontologyLearningZJProgress = ResourceFactory.createNewUserProgress();
//			
//			LearningGoal ontologyLearningZJ = new LearningGoal();
//			ontologyLearningZJ.setTitle("To Learn Ontologies");
//			ontologyLearningZJ.setMaker(anthonyMoore);
//			ontologyLearningZJ.addTag(getTag("ontology"));
//			ontologyLearningZJ.setGoalPriority(GoalPriority.HIGH);
//			ontologyLearningZJ.setDeadline(deadlineCal.getTime());
//			ontologyLearningZJ.setVisibility(createPublicVisibility());
//			ontologyLearningZJ.addTargetCompetence(domainModelingCompTCZJ);
//			ontologyLearningZJ.setProgress(ontologyLearningZJProgress);
//			ontologyLearningZJ = ums.saveResource(ontologyLearningZJ, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, ontologyLearningZJ, ontologyLearningZJ.getVisibility(), null);
//			EventFactory.getInstance().createAddEvent(anthonyMoore, domainModelingCompTCZJ, ontologyLearningZJ, null);
//			EventFactory.getInstance().createChangeProgressEvent(anthonyMoore, ontologyLearningZJ, 
//					ontologyLearningZJProgress.getUserProgressLevel(), 
//					ontologyLearningZJProgress.getProgressScale(),
//					null);
//			
//			anthonyMoore.addLearningGoal(ontologyLearningZJ);
//			anthonyMoore = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(anthonyMoore, false);
//			
//			// Nikola Milikic
//			UserProgress domainModelingProgressNM = new UserProgress();
//			domainModelingProgressNM.setProgressScale(ProgressScale.PROGRESS_SCALE_0_TO_100);
//			domainModelingProgressNM.setUserProgressLevel(60);
//			domainModelingProgressNM = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingProgressNM, false);
//
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, -7);
//			
//			CompetenceRecord domainModelingCompCRecordNM = new CompetenceRecord();
//			domainModelingCompCRecordNM.setCompetence(domainModelingComp);
//			domainModelingCompCRecordNM.setRecordedDate(new Date());
//			domainModelingCompCRecordNM.setRecordedLevel(CompetenceLevel.BEGINER);
//			domainModelingCompCRecordNM = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingCompCRecordNM, false);
//			
//			ReusedLearningTask rltNM=DomainModelUtil.cloneToReusedLearningTask(modelingLP, kevinHall, null);
//			rltNM.addTag(getTag("social and behavioral sciences"));
//			rltNM.addTag(getTag("reliability"));
//			rltNM.addTag(getTag("computability theory"));
//			rltNM.addTag(getTag("automatic programming"));
//			rltNM.addTag(getTag("elicitation methods"));
//			rltNM.addTag(getTag("rational approximation"));
//			rltNM.addTag(getTag("curriculum"));
//			rltNM.setTitle("Modeling Things basics");
//			rltNM.setVisibility(createPublicVisibility());			
//			rltNM = ums.saveResource(rltNM, false);
//			
//			setActivitiesInReusedLTAsPublic(rltNM, kevinHall);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, rltNM, rltNM.getVisibility(), null);
//			
//			TargetCompetence domainModelingCompTCNM = new TargetCompetence();
//			domainModelingCompTCNM.setCompetence(domainModelingComp);
//			domainModelingCompTCNM.setCurrentTask(rltNM);
//			domainModelingCompTCNM.setTargetLevel(CompetenceLevel.ADVANCED);
//			domainModelingCompTCNM.setCurrentLevel(CompetenceLevel.BEGINER);
//			domainModelingCompTCNM.addCompetenceRecord(domainModelingCompCRecordNM);
//			domainModelingCompTCNM.setVisibility(createPublicVisibility());	
//			domainModelingCompTCNM.setDeadline(deadlineCal.getTime());
//			domainModelingCompTCNM.setProgress(domainModelingProgressNM);
//			domainModelingCompTCNM.setMaker(kevinHall);
//			domainModelingCompTCNM = ums.saveResource(domainModelingCompTCNM, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, domainModelingCompTCNM, domainModelingCompTCNM.getVisibility(), null);
//			EventFactory.getInstance().createChangeProgressEvent(kevinHall, 
//					domainModelingCompTCNM, 
//					domainModelingProgressNM.getUserProgressLevel(), 
//					domainModelingProgressNM.getProgressScale(),
//					null);
//			
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, -3);
//			
//			LearningGoalLabel swRelatedGoalsLGLabel = new LearningGoalLabel();
//			swRelatedGoalsLGLabel.setTitle("SW Related Goals");
//			swRelatedGoalsLGLabel.setMaker(kevinHall);
//			swRelatedGoalsLGLabel = ums.saveResource(swRelatedGoalsLGLabel, false);
//			
//			UserProgress ontologyLearningNMProgress = ResourceFactory.createNewUserProgress();
//			
//			LearningGoal ontologyLearningNM = new LearningGoal();
//			ontologyLearningNM.setTitle("Learn Ontologies");
//			ontologyLearningNM.setMaker(kevinHall);
//			ontologyLearningNM.addTag(getTag("ontology"));
//			ontologyLearningNM.setGoalPriority(GoalPriority.MEDIUM);
//			ontologyLearningNM.setDeadline(deadlineCal.getTime());
//			ontologyLearningNM.setVisibility(createPublicVisibility());	
//			ontologyLearningNM.addTargetCompetence(domainModelingCompTCNM);
//			ontologyLearningNM.setProgress(ontologyLearningNMProgress);
//			ontologyLearningNM = ums.saveResource(ontologyLearningNM, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, ontologyLearningNM, ontologyLearningNM.getVisibility(), null);
//			EventFactory.getInstance().createAddEvent(kevinHall, domainModelingCompTCNM, ontologyLearningNM, null);
//			EventFactory.getInstance().createChangeProgressEvent(kevinHall, 
//					ontologyLearningNM, 
//					ontologyLearningNMProgress.getUserProgressLevel(), 
//					ontologyLearningNMProgress.getProgressScale(),
//					null);
//			
//			kevinHall.addLearningGoal(ontologyLearningNM);
//			kevinHall = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(kevinHall, false);
//			
//			// Dragan Djuric
//			UserProgress domainModelingProgressDDJ = new UserProgress();
//			domainModelingProgressDDJ.setProgressScale(ProgressScale.PROGRESS_SCALE_0_TO_100);
//			domainModelingProgressDDJ.setUserProgressLevel(40);
//			domainModelingProgressDDJ = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingProgressDDJ, false);
//
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, 5);
//			
//			CompetenceRecord domainModelingCompCRecordDDJ = new CompetenceRecord();
//			domainModelingCompCRecordDDJ.setCompetence(domainModelingComp);
//			domainModelingCompCRecordDDJ.setRecordedDate(new Date());
//			domainModelingCompCRecordDDJ.setRecordedLevel(CompetenceLevel.BEGINER);
//			domainModelingCompCRecordDDJ = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingCompCRecordDDJ, false);
//			
//			ReusedLearningTask rltDD=DomainModelUtil.cloneToReusedLearningTask(modelingLP, kennethCarter, null);
//			rltDD.setTitle("Modeling in SW world");
//			rltDD.addTag(getTag("Decision Problems"));
//			rltDD.addTag(getTag("Concept Learning"));
//			rltDD.addTag(getTag("Artificial"));
//			rltDD.addTag(getTag("Interactive Systems"));
//			rltDD.addTag(getTag("Process Management"));
//			rltDD.addTag(getTag("General"));
//			rltDD.addTag(getTag("reliability"));
//			rltDD.addTag(getTag("rational approximation"));
//			rltDD.addTag(getTag("OWL"));
//			rltDD.setVisibility(createPublicVisibility());
//			rltDD = ums.saveResource(rltDD,false);
//			
//			setActivitiesInReusedLTAsPublic(rltDD, kennethCarter);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, rltDD, rltDD.getVisibility(), null);
//			
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(georgeYoung,  kennethCarter, rltDD, "I would recommend this learning task as very useful.", null);
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(karenWhite,  kennethCarter, rltDD, "I would recommend this learning task as very nice.", null);
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(kevinHall,  kennethCarter, rltDD, "I would recommend this learning task as nice.", null);
//			
//			TargetCompetence domainModelingCompTCDDJ = new TargetCompetence();
//			domainModelingCompTCDDJ.setCompetence(domainModelingComp);
//			domainModelingCompTCDDJ.setCurrentTask(rltDD);
//			domainModelingCompTCDDJ.setTitle("How to model things advanced");
//			domainModelingCompTCDDJ.setTargetLevel(CompetenceLevel.ADVANCED);
//			domainModelingCompTCDDJ.setCurrentLevel(CompetenceLevel.BEGINER);
//			domainModelingCompTCDDJ.addCompetenceRecord(domainModelingCompCRecordDDJ);
//			domainModelingCompTCDDJ.setVisibility(createPublicVisibility());
//			domainModelingCompTCDDJ.setDeadline(deadlineCal.getTime());
//			domainModelingCompTCDDJ.setProgress(domainModelingProgressDDJ);
//			domainModelingCompTCDDJ.setMaker(georgeYoung);
//			domainModelingCompTCDDJ = ums.saveResource(domainModelingCompTCDDJ, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, domainModelingCompTCDDJ, domainModelingCompTCDDJ.getVisibility(), null);
//			EventFactory.getInstance().createChangeProgressEvent(georgeYoung, 
//					domainModelingCompTCDDJ, 
//					domainModelingProgressDDJ.getUserProgressLevel(), 
//					domainModelingProgressDDJ.getProgressScale(),
//					null);
//			
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, 8);
//			
//			LearningGoalLabel ontologiesLGLabel = new LearningGoalLabel();
//			ontologiesLGLabel.setTitle("Ontologies");
//			ontologiesLGLabel.setMaker(georgeYoung);
//			ontologiesLGLabel = ums.saveResource(ontologiesLGLabel, false);
//			
//			UserProgress ontologyLearningDDJProgress = ResourceFactory.createNewUserProgress();
//			
//			LearningGoal ontologyLearningDDJ = new LearningGoal();
//			ontologyLearningDDJ.setTitle("Learning Ontologies");
//			ontologyLearningDDJ.setMaker(georgeYoung);
//			ontologyLearningDDJ.addTag(getTag("ontology"));
//			ontologyLearningDDJ.setGoalPriority(GoalPriority.MEDIUM);
//			ontologyLearningDDJ.setDeadline(deadlineCal.getTime());
//			ontologyLearningDDJ.setVisibility(createPublicVisibility());
//			ontologyLearningDDJ.addTargetCompetence(domainModelingCompTCDDJ);
//			ontologyLearningDDJ.setProgress(ontologyLearningDDJProgress);
//			ontologyLearningDDJ = ums.saveResource(ontologyLearningDDJ, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, ontologyLearningDDJ, ontologyLearningDDJ.getVisibility(), null);
//			EventFactory.getInstance().createAddEvent(georgeYoung, domainModelingCompTCDDJ, ontologyLearningDDJ, null);
//			EventFactory.getInstance().createChangeProgressEvent(georgeYoung, 
//					ontologyLearningDDJ, 
//					ontologyLearningDDJProgress.getUserProgressLevel(), 
//					ontologyLearningDDJProgress.getProgressScale(),
//					null);
//			
//			georgeYoung.addLearningGoal(ontologyLearningDDJ);
//			georgeYoung = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(georgeYoung, false);
//			
//			//delete Activity "Create your own domain model for a domain of choice"
//			LearningTask draganDjdomainModelingCompCurrentTask = domainModelingCompTCDDJ.getCurrentTask();
//			
//			Collection<Activity> activities = draganDjdomainModelingCompCurrentTask.getLearningActivities();
//			
//			//set the activity as abandoned
//			for (Activity activity : activities) {
//				if (activity.getTitle().equals("Create your own domain model for a domain of choice")) {
//					draganDjdomainModelingCompCurrentTask.getActivities().remove(activity);
//					draganDjdomainModelingCompCurrentTask = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(draganDjdomainModelingCompCurrentTask, false);
//					EventFactory.getInstance().createDeleteEvent(georgeYoung, activity, draganDjdomainModelingCompCurrentTask, null);
//				}
//			}
//			
//			// Jelena Jovanovic
//			UserProgress domainModelingProgressJJ = new UserProgress();
//			domainModelingProgressJJ.setProgressScale(ProgressScale.PROGRESS_SCALE_0_TO_100);
//			domainModelingProgressJJ.setUserProgressLevel(ProgressScale.PROGRESS_SCALE_0_TO_100.getMaxValue());
//			domainModelingProgressJJ = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingProgressJJ, false);
//
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, -10);
//			
//			CompetenceRecord domainModelingCompCRecordJJ = new CompetenceRecord();
//			domainModelingCompCRecordJJ.setCompetence(domainModelingComp);
//			domainModelingCompCRecordJJ.setRecordedDate(new Date());
//			domainModelingCompCRecordJJ.setRecordedLevel(CompetenceLevel.ADVANCED);
//			domainModelingCompCRecordJJ = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(domainModelingCompCRecordJJ, false);
//			
//			ReusedLearningTask rltJJ=DomainModelUtil.cloneToReusedLearningTask(modelingLP, karenWhite, null);
//			rltJJ.setTitle("How to model Things - 10 simple steps");
//			rltJJ.addTag(getTag("Reasoning"));
//			rltJJ.addTag(getTag("Text mining"));
//			rltJJ.addTag(getTag("Data mining"));
//			rltJJ.addTag(getTag("Ontology extraction"));
//			rltJJ.setVisibility(createPublicVisibility());
//			rltJJ = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(rltJJ, false);
//
//			setActivitiesInReusedLTAsPublic(rltJJ, karenWhite);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, rltJJ, rltJJ.getVisibility(), null);
//			 
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(georgeYoung,  karenWhite, rltJJ, "I would recommend this learning task as very useful.", null);
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(kennethCarter,  karenWhite, rltJJ, "I would recommend this learning task as very nice.", null);
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(kevinHall,  karenWhite, rltJJ, "I would recommend this learning task as nice.", null);
//		
//			 
//			TargetCompetence domainModelingCompTCJJ = new TargetCompetence();
//			domainModelingCompTCJJ.setCompetence(domainModelingComp);
//			domainModelingCompTCJJ.setCurrentTask(rltJJ);
//			domainModelingCompTCJJ.setTitle("How to model Things - 10 simple steps");
//			domainModelingCompTCJJ.setTargetLevel(CompetenceLevel.ADVANCED);
//			domainModelingCompTCJJ.setCurrentLevel(CompetenceLevel.ADVANCED);
//			domainModelingCompTCJJ.addCompetenceRecord(domainModelingCompCRecordJJ);
//			domainModelingCompTCJJ.setVisibility(createPublicVisibility());
//			domainModelingCompTCJJ.setDeadline(deadlineCal.getTime());
//			domainModelingCompTCJJ.setProgress(domainModelingProgressJJ);
//			domainModelingCompTCJJ.setMaker(karenWhite);
//			domainModelingCompTCJJ = ums.saveResource(domainModelingCompTCJJ, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, domainModelingCompTCJJ, domainModelingCompTCJJ.getVisibility(), null);
//			EventFactory.getInstance().createChangeProgressEvent(karenWhite, 
//					domainModelingCompTCJJ, 
//					domainModelingProgressJJ.getUserProgressLevel(), 
//					domainModelingProgressJJ.getProgressScale(),
//					null);
//			
//			//set TargetCompetence "Create your own domain model for a domain of choice" as completed
//			MarkAsComplete.markAsCompleteCompetence(domainModelingCompTCJJ);
//			
//			deadlineCal.setTime(currentDate);
//			deadlineCal.add(Calendar.DATE, -8);
//			
//			LearningGoalLabel semanticWeb = new LearningGoalLabel();
//			semanticWeb.setTitle("Semantic Web");
//			semanticWeb.setMaker(karenWhite);
//			semanticWeb = ums.saveResource(semanticWeb, false);
//			
//			UserProgress ontologyLearningJJProgress = ResourceFactory.createNewUserProgress();
//			
//			LearningGoal ontologyLearningJJ = new LearningGoal();
//			ontologyLearningJJ.setTitle("Learn Ontology Modeling");
//			ontologyLearningJJ.setMaker(karenWhite);
//			ontologyLearningJJ.addTag(getTag("ontology"));
//			ontologyLearningJJ.addTag(getTag("ontology engineering"));
//			ontologyLearningJJ.setGoalPriority(GoalPriority.MEDIUM);
//			ontologyLearningJJ.setDeadline(deadlineCal.getTime());
//			ontologyLearningJJ.setVisibility(createPublicVisibility());
//			ontologyLearningJJ.addTargetCompetence(domainModelingCompTCJJ);
//			ontologyLearningJJ.setProgress(ontologyLearningJJProgress);
//			ontologyLearningJJ = ums.saveResource(ontologyLearningJJ, false);
//			
////			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, ontologyLearningJJ, ontologyLearningJJ.getVisibility(), null);
//			EventFactory.getInstance().createChangeProgressEvent(karenWhite, 
//					ontologyLearningJJ, 
//					ontologyLearningJJProgress.getUserProgressLevel(), 
//					ontologyLearningJJProgress.getProgressScale(),
//					null);
//			
//			EventFactory.getInstance().createAddEvent(karenWhite, 
//					domainModelingCompTCJJ, 
//					ontologyLearningJJ,
//					null);
//			
//			karenWhite.addLearningGoal(ontologyLearningJJ);
//			karenWhite = ServiceLocator1.getInstance().getService(BaseManager.class).saveResource(karenWhite, false);
//			
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
	}

	protected static UserRating createNumericRating(int ratingValue, Scale scale) throws Exception {
		UserRating rating = new UserRating();
		rating.setRatingValue(ratingValue);
		rating.setScale(scale);
		return ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(rating);
	}
	
	protected static Tag createTag(String tagTitle) throws Exception {
		Tag slidesTag = new Tag();
		slidesTag.setTitle(tagTitle);
		return ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(slidesTag);
	}
	
//	protected static void setActivitiesInReusedLTAsPublic(LearningPlan rlt, User user){
//		
//		Collection<Activity> activities=rlt.getActivities();
//		for(Activity act:activities){
//			try {
//		 
//				Visibility actVis = createPublicVisibility();
////				EventFactory.getInstance().createSetVisibilityEvent(user, act, actVis, null);
//				act.setVisibility(actVis);
//				
//				ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(act);
////				Collection<ContentUnit> contents=act.getContents();
////				for(ContentUnit cu:contents){
////					
////					Visibility cuVis = createPublicVisibility();
//////					EventFactory.getInstance().createSetVisibilityEvent(user, cu, cuVis, null);
////					cu.setVisibility(cuVis);
////					
////					ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(cu);
////				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
}
