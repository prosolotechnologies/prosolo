package org.prosolo.app.bc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.CompetenceActivity;
import org.prosolo.domainmodel.activities.RecommendationType;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.app.RegistrationKey;
import org.prosolo.domainmodel.app.RegistrationType;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.course.CourseCompetence;
import org.prosolo.domainmodel.course.CreatorType;
import org.prosolo.domainmodel.organization.Organization;
import org.prosolo.domainmodel.organization.OrganizationalPosition;
import org.prosolo.domainmodel.organization.OrganizationalUnit;
import org.prosolo.domainmodel.organization.Role;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.FollowedEntity;
import org.prosolo.domainmodel.user.FollowedUserEntity;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.NodeRecommendationManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.VisibilityManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.nodes.exceptions.VisibilityCoercionError;
import org.prosolo.util.string.StringUtil;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityType;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Oct 11, 2014
 *
 */
@Service("org.prosolo.app.bc.BusinessCase4_EDX")
public class BusinessCase4_EDX extends BusinessCase {

	public Map<String, Tag> allTags = new HashMap<String, Tag>();
	
	private Tag getTag(String tagString) throws Exception {
		if (!allTags.containsKey(tagString)) {
			Tag t = getOrCreateTag(tagString);
			allTags.put(tagString, t);
			return t;
		} else
			return allTags.get(tagString);
	}

	public void setFollowedUser(User user, User followedUser) {

		FollowedEntity fe = new FollowedUserEntity();
		fe.setUser(user);
		fe.setFollowedResource(followedUser);
		fe = ServiceLocator.getInstance().getService(DefaultManager.class)
				.saveEntity(fe);
		//user.addFollowedEntity(fe);
		user = ServiceLocator.getInstance().getService(DefaultManager.class)
				.saveEntity(user);
	}

	public void initRepository() {
		System.out.println("BusinessCaseTest - initRepository() with BC 4");
		RegistrationKey regKey0=new RegistrationKey();
		regKey0.setUid("reg793442b86584b46f7bd8a0dae72f31");
		regKey0.setRegistrationType(RegistrationType.NO_APPROVAL_ACCESS);
		logger.info("initRepository");
		try{
		ServiceLocator.getInstance().getService(RegistrationManager.class).saveEntity(regKey0);
		}catch(Exception ex){
			ex.getStackTrace();
		}
		RegistrationKey regKey=new RegistrationKey();
		regKey.setUid(UUID.randomUUID().toString().replace("-", ""));
		regKey.setRegistrationType(RegistrationType.NO_APPROVAL_ACCESS);
		ServiceLocator.getInstance().getService(RegistrationManager.class).saveEntity(regKey);
		logger.info("initRepository");
		RegistrationKey regKey2=new RegistrationKey();
		regKey2.setUid(UUID.randomUUID().toString().replace("-", ""));
		regKey2.setRegistrationType(RegistrationType.NO_APPROVAL_ACCESS);
		ServiceLocator.getInstance().getService(RegistrationManager.class).saveEntity(regKey2);
		logger.info("initRepository");
		Organization org = ServiceLocator.getInstance()
				.getService(OrganizationManager.class)
				.lookupDefaultOrganization();
		
		OrganizationalUnit headOfficeOrgUnit = ServiceLocator.getInstance()
				.getService(OrganizationManager.class)
				.lookupHeadOfficeUnit(org);
		OrganizationalUnit fosGoodOldAiResearchNetworkOrgUnit = new OrganizationalUnit();
		fosGoodOldAiResearchNetworkOrgUnit.setTitle("ProSolo");
		fosGoodOldAiResearchNetworkOrgUnit.setOrganization(org);
		fosGoodOldAiResearchNetworkOrgUnit.setParentUnit(headOfficeOrgUnit);
		fosGoodOldAiResearchNetworkOrgUnit = ServiceLocator.getInstance()
				.getService(DefaultManager.class)
				.saveEntity(fosGoodOldAiResearchNetworkOrgUnit);
		ServiceLocator
				.getInstance()
				.getService(OrganizationManager.class)
				.addSubUnit(headOfficeOrgUnit,
						fosGoodOldAiResearchNetworkOrgUnit);
		logger.info("initRepository");
//		String goodOldAiChair = "Senior Data Scientist";
		//String dataApplicationDeveloperOrgPosition = "Data Application Developer";
 		String fictitiousUser = "System analyst";
		ServiceLocator.getInstance().getService(OrganizationManager.class)
				.addOrgUnit(org, fosGoodOldAiResearchNetworkOrgUnit);
		logger.info("initRepository");
		User userNickPowell=null;
		try {
			userNickPowell = ServiceLocator
					.getInstance()
					.getService(UserManager.class)
					.createNewUser("Zoran", "Jeremic", "zoran.jeremic@gmail.com",
							true, "prosolo@2014", org, fictitiousUser, getAvatarInputStream("male1.png"), "male1.png");
		} catch (UserAlreadyRegisteredException e1) {
			logger.error(e1.getLocalizedMessage());
		} catch (EventException e) {
			logger.error(e.getMessage());
		}
		
 
		logger.info("initRepository");
		// create default ROLES
		String roleUserTitle = "User";
		String roleManagerTitle = "Manager";
		String roleAdminTitle = "Admin";
		
		@SuppressWarnings("unused")
		Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleUserTitle);
		Role roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleManagerTitle);
		Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleAdminTitle);

//		ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, userNickPowell, headOfficeOrgUnit, fosSeniorProgrammerOrgPosition);
 		 userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, userNickPowell);
 		 userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userNickPowell);
		
 
 

		OrganizationalPosition fosAssistantProfessorPosition = new OrganizationalPosition();
		fosAssistantProfessorPosition.setTitle("Assistant Professor");
		fosAssistantProfessorPosition.setAllocatedToOrgUnit(fosGoodOldAiResearchNetworkOrgUnit);
		fosAssistantProfessorPosition = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(fosAssistantProfessorPosition);
		logger.info("initRepository");
