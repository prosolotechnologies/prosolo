package org.prosolo.app.bc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prosolo.domainmodel.activities.ResourceActivity;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.annotation.UserRating;
import org.prosolo.domainmodel.organization.Organization;
import org.prosolo.domainmodel.organization.OrganizationalPosition;
import org.prosolo.domainmodel.organization.OrganizationalUnit;
import org.prosolo.domainmodel.organization.Role;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.FollowedEntity;
import org.prosolo.domainmodel.user.FollowedUserEntity;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TimeFrame;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.preferences.EmailPreferences;
import org.prosolo.domainmodel.user.preferences.TopicPreference;
import org.prosolo.domainmodel.workflow.Scale;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.app.bc.BusinessCase1_DL")
public class BusinessCase1_DL extends BusinessCase {
	
	public Map<String, Tag> allTags = new HashMap<String, Tag>();
	public Collection<User> allUsers = new ArrayList<User>();
	
	public void createTags(int s) throws Exception{
		for(int k=0;k<s;k++){
		//Tags - BEGINS
		String[] tags={"OWL"+k, "RDF"+k, "Semantic Web"+k, "Domain modeling"+k, "Linked Data"+k, "ontology"+k, "methodology"+k,"ontology engineering"+k,"Conceptual Modeling"+k,"Reasoning"+k,"Protege"+k,"ontology development"+k,
				"java"+k,"PHP"+k,"Web services"+k,"SCA"+k,"Tuscany" , "Software engineering"+k, "Development"+k, "Programming"+k,"Security assurance"+k, "Application frameworks"+k,"Formal specification"+k,"Component composition"+k,"Service engineering"+k,"Software modelling"+k,"Design Strategies"+k,"Software requirements"+k,
				"Real-tim operating systems"+k,"Operating systems"+k,"Distributed systems"+k,"Process analysis"+k,"Software quality"+k,
				"Software pricing"+k, "Risk management"+k,"Managing people"+k,"Team work"+k,"Version management"+k};
		
			for (int i = 0; i < tags.length; i++) {
			//example tag
				Tag t = createTag(tags[i]);
//			 Concept concept=createConcept(tags[1]);
			allTags.put(tags[i], t);
//			allConcepts.put(tags[i], concept);
			}
		}
	}
	
	private Tag getTag(String tagString) throws Exception {
		if(!allTags.containsKey(tagString)){
			Tag t=createTag(tagString);
			return t;
		}else
		return allTags.get(tagString);
	}
	
	@SuppressWarnings("unused")
	private void addIniUsers(Role role, Organization org, OrganizationalUnit keyToMetalsOrgUnit, String orgPos) throws Exception{
		User nikolaDamjanovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Nikola","Damjanovic", "nikola.damjanovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, nikolaDamjanovic, keyToMetalsOrgUnit, orgPos);
		nikolaDamjanovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, nikolaDamjanovic);
		
		User jelenaJovanovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Jelena","Jovanovic", "jelena.jovanovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaJovanovic, keyToMetalsOrgUnit, orgPos);
		jelenaJovanovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaJovanovic);
		
		User viktorPocajt = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Viktor","Pocajt", "viktor.pocajt@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaJovanovic, keyToMetalsOrgUnit, orgPos);
		jelenaJovanovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaJovanovic);
		
		User jelenaDjerkovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Jelena","Djerkovic", "jelena.djerkovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaDjerkovic, keyToMetalsOrgUnit, orgPos);
		jelenaDjerkovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaDjerkovic);
		
		User mirjanaStankovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Mirjana","Stankovic", "mirjana.stankovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, mirjanaStankovic, keyToMetalsOrgUnit, orgPos);
		mirjanaStankovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, mirjanaStankovic);
		
		User jelenaJankovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Jelena","Jankovic", "jelena.jankovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, mirjanaStankovic, keyToMetalsOrgUnit, orgPos);
		mirjanaStankovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, mirjanaStankovic);
		
		User radmilaDamjanovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Radmila","Damjanovic", "radmila.damjanovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, radmilaDamjanovic, keyToMetalsOrgUnit, orgPos);
		radmilaDamjanovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, radmilaDamjanovic);
		
		User aleksandraPetkovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Aleksandra","Petkovic", "aleksandra.petkovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, aleksandraPetkovic, keyToMetalsOrgUnit, orgPos);
		aleksandraPetkovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, aleksandraPetkovic);
		
		User zeljkaVragolovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Zeljka","Vragolovic", "zeljka.vragolovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, zeljkaVragolovic, keyToMetalsOrgUnit, orgPos);
		zeljkaVragolovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, zeljkaVragolovic);
		
		User vladimirMarkovic = ServiceLocator.getInstance().getService(UserManager.class).
									createNewUser("Vladimir","Markovic", "vladimir.markovic@ini.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, vladimirMarkovic, keyToMetalsOrgUnit, orgPos);
		vladimirMarkovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, vladimirMarkovic);
		
		
		allUsers.add(nikolaDamjanovic);
		allUsers.add(jelenaJovanovic);
		allUsers.add(viktorPocajt);
		allUsers.add(jelenaDjerkovic);
		allUsers.add(mirjanaStankovic);
//		for(User us:allUsers){
//			 setUserStatus(us);
//		}
		allUsers.add(jelenaJankovic);
		allUsers.add(radmilaDamjanovic);
		allUsers.add(aleksandraPetkovic);
		allUsers.add(zeljkaVragolovic);
		allUsers.add(vladimirMarkovic);
		for(User us:allUsers){
			//User us=zoranJeremic;
			//SocialStream sStream=new SocialStream();
			//ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(sStream);
		 
			//us.setSocialStream(sStream);
			//ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(us);
		us.setAvatarUrl(AvatarUtils.getDefaultAvatarUrl());
		 setFollowedUser(us,nikolaDamjanovic);
		 setFollowedUser(us,jelenaJovanovic);
		 setFollowedUser(us,viktorPocajt);
		 setFollowedUser(us,jelenaDjerkovic);
		 setFollowedUser(us,mirjanaStankovic);
		 setFollowedUser(us,jelenaJankovic);
		 setFollowedUser(us,radmilaDamjanovic);
		 setFollowedUser(us,aleksandraPetkovic);
		 setFollowedUser(us,zeljkaVragolovic);
		 setFollowedUser(us,vladimirMarkovic);
		 
			// addRandomFollowedEntity(us,4);
			  //addEventsExamples(us,org);
		
			addUserPreferences(us);
		}
		//generateTestEvents(nikolaDamjanovic);

	}
//	private void setUserStatus(User user){
//		ServiceLocator.getInstance().getService(UserManager.class).createNewStatus(user,"DefaultStatusForUser"+user.getName());
//		//user.createNewStatus("Default status for user "+user.getName());
//		ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(user);
//	}
	public void setFollowedUser(User user, User followedUser){
		FollowedEntity fe=new FollowedUserEntity();
		fe.setFollowedResource(followedUser);
		fe.setUser(user);
		fe = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(fe);
		//user.addFollowedEntity(fe);
		user = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(user);
	}
	
	@SuppressWarnings("unused")
	private void addNugsUsers(Role role, Organization org, OrganizationalUnit netBeansDevTeam, String orgPos) throws Exception{
		User djordjeGligorijevic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Djordje","Gligorijevic", "djordjegligorijevic90@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, djordjeGligorijevic, netBeansDevTeam, orgPos);
		djordjeGligorijevic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, djordjeGligorijevic);
		
		User jelenaDjordjevic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Jelena","Djordjevic", "jelena.djordjevic@fonis.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaDjordjevic, netBeansDevTeam, orgPos);
		jelenaDjordjevic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaDjordjevic);
		
		User jelenaStojanovic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Jelena","Stojanovic", "jelena.stojanovic.1989@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaStojanovic, netBeansDevTeam, orgPos);
		jelenaStojanovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, jelenaStojanovic);
		
		User marjanHrdzic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Marjan","Hrdzic", "marjan.hrzic@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, marjanHrdzic, netBeansDevTeam, orgPos);
		marjanHrdzic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, marjanHrdzic);
		
		User vedranaGajic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Vedrana","Gajic", "gajicvedrana@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, vedranaGajic, netBeansDevTeam, orgPos);
		vedranaGajic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, vedranaGajic);
		
		User misaLazovic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Misa","Lazovic", "misalazovic@open.telekom.rs", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, misaLazovic, netBeansDevTeam, orgPos);
		misaLazovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, misaLazovic);
		
		User ivanStankovic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Ivan","Stankovic", "ivan0089@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, ivanStankovic, netBeansDevTeam, orgPos);
		ivanStankovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, ivanStankovic);
		
		User dusanMilicevic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Dusan","Milicevic", "dusanmilicevic@ovi.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, dusanMilicevic, netBeansDevTeam, orgPos);
		dusanMilicevic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, dusanMilicevic);
		
		User milosMaksimovic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Milos","Maksimovic", "maksapn@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, milosMaksimovic, netBeansDevTeam, orgPos);
		milosMaksimovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, milosMaksimovic);
		
		User ninaBrcic = ServiceLocator.getInstance().getService(UserManager.class).
										createNewUser("Nina","Brcic", "brcic.nina@gmail.com", true, "pass", org, orgPos);
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, ninaBrcic, netBeansDevTeam, orgPos);
		ninaBrcic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(role, ninaBrcic);
		
		
		allUsers.add(djordjeGligorijevic);
		allUsers.add(jelenaDjordjevic);
		allUsers.add(jelenaStojanovic);
		allUsers.add(marjanHrdzic);
		allUsers.add(vedranaGajic);
		allUsers.add(misaLazovic);
		allUsers.add(ivanStankovic);
		allUsers.add(dusanMilicevic);
		allUsers.add(milosMaksimovic);
		allUsers.add(ninaBrcic);
		for(User us:allUsers){
			//User us=zoranJeremic;
//			SocialStream sStream=new SocialStream();
//			sStream = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(sStream);
//			
//			 us.setAvatarUrl(AvatarUtils.generateDefaultAvatarUrl("jpg"));
//			us.setSocialStream(sStream);
//			
			//  addRandomFollowedEntity(us,4);
			// addEventsExamples(us,org);
		//	addUserPreferences(us);
//			setUserStatus(us);
		}
		
	}
	
	private void addUserPreferences(User user) throws Exception{
		TopicPreference tPreference=new TopicPreference();
		for (int i=0;i<1;i++){
			 //tPreference.addPreferredConcept(getRandomConcept());
			 //tPreference.addPreferredKeyword(getRandomTag());
			//cPreference.addPreferredCompetence(getRandomCompetence());
		}
		//tPreference.addPreferredKeyword(getTag("OWL"));
	//	tPreference.addPreferredKeyword(getTag("Software engineering"));
		//tPreference.addPreferredKeyword(getTag("Software quality"));
		tPreference.setUser(user);
		tPreference = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tPreference);
//		cPreference = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(cPreference);
		 
		EmailPreferences ePreference = new EmailPreferences();
		ePreference.addNotificationFrequency(TimeFrame.DAILY);
		ePreference.setUser(user);
		ePreference = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ePreference);
		
		//user.addPreference(ePreference);
 
		//user.addPreference(tPreference);
//		user.addPreference(cPreference);
		 
		user = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(user);
	}
	
//	private Date setPreviousDate() throws Exception {
//		GregorianCalendar gc = new GregorianCalendar();
//		gc.setTime(currentDate);
//		int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
//		gc.roll(Calendar.DAY_OF_YEAR, -1);
//		int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
//
//		if (dayAfter > dayBefore) {
//			gc.roll(Calendar.YEAR, -1);
//		}
//
//		gc.get(Calendar.DATE);
//		java.util.Date yesterday = gc.getTime();
//		currentDate = yesterday;
//
//		return currentDate;
//	}

	@SuppressWarnings("unused")
	private void mapLearningGoal(LearningGoal lg) throws Exception {
//		LearningGoal lGoal = new LearningGoal();
//		lGoal.setTitle(lg.getTitle());
//		lGoal.setMaker(lg.getMaker());
//		lGoal.setDeadline(lg.getDeadline());
//		lGoal.setDateCreated(lg.getDateCreated());
//		//lGoal.addTargetCompetence(tc);
//		lGoal = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lGoal);
		
//		LearningGoal ontologyLearningZJ = ServiceLocator.getInstance().
//				getService(LearningGoalManager.class).createNewLearningGoal(
//						lg.getMaker(), 
//						lg.getTitle(), 
//						"", 
//						lg.getDeadline(), 
//						new ArrayList<Annotation>(lg.getAnnotationsByType(AnnotationType.Tag)));
//
//		User user = new User();
//		user.setName("N" + ((User) lg.getMaker()).getName());
//		user.setLastname("N"+((User) lg.getMaker()).getLastname());
//		user.addLearningGoal(ontologyLearningZJ);
//		user = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(user);
	}

//	private TargetCompetence mapTargetCompetence(
//			TargetCompetence tCompetence) {
//
//		Competence comp = new Competence();
//		comp.setTitle(tCompetence.getCompetence().getTitle());
//		comp.addTopic(getRandomConcept());
//		comp.addTopic(getRandomConcept());
//		Visibility compVis=new Visibility(VisibilityType.PUBLIC);
//		compVis = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(compVis);
//		comp.setVisibility(compVis);
//		comp = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(comp);
//
//		TargetCompetence dlTCompetence = new TargetCompetence();
//		dlTCompetence.setCompetence(comp);
//		dlTCompetence = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(dlTCompetence);
//
//		return dlTCompetence;
//	}
	
	@Transactional
	public void initRepository() throws Exception {
		System.out.println("BusinessCaseTest - initRepository() with BC 1");
		
		Organization org = ServiceLocator.getInstance().getService(OrganizationManager.class).
					getAllOrganizations().iterator().next();
		
		Collection<OrganizationalUnit> orgUnits = org.getOrgUnits();
		OrganizationalUnit headOfficeOrgUnit = null;
		
		for (OrganizationalUnit orgUnit : orgUnits) {
			if (!orgUnit.isSystem()) {
				headOfficeOrgUnit = orgUnit;
				break;
			}
		}
		
		OrganizationalUnit fosGoodOldAiResearchNetworkOrgUnit = new OrganizationalUnit();
		fosGoodOldAiResearchNetworkOrgUnit.setTitle("FOS GOOD OLD AI");
		fosGoodOldAiResearchNetworkOrgUnit.setOrganization(org);
		fosGoodOldAiResearchNetworkOrgUnit.setParentUnit(headOfficeOrgUnit);
		fosGoodOldAiResearchNetworkOrgUnit = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(fosGoodOldAiResearchNetworkOrgUnit);
		
		headOfficeOrgUnit.addSubUnit(fosGoodOldAiResearchNetworkOrgUnit);
		headOfficeOrgUnit = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(headOfficeOrgUnit);
		
//		String goodOldAiChair = "Senior Programmer";
		String fosSeniorProgrammerOrgPosition = "Senior Programmer";
		
		org.addOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
		org = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(org);
		
		User zoranJeremic = ServiceLocator.getInstance().getService(UserManager.class).
								createNewUser("Zoran","Jeremic", "zoran.jeremic@gmail.com", true, "pass", org, fosSeniorProgrammerOrgPosition);
		allUsers.add(zoranJeremic);
		
		// create default ROLES
		String roleUserTitle = "User";
		String roleAdminTitle = "Admin";
		Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleUserTitle);
		Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleAdminTitle);
		
//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, zoranJeremic, headOfficeOrgUnit, fosSeniorProgrammerOrgPosition);
		zoranJeremic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, zoranJeremic);
		
		
		OrganizationalUnit keyToMetalsIniOrgUnit = new OrganizationalUnit();
		keyToMetalsIniOrgUnit.setTitle("Key to Metals");
		keyToMetalsIniOrgUnit.setOrganization(org);
		keyToMetalsIniOrgUnit.setParentUnit(headOfficeOrgUnit);
		keyToMetalsIniOrgUnit = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(keyToMetalsIniOrgUnit);
		
		headOfficeOrgUnit.addSubUnit(keyToMetalsIniOrgUnit);
		headOfficeOrgUnit = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(headOfficeOrgUnit);
		
		OrganizationalUnit netBeansPlatformDev = new OrganizationalUnit();
		netBeansPlatformDev.setTitle("NetBeans Platform Development Unit");
		netBeansPlatformDev.setOrganization(org);
		netBeansPlatformDev.setParentUnit(headOfficeOrgUnit);
		netBeansPlatformDev = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(netBeansPlatformDev);
		
		headOfficeOrgUnit.addSubUnit(netBeansPlatformDev);
		headOfficeOrgUnit = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(headOfficeOrgUnit);
		
		org.addOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
		org = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(org);
		
		@SuppressWarnings("unused")
		String iniSeniorProgrammerOrgPosition = "Senior Programmer";
		
		OrganizationalPosition nugsJuniorDeveloper = new OrganizationalPosition();
		nugsJuniorDeveloper.setTitle("Junior Developer");
		nugsJuniorDeveloper.setAllocatedToOrgUnit(netBeansPlatformDev);
		nugsJuniorDeveloper = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(nugsJuniorDeveloper);
		
		OrganizationalPosition fosAssistantProfessorPosition = new OrganizationalPosition();
		fosAssistantProfessorPosition.setTitle("Assistant Professor");
		fosAssistantProfessorPosition.setAllocatedToOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
		ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(fosAssistantProfessorPosition);
		
//		String fosTeachingAssistantPosition = "Teaching Assistant";
		
		int cicles=3;
		for(int s=0;s<cicles;s++){
			createTags(s);
		}
		//int i=1;
		//	addCompetencesToRepository();
		try {
			User bojanTomic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Bojan","Tomic", "tomic.bojan@fon.rs", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, bojanTomic, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
			bojanTomic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, bojanTomic);
			
			User zoranSevarac = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Zoran","Sevarac", "sevarac.zoran@fon.rs", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, zoranSevarac, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
			zoranSevarac = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, zoranSevarac);
			
			User draganDjuric = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Dragan","Djuric", "djuric.dragan@fon.rs", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, draganDjuric, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
			draganDjuric = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, draganDjuric);
			
			User nikolaMilikic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Nikola","Milikic", "nikola.milikic@gmail.com", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, nikolaMilikic, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
			nikolaMilikic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, nikolaMilikic);
			
			User vladanDevedzic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Vladan","Devedzic", "devedzic.vladan@fon.rs", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, vladanDevedzic, fosGoodOldAiResearchNetworkOrgUnit, goodOldAiChair);
			vladanDevedzic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, vladanDevedzic);
			
			User jelenaJovanovic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Jelena","Jovanovic", "jovanovic.jelena@fon.rs", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, jelenaJovanovic, fosGoodOldAiResearchNetworkOrgUnit, fosTeachingAssistantPosition);
			jelenaJovanovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, jelenaJovanovic);
			
			User sonjaRadenkovic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Sonja","Radenkovic", "sonjafon@gmail.com", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, sonjaRadenkovic, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
			sonjaRadenkovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, sonjaRadenkovic);
			
			User tanjaMilic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Tanja","Milic", "tanja.milic@fon.b.ac.rs", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, tanjaMilic, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
			tanjaMilic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, tanjaMilic);
			
			User sreckoJoksimovic = ServiceLocator.getInstance().getService(UserManager.class).
					createNewUser("Srecko","Joksimovic", "sreckojoksimovic@gmail.com", true, "pass", org, fosSeniorProgrammerOrgPosition);
//			ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, sreckoJoksimovic, fosGoodOldAiResearchNetworkOrgUnit, fosSeniorProgrammerOrgPosition);
			sreckoJoksimovic = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleUser, sreckoJoksimovic);
			
			allUsers.add(bojanTomic);
			allUsers.add(zoranSevarac);
			allUsers.add(draganDjuric);
			//allUsers.add(zoranJeremic);
//			for(User us:allUsers){
//				setUserStatus(us);
//			}
			allUsers.add(nikolaMilikic);
			allUsers.add(vladanDevedzic);
			allUsers.add(jelenaJovanovic);
			allUsers.add(sonjaRadenkovic);
			allUsers.add(tanjaMilic);
			allUsers.add(sreckoJoksimovic);
			
			TopicPreference zjPreference=new TopicPreference();
			//zjPreference.addPreferredKeyword(getTag("Semantic Web"));
			//zjPreference.addPreferredKeyword(getTag("RDF"));
			//zjPreference.addPreferredKeyword(getTag("Ontology development"));
			zjPreference.setUser(zoranJeremic);
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(zjPreference);
			
			//zoranJeremic.addPreference(zjPreference);
 
			TopicPreference nmPreference=new TopicPreference();
			nmPreference.addPreferredKeyword(getTag("Semantic Web"));
			nmPreference.addPreferredKeyword(getTag("RDF"));
			nmPreference.addPreferredKeyword(getTag("Ontology development"));
			nmPreference.addPreferredKeyword(getTag("domain modelling"));
			nmPreference.addPreferredKeyword(getTag("Ontology modelling"));
			nmPreference.addPreferredKeyword(getTag("Ontology extraction"));
			nmPreference.addPreferredKeyword(getTag("Data mining"));
			nmPreference.addPreferredKeyword(getTag("Text mining"));
			nmPreference.addPreferredKeyword(getTag("Reasoning"));
			nmPreference.setUser(zoranJeremic);
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(nmPreference);
			
			//zoranJeremic.addPreference(nmPreference);
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(zoranJeremic);
			
			//C1: Ability to create a domain model at conceptual level - BEGINS
//			Competence domainModelingComp = new  Competence();
//			domainModelingComp.setTitle("Ability to create a domain model at conceptual level");
//			domainModelingComp.setMaker(zoranJeremic);
//			domainModelingComp.addTag(getTag("OWL"));
//			domainModelingComp.addTag(getTag("Domain modeling"));
//			domainModelingComp.addTag(getTag("Semantic Web"));
//			domainModelingComp.addTag(getTag("Linked Data"));
//			domainModelingComp.addTag(getTag("ontology engineering"));
//			domainModelingComp.setVisibility(VisibilityType.PUBLIC);
//			domainModelingComp = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingComp);
			
//			CompetenceRequirement domModelingCompRequirement = new CompetenceRequirement();
//			domModelingCompRequirement.setCompetence(domainModelingComp);
//			domModelingCompRequirement.setCompetenceLevel(advancedLevel);
//			domModelingCompRequirement.setVisibility(createPrivateVisibility());
//			domModelingCompRequirement.setMaker(zoranJeremic);
//			domModelingCompRequirement.setOrganization(ini);
//			domModelingCompRequirement = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domModelingCompRequirement);
			
			
//			LearningObjective domainModelingObjective = new LearningObjective();
//			domainModelingObjective.setTargetCompetence(domainModelingComp);
//			//domainModelingObjective.setOrganization(ini);
//			domainModelingObjective = ServiceLocator1.getInstance().getService(BaseManager.class).saveEntity(domainModelingObjective);
			 
//			ContentUnit modelingIntroContent = new ContentUnit();
//			modelingIntroContent.setTitle("Conceptual Modeling for Absolute Beginners");
//			modelingIntroContent.addTag(getTag("Conceptual Modeling"));
//			modelingIntroContent.addTag(getTag("Domain Modeling"));
//			modelingIntroContent.setHref(URI.create("http://www.slideshare.net/signer/introduction-and-conceptual-modelling"));
//			modelingIntroContent.setVisibility(VisibilityType.PUBLIC);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(modelingIntroContent);
//			
//			KnowledgeAsset modelingIntroKnowledgeAsset = new KnowledgeAsset();
//			modelingIntroKnowledgeAsset.setTitle(modelingIntroContent.getTitle());
//			modelingIntroKnowledgeAsset.setReferenceToContent(modelingIntroContent);
//			modelingIntroKnowledgeAsset.addTag(getTag("Ontology"));
//			modelingIntroKnowledgeAsset.setVisibility(createPrivateVisibility());
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(modelingIntroKnowledgeAsset);
			
			ResourceActivity learnModelingBasics = new ResourceActivity();
			learnModelingBasics.setTitle("Read the introductory content on Conceptual Modeling");
//			learnModelingBasics.addContent(modelingIntroContent);
//			learnModelingBasics.addRequiresKA(modelingIntroKnowledgeAsset);
			learnModelingBasics.setVisibility(VisibilityType.PUBLIC);
//			learnModelingBasics.addTag(getTag("Conceptual Modeling"));
//			learnModelingBasics.addTag(getTag("Domain Modeling"));
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnModelingBasics);
			
//			modelingIntroKnowledgeAsset.setActivity(learnModelingBasics);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(modelingIntroKnowledgeAsset);
			
//			Activity createDomainModel = new ResourceActivity();
//			createDomainModel.setTitle("Create your own domain model for a domain of choice");
//			////createDomainModel.setPrecedingActivity(learnModelingBasics);
//			createDomainModel.setVisibility(createPrivateVisibility());
//			createDomainModel.addTag(getTag("Domain Modeling"));
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(createDomainModel);
			
//			LearningPlan modelingLP = new LearningPlan();
//			modelingLP.setTitle("How to Model Things");
//			modelingLP.addActivity(learnModelingBasics);
//			modelingLP.addActivity(createDomainModel);
////			modelingLP.addObjective(domainModelingObjective);
//			modelingLP.setVisibility(createPrivateVisibility());
//			modelingLP.setMaker(zoranJeremic);
//			modelingLP.addTag(getTag("OWL"));
//			modelingLP.addTag(getTag("RDF"));
//			modelingLP.addTag(getTag("Semantic Web"));
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(modelingLP);	
			
			//C1: Ability to create a domain model at conceptual level - ENDS
			
			//C1.1: Ability to develop complex software systems - BEGINS
//			Competence softwEngComp = new  Competence();
//			softwEngComp.setTitle("Ability to develop complex software systems");
//			softwEngComp.setMaker(zoranJeremic);
//			softwEngComp.addTag(getTag("Software engineering"));
//			softwEngComp.setVisibility(createPrivateVisibility());
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngComp);
			
//			CompetenceRequirement softwEngCompRequirement = new CompetenceRequirement();
//			softwEngCompRequirement.setCompetence(softwEngComp);
//			softwEngCompRequirement.setCompetenceLevel(advancedLevel);
//			softwEngCompRequirement.setVisibility(VisibilityType.PUBLIC);
//			softwEngCompRequirement.setMaker(zoranJeremic);
//			softwEngCompRequirement.setOrganization(ini);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngCompRequirement);
			
			ResourceActivity softwConstrBasicsz = new ResourceActivity();
			softwConstrBasicsz.setTitle("Read about the software design strategies");
			//softwConstrBasicsz.addContent(softwConstrIntroContentz);
			//softwConstrBasicsz.addRequiresKA(softwConstrIntroAsset);
			softwConstrBasicsz.setVisibility(VisibilityType.PRIVATE);
			//softwConstrIntroAssetz.addTag(getTag("Design Strategies"));
			//softwConstrIntroAsset.addTag(getTag("Software quality"));
			//softwConstrIntroAsset.addTag(getTag("Process analysis"));
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrBasicsz);
			
//			LearningPlan softwEngLPx = new LearningPlan();
//			softwEngLPx.setTitle("How to Model software");
//			softwEngLPx.addActivity(softwConstrBasicsz);
			//softwEngLPx.addLearningActivity(softwEngModel);
			//softwEngLPx.addObjective(softwEngObjective);
//			softwEngLPx.addTag(getTag("Software modelling"));
//			softwEngLPx.setMaker(vladanDevedzic);
//			softwEngLPx.addTag(getTag("Application frameworks"));
//			softwEngLPx.setVisibility(VisibilityType.PUBLIC);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngLPx);	
			 