//		String fosTeachingAssistantPosition = "Teaching Assistant";
  

			Collection<Tag> nickPowellPreferences = new ArrayList<Tag>();
			try {
				nickPowellPreferences.add(getTag("Statistics"));
				nickPowellPreferences.add(getTag("Descriptive data"));
				nickPowellPreferences.add(getTag("Data analysis"));
				nickPowellPreferences.add(getTag("Probability"));
			} catch (Exception e5) {
				
				logger.error(e5.getLocalizedMessage());
			}
	
 
			
			// ////////////////////////////
			// LearningGoal for Nick Powell
			// ///////////////////////////////
			// activity A1
			List<Tag> lg2Tags = new ArrayList<Tag>();
			try {
				lg2Tags.add(getTag("data"));
				lg2Tags.add(getTag("statistics"));
				lg2Tags.add(getTag("exploring data"));
			} catch (Exception e1) {
				
				logger.error(e1.getLocalizedMessage());
			}
			logger.info("initRepository");
			Date currentDate = new Date();
			Calendar deadlineCal = Calendar.getInstance();
			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, 5);

			LearningGoal lgnp1 = null;
			try {
				lgnp1 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Preparing Statistical Data for Analysis",
								"This section provides an example of the programming code needed to read "
										+ "in a multilevel data file, to create an incident-level aggregated flat file "
										+ "for summary-level analysis, and to prepare individual data segments for detailed "
										+ "analysis. For illustration purposes, a National Incident-Based Reporting System "
										+ "(NIBRS) data file obtained from the FBI is read into and restructured in SPSS, "
										+ "SAS, and Microsoft ACCESS. The concepts illustrated are applicable to state-level "
										+ "data sets and transferable to other software.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			TargetLearningGoal lgnp1NickPowell = null; 
			try {
				lgnp1NickPowell =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgnp1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			logger.info("initRepository");
//			userNickPowell = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userNickPowell);
			
			try {
				ServiceLocator
					.getInstance()
					.getService(VisibilityManager.class)
					.setResourceVisibility(userNickPowell, lgnp1, VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			lgnp1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lgnp1);
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgnp1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgnp1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			logger.info("initRepository");
			
		/////////////////////////////////////////////////////////////
			TargetCompetence tc3 = null;
			try {
				tc3 = createTargetCompetence(
						userNickPowell,
						"Outline Descriptive statistics",
						"Descriptive statistics is the discipline of quantitatively "
								+ "describing the main features of a collection of data.Descriptive "
								+ "statistics are distinguished from inferential statistics (or inductive statistics), "
								+ "in that descriptive statistics aim to summarize a sample, rather than use the data to "
								+ "learn about the population that the sample of data is thought to represent.",
						12,
						9);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			// activity A9
//			RichContent rcA9 = ServiceLocator
//					.getInstance()
//					.getService(PostManager.class)
//					.createRichContent("http://www.socialresearchmethods.net/kb/statdesc.php", true);
			AttachmentPreview rcA9 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.socialresearchmethods.net/kb/statdesc.php", true);

			Collection<Tag> a9Tags = new ArrayList<Tag>();

			try {
				a9Tags.add(getTag("statistics"));
				a9Tags.add(getTag("parametric data"));
				a9Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");

			Activity a9 = null;
			try {
				a9 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Read introduction to Descriptive statistics", null,
								rcA9, VisibilityType.PUBLIC, a9Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
		 
			// activity A10
			AttachmentPreview rcA10 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.slideshare.net/christineshearer/univariate-analysis", true);
//			RichContent rcA10 = ServiceLocator
//					.getInstance()
//					.getService(PostManager.class)
//					.createRichContent("http://www.socqrl.niu.edu/myers/univariate.htm", true);
 
			Collection<Tag> a10Tags = new ArrayList<Tag>();
			try {
				a10Tags.add(getTag("statistics"));
				a10Tags.add(getTag("parametric data"));
				a10Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
		

			Activity a10 = null;
			try {
				a10 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell, "Univariate analysis", null,
								rcA10, VisibilityType.PUBLIC, a10Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			// activity A11
			AttachmentPreview rcA11 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://en.wikipedia.org/wiki/Data_collection", true);
//			RichContent rcA11 = ServiceLocator
//					.getInstance()
//					.getService(PostManager.class)
//					.createRichContent("http://people.uwec.edu/piercech/researchmethods/data%20collection%20methods/data%20collection%20methods.htm", true);
			logger.info("initRepository");

			Collection<Tag> a11Tags = new ArrayList<Tag>();
			try {
				a11Tags.add(getTag("statistics"));
				a11Tags.add(getTag("parametric data"));
				a11Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}


			Activity a11 = null;
			try {
				a11 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell, "Data collection", null,
								rcA11, VisibilityType.PUBLIC, a11Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			// activity A12
			AttachmentPreview rcA12 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.stat.yale.edu/Courses/1997-98/101/sampinf.htm", true);
//			RichContent rcA12 = ServiceLocator
//					.getInstance()
//					.getService(PostManager.class)
//					.createRichContent("http://www.stat.yale.edu/Courses/1997-98/101/sampinf.htm", true);
 
 
			Collection<Tag> a12Tags = new ArrayList<Tag>();
			try {
				a12Tags.add(getTag("statistics"));
				a12Tags.add(getTag("parametric data"));
				a12Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
			

			Activity a12 = null;
			try {
				a12 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Probability through simulation", null, rcA12,
								VisibilityType.PUBLIC, a12Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}

 
			logger.info("initRepository");
			TargetCompetence tcnp2 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc3.getCompetence(), VisibilityType.PRIVATE);
			
			try {
				tcnp2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp2, a9, true);
				tcnp2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp2, a10, true);
				tcnp2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp2, a11, true);
				tcnp2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp2, a12, true);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			tcnp2 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(tcnp2);
			
			try {
				tcnp2 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lgnp1NickPowell, tcnp2, false, null).getNode();
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			lgnp1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lgnp1);
			
			AttachmentPreview rcA1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.uvm.edu/~dhowell/StatPages/Resampling/Resampling.html", true);
 
		 
			
			Collection<Tag> a1Tags = new ArrayList<Tag>();
			try {
				a1Tags.add(getTag("statistics"));
				a1Tags.add(getTag("parametric data"));
				a1Tags.add(getTag("resampling"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");

			Activity a1 = null;
			try {
				a1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Parametric and Resampling Statistics", null, rcA1,
								VisibilityType.PUBLIC, a1Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			// activity A2
			AttachmentPreview rcA2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://laboratory-manager.advanceweb.com/Columns/Interpreting-Statistics/Non-Parametric-Statistics.aspx", true);
 
 

			Collection<Tag> a2Tags = new ArrayList<Tag>();
			try {
				a2Tags.add(getTag("statistics"));
				a2Tags.add(getTag("parametric data"));
				a2Tags.add(getTag("resampling"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
	

			Activity a2 = null;
			try {
				a2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Read about Parametric statistics", null, rcA2,
								VisibilityType.PUBLIC, a2Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}

			// activity A3
			AttachmentPreview rcA3 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://isomorphismes.tumblr.com/post/18913494015/probability-distributions", true);
 
 
			
			Collection<Tag> a3Tags = new ArrayList<Tag>();
			try {
				a3Tags.add(getTag("statistics"));
				a3Tags.add(getTag("parametric data"));
				a3Tags.add(getTag("resampling"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");

			Activity a3 = null;
			try {
				a3 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Read about Probability distribution", null, rcA3,
								VisibilityType.PUBLIC, a3Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}

			// activity A4
			AttachmentPreview rcA4 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.mathwave.com/articles/distribution-fitting-graphs.html", true);
 			Collection<Tag> a4Tags = new ArrayList<Tag>();
			try {
				a4Tags.add(getTag("statistics"));
				a4Tags.add(getTag("parametric data"));
				a4Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}


			Activity a4 = null;
			try {
				a4 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"List of probability distributions", null, rcA4,
								VisibilityType.PUBLIC, a4Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}

			List<Tag> lg1Tags = new ArrayList<Tag>();
			try {
				lg1Tags.add(getTag("data"));
				lg1Tags.add(getTag("statistics"));
				lg1Tags.add(getTag("exploring data"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}


			LearningGoal lg1 = null;
			try {
				lg1 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learn how to explore data in statistics",
								"Learn the first steps in analyzing data: exploring it.In statistics, exploratory data analysis (EDA) "
										+ "is an approach to analyzing data sets to summarize their main characteristics in easy-to-understand form, "
										+ "often with visual graphs, without using a statistical model or having formulated a hypothesis. "
										+ "Exploratory data analysis was promoted by John Tukey to encourage statisticians visually to examine "
										+ "their data sets, to formulate hypotheses that could be tested on new data-sets.",
								deadlineCal.getTime(), lg1Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				ServiceLocator.getInstance().getService(VisibilityManager.class)
					.setResourceVisibility(userNickPowell, lg1,	VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e) {
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			lg1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).merge(lg1);
			
			userNickPowell = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userNickPowell);
			logger.info("initRepository");
			TargetLearningGoal lg1NickPowell = null;
			try {
				lg1NickPowell =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			lg1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).merge(lg1);
			logger.info("initRepository");
			TargetCompetence tc1 = null;
			try {
				tc1 = ServiceLocator
						.getInstance()
						.getService(CompetenceManager.class)
						.createNewTargetCompetence(
								userNickPowell,
								"Differentiate Parametric Data",
								"Familiarity with parametric tests and parametric data. "
										+ "Parametric statistics is a branch of statistics that assumes that "
										+ "the data has come from a type of probability distribution and makes "
										+ "inferences about the parameters of the distribution. Most well-known "
										+ "elementary statistical methods are parametric.",
								12,
								8,
								null, 
								VisibilityType.PRIVATE);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
 			logger.info("initRepository");
			try {
				tc1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc1, a1, true);
				tc1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc1, a2, true);
				tc1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc1, a3, true);
				tc1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc1, a4, true);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			lg1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).merge(lg1);
			
			try {
				tc1 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg1NickPowell, tc1, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			// activity A5
			AttachmentPreview rcA5 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://records.viu.ca/~johnstoi/maybe/maybe3.htm", true);
  
 
			Collection<Tag> a5Tags = new ArrayList<Tag>();
			try {
				a5Tags.add(getTag("statistics"));
				a5Tags.add(getTag("parametric data"));
				a5Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}


			Activity a5 = null;
			try {
				a5 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"An Introductory Handbook to Probability, Statistics and Excel", null,
								rcA5, VisibilityType.PUBLIC, a5Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			// activity A6
			AttachmentPreview rcA6 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.physics.csbsju.edu/stats/box2.html", true);
  
 
			Collection<Tag> a6Tags = new ArrayList<Tag>();
			try {
				a6Tags.add(getTag("statistics"));
				a6Tags.add(getTag("parametric data"));
				a6Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
		
			logger.info("initRepository");
			Activity a6 = null;
			try {
				a6 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Box Plot: Display of Distribution", null, rcA6,
								VisibilityType.PUBLIC, a6Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			// activity A7
			AttachmentPreview rcA7 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://wiki.stat.ucla.edu/socr/index.php/AP_Statistics_Curriculum_2007_EDA_DataTypes", true);
  
			Collection<Tag> a7Tags = new ArrayList<Tag>();
			try {
				a7Tags.add(getTag("statistics"));
				a7Tags.add(getTag("parametric data"));
				a7Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");

			Activity a7 = null;
			try {
				a7 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell, "Data Types", null, rcA7,
								VisibilityType.PUBLIC, a7Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			// activity A8
			AttachmentPreview rcA8 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://wiki.stat.ucla.edu/socr/index.php/AP_Statistics_Curriculum_2007_Prob_Simul", true);
  

			Collection<Tag> a8Tags = new ArrayList<Tag>();
			try {
				a8Tags.add(getTag("statistics"));
				a8Tags.add(getTag("parametric data"));
				a8Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
	

			Activity a8 = null;
			try {
				a8 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Probability through simulation", null, rcA8,
								VisibilityType.PUBLIC, a8Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			TargetCompetence tc2 = null;
			try {
				tc2 = ServiceLocator
						.getInstance()
						.getService(CompetenceManager.class)
						.createNewTargetCompetence(
								userNickPowell,
								"Illustrate and Prepare Data",
								"Knowledge in Using frequency distributions, other graphs and "
										+ "descriptive statistics to screen our data. Statistical graphs "
										+ "present data and the results of statistical analysis, assist in "
										+ "the analysis of data, and occasionally are used to facilitate statistical "
										+ "computation. Presentation graphs include the familiar bar graph, pie chart, "
										+ "line graph, scatterplot, and statistical map. Data analysis employs these graphical "
										+ "forms as well as others.", 
								12,
								15,
								null, 
								VisibilityType.PRIVATE);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 			
			try {
				tc2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc2, a5, true);
				tc2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc2, a6, true);
				tc2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc2, a7, true);
				tc2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc2, a8, true);

			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lg1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).merge(lg1);
			
			try {
				tc2 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg1NickPowell, tc2, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			lg1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).merge(lg1);
			
			try {
				tc3 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
				 		.addTargetCompetenceToGoal(userNickPowell, lg1NickPowell, tc3, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			////////////////////////////////////////////////
			///Defining the course 'Understanding of Applications of Learning Analytics in Education'
			HashSet<Tag> tags = new HashSet<Tag>();
			try {
				tags.add(getOrCreateTag("big data"));
				tags.add(getOrCreateTag("educational data"));
				tags.add(getOrCreateTag("learning analytics"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}

			List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
		
			Competence comp1 = null;
			try {
				comp1 = ServiceLocator.getInstance().getService(CompetenceManager.class).createCompetence(userNickPowell, 
						"Define social network analysis", 
						"Define networks and articulate why they are important for education and educational research.", 
						3, 
						21, null, null, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");
			CourseCompetence oc1=new CourseCompetence(comp1);
			oc1.setDaysOffset(14);
			oc1.setDuration(14);
			ServiceLocator.getInstance().getService(CompetenceManager.class).saveEntity(oc1);
			courseCompetences.add(oc1);
			
			
			String c2title="Perform social network analysis centrality measures using Gephi";
			String c2description="See the title. This also includes being able to import data in to Gephi.";
			Competence comp2 = null;
			try {
				comp2 = ServiceLocator.getInstance().getService(CompetenceManager.class).createCompetence(userNickPowell, 
						c2title, c2description, 1, 
						7, null, null, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			AttachmentPreview gephiHomepage = ServiceLocator.getInstance().getService(HTMLParser.class).
					parseUrl(StringUtil.cleanHtml("https://gephi.org"));
			
			try {
				ServiceLocator.getInstance().getService(CompetenceManager.class).
				createNewActivityAndAddToCompetence(
						userNickPowell,
						"Gephi",
						"Gephi, an open source graph visualization and manipulation software",
						ActivityType.RESOURCE,
						true,
						gephiHomepage,
						0,
						true,
						0,
						comp2);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			AttachmentPreview gephiDemoYoutubeLink= ServiceLocator.getInstance().getService(HTMLParser.class).
					parseUrl(StringUtil.cleanHtml("http://www.youtube.com/watch?v=JgDYV5ArXgw"));
			
			try {
				ServiceLocator.getInstance().getService(CompetenceManager.class).
					createNewActivityAndAddToCompetence(
						userNickPowell,
						"Gephi Demo 920",
						"",
						ActivityType.RESOURCE,
						true,
						gephiDemoYoutubeLink,
						0,
						true,
						0,
						comp2);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			AttachmentPreview gephiPaper = ServiceLocator.getInstance().getService(HTMLParser.class).
					parseUrl(StringUtil.cleanHtml("http://www.aaai.org/ocs/index.php/ICWSM/09/paper/view/154"));
			
			try {
				ServiceLocator.getInstance().getService(CompetenceManager.class).
					createNewActivityAndAddToCompetence(
						userNickPowell,
						"Paper: 'Gephi: An Open Source Software for Exploring and Manipulating Networks'",
						"Gephi is an open source software for graph and network analysis. It uses a " +
						"3D render engine to display large networks in real-time and to speed up the " +
						"exploration.",
						ActivityType.RESOURCE,
						true,
						gephiPaper,
						0,
						true,
						0,
						comp2);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				Activity uplaodGephiAssignment = ServiceLocator.getInstance().getService(ResourceFactory.class).createNewActivity(
						userNickPowell, 
						"Upload your own visualisation",
						"Find some example data and create a visualisation using Gephi. Compress all visualisation files and uplaod it here.", 
						ActivityType.ASSIGNMENTUPLOAD, 
						true, 
						null, 
						1, 
						true, 
						15, 
						VisibilityType.PUBLIC);
				
				CompetenceActivity uplaodGephiAssignmentCompActivity = new CompetenceActivity(4, uplaodGephiAssignment);
				uplaodGephiAssignmentCompActivity = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(uplaodGephiAssignmentCompActivity);
				
				comp2 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(comp2);
				comp2.addActivity(uplaodGephiAssignmentCompActivity);
				comp2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(comp2);
			} catch (EventException e5) {
				logger.error(e5.getLocalizedMessage());
			}
			
			CourseCompetence oc2=new CourseCompetence();
			oc2.setCompetence(comp2);
			oc2.setDaysOffset(7);
			oc2.setDuration(21);
			ServiceLocator.getInstance().getService(CompetenceManager.class).saveEntity(oc2);
			courseCompetences.add(oc2);
			
			String c3title="Interpret results of social network analysis";
			String c3description="Interpret detailed meaning of SNA result and importance of the position of actors in social networks for information flow. Discuss implications for educational research and practice. ";
			Competence comp3 = null;
			try {
				comp3 = ServiceLocator.getInstance().getService(CompetenceManager.class).createCompetence(userNickPowell, 
						c3title, c3description, 12, 
						7, null, null, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			CourseCompetence oc3=new CourseCompetence();
			oc3.setDaysOffset(5);
			oc3.setDuration(30);
			oc3.setCompetence(comp3);
			ServiceLocator.getInstance().getService(CompetenceManager.class).saveEntity(oc3);
			courseCompetences.add(oc3);
			
			
			try {
				@SuppressWarnings("unused")
				Course nickActiveCourse = ServiceLocator.getInstance().getService(CourseManager.class).saveNewCourse(
						"Understanding of Applications of Learning Analytics in Education",
						"This is a credential provides a set of competences for the EdX Data Analytics and Learning MOOC", 
						null, 
						courseCompetences, 
						tags,
						null,
						userNickPowell, 
						CreatorType.MANAGER, 
						true, 
						true);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 			 
			logger.info("initRepository");
 
			try {
				ServiceLocator
						.getInstance()
						.getService(PostManager.class)
						.createNewPost(userNickPowell,
								"Learning parametric data.", VisibilityType.PUBLIC, null, null, true, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				ServiceLocator
					.getInstance()
					.getService(PostManager.class)
					.createNewPost(userNickPowell,
							"Learning parametric data.", VisibilityType.PUBLIC, null, null, true, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 
	 		AttachmentPreview rcA13 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://hsc.uwe.ac.uk/dataanalysis/quantinfasspear.asp", true);
 
			Collection<Tag> a13Tags = new ArrayList<Tag>();
		
			try {
				a13Tags.add(getTag("statistics"));
				a13Tags.add(getTag("parametric data"));
				a13Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
 
			Activity a13 = null;
			try {
				a13 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Pearson's Correlation Coeeficient", null, rcA13,
								VisibilityType.PUBLIC, a13Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 		
			AttachmentPreview rcA14 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.math.uah.edu/stat/sample/Covariance.html", true);
 			Collection<Tag> a14Tags = new ArrayList<Tag>();
			try {
				a14Tags.add(getTag("statistics"));
				a14Tags.add(getTag("parametric data"));
				a14Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
		

			Activity a14 = null;
			try {
				a14 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Instructions for Covariance, Correlation, and Bivariate Graphs", null,
								rcA14, VisibilityType.PUBLIC, a14Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

  
			AttachmentPreview rcA15 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.statisticshowto.com/articles/how-to-find-the-coefficient-of-determination/", true);
 			Collection<Tag> a15Tags = new ArrayList<Tag>();
			try {
				a15Tags.add(getTag("statistics"));
				a15Tags.add(getTag("parametric data"));
				a15Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
		

			Activity a15 = null;
			try {
				a15 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Coefficient of determination", null, rcA15,
								VisibilityType.PUBLIC, a15Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 
			AttachmentPreview rcA16 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://udel.edu/~mcdonald/statspearman.html", true);
  

			Collection<Tag> a16Tags = new ArrayList<Tag>();
			try {
				a16Tags.add(getTag("statistics"));
				a16Tags.add(getTag("parametric data"));
				a16Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}


			Activity a16 = null;
			try {
				a16 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Spearman's rank correlation coefficient", null, rcA16,
								VisibilityType.PUBLIC, a16Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 			
			AttachmentPreview rcA17 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.statisticssolutions.com/academic-solutions/resources/directory-of-statistical-analyses/kendalls-tau-and-spearmans-rank-correlation-coefficient/", 
							true);
 
			Collection<Tag> a17Tags = new ArrayList<Tag>();
			try {
				a17Tags.add(getTag("statistics"));
				a17Tags.add(getTag("parametric data"));
				a17Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
	

			Activity a17 = null;
			try {
				a17 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Kendall tau rank correlation coefficient", null, rcA17,
								VisibilityType.PUBLIC, a17Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

  			AttachmentPreview rcA18 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.apexdissertations.com/articles/point-biserial_correlation.html", true);
 
			Collection<Tag> a18Tags = new ArrayList<Tag>();
			try {
				a18Tags.add(getTag("statistics"));
				a18Tags.add(getTag("parametric data"));
				a18Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
	
			logger.info("initRepository");
			Activity a18 = null;
			try {
				a18 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Biserial and Point-Biserial Correlations", null, rcA18,
								VisibilityType.PUBLIC, a18Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			deadlineCal.setTime(currentDate);
			deadlineCal.add(Calendar.DATE, 15);

			LearningGoal lg2 = null;
			try {
				lg2 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning Statistical Correlation",
								"Learn how to identify relationship between two or "
										+ "more variable and what are the most usually used relationships. "
										+ "Correlation is a measure of relationship between two mathematical "
										+ "variables or measured data values, which includes the Pearson correlation "
										+ "coefficient as a special case.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			userNickPowell = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userNickPowell);

			try {
				ServiceLocator
					.getInstance()
					.getService(VisibilityManager.class)
					.setResourceVisibility(userNickPowell, lg2, VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e) {
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			lg2 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(lg2);
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg2);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			TargetLearningGoal lg2PhillAmstrong = null;
			try {
				lg2PhillAmstrong =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg2);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
				
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg2);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
 

			TargetCompetence tc4 = null;
			try {
				tc4 = ServiceLocator
						.getInstance()
						.getService(CompetenceManager.class)
						.createNewTargetCompetence(
								userNickPowell,
								"Construct Bivariate Correlations",
								"A statistical test that measures the association or relationship between two "
										+ "continuous/interval/ordinal level variables. Bivariate correlation is a measure "
										+ "of the relationship between the two variables; it measures the strength of their "
										+ "relationship, which can range from absolute value 1 to 0. The stronger the relationship, "
										+ "the closer the value is to 1. The relationship can be positive or negative; in positive "
										+ "relationship, as one value increases, another value increases with it. In the negative "
										+ "relationship, as one value increases, the other one decreases.",
								12,
								23,
								null, 
								VisibilityType.PRIVATE);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}

 
			
			try {
				tc4 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc4, a13, true);
				tc4 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc4, a14, true);
				tc4 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc4, a15, true);
				tc4 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc4, a16, true);
				tc4 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc4, a17, true);
				tc4 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc4, a18, true);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				tc4 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg2PhillAmstrong, tc4, false, null).getNode();
			} catch (EventException e) {
			}
			logger.info("initRepository");
			// activity A19
 
			AttachmentPreview rcA19 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.apexdissertations.com/articles/point-biserial_correlation.html", true);
 
			Collection<Tag> a19Tags = new ArrayList<Tag>();
			try {
				a19Tags.add(getTag("statistics"));
				a19Tags.add(getTag("parametric data"));
				a19Tags.add(getTag("resampling"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
	

			Activity a19 = null;
			try {
				a19 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Partial and Semi-Partial Correlations", null, rcA19,
								VisibilityType.PUBLIC, a19Tags);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}

 
			AttachmentPreview rcA20 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://explorable.com/partial-correlation-analysis.html", true);
 
			Collection<Tag> a20Tags = new ArrayList<Tag>();
			try {
				a20Tags.add(getTag("statistics"));
				a20Tags.add(getTag("parametric data"));
				a20Tags.add(getTag("resampling"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
	

			Activity a20 = null;
			try {
				a20 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Partial Correlation Analysis", null, rcA20,
								VisibilityType.PUBLIC, a20Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			TargetCompetence tc5 = null;
			try {
				tc5 = ServiceLocator
						.getInstance()
						.getService(CompetenceManager.class)
						.createNewTargetCompetence(
								userNickPowell,
								"Construct Partial Correlations",
								"Partial correlation is the relationship between two variables while controlling "
										+ "for a third variable. The purpose is to find the unique variance between two "
										+ "variables while eliminating the variance from a third variables.",
								12,
								11,
								null, 
								VisibilityType.PRIVATE);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");
 			
			try {
				tc5 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc5, a19, true);
				tc5 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc5, a20, true);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
		
			try {
				tc5 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg2PhillAmstrong, tc5, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			AttachmentPreview rcA21 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://exploringdata.net/sampling.htm", true);
 			

			Collection<Tag> a21Tags = new ArrayList<Tag>();
			try {
				a21Tags.add(getTag("statistics"));
				a21Tags.add(getTag("parametric data"));
				a21Tags.add(getTag("resampling"));
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
	

			Activity a21 = null;
			try {
				a21 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell, "Sampling activity", null, rcA21,
								VisibilityType.PUBLIC, a21Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
  
			AttachmentPreview rcA22 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.khanacademy.org/math/statistics/v/introduction-to-the-normal-distribution", true);
 			
			Collection<Tag> a22Tags = new ArrayList<Tag>();
			try {
				a22Tags.add(getTag("statistics"));
				a22Tags.add(getTag("parametric data"));
				a22Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}


			Activity a22 = null;
			try {
				a22 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell, "Normal Distribution", null,
								rcA22, VisibilityType.PUBLIC, a22Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 

			deadlineCal.add(Calendar.DATE, -3);
			logger.info("initRepository");
			LearningGoal lg3 = null;
			try {
				lg3 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Exploratory analysis of data",
								"Exploratory analysis of data makes use of graphical and numerical techniques to "
										+ "study patterns and departures from patterns. In examining distributions of data, "
										+ "students should be able to detect important characteristics, such as shape, location, "
										+ "variability, and unusual values. From careful observations of patterns in data, "
										+ "students can generate conjectures about relationships among variables. The notion of "
										+ "how one variable may be associated with another permeates almost all of statistics, from "
										+ "simple comparisons of proportions through linear regression. The difference between "
										+ "association and causation must accompany this conceptual development throughout.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			//userKevinHall = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userKevinHall);
			
			TargetCompetence tc6 = null;
			try {
				tc6 = ServiceLocator
						.getInstance()
						.getService(CompetenceManager.class)
						.createNewTargetCompetence(
								userNickPowell,
								"Analyze Data",
								"Know how to take raw data, extract meaningful information and use statistical tools.",
								12,
								7,
								null, 
								VisibilityType.PRIVATE);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 			
			try {
				tc6 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc6, a21, true);
				tc6= ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc6, a22, true);

			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			TargetLearningGoal lg3KevinHall = null;
			try {
				lg3KevinHall =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg3);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tc6 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg3KevinHall, tc6, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			LearningGoal lgtac1 = null;
			try {
				lgtac1 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Statistics 2 – Inference and Association",
								"This course, the second in a three-course sequence, "
										+ "provides an easy introduction to inference and association through a series of practical applications, "
										+ "based on the resampling/simulation approach. Once you have completed this course you will be able to "
										+ "test hypotheses and compute confidence intervals regarding proportions or means, computer correlations and "
										+ "fit simple linear regressions.  Topics covered also include chi-square goodness-of-fit and paired comparisons.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			//userAnthonyMoore = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userAnthonyMoore);

			TargetCompetence tac1 = null;
			try {
				tac1 = ServiceLocator
						.getInstance()
						.getService(CompetenceManager.class)
						.createNewTargetCompetence(
								userNickPowell,
								"Analyse statistical data",
								"The process of evaluating data using analytical and logical "
									+ "reasoning to examine each component of the data provided. "
									+ "This form of analysis is just one of the many steps that must "
									+ "be completed when conducting a research experiment. Data from "
									+ "various sources is gathered, reviewed, and then analyzed to form "
									+ "some sort of finding or conclusion. There are a variety of specific "
									+ "data analysis method, some of which include data mining, text analytics, "
									+ "business intelligence, and data visualizations.",
								12,
								7,
								null, 
								VisibilityType.PRIVATE);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			TargetLearningGoal lgtac1AnthonyMoore = null;
			try {
				lgtac1AnthonyMoore =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgtac1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tac1 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class).
						addTargetCompetenceToGoal(userNickPowell, lgtac1AnthonyMoore, tac1, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
		//	userAnthonyMoore = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userAnthonyMoore);
			
			try {
				@SuppressWarnings("unused")
				LearningGoal lgtac2 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Spatial Analysis Techniques in R taught by Dave Unwin",
								"This course will teach users how to implement spatial statistical "
										+ "analysis procedures using R software. Topics covered include point pattern analysis, "
										+ "identifying clusters, measures of spatial association, geographically weighted regression "
										+ "and surface procession.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			TargetCompetence tac2 = ServiceLocator.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell, tac1.getCompetence(), 
							VisibilityType.PRIVATE);
 
			List<Activity> acts=new ArrayList<Activity>();
			for(TargetActivity ta:tac1.getTargetActivities()){
				acts.add(ta.getActivity());
			}
			try {
				ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.cloneActivitiesAndAddToTargetCompetence(
								userNickPowell, tac2, acts, false, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			tac2 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(tac2);
 
			
			try {
				tac2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tac2, a14, true);
				tac2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tac2, a19, true);
				
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
				tac2 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(tac2);
				logger.info("initRepository");
			// post status
			try {
				ServiceLocator
						.getInstance()
						.getService(PostManager.class)
						.createNewPost(
								userNickPowell,
								"Can anybody recommend me a good book for SPSS basics? Thanks!",
								VisibilityType.PUBLIC, null, null, true, null);
			} catch (EventException e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
 
			LearningGoal lg4 = null;
			try {
				lg4 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning Parametric statistics",
								"Parametric statistics is a branch of statistics that assumes that the data has come from a type of probability distribution and makes inferences about the parameters of the distribution. Most well-known elementary statistical methods are parametric",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
 
			
			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lg4, VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e4) {
				
				logger.error(e4.getLocalizedMessage());
			} catch (EventException e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
			lg4 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(lg4);

			TargetCompetence tc7 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc1.getCompetence(), 
							VisibilityType.PRIVATE);

			try {
				ServiceLocator.getInstance().getService(EventFactory.class)
						.generateEvent(EventType.Create, userNickPowell, tc7);
			} catch (EventException e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
			logger.info("initRepository");
 
			AttachmentPreview rcAB1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.mathsisfun.com/data/standard-normal-distribution.html", true);
 

			Collection<Tag> ab1Tags = new ArrayList<Tag>();
			try {
				ab1Tags.add(getTag("statistics"));
				ab1Tags.add(getTag("parametric data"));
				ab1Tags.add(getTag("resampling"));
			} catch (Exception e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
	

			Activity ab1 = null;
			try {
				ab1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Parametric statistics, From Wikipedia, the free encyclopedia", null,
								rcAB1, VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
 
			
			AttachmentPreview rcAB2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://laboratory-manager.advanceweb.com/Columns/Interpreting-Statistics/Non-Parametric-Statistics.aspx", 
							true);
 
			
			try {
				@SuppressWarnings("unused")
				Activity ab2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Non-parametric statistics, From Wikipedia, the free encyclopedia", null,
								rcAB2, VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e4) {
				
				logger.error(e4.getLocalizedMessage());
			}
			
 
			
			try {
				tc7 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc7, ab1, true);
			} catch (EventException e3) {
				logger.error(e3.getLocalizedMessage());
			}
			
			TargetLearningGoal lg4NickPowell = null;
			try {
				lg4NickPowell =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg4);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}

			try {
				tc7 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg4NickPowell, tc7, false, null).getNode();
			} catch (EventException e2) {
				
				logger.error(e2.getLocalizedMessage());
			}
			
			lg4 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(lg4);

			// /Andrew Camper activities
			LearningGoal lg5 = null;
			try {
				lg5 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning Probability theory",
								"Probability theory is the branch of mathematics concerned with probability, "
										+ "the analysis of random phenomena.The central objects of probability theory are random variables, "
										+ "stochastic processes, and events: mathematical abstractions of non-deterministic events or measured quantities "
										+ "that may either be single occurrences or evolve over time in an apparently random fashion. If an individual coin toss "
										+ "or the roll of dice is considered to be a random event, then if repeated many times the sequence of random events will "
										+ "exhibit certain patterns, which can be studied and predicted."
										+ "Two representative mathematical results describing such patterns are the law of large numbers and the central limit theorem.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}
			logger.info("initRepository");
			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lg5, VisibilityType.PRIVATE.toString(), null);
			} catch (VisibilityCoercionError e1) {
				
				logger.error(e1.getLocalizedMessage());
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}
			
			lg5 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(lg5);
			
			//userAndrewCamper = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userAndrewCamper);

			TargetCompetence tc8 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc1.getCompetence(), 
							VisibilityType.PRIVATE);

			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tc8);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}

 	
			AttachmentPreview rcACs = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.probabilitytheory.info/", true);
 

			Activity ac1 = null;
			try {
				ac1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Probability theory, From Wikipedia, the free encyclopedia", null,
								rcACs, VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}
	 		AttachmentPreview rcAC2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.vosesoftware.com/ModelRiskHelp/index.htm#Probability_theory_and_statistics/The_basics/Probability_equations/Probability_mass_function_%28pmf%29.htm", 
						true);
  
			

			Activity ac2 = null;
			try {
				ac2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Probability mass function, From Wikipedia, the free encyclopedia", null,
								rcAC2, VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			List<Activity> acts2=new ArrayList<Activity>();
			for(TargetActivity ta:tac1.getTargetActivities()){
				acts2.add(ta.getActivity());
			}
			try {
				ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.cloneActivitiesAndAddToTargetCompetence(
								userNickPowell, tc8, acts2, false, null);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			tc8 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(tc8);
			logger.info("initRepository");
 
			
			try {
				tc8 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc8, ac1, true);
				tc8 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc8, ac2, true);
				
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
				tc8 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(tc8);
			
 

			TargetLearningGoal lg5NickPowell = null;
			try {
				lg5NickPowell =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg5);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
				
			try {
				tc8 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg5NickPowell, tc8, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			// /Andrew Camper activities
			LearningGoal lg6 = null;
			try {
				lg6 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning Regression Analyses",
								"Parametric statistics assume more about the quality of the data, "
										+ "but in return they can tell us more about what is going on with those data.  "
										+ "The most common parametric statistics assume the “General Linear Model”—that is, "
										+ "they assume that the “true,” underlying distribution of the data can be described "
										+ "by a straight line (or one of its variants).  We will look particularly at correlation "
										+ "and analysis of variance.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lg6,	VisibilityType.PRIVATE.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			lg6 = ServiceLocator.getInstance().getService(DefaultManager.class).merge(lg6);
			
			//userAkikoKido = ServiceLocator.getInstance().getService(DefaultManager.class).merge(userAkikoKido);

			ServiceLocator
					.getInstance()
					.getService(NodeRecommendationManager.class)
					.sendRecommendation(userNickPowell, userNickPowell, lg6,
							RecommendationType.USER);

			TargetCompetence tc9 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc1.getCompetence(), 
							VisibilityType.PRIVATE);
			logger.info("initRepository");
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tc9);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 		
			AttachmentPreview rcAD1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://krypton.mnsu.edu/~tony/courses/502/Regression.html", true);
 
			
 

			Activity ad1 = null;
			try {
				ad1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"URBS 502:  Regression Analyses", null, rcAD1,
								VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 
			AttachmentPreview rcAD2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.csse.monash.edu.au/~smarkham/resources/param.htm", true);
 
			
			
			Activity ad2 = null;
			try {
				ad2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Parametric versus non-parametric", null, rcAD2,
								VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 
			
			try {
				tc9 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc9, ad1, true);
				tc9 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tc9, ad2, true);

			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 
			
			TargetLearningGoal lg6NickPowell = null;
			try {
				lg6NickPowell =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg6);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tc9 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lg6NickPowell, tc9, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");
			lg1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).merge(lg1);
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
				
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lg1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			// ///////////////////////////////
			// LearningPlans for Ilustrate and Prepare Data
			// ////////////////////////////////////////////

			// ///Erica Ames activities

			LearningGoal lgd1 = null;
			try {
				lgd1 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Preparing Data for Analysis",
								"This section provides an example of the programming code needed to read "
										+ "in a multilevel data file, to create an incident-level aggregated flat file "
										+ "for summary-level analysis, and to prepare individual data segments for detailed "
										+ "analysis. For illustration purposes, a National Incident-Based Reporting System "
										+ "(NIBRS) data file obtained from the FBI is read into and restructured in SPSS, "
										+ "SAS, and Microsoft ACCESS. The concepts illustrated are applicable to state-level "
										+ "data sets and transferable to other software.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lgd1, VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lgd1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lgd1);
 

			TargetCompetence tcd1 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc2.getCompetence(), 
							VisibilityType.PRIVATE);
			
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tcd1);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			TargetCompetence tcnp1 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc2.getCompetence(), 
							VisibilityType.PRIVATE);
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tcnp1);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
 		
			AttachmentPreview rcBA1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/index.shtml", true);
 
 
			logger.info("initRepository");
			Collection<Tag> ba1Tags = new ArrayList<Tag>();
			try {
				ab1Tags.add(getTag("statistics"));
				ab1Tags.add(getTag("parametric data"));
				ab1Tags.add(getTag("resampling"));
			} catch (Exception e2) {
				
				logger.error(e2.getLocalizedMessage());
			}
		

			Activity ba1 = null;
			try {
				ba1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Extracting Data from Incident-Based Systems and NIBRS", null,
								rcBA1, VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}

 
			AttachmentPreview rcBA2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/preparing_data.shtml", true);
 
			
			Activity ba2 = null;
			try {
				ba2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"PREPARING A FILE FOR ANALYSIS", null, rcBA2,
								VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}

 			
			AttachmentPreview rcBA3 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/reading_data.shtml", true);
 
 

			Activity ba3 = null;
			try {
				ba3 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Reading a Multilevel Data File", null, rcBA3,
								VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}

 

			try {
				tcnp1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp1, ba1, true);
				tcnp1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp1, ba2, true);
				tcnp1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcnp1, ba3, true);
				
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			tcnp1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tcnp1);
			
			TargetLearningGoal lgd1RachelWiggins = null;
			try {
				lgd1RachelWiggins =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgd1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tcd1 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lgd1RachelWiggins, tcd1, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			// /Janie Biggs activities
			LearningGoal lgd2 = null;
			try {
				lgd2 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Drawing conclusions from data ",
								"How well do measurements of mercury concentrations in ten "
										+ "cans of tuna reflect the composition of the factory's entire output? "
										+ "Why can't you just use the average of these measurements? "
										+ "How much better would the results of 100 such tests be? This "
										+ "final lesson on measurement will examine these questions and introduce "
										+ "you to some of the methods of dealing with data. This stuff is important "
										+ "not only for scientists, but also for any intelligent citizen who wishes "
										+ "to independenly evaluate the flood of numbers served up by advertisers, "
										+ "politicians,  experts , and yes— by other scientists.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lgd2, VisibilityType.PRIVATE.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lgd2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lgd2);
 

			TargetCompetence tcd2 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tcd1.getCompetence(), 
							VisibilityType.PRIVATE);

			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tcd2);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			logger.info("initRepository");
			AttachmentPreview rcBB2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.chem1.com/acad/webtext/matmeasure/mm5.html", true);
 
 

			Activity bb1 = null;
			try {
				bb1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Drawing conclusions from data", null, rcBB2,
								VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e2) {
				
				logger.error(e2.getLocalizedMessage());
			}

 			
			AttachmentPreview rcBB3 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.chem1.com/acad/webtext/matmeasure/mm1.html", true);
 
 

			Activity bb2 = null;
			try {
				bb2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(
								userNickPowell,
								"Understanding the units of scientific measurement", null,
								rcBB3, VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}
			List<Activity> acts3=new ArrayList<Activity>();
			for(TargetActivity ta:tac1.getTargetActivities()){
				acts3.add(ta.getActivity());
			}
			try {
				ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.cloneActivitiesAndAddToTargetCompetence(
								userNickPowell, tcd2, acts3, false, null);
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}
			
			tcd2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tcd2);
			
 
			
			try {
				tcd2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcd2, bb1, true);
				tcd2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcd2, bb2, true);
			} catch (EventException e1) {
				logger.error(e1.getLocalizedMessage());
			}
			
			TargetLearningGoal lgd2AkikoKido = null;
			try {
				lgd2AkikoKido =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgd2);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tcd2 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lgd2AkikoKido, tcd2, false, null).getNode();
			} catch (EventException e1) {
				
				logger.error(e1.getLocalizedMessage());
			}

			// /Angelica Fallou activities
			LearningGoal lgd3 = null;
			try {
				lgd3 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Understanding Descriptive Statistics ",
								"Descriptive statistics can include graphical summaries that show the "
										+ "spread of the data, and numerical summaries that either measure the central "
										+ "tendency (a 'typical' data value) of a data set or that describe the spread of the data.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lgd3, VisibilityType.PRIVATE.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lgd3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lgd3);
 

			TargetCompetence tcd3 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tcd1.getCompetence(), 
							VisibilityType.PRIVATE);
			logger.info("initRepository");
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tcd3);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

  
			AttachmentPreview rcBC1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.nationalatlas.gov/articles/mapping/a_statistics.html", true);
 
			
			
			Activity bc1 = null;
			try {
				bc1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Understanding Descriptive Statistics", null, rcBC1,
								VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 
			
			AttachmentPreview rcBC2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.amstat.org/publications/jse/secure/v8n3/preston.cfm", true);
 
			

			Activity bc2 = null;
			try {
				bc2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Teaching Prediction Intervals", null, rcBC2,
								VisibilityType.PUBLIC, ab1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 
			
			try {
				tcd3 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcd3, bc1, true);
				tcd3 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tcd3, bc2, true);

			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			tcd3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tcd3);
			
			TargetLearningGoal lgd3IdaFritz = null;
			try {
				lgd3IdaFritz =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgd3);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tcd3 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lgd3IdaFritz, tcd3, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lgd1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			// ///////////////////////////////
			// LearningPlans for Outline Descriptive Statistic
			// ////////////////////////////////////////////

			// ///Erica Ames activities

			LearningGoal lge1 = null;
			try {
				lge1 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning Descriptive statistics",
								"Descriptive statistics is the discipline of quantitatively describing the main features"
										+ " of a collection of data.Descriptive statistics are distinguished from inferential "
										+ "statistics (or inductive statistics), in that descriptive statistics aim to summarize "
										+ "a sample, rather than use the data to learn about the population that the sample of "
										+ "data is thought to represent. This generally means that descriptive statistics, unlike "
										+ "inferential statistics, are not developed on the basis of probability theory.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lge1, VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lge1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lge1);
 

			LearningGoal lge2 = null;
			try {
				lge2 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning about Partial Correlations",
								"In probability theory and statistics, partial correlation measures "
										+ "the degree of association between two random variables, with the effect "
										+ "of a set of controlling random variables removed.",
								deadlineCal.getTime(), lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lge2, VisibilityType.PUBLIC.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lge2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lge2);
 

			LearningGoal lge3 = null;
			try {
				lge3 = ServiceLocator
						.getInstance()
						.getService(LearningGoalManager.class)
						.createNewLearningGoal(
								userNickPowell,
								"Learning about Approximation theory",
								"In mathematics, approximation theory is concerned with how "
										+ "functions can best be approximated with simpler functions, "
										+ "and with quantitatively characterizing the errors introduced thereby. "
										+ "Note that what is meant by best and simpler will depend on the "
										+ "application..", deadlineCal.getTime(),
								lg2Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			try {
				ServiceLocator
						.getInstance()
						.getService(VisibilityManager.class)
						.setResourceVisibility(userNickPowell, lge3, VisibilityType.PRIVATE.toString(), null);
			} catch (VisibilityCoercionError e) {
				
				logger.error(e.getLocalizedMessage());
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			lge3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lge3);
 

			TargetCompetence tce1 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc3.getCompetence(), 
							VisibilityType.PRIVATE);
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tce1);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			TargetCompetence tce2 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc5.getCompetence(), 
							VisibilityType.PRIVATE);
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tce2);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			TargetCompetence tce3 = ServiceLocator
					.getInstance()
					.getService(ResourceFactory.class)
					.createNewTargetCompetence(userNickPowell,
							tc5.getCompetence(), 
							VisibilityType.PRIVATE);
			try {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Create, userNickPowell, tce3);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			logger.info("initRepository");
			AttachmentPreview rcCA1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://mste.illinois.edu/hill/dstat/dstat.html", true);
 
 

			Collection<Tag> ca1Tags = new ArrayList<Tag>();
			try {
				ab1Tags.add(getTag("statistics"));
				ab1Tags.add(getTag("parametric data"));
				ab1Tags.add(getTag("resampling"));
			} catch (Exception e) {
				
				logger.error(e.getLocalizedMessage());
			}
		

			Activity ca1 = null;
			try {
				ca1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Descriptive statistics", null, rcCA1,
								VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 		
			
			AttachmentPreview rcCA2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://psychology.ucdavis.edu/sommerb/sommerdemo/stat_inf/intro.htm", true);
 
 

			Activity ca2 = null;
			try {
				ca2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Statistical inference", null, rcCA2,
								VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 
			AttachmentPreview rcCA3 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.rgs.org/OurWork/Schools/Fieldwork+and+local+learning/Fieldwork+techniques/Sampling+techniques.htm", 
							true);
 
			

			Activity ca3 = null;
			try {
				ca3 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Sampling (statistics)", null, rcCA3,
								VisibilityType.PUBLIC, ca1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
 
			
			try {
				tce1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce1, ca1, true);
				tce1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce1, ca2, true);
				tce1 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce1, ca3, true);

			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			
			tce1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tce1);
			logger.info("initRepository");
			TargetLearningGoal lge1AnnaHallowell = null;
			try {
				lge1AnnaHallowell =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lge1);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tce1 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class)
						.addTargetCompetenceToGoal(userNickPowell, lge1AnnaHallowell, tce1, false, null).getNode();
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 
			AttachmentPreview rcCC1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.psychwiki.com/wiki/Inferential_Statistics", true);
 
			
			
			Activity cc1 = null;
			try {
				cc1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Inferential Statistics", null, rcCC1,
								VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

 			
			AttachmentPreview rcCC2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.psychwiki.com/wiki/What_is_%22normality%22%3F", true);
 
 

			Activity cc2 = null;
			try {
				cc2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell, "What is normality?", null,
								rcCC2, VisibilityType.PUBLIC, ca1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
 
			logger.info("initRepository");
			try {
				tce2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce2, cc1, true);
				tce2 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce2, cc2, true);
				
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");
			tce2 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tce2);
			
			AttachmentPreview rcCD1 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://www.psychwiki.com/wiki/Inferential_Statistics", true);
 
			logger.info("initRepository");
			Activity cd1 = null;
			try {
				cd1 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Approximation theory", null, rcCD1,
								VisibilityType.PUBLIC, ba1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			}

			logger.info("initRepository");
 
			AttachmentPreview rcCD2 = ServiceLocator
					.getInstance()
					.getService(HTMLParser.class)
					.parseUrl("http://en.wikipedia.org/wiki/Approximation_theory", true);
			logger.info("initRepository");
			 Activity cd2 = null;
			try {
				cd2 = ServiceLocator
						.getInstance()
						.getService(ActivityManager.class)
						.createNewActivity(userNickPowell,
								"Information theory", null, rcCD2,
								VisibilityType.PUBLIC, ca1Tags);
			} catch (EventException e) {
				
				logger.error(e.getLocalizedMessage());
			} 
			logger.info("initRepository");
			
			try {
				tce3 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce3, cd1, true);
			 	tce3 = ServiceLocator.getInstance().getService(LearningGoalManager.class).addActivityToTargetCompetence(userNickPowell, tce3, cd2, true);
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			tce3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(tce3);
			logger.info("initRepository");

			try {
				tce1 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class).
						addTargetCompetenceToGoal(userNickPowell, lge1AnnaHallowell, tce1, false, null).getNode();
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			logger.info("initRepository");
			lge1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lge1);
			
			try {
				tce2 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class).
						addTargetCompetenceToGoal(userNickPowell, lge1AnnaHallowell, tce2, false, null).getNode();
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			
			lge1 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lge1);
			logger.info("initRepository");
			TargetLearningGoal lge3StevenTurner = null;
			try {
				lge3StevenTurner =
					ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lge3);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
			try {
				tce3 = (TargetCompetence) ServiceLocator.getInstance().getService(LearningGoalManager.class).
						addTargetCompetenceToGoal(userNickPowell, lge3StevenTurner, tce3, false, null).getNode();
			} catch (EventException e) {
				logger.error(e.getLocalizedMessage());
			}
			lge3 = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(lge3);

			try {
				ServiceLocator
					.getInstance()
					.getService(LearningGoalManager.class)
					.createNewTargetLearningGoal(userNickPowell, lge3);
			} catch (EventException e) {
				logger.error(e.getMessage());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e.getMessage());
			}
			
 
			logger.info("initRepository");
			ServiceLocator.getInstance().getService(RegistrationManager.class).setEmailAsVerified("prosolo.admin@gmail.com", true);
			ServiceLocator.getInstance().getService(RegistrationManager.class).setEmailAsVerified("nick.powell@gmail.com", true);
 	}

	public TargetCompetence createTargetCompetence(User user, String title, String description, int validity, int duration)
			throws EventException {
		
		TargetCompetence tComp = ServiceLocator
				.getInstance()
				.getService(CompetenceManager.class)
				.createNewTargetCompetence(
						user,
						title,
						description,
						validity,
						duration,
						null, 
						VisibilityType.PRIVATE);
		
		return tComp;
	}

	private InputStream getAvatarInputStream(String avatarName) {
		URL url = Thread.currentThread().getContextClassLoader()
				.getResource("test_avatars120x120/" + avatarName);

		try {
			return new FileInputStream(new File(url.getFile()));
		} catch (FileNotFoundException e) {
			logger.error(e.getLocalizedMessage());
		}
		return null;
	}


	protected Tag getOrCreateTag(String tagTitle) throws Exception {
		return ServiceLocator.getInstance().getService(TagManager.class)
				.getOrCreateTag(tagTitle);
	}

}