//			LearningPlan scrltvd= nodeCloneManager.cloneToReusedLearningTask(softwEngLPx, jelenaJovanovic);
//			scrltvd.addTag(getTag("Software quality"));
//			scrltvd.addTag(getTag("reliability"));
//			scrltvd.addTag(getTag("computability theory"));
//			scrltvd.addTag(getTag("requirement analysis"));
//			scrltvd.addTag(getTag("Design objectives"));
//			scrltvd.setTitle("Defining user requirements");
//			scrltvd.setVisibility(VisibilityType.PUBLIC); 
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(scrltvd);
//			
//			setActivitiesInReusedLTAsPublic(scrltvd, jelenaJovanovic);
			
//			TargetCompetence softwConstrCompTCvd = new TargetCompetence();
//			softwConstrCompTCvd.setCompetence(softwEngComp);
//			softwConstrCompTCvd.setCurrentTask(scrltvd);
//			softwConstrCompTCvd.setTargetLevel(advancedLevel);
//			softwConstrCompTCvd.setCurrentLevel(beginnerLevel);
//			softwConstrCompTCvd.setVisibility(createPrivateVisibility());
//			softwConstrCompTCvd.setMaker(jelenaJovanovic);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompTCvd);
//			TargetCompetence dlTC1=mapTargetCompetence(softwConstrCompTCvd);
			
			
//			ContentUnit softwEngIntroContent = new ContentUnit();
//			softwEngIntroContent.setTitle("Software engineering for Absolute Beginners");
//			softwEngIntroContent.setHref(URI.create("http://www.slideshare.net/signer/introduction-and-conceptual-modelling"));
//			softwEngIntroContent.setVisibility(VisibilityType.PUBLIC);
//			softwEngIntroContent.addTag(getTag("Software engineering"));
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngIntroContent);
//			
//			KnowledgeAsset softwEngIntroAsset = new KnowledgeAsset();
//			softwEngIntroAsset.setTitle(softwEngIntroContent.getTitle());
//			softwEngIntroAsset.setReferenceToContent(softwEngIntroContent);
//			softwEngIntroAsset.setVisibility(VisibilityType.PUBLIC);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngIntroAsset);
			
			ResourceActivity softwEngBasics = new ResourceActivity();
			softwEngBasics.setTitle("Read the introductory content on Software engineering");
//			softwEngBasics.addContent(softwEngIntroContent);
//			softwEngBasics.addRequiresKA(softwEngIntroAsset);
			softwEngBasics.setVisibility(VisibilityType.PUBLIC);
//			softwEngBasics.addTag(getTag("Software engineering"));
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngBasics);
			
//			softwEngIntroAsset.setActivity(softwEngBasics);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngIntroAsset);
//			
			ResourceActivity softwEngModel = new ResourceActivity();
			softwEngModel.setTitle("Develop your own application for a domain of choice");
			////createDomainModel.setPrecedingActivity(learnModelingBasics);
			softwEngModel.setVisibility(VisibilityType.PUBLIC);
//			softwEngModel.addTag(getTag("Programming"));
//			softwEngModel.addTag(getTag("Development"));
			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngModel);
			
//			LearningPlan softwEngLP = new LearningPlan();
//			softwEngLP.setTitle("How to Model software system");
//			softwEngLP.addActivity(softwEngBasics);
//			softwEngLP.addActivity(softwEngModel);
			//softwEngLP.addObjective(softwEngObjective);
//			softwEngLP.setMaker(nikolaMilikic);
//			softwEngLP.addTag(getTag("Software modelling"));
//			softwEngLP.addTag(getTag("Application frameworks"));
//			softwEngLP.setVisibility(VisibilityType.PUBLIC);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngLP);		
			//C1.1: Ability to develop complex software systems - ENDS
 
//			LearningPlan scrltDD=nodeCloneManager.cloneToReusedLearningTask(softwEngLP, draganDjuric);
//			scrltDD.addTag(getTag("Software quality"));
//			scrltDD.addTag(getTag("reliability"));
//			scrltDD.addTag(getTag("computability theory"));
//			scrltDD.addTag(getTag("automatic programming"));
//			scrltDD.addTag(getTag("Software requirements"));
//			scrltDD.addTag(getTag("rational approximation"));
//			scrltDD.addTag(getTag("Design Strategies"));
//			scrltDD.setTitle("Developing industrial software");
//			scrltDD.setVisibility(VisibilityType.PUBLIC);
//			setActivitiesInReusedLTAsPublic(scrltDD, draganDjuric);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(scrltDD);
			
//			TargetCompetence softwConstrCompTCSR = new TargetCompetence();
//			softwConstrCompTCSR.setCompetence(softwEngComp);
//			softwConstrCompTCSR.setCurrentTask(scrltDD);
//			softwConstrCompTCSR.setTargetLevel(advancedLevel);
//			softwConstrCompTCSR.setCurrentLevel(beginnerLevel);
//			//softwConstrCompTCSR.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCSR.setVisibility(VisibilityType.PUBLIC);
//			//softwConstrCompTCSR.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCSR.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCSR.setMaker(draganDjuric);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompTCSR);
//			TargetCompetence dlTC2=mapTargetCompetence(softwConstrCompTCSR);
			
			//C1.1: Ability to develop complex software systems - BEGINS
//			Competence softwConstrComp = new  Competence();
//			softwConstrComp.setTitle("Ability to construct industrial software systems");
//			softwConstrComp.setMaker(zoranJeremic);
//			softwConstrComp.setVisibility(VisibilityType.PUBLIC);
//			softwConstrComp.addTag(getTag("Software engineering"));
//			softwConstrComp.addTag(getTag("Software quality"));
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrComp);
			
//			CompetenceRequirement softwConstrCompRequirement = new CompetenceRequirement();
//			softwConstrCompRequirement.setCompetence(softwConstrComp);
//			softwConstrCompRequirement.setCompetenceLevel(advancedLevel);
//			softwConstrCompRequirement.setVisibility(VisibilityType.PUBLIC);
//			softwConstrCompRequirement.setMaker(zoranJeremic);
//			softwConstrCompRequirement.setOrganization(ini);
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompRequirement);
			
//			ContentUnit softwConstrIntroContent = new ContentUnit() ;
//			softwConstrIntroContent.setTitle("Software high-level design");
//			softwConstrIntroContent.setHref(URI.create("http://www.slideshare.net/signer/introduction-and-conceptual-modelling"));
//			softwConstrIntroContent.setVisibility(VisibilityType.PUBLIC);
//			softwConstrIntroContent.addTag(getTag("Software modelling"));
//			softwConstrIntroContent.addTag(getTag("Component composition"));
//			softwConstrIntroContent.addTag(getTag("Formal specification"));
//			ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrIntroContent);
//			
//			KnowledgeAsset softwConstrIntroAsset = new KnowledgeAsset();
//			softwConstrIntroAsset.setTitle(softwConstrIntroContent.getTitle());
//			softwConstrIntroAsset.setReferenceToContent(softwConstrIntroContent);
//			softwConstrIntroAsset.addTag(getTag("Software modelling"));
//			softwConstrIntroAsset.setVisibility(VisibilityType.PUBLIC);
//			softwConstrIntroAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrIntroAsset);
			
			ResourceActivity softwConstrBasics = new ResourceActivity();
			softwConstrBasics.setTitle("Read about the software design strategies");
//			softwConstrBasics.addContent(softwConstrIntroContent);
//			softwConstrBasics.addRequiresKA(softwConstrIntroAsset);
			softwConstrBasics.setVisibility(VisibilityType.PUBLIC);
//			softwConstrBasics.addTag(getTag("Design Strategies"));
//			softwConstrBasics.addTag(getTag("Software quality"));
//			softwConstrBasics.addTag(getTag("Process analysis"));
			softwConstrBasics = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrBasics);
			
//			softwConstrIntroAsset.setActivity(softwConstrBasics);
//			softwConstrIntroAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrIntroAsset);
			
			ResourceActivity softwConstrModel = new ResourceActivity();
			softwConstrModel.setTitle("Develop design documentation: case study");
			////createDomainModel.setPrecedingActivity(learnModelingBasics);
			softwConstrModel.setVisibility(VisibilityType.PUBLIC);
//			softwConstrModel.addTag(getTag("Version management"));
//			softwConstrModel.addTag(getTag("Design Strategies"));
//			softwConstrModel.addTag(getTag("Software quality"));
//			softwConstrModel.addTag(getTag("Formal specification"));
			softwConstrModel = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrModel);
			
//			LearningPlan softwConstrLP = new LearningPlan();
//			softwConstrLP.setTitle("How to interpret software requirements");
//			softwConstrLP.addActivity(softwConstrBasics);
//			softwConstrLP.addActivity(softwConstrModel);
//			softwConstrLP.setMaker(bojanTomic);
			//softwConstrLP.addObjective(softwConstrObjective);
//			softwConstrLP.setVisibility(VisibilityType.PUBLIC);
//			softwConstrLP.addTag(getTag("Software quality"));
//			softwConstrLP.addTag(getTag("Software requirements"));
//			softwConstrLP.addTag(getTag("Design Strategies"));
//			softwConstrLP = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrLP);		
// 
//			LearningPlan scrltDD2=nodeCloneManager.cloneToReusedLearningTask(softwEngLP, sonjaRadenkovic);
//			scrltDD2.addTag(getTag("Software quality"));
//			scrltDD2.addTag(getTag("reliability"));
//			scrltDD2.addTag(getTag("computability theory"));
//			scrltDD2.addTag(getTag("Design Strategies"));
//			scrltDD2.setTitle("Developing domain specific applications");
//			scrltDD2.setVisibility(VisibilityType.PUBLIC);
//			scrltDD2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(scrltDD2);
//			
//			setActivitiesInReusedLTAsPublic(scrltDD2, sonjaRadenkovic);
			
//			TargetCompetence softwConstrCompTCDD = new TargetCompetence();
//			softwConstrCompTCDD.setCompetence(softwConstrComp);
//			softwConstrCompTCDD.setCurrentTask(scrltDD2);
//			softwConstrCompTCDD.setTargetLevel(advancedLevel);
//			softwConstrCompTCDD.setCurrentLevel(beginnerLevel);
//			//softwConstrCompTCDD.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCDD.setVisibility(VisibilityType.PUBLIC);
//			//softwConstrCompTCDD.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCDD.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCDD.setMaker(sonjaRadenkovic);
//			softwConstrCompTCDD = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompTCDD);
//			
//			org.prosolo.domainmodel.user.TargetCompetence dlTC3=mapTargetCompetence(softwConstrCompTCDD);
 
//			LearningPlan scrltVDx=nodeCloneManager.cloneToReusedLearningTask(softwEngLP, draganDjuric);
//			scrltVDx.addTag(getTag("Software quality"));
//			scrltVDx.addTag(getTag("reliability"));
//			scrltVDx.addTag(getTag("Software requirements"));
//			scrltVDx.addTag(getTag("Design Strategies"));
//			scrltVDx.setTitle("Specifying software requirements");
//			scrltVDx.setVisibility(VisibilityType.PUBLIC);
//			scrltVDx = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(scrltVDx);
//			
//			setActivitiesInReusedLTAsPublic(scrltVDx, draganDjuric);
			
//			TargetCompetence softwConstrCompTCVD = new TargetCompetence();
//			softwConstrCompTCVD.setCompetence(softwConstrComp);
//			softwConstrCompTCVD.setCurrentTask(scrltVDx);
//			softwConstrCompTCVD.setTargetLevel(advancedLevel);
//			softwConstrCompTCVD.setCurrentLevel(beginnerLevel);
//			//softwConstrCompTCVD.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCVD.setVisibility(VisibilityType.PUBLIC);
//			//softwConstrCompTCVD.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCVD.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCVD.setMaker(draganDjuric);
//			softwConstrCompTCVD = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompTCVD);
//			
//			org.prosolo.domainmodel.user.TargetCompetence dlTC4=mapTargetCompetence(softwConstrCompTCVD);
			
			ResourceActivity softwConstrBasics2 = new ResourceActivity();
			softwConstrBasics2.setTitle("Software design strategies - introduction");
//			softwConstrBasics2.addContent(softwConstrIntroContent);
//			softwConstrBasics2.addRequiresKA(softwConstrIntroAsset);
			softwConstrBasics2.setVisibility(VisibilityType.PUBLIC);
//			softwConstrBasics2.addTag(getTag("Design Quality"));
//			softwConstrBasics2.addTag(getTag("Software quality"));
//			softwConstrBasics2.addTag(getTag("Process analysis"));
			softwConstrBasics2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrBasics2);
			
//			softwConstrIntroAsset.setActivity(softwConstrBasics);
//			softwConstrIntroAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrIntroAsset);
			
			ResourceActivity softwConstrModel2 = new ResourceActivity();
			softwConstrModel2.setTitle("Develop design documentation: practice");
			////createDomainModel.setPrecedingActivity(learnModelingBasics);
			softwConstrModel2.setVisibility(VisibilityType.PUBLIC);
//			softwConstrModel2.addTag(getTag("Version control"));
//			softwConstrModel2.addTag(getTag("Design Strategies"));
//			softwConstrModel2.addTag(getTag("Requirements specification"));
//			softwConstrModel2.addTag(getTag("Formal specification"));
			
			softwConstrModel2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrModel2);
			
			
//			LearningPlan softwConstrLP2 = new LearningPlan();
//			softwConstrLP2.setTitle("How to model user requirements");
//			softwConstrLP2.addActivity(softwConstrBasics2);
//			softwConstrLP2.addActivity(softwConstrModel2);
			//softwConstrLP2.addObjective(softwConstrObjective);
//			softwConstrLP2.setMaker(zoranJeremic);
//			softwConstrLP2.setVisibility(VisibilityType.PUBLIC);
//			softwConstrLP2.addTag(getTag("Software quality"));
//			softwConstrLP2.addTag(getTag("Software requirements"));
//			softwConstrLP2.addTag(getTag("Design Strategies"));
//			softwConstrLP2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrLP2);	
 
//			LearningPlan scrltDDj2=nodeCloneManager.cloneToReusedLearningTask(softwConstrLP, jelenaJovanovic);
//			scrltDDj2.addTag(getTag("Software quality"));
//			scrltDDj2.addTag(getTag("reliability"));
//			scrltDDj2.addTag(getTag("computability theory"));
//			scrltDDj2.addTag(getTag("automatic programming"));
//			scrltDDj2.addTag(getTag("Software requirements"));
//			scrltDDj2.addTag(getTag("rational approximation"));
//			scrltDDj2.addTag(getTag("Design Strategies"));
//			scrltDDj2.setTitle("Most common design strategies");
//			scrltDDj2.setVisibility(VisibilityType.PUBLIC);
//			scrltDDj2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(scrltDDj2);
//			
//			setActivitiesInReusedLTAsPublic(scrltDDj2, jelenaJovanovic);
			
//			TargetCompetence softwConstrCompTCDD2 = new TargetCompetence();
//			softwConstrCompTCDD2.setCompetence(softwConstrComp);
//			softwConstrCompTCDD2.setCurrentTask(scrltDDj2);
//			softwConstrCompTCDD2.setTargetLevel(advancedLevel);
//			softwConstrCompTCDD2.setCurrentLevel(beginnerLevel);
//			//softwConstrCompTCDD2.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCDD2.setVisibility(VisibilityType.PUBLIC);
//			//softwConstrCompTCDD2.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCDD2.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCDD2.setMaker(jelenaJovanovic);
//			softwConstrCompTCDD2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompTCDD2);
//			
//			org.prosolo.domainmodel.user.TargetCompetence dlTC5=mapTargetCompetence(softwConstrCompTCDD2);
 
//			LearningPlan scrltDD3 = nodeCloneManager.cloneToReusedLearningTask(softwConstrLP, zoranJeremic);
//			scrltDD3.addTag(getTag("Software quality"));
//			scrltDD3.addTag(getTag("reliability"));
//			scrltDD3.addTag(getTag("computability theory"));
//			scrltDD3.addTag(getTag("automatic programming"));
//			scrltDD3.addTag(getTag("Software requirements"));
//			scrltDD3.addTag(getTag("rational approximation"));
//			scrltDD3.addTag(getTag("Design objectives"));
//			scrltDD3.setTitle("Defining user requirements");
//			scrltDD3.setVisibility(VisibilityType.PUBLIC);
//			scrltDD3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(scrltDD3);
//			
//			setActivitiesInReusedLTAsPublic(scrltDD3, zoranJeremic);
			
//			TargetCompetence softwConstrCompTCDD3 = new TargetCompetence();
//			softwConstrCompTCDD3.setCompetence(softwConstrComp);
//			softwConstrCompTCDD3.setCurrentTask(scrltDD3);
//			softwConstrCompTCDD3.setTargetLevel(advancedLevel);
//			softwConstrCompTCDD3.setCurrentLevel(beginnerLevel);
//			//softwConstrCompTCDD3.addCompetenceRecord(domainModelingCompCRecordNM);
//			softwConstrCompTCDD3.setVisibility(VisibilityType.PUBLIC);
//			//softwConstrCompTCDD3.setDeadline(deadlineCal.getTime());
//			//softwConstrCompTCDD3.setCompetenceProgress(domainModelingProgressNM);
//			softwConstrCompTCDD3.setMaker(zoranJeremic);
//			softwConstrCompTCDD3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwConstrCompTCDD3);
			//C1.1: Ability to develop complex software systems - ENDS
			
//			org.prosolo.domainmodel.user.TargetCompetence dlTC6=mapTargetCompetence(softwConstrCompTCDD3);
			
			//C2: Ability to formalize domain model using OWL ontology language - BEGINS
//			Competence owlModelingComp = new Competence();
//			owlModelingComp.setTitle("Ability to formalize domain model using OWL ontology language");
//			////owlModelingComp.addRequiredCompetence(domainModelingComp);
//			owlModelingComp.setMaker(zoranJeremic);
//			owlModelingComp.addTag(getTag("Linked Data"));
//			owlModelingComp.addTag(getTag("Semantic Web"));
//			owlModelingComp.addTag(getTag("OWL"));
//			owlModelingComp.addTag(getTag("RDF"));
//			owlModelingComp.addTag(getTag("ontology"));
//			owlModelingComp.setVisibility(VisibilityType.PUBLIC);
//			owlModelingComp = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlModelingComp);
			
//			CompetenceRequirement owlModelingCompRequirement = new CompetenceRequirement();
//			owlModelingCompRequirement.setCompetence(owlModelingComp);
//			owlModelingCompRequirement.setCompetenceLevel(advancedLevel);
//			owlModelingCompRequirement.setVisibility(VisibilityType.PUBLIC);
//			owlModelingCompRequirement.setMaker(zoranJeremic);
//			owlModelingCompRequirement.setOrganization(org);
//			owlModelingCompRequirement = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlModelingCompRequirement);
			
			//LP2.1: OWL Learning Path for Absolute Beginners - BEGINS
//			ContentUnit semwebIntroVideo = new ContentUnit();
//			semwebIntroVideo.setTitle("Video: A short Tutorial on Semantic Web");
//			semwebIntroVideo.setHref(URI.create("http://videolectures.net/training06_sure_stsw/"));
//			semwebIntroVideo.addTag(getTag("Semantic Web"));
//			semwebIntroVideo.addTag(getTag("ontology engineering"));
//			semwebIntroVideo.addAnnotation(createComment("very useful for getting an overall understanding of the SemWeb technology", bojanTomic));
//			////semwebIntroVideo.addUserRating(rating5);
//			semwebIntroVideo.setVisibility(VisibilityType.PUBLIC);
//			semwebIntroVideo = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(semwebIntroVideo);
//			
//			KnowledgeAsset semwebIntroVideoAsset = new KnowledgeAsset();
//			semwebIntroVideoAsset.setTitle(semwebIntroVideo.getTitle());
//			semwebIntroVideoAsset.setReferenceToContent(semwebIntroVideo);
//			semwebIntroVideoAsset.addTag(getTag("ontology"));
//			semwebIntroVideoAsset.setVisibility(VisibilityType.PUBLIC);
//			semwebIntroVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(semwebIntroVideoAsset);
			
//			ContentUnit semwebIntro = new ContentUnit();
//			semwebIntro.setTitle("Semantic Web - Introduction and Overview");
//			semwebIntro.setHref(URI.create("http://dret.net/netdret/docs/wilde-einiras2005-semweb/"));
//		//	addRandomTag(semwebIntro,3);
//			semwebIntro.addAnnotation(createComment("not very useful; hard to follow", zoranSevarac));
//			semwebIntro.setVisibility(VisibilityType.PUBLIC);
//			semwebIntro.addTag(getTag("Semantic Web"));
//			semwebIntro = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(semwebIntro);
//			
//			KnowledgeAsset semwebIntroAsset = new KnowledgeAsset();
//			semwebIntroAsset.setTitle(semwebIntro.getTitle());
//			semwebIntroAsset.setReferenceToContent(semwebIntro);
//			semwebIntroAsset.addTag(getTag("Semantic Web"));
//			semwebIntroAsset.setVisibility(VisibilityType.PUBLIC);
//			semwebIntroAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(semwebIntroAsset);
			
			ResourceActivity learnSemWebBasics = new ResourceActivity();
			learnSemWebBasics.setTitle("Learn the basics of the Semantic Web technologies");
		//	addRandomTag(learnSemWebBasics,4);
//			learnSemWebBasics.addContent(semwebIntroVideo);
//			learnSemWebBasics.addRequiresKA(semwebIntroVideoAsset);
//			learnSemWebBasics.addContent(semwebIntro);
//			learnSemWebBasics.addRequiresKA(semwebIntroAsset);
//			learnSemWebBasics.addTag(getTag("OWL"));
//			learnSemWebBasics.addTag(getTag("ontology"));
//			learnSemWebBasics.addTag(getTag("Linked Data"));
//			learnSemWebBasics.addTag(getTag("RDF"));
			learnSemWebBasics.setVisibility(VisibilityType.PUBLIC);
			learnSemWebBasics = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnSemWebBasics);
			
//			semwebIntroAsset.setActivity(learnSemWebBasics);
//			semwebIntroAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(semwebIntroAsset);
			
//			ContentUnit owlIntroVideo = new ContentUnit();
//			owlIntroVideo.setTitle("Video: An Introduction to OWL");
//			owlIntroVideo.setHref(URI.create("http://videolectures.net/training06_sure_stsw/"));
//			owlIntroVideo.addTag(getTag("OWL"));
//		//	addRandomTag(owlIntroVideo,4);
//			owlIntroVideo.addAnnotation(createComment("very interesting and useful - recommend", jelenaJovanovic));
//			////owlIntroVideo.addUserRating(rating5);
//			owlIntroVideo.setVisibility(VisibilityType.PUBLIC);
//			owlIntroVideo = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlIntroVideo);
			
//			KnowledgeAsset owlIntroVideoAsset = new KnowledgeAsset();
//			owlIntroVideoAsset.setTitle(owlIntroVideo.getTitle());
//			owlIntroVideoAsset.setReferenceToContent(owlIntroVideo);
//			owlIntroVideoAsset.addTag(getTag("OWL"));
//			owlIntroVideoAsset.setVisibility(VisibilityType.PUBLIC);
//			owlIntroVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlIntroVideoAsset);
			
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
//			owlPrimer.setVisibility(VisibilityType.PUBLIC);
//			owlPrimer = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlPrimer);
//			
//			KnowledgeAsset owlPrimerAsset = new KnowledgeAsset();
//			owlPrimerAsset.setTitle(owlPrimer.getTitle());
//			owlPrimerAsset.setReferenceToContent(owlPrimer);
//			owlPrimerAsset.addTag(getTag("OWL"));
//			owlPrimerAsset.addTag(getTag("RDF"));
//			owlPrimerAsset.setVisibility(VisibilityType.PUBLIC);
//			owlPrimerAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlPrimerAsset);
			
			ResourceActivity takeOWLTutorial = new ResourceActivity();
			takeOWLTutorial.setTitle("Take an OWL tutorial");
//			takeOWLTutorial.addTag(getTag("OWL"));
			////takeOWLTutorial.addUserRating(rating4);
			////takeOWLTutorial.setPrecedingActivity(learnSemWebBasics);
//			takeOWLTutorial.addContent(owlIntroVideo);
//			takeOWLTutorial.addRequiresKA(owlPrimerAsset);
//			takeOWLTutorial.addContent(owlPrimer);
//			takeOWLTutorial.addRequiresKA(owlPrimerAsset);
			takeOWLTutorial.setVisibility(VisibilityType.PUBLIC);
			takeOWLTutorial = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(takeOWLTutorial);
			
//			owlPrimerAsset.setActivity(takeOWLTutorial);
//			owlPrimerAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlPrimerAsset);
			
//			ContentUnit owlExamples = new ContentUnit();
//			owlExamples.setTitle("OWL Reasoning Examples and Hands-On Session");
//			owlExamples.setHref(URI.create("http://owl.cs.manchester.ac.uk/2009/07/sssw/"));
//		//	addRandomTag(owlExamples,2);
//			owlExamples.setVisibility(VisibilityType.PUBLIC);
//			owlExamples.addTag(getTag("Reasoning"));
//			owlExamples = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlExamples);
//			
//			KnowledgeAsset owlExamplesAsset = new KnowledgeAsset();
//			owlExamplesAsset.setTitle(owlExamples.getTitle());
//			owlExamplesAsset.setReferenceToContent(owlExamples);
//			owlExamplesAsset.addTag(getTag("Reasoning"));
//			owlExamplesAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlExamplesAsset);
			
			ResourceActivity practiceOWL = new ResourceActivity();
			practiceOWL.setTitle("Practice OWL through examples");
//			practiceOWL.addContent(owlExamples);
//			practiceOWL.addRequiresKA(owlExamplesAsset);
//			practiceOWL.addTag(getTag("ontology"));
//			practiceOWL.addTag(getTag("OWL"));
			////practiceOWL.setPrecedingActivity(takeOWLTutorial);
			practiceOWL.setVisibility(VisibilityType.PUBLIC);
			practiceOWL = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(practiceOWL);
			
//			owlExamplesAsset.setActivity(practiceOWL);
//			owlExamplesAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlExamplesAsset);
			
//			LearningPlan owlINILP = new LearningPlan();
//			owlINILP.setTitle("OWL for Absolute Beginners");
//			owlINILP.addActivity(learnSemWebBasics);
//			owlINILP.addActivity(takeOWLTutorial);
//			owlINILP.addActivity(practiceOWL);
//			owlINILP.setMaker(sonjaRadenkovic);
//			owlINILP.addObjective(owlModelingObjective);
//			owlINILP.addTag(getTag("OWL"));
//			owlINILP.addTag(getTag("Semantic Web"));
//			owlINILP.setVisibility(VisibilityType.PUBLIC);
//			owlINILP = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlINILP);
			//LP2.1: OWL Learning Path for Absolute Beginners - ENDS
			
			//LP2.2: Linked Data Learning Path: Learning OWL in Linked Data Way - BEGINS
			
//			ContentUnit linkedDataIntroVideo = new ContentUnit();
//			linkedDataIntroVideo.setTitle("Video: An Introduction to Linked Data");
//			linkedDataIntroVideo.setDescription("This talk will cover the basic concepts and techniques " +
//					"of publishing and using Linked Data, assuming some familiarity with programming and " +
//					"the Web. No prior knowledge of Semantic Web technologies is required.");
//			linkedDataIntroVideo.setHref(URI.create("http://vimeo.com/12444260"));
//			linkedDataIntroVideo.addTag(getTag("Linked Data"));
//			linkedDataIntroVideo.setVisibility(VisibilityType.PUBLIC);
//			linkedDataIntroVideo = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataIntroVideo);
//			
//			KnowledgeAsset linkedDataIntroVideoAsset = new KnowledgeAsset();
//			linkedDataIntroVideoAsset.setTitle(linkedDataIntroVideo.getTitle());
//			linkedDataIntroVideoAsset.setReferenceToContent(linkedDataIntroVideo);
//			linkedDataIntroVideoAsset.addTag(getTag("Linked Data"));
//			linkedDataIntroVideoAsset.setVisibility(VisibilityType.PUBLIC);
//			linkedDataIntroVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataIntroVideoAsset);
			
//			ContentUnit linkedDataSlides = new ContentUnit();
//			linkedDataSlides.setTitle("Slides: Hello Open Data World");
//			linkedDataSlides.setHref(URI.create("http://www.slideshare.net/terraces/hello-open-world-semtech-2009"));
//			linkedDataSlides.addTag(getTag("Linked Data"));
//			linkedDataSlides.addTag(getTag("Semantic Web"));
//			linkedDataSlides.addAnnotation(createComment("Good set of slides - amusing and useful", draganDjuric));
//			////linkedDataSlides.addUserRating(rating5);
//			linkedDataSlides.setVisibility(VisibilityType.PUBLIC);
//			linkedDataSlides = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataSlides);
//			
//			KnowledgeAsset linkedDataSlidesAsset = new KnowledgeAsset();
//			linkedDataSlidesAsset.setTitle(linkedDataSlides.getTitle());
//			linkedDataSlidesAsset.setReferenceToContent(linkedDataSlides);
//			linkedDataSlidesAsset.addTag(getTag("Linked Data"));
//			linkedDataSlidesAsset.setVisibility(VisibilityType.PUBLIC);
//			linkedDataSlidesAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataSlidesAsset);
			
			ResourceActivity learnLinkedDataBasics = new ResourceActivity();
			learnLinkedDataBasics.setTitle("Familiarize yourself with Linked Data concepts and principles");
//			learnLinkedDataBasics.addContent(linkedDataIntroVideo);
//			learnLinkedDataBasics.addRequiresKA(linkedDataIntroVideoAsset);
//			learnLinkedDataBasics.addContent(linkedDataSlides);
//			learnLinkedDataBasics.addTag(getTag("Linked Data"));
//			learnLinkedDataBasics.addRequiresKA(linkedDataSlidesAsset);
			learnLinkedDataBasics.setVisibility(VisibilityType.PUBLIC);
			learnLinkedDataBasics = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnLinkedDataBasics);
//			
//			linkedDataIntroVideoAsset.setActivity(learnLinkedDataBasics);
//			linkedDataIntroVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataIntroVideoAsset);
//			
//			linkedDataSlidesAsset.setActivity(learnLinkedDataBasics);
//			linkedDataSlidesAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataSlidesAsset);
			
			ResourceActivity takeOWLTutorial2 = new ResourceActivity();
			takeOWLTutorial2.setTitle("Take an OWL tutorial");
			////takeOWLTutorial2.setPrecedingActivity(learnLinkedDataBasics);
//			takeOWLTutorial2.addContent(owlIntroVideo);
//			takeOWLTutorial2.addRequiresKA(owlIntroVideoAsset);
//			takeOWLTutorial2.addContent(owlPrimer);
//			takeOWLTutorial2.addRequiresKA(owlPrimerAsset);
//			takeOWLTutorial2.addTag(getTag("OWL"));
			takeOWLTutorial2.setVisibility(VisibilityType.PUBLIC);
			takeOWLTutorial2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(takeOWLTutorial2);
			
//			ContentUnit ontologyReuseVideo = new ContentUnit();
//			ontologyReuseVideo.setTitle("Video: Learning from the Masters: Understanding Ontologies found on the Web ");
//			ontologyReuseVideo.setHref(URI.create("http://videolectures.net/iswc06_parsia_uofw/"));
//			ontologyReuseVideo.addTag(getTag("OWL"));
//			ontologyReuseVideo.addTag(getTag("ontology"));
//			ontologyReuseVideo.addAnnotation(createComment("difficult to follow, requires good understanding of owl and ontology development in general", nikolaMilikic));
//			////ontologyReuseVideo.addUserRating(rating3);
//			ontologyReuseVideo.setVisibility(VisibilityType.PUBLIC);
//			ontologyReuseVideo = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyReuseVideo);
//			
//			KnowledgeAsset ontologyReuseVideoAsset = new KnowledgeAsset();
//			ontologyReuseVideoAsset.setTitle(ontologyReuseVideo.getTitle());
//			ontologyReuseVideoAsset.setReferenceToContent(ontologyReuseVideo);
//			ontologyReuseVideoAsset.addTag(getTag("ontology"));
//			ontologyReuseVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyReuseVideoAsset);
			
			ResourceActivity learnToReuseOntologies = new ResourceActivity();
			learnToReuseOntologies.setTitle("Learn how to make use of the existing ontologies on the Web");
//			learnToReuseOntologies.addContent(ontologyReuseVideo);
//			learnToReuseOntologies.addRequiresKA(ontologyReuseVideoAsset);
//			learnToReuseOntologies.addTag(getTag("ontology"));
			////learnToReuseOntologies.setPrecedingActivity(takeOWLTutorial2);
			learnToReuseOntologies.setVisibility(VisibilityType.PUBLIC);
			learnToReuseOntologies = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnToReuseOntologies);
			
//			ontologyReuseVideoAsset.setActivity(learnToReuseOntologies);
//			ontologyReuseVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyReuseVideoAsset);
			
//			LearningPlan linkedDataLP = new LearningPlan();
//			linkedDataLP.setTitle("Learning OWL in Linked Data Way");
//			linkedDataLP.addActivity(learnLinkedDataBasics);
//			linkedDataLP.addActivity(takeOWLTutorial2);
//			linkedDataLP.addActivity(learnToReuseOntologies);
//			linkedDataLP.setMaker(vladanDevedzic);
//			linkedDataLP.addTag(getTag("ontology"));
//			linkedDataLP.addTag(getTag("Linked Data"));
//			linkedDataLP.addTag(getTag("Semantic Web"));
//			linkedDataLP.addObjective(owlModelingObjective);
//			linkedDataLP.setVisibility(VisibilityType.PUBLIC);
//			linkedDataLP = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedDataLP);
			
			//LP2.2: Linked Data Learning Path: Learning OWL in Linked Data Way - ENDS
			
			//LP2.3: Personal Learning Path: My Path Towards OWL Mastery - BEGINS
						
//			ContentUnit linkedData4Dummies = new ContentUnit();
//			linkedData4Dummies.setTitle("Linked Data in Plain English");
//			linkedData4Dummies.setDescription("Tim Berners Lee illustrates Linked Data through an analogy " +
//					"with a bag of potato chips");
//			linkedData4Dummies.setHref(URI.create("http://www.w3.org/QA/2010/05/linked_data_its_is_not_like_th.html"));
//		//	addRandomTag(linkedData4Dummies,3);
//			linkedData4Dummies.setVisibility(VisibilityType.PUBLIC);
//			linkedData4Dummies.addTag(getTag("Linked Data"));
//			linkedData4Dummies = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedData4Dummies);
//			
//			KnowledgeAsset linkedData4DummiesAsset = new KnowledgeAsset();
//			linkedData4DummiesAsset.setTitle(linkedData4Dummies.getTitle());
//			linkedData4DummiesAsset.setReferenceToContent(linkedData4Dummies);
//			linkedData4DummiesAsset.addTag(getTag("Linked Data"));
//			linkedData4DummiesAsset.setVisibility(VisibilityType.PUBLIC);
//			linkedData4DummiesAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedData4DummiesAsset);
			
			ResourceActivity learnLinkedDataBasics2 = new ResourceActivity();
			learnLinkedDataBasics2.setTitle("Familiarize yourself with Linked Data concepts and principles");
//			learnLinkedDataBasics2.addContent(linkedDataSlides);
//			learnLinkedDataBasics2.addRequiresKA(linkedDataSlidesAsset);
//			learnLinkedDataBasics2.addContent(linkedData4Dummies);
//			learnLinkedDataBasics2.addRequiresKA(linkedData4DummiesAsset);
//			learnLinkedDataBasics2.addTag(getTag("Linked Data"));
//			learnLinkedDataBasics2.addTag(getTag("Semantic Web"));
			learnLinkedDataBasics2.setVisibility(VisibilityType.PUBLIC);
			learnLinkedDataBasics2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnLinkedDataBasics2);
			
//			linkedData4DummiesAsset.setActivity(learnLinkedDataBasics2);
//			linkedData4DummiesAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(linkedData4DummiesAsset);
			
//			ContentUnit owlVideo = new ContentUnit();
//			owlVideo.setTitle("Video: OWL");
//			owlVideo.setHref(URI.create("http://videolectures.net/koml04_harmelen_o/"));
//			owlVideo.addTag(getTag("OWL"));
//			owlVideo.setVisibility(VisibilityType.PUBLIC);
//			owlVideo = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlVideo);
//			
//			KnowledgeAsset owlVideoAsset = new KnowledgeAsset();
//			owlVideoAsset.setTitle(owlVideo.getTitle());
//			owlVideoAsset.setReferenceToContent(owlVideo);
//			owlVideoAsset.addTag(getTag("OWL"));
//			owlVideoAsset.setVisibility(VisibilityType.PUBLIC);
//			owlVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlVideoAsset);
						
			ResourceActivity learnOWLBasics = new ResourceActivity();
			learnOWLBasics.setTitle("Learn the basics of OWL language");
//			learnOWLBasics.addContent(owlVideo);
//			learnOWLBasics.addRequiresKA(owlVideoAsset);
//			learnOWLBasics.addContent(owlPrimer);
//			learnOWLBasics.addRequiresKA(owlPrimerAsset);
//			learnOWLBasics.addTag(getTag("OWL"));
//			learnOWLBasics.addTag(getTag("RDF"));
//			learnOWLBasics.addTag(getTag("ontology"));
			////learnOWLBasics.setPrecedingActivity(learnLinkedDataBasics2);
			learnOWLBasics.setVisibility(VisibilityType.PUBLIC);
			learnOWLBasics = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnOWLBasics);
			
//			owlVideoAsset.setActivity(learnOWLBasics);
//			owlVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlVideoAsset);
//			
//			ContentUnit ontMethodologiesVideo = new ContentUnit();
//			ontMethodologiesVideo.setTitle("Video: Ontology Engineering Methodologies");
//			ontMethodologiesVideo.setHref(URI.create("http://videolectures.net/iswc07_perez_oem/"));
//			ontMethodologiesVideo.addTag(getTag("ontology engineering"));
//			////ontMethodologiesVideo.addUserRating(rating4);
//			ontMethodologiesVideo.setVisibility(VisibilityType.PUBLIC);
//			ontMethodologiesVideo = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontMethodologiesVideo);
			
//			KnowledgeAsset ontMethodologiesVideoAsset = new KnowledgeAsset();
//			ontMethodologiesVideoAsset.setTitle(ontMethodologiesVideo.getTitle());
//			ontMethodologiesVideoAsset.setReferenceToContent(ontMethodologiesVideo);
//			ontMethodologiesVideoAsset.addTag(getTag("ontology engineering"));
//			ontMethodologiesVideoAsset.setVisibility(VisibilityType.PUBLIC);
//			ontMethodologiesVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontMethodologiesVideoAsset);
			
			ResourceActivity learnOntEngMethodlogies = new ResourceActivity();
			learnOntEngMethodlogies.setTitle("Learn about ontology engineering methodologies");
//			learnOntEngMethodlogies.addContent(ontMethodologiesVideo);
//			learnOntEngMethodlogies.addTag(getTag("ontology engineering"));
//			learnOntEngMethodlogies.addRequiresKA(ontMethodologiesVideoAsset);
			////learnOntEngMethodlogies.setPrecedingActivity(learnOWLBasics);
			learnOntEngMethodlogies.setVisibility(VisibilityType.PUBLIC);
			learnOntEngMethodlogies = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnOntEngMethodlogies);
			
//			ontMethodologiesVideoAsset.setActivity(learnOntEngMethodlogies);
//			ontMethodologiesVideoAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontMethodologiesVideoAsset);
			
//			ContentUnit owlOnSemOverflow = new ContentUnit();
//			owlOnSemOverflow.setTitle("OWL-related discussions on semanticoveflow.com ");
//			owlOnSemOverflow.setHref(URI.create("http://www.semanticoverflow.com/questions/tagged/owl"));
//			owlOnSemOverflow.setVisibility(VisibilityType.PUBLIC);
//			owlOnSemOverflow.addTag(getTag("Semantic Web"));
//			owlOnSemOverflow.addTag(getTag("OWL"));
//			owlOnSemOverflow = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlOnSemOverflow);
			
//			KnowledgeAsset owlOnSemOverflowAsset = new KnowledgeAsset();
//			owlOnSemOverflowAsset.setTitle(owlOnSemOverflow.getTitle());
//			owlOnSemOverflowAsset.setReferenceToContent(owlOnSemOverflow);
//			owlOnSemOverflowAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlOnSemOverflowAsset);
			
			ResourceActivity learnFromPractitioners = new ResourceActivity();
			learnFromPractitioners.setTitle("Check the discussion about OWL at practitioners forums");
//			learnFromPractitioners.addContent(owlOnSemOverflow);
//			learnFromPractitioners.addRequiresKA(owlOnSemOverflowAsset);
//			learnFromPractitioners.addTag(getTag("OWL"));
			////learnFromPractitioners.setPrecedingActivity(learnOntEngMethodlogies);
			learnFromPractitioners.setVisibility(VisibilityType.PUBLIC);
			learnFromPractitioners = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(learnFromPractitioners);
			
//			owlOnSemOverflowAsset.setActivity(learnFromPractitioners);
//			owlOnSemOverflowAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(owlOnSemOverflowAsset);
			
//			LearningPlan personalOWLLP = new LearningPlan();
//			personalOWLLP.setTitle("My Path Towards OWL Mastery");
//			personalOWLLP.addActivity(learnLinkedDataBasics2);
//			personalOWLLP.addActivity(learnOWLBasics);
//			personalOWLLP.addActivity(learnOntEngMethodlogies);
//			personalOWLLP.addActivity(learnFromPractitioners);
//			personalOWLLP.addTag(getTag("OWL"));
//			personalOWLLP.setMaker(jelenaJovanovic);
//			personalOWLLP.setVisibility(VisibilityType.PUBLIC);
			//personalOWLLP.setMaker(maker);
//			personalOWLLP = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(personalOWLLP);
 
//			LearningPlan reusLearnTaskVD = nodeCloneManager.cloneToReusedLearningTask(softwEngLPx, vladanDevedzic);
//			reusLearnTaskVD.addTag(getTag("OWL"));
//			reusLearnTaskVD.addTag(getTag("Semantic Web education"));
//			reusLearnTaskVD.addTag(getTag("education"));
//			reusLearnTaskVD.setTitle("Semantic Web in education");
//			reusLearnTaskVD.setVisibility(VisibilityType.PUBLIC);
//			reusLearnTaskVD = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(reusLearnTaskVD);
//			
//			setActivitiesInReusedLTAsPublic(reusLearnTaskVD, vladanDevedzic);
			
//			TargetCompetence personalOWLLPVlDev = new TargetCompetence();
//			personalOWLLPVlDev.setCompetence(softwEngComp);
//			personalOWLLPVlDev.setCurrentTask(reusLearnTaskVD);
//			personalOWLLPVlDev.setTargetLevel(advancedLevel);
//			personalOWLLPVlDev.setCurrentLevel(beginnerLevel);
//			personalOWLLPVlDev.setVisibility(VisibilityType.PUBLIC);
//			personalOWLLPVlDev.setMaker(vladanDevedzic);
//			personalOWLLPVlDev = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(personalOWLLPVlDev);
//			TargetCompetence dlTC7=mapTargetCompetence(personalOWLLPVlDev);
			
//			CompetenceRecord personalOWLCRecord = new CompetenceRecord();
//			personalOWLCRecord.setCompetence(owlModelingComp);
//			personalOWLCRecord.setRecordedDate(new Date());
//			personalOWLCRecord.setRecordedDate(setPreviousDate());
////			personalOWLCRecord.setRecordedLevel(intermediateLevel);
//			//personalOWLCRecord.setVisibility(VisibilityType.PUBLIC);
//			personalOWLCRecord = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(personalOWLCRecord);
			
			Date personalOWLTCDeadline = new Date();
			Calendar personalOWLTCCal = Calendar.getInstance();
			personalOWLTCCal.setTime(personalOWLTCDeadline);
			personalOWLTCCal.add(Calendar.DATE, 10);
			
//			Progress personalOWLProgress = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewProgress();
			
//			TargetCompetence personalOWLTC = new TargetCompetence();
//			personalOWLTC.setCurrentTask(personalOWLLP);
//			personalOWLTC.setCompetence(owlModelingComp);
//			personalOWLTC.setTargetLevel(advancedLevel);
//			personalOWLTC.setCurrentLevel(intermediateLevel);
//			personalOWLTC.addCompetenceRecord(personalOWLCRecord);
//			personalOWLTC.setVisibility(VisibilityType.PUBLIC);
//			personalOWLTC.setDeadline(personalOWLTCCal.getTime());
////			personalOWLTC.setProgress(personalOWLProgress);
//			personalOWLTC.setProgress(0);
//			personalOWLTC = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(personalOWLTC);
			
			//LP2.3: Personal Learning Path: My Path Towards OWL Mastery - ENDS
	
			//C2: Ability to formalize domain model using OWL ontology language - ENDS
			
			//C3: Ability to use the Protege ontology editor for ontology development - BEGINS
//			Competence protegeComp = new Competence();
//			protegeComp.setTitle("Ability to use the Protege ontology editor for ontology development");
////			protegeComp.addRequiredCompetence(owlModelingComp);
//			protegeComp.addTag(getTag("Protege"));
//			protegeComp.addTag(getTag("ontology engineering"));
//			protegeComp.addTag(getTag("ontology development"));
//			protegeComp.setVisibility(VisibilityType.PUBLIC);
//			protegeComp = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(protegeComp);
			
//			CompetenceRequirement protegeCompRequirement = new CompetenceRequirement();
//			protegeCompRequirement.setCompetence(protegeComp);
//			protegeCompRequirement.setCompetenceLevel(advancedLevel);
//			protegeCompRequirement.setVisibility(VisibilityType.PUBLIC);
//			protegeCompRequirement = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(protegeCompRequirement);
			
//			ContentUnit protegeTutorial = new ContentUnit();
//			protegeTutorial.setTitle("Protege OWL tutorial");
//			protegeTutorial.setHref(URI.create("http://owl.cs.manchester.ac.uk/tutorials/protegeowltutorial/"));
//			protegeTutorial.setVisibility(VisibilityType.PUBLIC);
//			protegeTutorial = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(protegeTutorial);
//			
//			KnowledgeAsset protegeTutorialAsset = new KnowledgeAsset();
//			protegeTutorialAsset.setTitle(protegeTutorial.getTitle());
//			protegeTutorialAsset.setReferenceToContent(protegeTutorial);
//			protegeTutorialAsset.addTag(getTag("Protege"));
//			protegeTutorialAsset.setVisibility(VisibilityType.PUBLIC);
//			protegeTutorialAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(protegeTutorialAsset);
			
			ResourceActivity takeProtegeTutorial = new ResourceActivity();
			takeProtegeTutorial.setTitle("Take a Protege OWL tutorial");
//			takeProtegeTutorial.addContent(protegeTutorial);
//			takeProtegeTutorial.addRequiresKA(protegeTutorialAsset);
//			takeProtegeTutorial.addTag(getTag("Protege"));
//			takeProtegeTutorial.addTag(getTag("OWL"));
			////takeProtegeTutorial.addUserRating(rating5);
			takeProtegeTutorial.setVisibility(VisibilityType.PUBLIC);
			takeProtegeTutorial = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(takeProtegeTutorial);
			
//			protegeTutorialAsset.setActivity(takeProtegeTutorial);
//			protegeTutorialAsset = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(protegeTutorialAsset);
			
			ResourceActivity practiceExamples = new ResourceActivity();
			practiceExamples.setTitle("Do some practice through examples");
			////practiceExamples.addUserRating(rating2);
//			practiceExamples.addContent(owlExamples);
//			practiceExamples.addRequiresKA(owlExamplesAsset);
//			practiceExamples.addTag(getTag("Protege"));
			////practiceExamples.setPrecedingActivity(takeProtegeTutorial);
			practiceExamples.setVisibility(VisibilityType.PUBLIC);
			practiceExamples = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(practiceExamples);
			
			ResourceActivity createOntology = new ResourceActivity();
			createOntology.setTitle("Create in Protege your own OWL ontology for the chosen domain");
			////createOntology.setPrecedingActivity(practiceExamples);
			createOntology.setVisibility(VisibilityType.PUBLIC);
//			createOntology.addTag(getTag("Protege"));
//			createOntology.addTag(getTag("OWL"));
//			createOntology.addTag(getTag("ontology"));
			createOntology = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(createOntology);
			
//			LearningPlan protegeLP = new LearningPlan();
//			protegeLP.setTitle("Best Practice: Stanford Guide to Protege and OWL");
//			protegeLP.addActivity(takeProtegeTutorial);
//			protegeLP.addActivity(practiceExamples);
//			protegeLP.addActivity(createOntology);
//			protegeLP.addObjective(protegeObjective);
//			protegeLP.addTag(getTag("Protege"));
//			protegeLP.addTag(getTag("OWL"));
//			protegeLP.setMaker(draganDjuric);
//			protegeLP.setVisibility(VisibilityType.PUBLIC);
//			protegeLP = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(protegeLP);	
			//C3: Ability to use the Protege ontology editor for ontology development - ENDS

//			Duty ontDevDuty = new Duty();
//			ontDevDuty.setTitle("Ontology Development");
//			ontDevDuty.addRequiredCompetence(domModelingCompRequirement);
//			ontDevDuty.addRequiredCompetence(owlModelingCompRequirement);
//			ontDevDuty.addRequiredCompetence(protegeCompRequirement);
//			ontDevDuty.addTag(getTag("ontology development"));
//			ontDevDuty.addTag(getTag("ontology engineering"));
//			ontDevDuty.setVisibility(VisibilityType.PUBLIC);
//			ontDevDuty = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontDevDuty);
			
//			iniSeniorProgrammerOrgPosition.addAssignedDuty(ontDevDuty);
//			iniSeniorProgrammerOrgPosition = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(iniSeniorProgrammerOrgPosition);
//			
//			fosSeniorProgrammerOrgPosition.addAssignedDuty(ontDevDuty);
//			fosSeniorProgrammerOrgPosition = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(fosSeniorProgrammerOrgPosition);
//			
//			fosTeachingAssistantPosition.addAssignedDuty(ontDevDuty);
//			fosTeachingAssistantPosition = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(fosTeachingAssistantPosition);
			
//			Duty softwEngDevDuty = new Duty();
//			softwEngDevDuty.setTitle("Software Systems Development");
//			softwEngDevDuty.addRequiredCompetence(softwEngCompRequirement);
//			softwEngDevDuty.addRequiredCompetence(softwConstrCompRequirement);
//			 
//			softwEngDevDuty = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(softwEngDevDuty);
			
//			iniSeniorProgrammerOrgPosition.addAssignedDuty(softwEngDevDuty);
//			iniSeniorProgrammerOrgPosition = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(iniSeniorProgrammerOrgPosition);
//			
//			fosSeniorProgrammerOrgPosition.addAssignedDuty(softwEngDevDuty);
//			fosSeniorProgrammerOrgPosition = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(fosSeniorProgrammerOrgPosition);
//			
//			fosTeachingAssistantPosition.addAssignedDuty(softwEngDevDuty);
//			fosTeachingAssistantPosition = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(fosTeachingAssistantPosition);
			
			for(User us:allUsers){
				//User us=zoranJeremic;
//				SocialStream sStream=new SocialStream();
//				sStream = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(sStream);
//	 			us.setSocialStream(sStream);
//	 			PersonalCalendar pCalendar=new PersonalCalendar();
//				pCalendar=ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(pCalendar);
//				us.setPersonalCalendar(pCalendar);
//	 			us = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(us);
	  			// addRandomFollowedEntity(us,4);
				//addEventsExamples(us,nugs);
				addUserPreferences(us);
			}

//			Project op4lProject = new Project();
//			op4lProject.setTitle("OP4L project");
//			//op4lProject.setUserOrganization(fos);
//			op4lProject = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(op4lProject);
			
//			Team goodOldAiTeam = new Team();
//			goodOldAiTeam.setTitle("GOOD OLD AI Team");
//			goodOldAiTeam.setProject(op4lProject);
//			goodOldAiTeam.setTeamInitiator(nikolaMilikic);
//			goodOldAiTeam = ServiceLocator1.getInstance().getService(BaseManager.class).saveEntity(goodOldAiTeam);
//			
//			TeamMembership nikolaGoodOldAiTeamMembership = new TeamMembership();
//			nikolaGoodOldAiTeamMembership.setUser(nikolaMilikic);
//			nikolaGoodOldAiTeamMembership.setTeam(goodOldAiTeam);
//			 
//			nikolaGoodOldAiTeamMembership.setStatus(MembershipStatus.ACCEPTED);
//			nikolaGoodOldAiTeamMembership = ServiceLocator1.getInstance().getService(BaseManager.class).saveEntity(nikolaGoodOldAiTeamMembership);
//			nikolaMilikic.addMembership(nikolaGoodOldAiTeamMembership);
//			nikolaMilikic = ServiceLocator1.getInstance().getService(BaseManager.class).updateResource(nikolaMilikic);
//			goodOldAiTeam.addMembership(nikolaGoodOldAiTeamMembership);
//			goodOldAiTeam = ServiceLocator1.getInstance().getService(BaseManager.class).saveEntity(goodOldAiTeam);
			

//			Bookmark rdfW3cBookamark = new Bookmark();
//			rdfW3cBookamark.setAuthor(nikolaMilikic);
//			rdfW3cBookamark.setBookmarkURL("http://www.w3.org/RDF/");
//			rdfW3cBookamark.setTitle("RDF - Semantic Web Standards");
//			rdfW3cBookamark.setDescription("RDF is a standard model for data interchange on the Web.");
//			rdfW3cBookamark.addTag(getTag("rdf"));
//			rdfW3cBookamark.addTag(getTag("w3c"));
//			rdfW3cBookamark.addTag(getTag("semantic web"));
//			rdfW3cBookamark.setVisibility(VisibilityType.PUBLIC);
//			rdfW3cBookamark = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(rdfW3cBookamark);
//			
//			AnnotationEvent rdfW3cBookamarkEvent = new AnnotationEvent(EventType.BookmarkEvent);
//			rdfW3cBookamarkEvent.setAnnotationRef(rdfW3cBookamark);
//			rdfW3cBookamarkEvent.setPerformedBy(zoranJeremic);
//			rdfW3cBookamarkEvent.setTimeStamp(new Date());
//			rdfW3cBookamarkEvent.setVisibility(VisibilityType.PUBLIC);
//			rdfW3cBookamarkEvent = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(rdfW3cBookamarkEvent);
//			
//			Bookmark semanticWebBookamark = new Bookmark();
//			semanticWebBookamark.setAuthor(zoranJeremic);
//			semanticWebBookamark.setBookmarkURL("http://semanticweb.org");
//			semanticWebBookamark.setTitle("semanticweb.org");
//			semanticWebBookamark.setDescription("The Semantic Web is the extension of the World Wide Web that enables people to share content beyond the boundaries of applications and websites.");
//			semanticWebBookamark.addTag(getTag("rdf"));
//			semanticWebBookamark.addTag(getTag("semantic web"));
//			semanticWebBookamark.setVisibility(VisibilityType.PUBLIC);
//			semanticWebBookamark = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(semanticWebBookamark);
//			
//			AnnotationEvent semanticWebBookamarkEvent=new AnnotationEvent(EventType.BookmarkEvent);
//			semanticWebBookamarkEvent.setAnnotationRef(semanticWebBookamark);
//			semanticWebBookamarkEvent.setPerformedBy(zoranJeremic);
//			semanticWebBookamarkEvent.setTimeStamp(new Date());
//			semanticWebBookamarkEvent = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(semanticWebBookamarkEvent);
//			
//			Bookmark sparqlByExampleBookamark = new Bookmark();
//			sparqlByExampleBookamark.setAuthor(zoranJeremic);
//			sparqlByExampleBookamark.setBookmarkURL("http://www.cambridgesemantics.com/2008/09/sparql-by-example/");
//			sparqlByExampleBookamark.setTitle("SPARQL By Example");
//			sparqlByExampleBookamark.setDescription("SPARQL By Example - A Tutorial");
//			sparqlByExampleBookamark.addTag(getTag("sparql"));
//			sparqlByExampleBookamark.addTag(getTag("tutorial"));
//			sparqlByExampleBookamark.addTag(getTag("w3c"));
//			sparqlByExampleBookamark.setVisibility(VisibilityType.PUBLIC);
//			sparqlByExampleBookamark = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(sparqlByExampleBookamark);
//			
//			AnnotationEvent sparqlByExampleBookamarkEvent=new AnnotationEvent(EventType.BookmarkEvent);
//			sparqlByExampleBookamarkEvent.setAnnotationRef(sparqlByExampleBookamark);
//			sparqlByExampleBookamarkEvent.setPerformedBy(zoranJeremic);
//			sparqlByExampleBookamarkEvent.setTimeStamp(new Date());
//			sparqlByExampleBookamarkEvent = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(sparqlByExampleBookamarkEvent);
//			
//			Bookmark protegeDocsBookamark = new Bookmark();
//			protegeDocsBookamark.setAuthor(nikolaMilikic);
//			protegeDocsBookamark.setBookmarkURL("http://protege.stanford.edu/doc/sparql/");
//			protegeDocsBookamark.setTitle("Protege;: Using SPARQL in Protege-OWL");
//			protegeDocsBookamark.addTag(getTag("sparql"));
//			protegeDocsBookamark.addTag(getTag("documentation"));
//			protegeDocsBookamark.addTag(getTag("protege"));
//			protegeDocsBookamark.addTag(getTag("owl"));
//			protegeDocsBookamark.setVisibility(VisibilityType.PUBLIC);
//			protegeDocsBookamark = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(protegeDocsBookamark);
//			
//			AnnotationEvent protegeDocsBookamarkEvent=new AnnotationEvent(EventType.BookmarkEvent);
//			protegeDocsBookamarkEvent.setAnnotationRef(protegeDocsBookamark);
//			protegeDocsBookamarkEvent.setPerformedBy(nikolaMilikic);
//			protegeDocsBookamarkEvent.setTimeStamp(new Date()); 
//			protegeDocsBookamarkEvent = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(protegeDocsBookamarkEvent);
//			
//			Bookmark rdfW3cBookamark2 = new Bookmark();
//			rdfW3cBookamark2.setAuthor(nikolaMilikic);
//			rdfW3cBookamark2.setBookmarkURL("http://www.w3.org/2001/sw/");
//			rdfW3cBookamark2.setTitle("W3C Semantic Web Activitys");
//			rdfW3cBookamark2.addTag(getTag("rdf"));
//			rdfW3cBookamark2.addTag(getTag("w3c"));
//			rdfW3cBookamark2.addTag(getTag("semantic web"));
//			rdfW3cBookamark2.addTag(getTag("semantic web activity"));
//			rdfW3cBookamark2.setVisibility(VisibilityType.PUBLIC);
//			rdfW3cBookamark2 = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(rdfW3cBookamark2);
//			
//			AnnotationEvent rdfW3cBookamarkEvent2=new AnnotationEvent(EventType.BookmarkEvent);
//			rdfW3cBookamarkEvent2.setAnnotationRef(rdfW3cBookamark2);
//			rdfW3cBookamarkEvent2.setPerformedBy(nikolaMilikic);
//			rdfW3cBookamarkEvent2.setTimeStamp(new Date());
//			rdfW3cBookamarkEvent2 = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(rdfW3cBookamarkEvent2);
//			
//			Bookmark rdfW3cBookamark3 = new Bookmark();
//			rdfW3cBookamark3.setAuthor(nikolaMilikic);
//			rdfW3cBookamark3.setBookmarkURL("http://semanticarts.com/");
//			rdfW3cBookamark3.setTitle("Semantic Arts - Semantic and Service Oriented Architects");
//			rdfW3cBookamark3.setDescription("In a nutshell, we strive to help our clients remove complexity from existing systems. We do this by first understanding and documenting that complexity.");
//			rdfW3cBookamark3.addTag(getTag("rdf"));
//			rdfW3cBookamark3.addTag(getTag("w3c"));
//			rdfW3cBookamark3.addTag(getTag("semantic web"));
//			rdfW3cBookamark3.addTag(getTag("semantic web activity"));
//			rdfW3cBookamark3.setVisibility(VisibilityType.PUBLIC);
//			rdfW3cBookamark3 = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(rdfW3cBookamark3);
//			
//			AnnotationEvent rdfW3cBookamarkEvent3=new AnnotationEvent(EventType.BookmarkEvent);
//			rdfW3cBookamarkEvent3.setAnnotationRef(rdfW3cBookamark3);
//			rdfW3cBookamarkEvent3.setPerformedBy(nikolaMilikic);
//			rdfW3cBookamarkEvent3.setTimeStamp(new Date());
//			rdfW3cBookamarkEvent3 = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(rdfW3cBookamarkEvent3);
//			
//			Bookmark fourStoreBookamark = new Bookmark();
//			fourStoreBookamark.setAuthor(zoranJeremic);
//			fourStoreBookamark.setBookmarkURL("http://4store.org/trac/wiki/SparqlServer");
//			fourStoreBookamark.setTitle("SparqlServer 4store");
//			fourStoreBookamark.setDescription("4store includes a SPARQL HTTP protocol server, which can answer SPARQL queries using the standard SPARQL HTTP query protocol among other features.");
//			fourStoreBookamark.addTag(getTag("4store"));
//			fourStoreBookamark.addTag(getTag("sparql"));
//			fourStoreBookamark.addTag(getTag("rdf"));
//			fourStoreBookamark.addTag(getTag("repository"));
//			fourStoreBookamark.setVisibility(VisibilityType.PUBLIC);
//			fourStoreBookamark = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(fourStoreBookamark);
//
//			
//
//			AnnotationEvent fourStoreBookamarkEvent=new AnnotationEvent(EventType.BookmarkEvent);
//			fourStoreBookamarkEvent.setAnnotationRef(fourStoreBookamark);
//			fourStoreBookamarkEvent.setPerformedBy(zoranJeremic);
//			fourStoreBookamarkEvent.setTimeStamp(new Date());
//			fourStoreBookamarkEvent = ServiceLocator1.getInstance().getService(DefaultManager.class).saveEntity(fourStoreBookamarkEvent);
//			// adding the same Competence to the couple of users


			//Zoran Jeremic
//			Progress domainModelingProgressZJ = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewProgress();

			Date currentDate = new Date();
			Calendar deadlineCal = Calendar.getInstance();
			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, 3);
			
//			CompetenceRecord domainModelingCompCRecordZJ = new CompetenceRecord();
//			domainModelingCompCRecordZJ.setCompetence(domainModelingComp);
//			domainModelingCompCRecordZJ.setRecordedDate(new Date());
////			domainModelingCompCRecordZJ.setRecordedLevel(intermediateLevel);
//			domainModelingCompCRecordZJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompCRecordZJ);
			
//			LearningPlan zjRLT = nodeCloneManager.cloneToReusedLearningTask(modelingLP, zoranJeremic);
//			zjRLT.setTitle("Modeling in OWL");
//			zjRLT.addTag(getTag("Linked Data"));
//			zjRLT.addTag(getTag("Ontology modeling"));
//			zjRLT.addTag(getTag("RDF"));
//			zjRLT.addTag(getTag("OWL"));
//			zjRLT.addTag(getTag("Semantic Web"));
//			zjRLT.setVisibility(VisibilityType.PUBLIC);
//			zjRLT = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(zjRLT);
//			
//			setActivitiesInReusedLTAsPublic(zjRLT, zoranJeremic);
			
//			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, zjRLT, zjRLT.getVisibility(), null);
			
//			TargetCompetence domainModelingCompTCZJ = new TargetCompetence();
//			domainModelingCompTCZJ.setCompetence(domainModelingComp);
//			domainModelingCompTCZJ.setCurrentTask(zjRLT);
//			domainModelingCompTCZJ.setTargetLevel(advancedLevel);
//			domainModelingCompTCZJ.setCurrentLevel(intermediateLevel);
//			domainModelingCompTCZJ.addCompetenceRecord(domainModelingCompCRecordZJ);
//			domainModelingCompTCZJ.setVisibility(VisibilityType.PUBLIC);
//			domainModelingCompTCZJ.setDeadline(deadlineCal.getTime());
////			domainModelingCompTCZJ.setProgress(domainModelingProgressZJ);
//			domainModelingCompTCZJ.setProgress(0);
//			domainModelingCompTCZJ.setMaker(zoranJeremic);
//			domainModelingCompTCZJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompTCZJ);
			
//			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, domainModelingCompTCZJ, domainModelingCompTCZJ.getVisibility(), null);
//			EventFactory.getInstance().createChangeProgressEvent(zoranJeremic, 
//					domainModelingCompTCZJ, 
//					domainModelingProgressZJ.getUserProgressLevel(), 
//					domainModelingProgressZJ.getProgressScale(),
//					null);
			
			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, 10);
			
//			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, knowledgeModelingLGLabel, knowledgeModelingLGLabel.getVisibility(), null);
			
//			Progress ontologyLearningZJProgress = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewProgress();
			
//			LearningGoal ontologyLearningZJ = new LearningGoal();
//			ontologyLearningZJ.setTitle("To Learn Ontologies");
//			ontologyLearningZJ.setMaker(zoranJeremic);
//			ontologyLearningZJ.setDateCreated(new Date());
//			ontologyLearningZJ.addTag(getTag("ontology"));
////			ontologyLearningZJ.setPriority(Priority.HIGH);
//			ontologyLearningZJ.setDeadline(deadlineCal.getTime());
//			ontologyLearningZJ.setVisibility(VisibilityType.PUBLIC);
//		//	ontologyLearningZJ.addTargetCompetence(domainModelingCompTCZJ);
////			ontologyLearningZJ.setProgress(ontologyLearningZJProgress);
//			ontologyLearningZJ.setProgress(0);
//			ontologyLearningZJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyLearningZJ);
			
			List<Tag> ontologyLearningZJKeywords = new ArrayList<Tag>();
			ontologyLearningZJKeywords.add(getTag("ontology"));
			
//			LearningGoal ontologyLearningZJ = ServiceLocator.getInstance().
//					getService(LearningGoalManager.class).createNewLearningGoal(
//							zoranJeremic, 
//							"To Learn Ontologies", 
//							"It addresses foundational issues in building ontologies. The primary " +
//							"objective of the goal is to expose students to main questions in this " +
//							"area and to survey some currently fashionable tools and logics.", 
//							deadlineCal.getTime(), 
//							ontologyLearningZJKeywords);
			
//			mapLearningGoal(ontologyLearningZJ);
			
//			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, ontologyLearningZJ, ontologyLearningZJ.getVisibility(), null);
//			EventFactory.getInstance().createAddEvent(zoranJeremic, domainModelingCompTCZJ, ontologyLearningZJ, null);
//			EventFactory.getInstance().createChangeProgressEvent(zoranJeremic, ontologyLearningZJ, 
//					ontologyLearningZJProgress.getProgressValue(), 
//					ontologyLearningZJProgress.getProgressScale(),
//					null);
			
//			TargetLearningGoal ontologyLearningZJTargetGoal = ServiceLocator.getInstance().
//					getService(LearningGoalManager.class).createNewTargetLearningGoal(zoranJeremic, ontologyLearningZJ);
			
//			zoranJeremic.addLearningGoal(ontologyLearningZJTargetGoal);
			zoranJeremic = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(zoranJeremic);
			
			// Nikola Milikic
//			Progress domainModelingProgressNM = new Progress();
//			domainModelingProgressNM.setProgressScale(progressScale0To100);
//			domainModelingProgressNM.setProgressValue(60);
//			domainModelingProgressNM = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingProgressNM);

			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, -7);
			
//			CompetenceRecord domainModelingCompCRecordNM = new CompetenceRecord();
//			domainModelingCompCRecordNM.setCompetence(domainModelingComp);
//			domainModelingCompCRecordNM.setRecordedDate(new Date());
////			domainModelingCompCRecordNM.setRecordedLevel(beginnerLevel);
//			domainModelingCompCRecordNM = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompCRecordNM);
			
//			LearningPlan rltNM = nodeCloneManager.cloneToReusedLearningTask(modelingLP, nikolaMilikic);
//			rltNM.addTag(getTag("social and behavioral sciences"));
//			rltNM.addTag(getTag("reliability"));
//			rltNM.addTag(getTag("computability theory"));
//			rltNM.addTag(getTag("automatic programming"));
//			rltNM.addTag(getTag("elicitation methods"));
//			rltNM.addTag(getTag("rational approximation"));
//			rltNM.addTag(getTag("curriculum"));
//			rltNM.setTitle("Modeling Things basics");
//			rltNM.setVisibility(VisibilityType.PUBLIC);			
//			rltNM = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(rltNM);
//			
//			setActivitiesInReusedLTAsPublic(rltNM, nikolaMilikic);
			
//			EventFactory.getInstance().createSetVisibilityEvent(zoranJeremic, rltNM, rltNM.getVisibility());
			
//			TargetCompetence domainModelingCompTCNM = new TargetCompetence();
//			domainModelingCompTCNM.setCompetence(domainModelingComp);
//			domainModelingCompTCNM.setCurrentTask(rltNM);
//			domainModelingCompTCNM.setTargetLevel(advancedLevel);
//			domainModelingCompTCNM.setCurrentLevel(beginnerLevel);
//			domainModelingCompTCNM.addCompetenceRecord(domainModelingCompCRecordNM);
//			domainModelingCompTCNM.setVisibility(VisibilityType.PUBLIC);	
//			domainModelingCompTCNM.setDeadline(deadlineCal.getTime());
////			domainModelingCompTCNM.setProgress(domainModelingProgressNM);
//			domainModelingCompTCNM.setProgress(60);
//			domainModelingCompTCNM.setMaker(nikolaMilikic);
//			domainModelingCompTCNM = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompTCNM);
			
//			EventFactory.getInstance().createChangeProgressEvent(nikolaMilikic, 
//					domainModelingCompTCNM, 
//					domainModelingProgressNM.getProgressValue(), 
//					domainModelingProgressNM.getProgressScale(),
//					null);
			
			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, -3);
			
//			Progress ontologyLearningNMProgress = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewProgress();
			
//			LearningGoal ontologyLearningNM = new LearningGoal();
//			ontologyLearningNM.setTitle("Learn Ontologies");
//			ontologyLearningNM.setMaker(nikolaMilikic);
//			ontologyLearningNM.setDateCreated(new Date());
//			ontologyLearningNM.addTag(getTag("ontology"));
////			ontologyLearningNM.setPriority(Priority.MEDIUM);
//			ontologyLearningNM.setDeadline(deadlineCal.getTime());
//			ontologyLearningNM.setVisibility(VisibilityType.PUBLIC);	
//			//ontologyLearningNM.addTargetCompetence(domainModelingCompTCNM);
////			ontologyLearningNM.setProgress(ontologyLearningNMProgress);
//			ontologyLearningNM.setProgress(0);
//			ontologyLearningNM = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyLearningNM);
			
//			mapLearningGoal(ontologyLearningNM);
		
// 			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, ontologyLearningNM, ontologyLearningNM.getVisibility(), null);
// 			EventFactory.getInstance().createAddEvent(nikolaMilikic, domainModelingCompTCNM, ontologyLearningNM, null);
// 			EventFactory.getInstance().createChangeProgressEvent(nikolaMilikic, 
// 					ontologyLearningNM, 
// 					ontologyLearningNMProgress.getProgressValue(), 
// 					ontologyLearningNMProgress.getProgressScale(),
// 					null);
			
//			TargetLearningGoal ontologyLearningNMTargetGoal = ServiceLocator.getInstance().
//					getService(LearningGoalManager.class).createNewTargetLearningGoal(zoranJeremic, ontologyLearningZJ);
// 
//			nikolaMilikic.addLearningGoal(ontologyLearningNMTargetGoal);
//			nikolaMilikic = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(nikolaMilikic);
			
			// Dragan Djuric
//			Progress domainModelingProgressDDJ = new Progress();
//			domainModelingProgressDDJ.setProgressScale(progressScale0To100);
//			domainModelingProgressDDJ.setProgressValue(40);
//			domainModelingProgressDDJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingProgressDDJ);

			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, 5);
			
//			CompetenceRecord domainModelingCompCRecordDDJ = new CompetenceRecord();
//			domainModelingCompCRecordDDJ.setCompetence(domainModelingComp);
//			domainModelingCompCRecordDDJ.setRecordedDate(new Date());
////			domainModelingCompCRecordDDJ.setRecordedLevel(beginnerLevel);
//			domainModelingCompCRecordDDJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompCRecordDDJ);
			
//			LearningPlan rltDD = nodeCloneManager.cloneToReusedLearningTask(modelingLP, vladanDevedzic);
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
//			rltDD.setVisibility(VisibilityType.PUBLIC);
//			rltDD = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(rltDD);
//			
//			setActivitiesInReusedLTAsPublic(rltDD, vladanDevedzic);
			
			//RecognitionsManager.getInstance().addGivenRecognitionToUser(draganDjuric,  vladanDevedzic, rltDD, "I would recommend this learning task as very useful.", null);
			//RecognitionsManager.getInstance().addGivenRecognitionToUser(jelenaJovanovic,  vladanDevedzic, rltDD, "I would recommend this learning task as very nice.", null);
			//RecognitionsManager.getInstance().addGivenRecognitionToUser(nikolaMilikic,  vladanDevedzic, rltDD, "I would recommend this learning task as nice.", null);
			
//			TargetCompetence domainModelingCompTCDDJ = new TargetCompetence();
//			domainModelingCompTCDDJ.setCompetence(domainModelingComp);
//			domainModelingCompTCDDJ.setCurrentTask(rltDD);
//			domainModelingCompTCDDJ.setTitle("How to model things advanced");
//			domainModelingCompTCDDJ.setTargetLevel(advancedLevel);
//			domainModelingCompTCDDJ.setCurrentLevel(beginnerLevel);
//			domainModelingCompTCDDJ.addCompetenceRecord(domainModelingCompCRecordDDJ);
//			domainModelingCompTCDDJ.setVisibility(VisibilityType.PUBLIC);
//			domainModelingCompTCDDJ.setDeadline(deadlineCal.getTime());
////			domainModelingCompTCDDJ.setProgress(domainModelingProgressDDJ);
//			domainModelingCompTCDDJ.setProgress(40);
//			domainModelingCompTCDDJ.setMaker(draganDjuric);
//			domainModelingCompTCDDJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompTCDDJ);
			
//			EventFactory.getInstance().createChangeProgressEvent(draganDjuric, 
//					domainModelingCompTCDDJ, 
//					domainModelingProgressDDJ.getProgressValue(), 
//					domainModelingProgressDDJ.getProgressScale(),
//					null);
			
			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, 8);
			
//			Progress ontologyLearningDDJProgress = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewProgress();
			
//			LearningGoal ontologyLearningDDJ = new LearningGoal();
//			ontologyLearningDDJ.setTitle("Learning Ontologies");
//			ontologyLearningDDJ.setMaker(draganDjuric);
//			ontologyLearningDDJ.setDateCreated(new Date());
//			ontologyLearningDDJ.addTag(getTag("ontology"));
////			ontologyLearningDDJ.setPriority(Priority.MEDIUM);
//			ontologyLearningDDJ.setDeadline(deadlineCal.getTime());
//			ontologyLearningDDJ.setVisibility(VisibilityType.PUBLIC);
//		//	ontologyLearningDDJ.addTargetCompetence(domainModelingCompTCDDJ);
////			ontologyLearningDDJ.setProgress(ontologyLearningDDJProgress);
//			ontologyLearningDDJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyLearningDDJ);
			
//			mapLearningGoal(ontologyLearningDDJ);
			
//			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, ontologyLearningDDJ, ontologyLearningDDJ.getVisibility(), null);
//			EventFactory.getInstance().createAddEvent(draganDjuric, domainModelingCompTCDDJ, ontologyLearningDDJ, null);
//			EventFactory.getInstance().createChangeProgressEvent(draganDjuric, 
//					ontologyLearningDDJ, 
//					ontologyLearningDDJProgress.getProgressValue(), 
//					ontologyLearningDDJProgress.getProgressScale(),
//					null);
			
//			TargetLearningGoal ontologyLearningDDJTargetGoal = ServiceLocator.getInstance().
//					getService(LearningGoalManager.class).createNewTargetLearningGoal(zoranJeremic, ontologyLearningZJ);
//			
//			draganDjuric.addLearningGoal(ontologyLearningDDJTargetGoal);
//			draganDjuric = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(draganDjuric);
			
			// Jelena Jovanovic
//			Progress domainModelingProgressJJ = new Progress();
//			domainModelingProgressJJ.setProgressScale(progressScale0To100);
//			domainModelingProgressJJ.setProgressValue(progressScale0To100.getMaxValue());
//			domainModelingProgressJJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingProgressJJ);

			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, -10);
			
//			CompetenceRecord domainModelingCompCRecordJJ = new CompetenceRecord();
//			domainModelingCompCRecordJJ.setCompetence(domainModelingComp);
//			domainModelingCompCRecordJJ.setRecordedDate(new Date());
////			domainModelingCompCRecordJJ.setRecordedLevel(advancedLevel);
//			domainModelingCompCRecordJJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompCRecordJJ);
			
//			LearningPlan rltJJ = nodeCloneManager.cloneToReusedLearningTask(modelingLP, jelenaJovanovic);
//			rltJJ.setTitle("How to model Things - 10 simple steps");
//			rltJJ.addTag(getTag("Reasoning"));
//			rltJJ.addTag(getTag("Text mining"));
//			rltJJ.addTag(getTag("Data mining"));
//			rltJJ.addTag(getTag("Ontology extraction"));
//			rltJJ.setVisibility(VisibilityType.PUBLIC);
//			rltJJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(rltJJ);
//
//			setActivitiesInReusedLTAsPublic(rltJJ, jelenaJovanovic);
			
//			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, rltJJ, rltJJ.getVisibility(), null);
			 
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(draganDjuric,  jelenaJovanovic, rltJJ, "I would recommend this learning task as very useful.", null);
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(vladanDevedzic,  jelenaJovanovic, rltJJ, "I would recommend this learning task as very nice.", null);
//			RecognitionsManager.getInstance().addGivenRecognitionToUser(nikolaMilikic,  jelenaJovanovic, rltJJ, "I would recommend this learning task as nice.", null);
			 
//			TargetCompetence domainModelingCompTCJJ = new TargetCompetence();
//			domainModelingCompTCJJ.setCompetence(domainModelingComp);
//			domainModelingCompTCJJ.setCurrentTask(rltJJ);
//			domainModelingCompTCJJ.setTitle("How to model Things - 10 simple steps");
//			domainModelingCompTCJJ.setTargetLevel(advancedLevel);
//			domainModelingCompTCJJ.setCurrentLevel(advancedLevel);
//			domainModelingCompTCJJ.addCompetenceRecord(domainModelingCompCRecordJJ);
//			domainModelingCompTCJJ.setVisibility(VisibilityType.PUBLIC);
//			domainModelingCompTCJJ.setDeadline(deadlineCal.getTime());
////			domainModelingCompTCJJ.setProgress(domainModelingProgressJJ);
//			domainModelingCompTCJJ.setProgress(100);
//			domainModelingCompTCJJ.setMaker(jelenaJovanovic);
//			domainModelingCompTCJJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(domainModelingCompTCJJ);
			
//			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, domainModelingCompTCJJ, domainModelingCompTCJJ.getVisibility(), null);
//			EventFactory.getInstance().createChangeProgressEvent(jelenaJovanovic, 
//					domainModelingCompTCJJ, 
//					domainModelingProgressJJ.getProgressValue(), 
//					domainModelingProgressJJ.getProgressScale(),
//					null);
			
			//set TargetCompetence "Create your own domain model for a domain of choice" as completed
//			MarkAsComplete.markAsCompleteCompetence(domainModelingCompTCJJ);
			
			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, -8);
			
//			Progress ontologyLearningJJProgress = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewProgress();
			
//			LearningGoal ontologyLearningJJ = new LearningGoal();
//			ontologyLearningJJ.setTitle("Learn Ontology Modeling");
//			ontologyLearningJJ.setMaker(jelenaJovanovic);
//			ontologyLearningJJ.setDateCreated(new Date());
//			ontologyLearningJJ.addTag(getTag("ontology"));
//			ontologyLearningJJ.addTag(getTag("ontology engineering"));
//			ontologyLearningJJ.setPriority(Priority.MEDIUM);
//			ontologyLearningJJ.setDeadline(deadlineCal.getTime());
//			ontologyLearningJJ.setVisibility(VisibilityType.PUBLIC);
			//ontologyLearningJJ.addTargetCompetence(domainModelingCompTCJJ);
//			ontologyLearningJJ.setProgress(ontologyLearningJJProgress);
//			ontologyLearningJJ = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(ontologyLearningJJ);
			
//			mapLearningGoal(ontologyLearningJJ);
//			mapLearningGoal(ontologyLearningJJ);
//			mapLearningGoal(ontologyLearningJJ);
//			mapLearningGoal(ontologyLearningJJ);
//			EventFactory.getInstance().createSetVisibilityEvent(nikolaMilikic, ontologyLearningJJ, ontologyLearningJJ.getVisibility(), null);
 
//			EventFactory.getInstance().createChangeProgressEvent(jelenaJovanovic, 
//					ontologyLearningJJ, 
//					ontologyLearningJJProgress.getProgressValue(), 
//					ontologyLearningJJProgress.getProgressScale(),
//					null);
			
//			EventFactory.getInstance().createAddEvent(jelenaJovanovic, 
//					domainModelingCompTCJJ, 
//					ontologyLearningJJ,
//					null);
			
//			TargetLearningGoal ontologyLearningJJTargetGoal = ServiceLocator.getInstance().
//					getService(LearningGoalManager.class).createNewTargetLearningGoal(zoranJeremic, ontologyLearningZJ);
//			
//			jelenaJovanovic.addLearningGoal(ontologyLearningJJTargetGoal);
//			jelenaJovanovic = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(jelenaJovanovic);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
 
	protected UserRating createNumericRating(int ratingValue, Scale scale) throws Exception {
		UserRating rating = new UserRating();
		rating.setRatingValue(ratingValue);
		rating.setScale(scale);
		return ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(rating);
	}
	
	protected Tag createTag(String tagTitle) throws Exception {
		Tag tag = null;
		tag = ServiceLocator.getInstance().getService(TagManager.class).getOrCreateTag(tagTitle);
		
		return tag;
 
	}
			
//	protected Concept createConcept(String concept) throws Exception {
//		Concept c=null;
//		c=ServiceLocator.getInstance().getService(SearchQueries.class).getConceptByTitle(concept);
//		if(c==null){
//		 c=new Concept();
//		c.setTitle(concept);
//		c=ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(c);
//		} 
//		return c;
//	}
	
//	protected void setActivitiesInReusedLTAsPublic(LearningPlan rlt, User user){
//		Collection<Activity> activities=rlt.getActivities();
//		for(Activity act:activities){
//			try {
//		 
//				Visibility actVis = VisibilityType.PUBLIC;
////				EventFactory.getInstance().createSetVisibilityEvent(user, act, actVis, null);
//				act.setVisibility(actVis);
//				
//				ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(act);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
}
