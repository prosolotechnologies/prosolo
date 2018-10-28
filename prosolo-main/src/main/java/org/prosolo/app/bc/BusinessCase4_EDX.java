package org.prosolo.app.bc;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.admin.BulkDataAdministrationService;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @author Zoran Jeremic Oct 11, 2014
 *
 */
@Service
public class BusinessCase4_EDX {

    private static Logger logger = Logger.getLogger(BusinessCase4_EDX.class.getName());

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy. HH:mm");

	public void initRepository() {
        logger.info("BusinessCaseTest - initRepository() with BC 4");

		EventQueue events = EventQueue.newEventQueue();

		// get ROLES
		Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.USER);
		Role roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.MANAGER);
		Role roleInstructor = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.INSTRUCTOR);
		Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.ADMIN);
		Role roleSuperAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.SUPER_ADMIN);

		/*
		 * CREATING USERS
		 */
		String genericPassword = "prosolo@2018";

		User userNickPowell = extractResultAndAddEvents(events, createUser(0,"Nick", "Powell", "nick.powell@gmail.com", genericPassword, "Teacher", "male1.png", roleAdmin));

		//generate event after roles are updated
		Map<String, String> params = null;
		events.appendEvent(ServiceLocator.getInstance().getService(EventFactory.class).generateEventData(
				EventType.USER_ROLES_UPDATED, UserContextData.ofActor(userNickPowell.getId()),
				userNickPowell, null, null, params));

		//create organization
		OrganizationData orgData = new OrganizationData();
		orgData.setTitle("Desert Winds University");
		orgData.setAdmins(Arrays.asList(new UserData(userNickPowell)));


		Organization org = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(OrganizationManager.class)
				.createNewOrganizationAndGetEvents(orgData, UserContextData.empty()));

		userNickPowell.setOrganization(org);

		// create org. unit School of Information Technologies
		Unit unit = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
					.createNewUnitAndGetEvents("School of Information Technologies", org.getId(), 0, createUserContext(userNickPowell)));

		// create 20 students
		User userRichardAnderson = extractResultAndAddEvents(events, createUser(org.getId(), "Richard", "Anderson", "richard.anderson@gmail.com", genericPassword, "Student", "male2.png", roleUser));
		User userKevinMitchell = extractResultAndAddEvents(events, createUser(org.getId(), "Kevin", "Mitchell", "kevin.mitchell@gmail.com", genericPassword, "Student", "male3.png", roleUser));
		User userPaulEdwards = extractResultAndAddEvents(events, createUser(org.getId(), "Paul", "Edwards", "paul.edwards@gmail.com", genericPassword, "Student", "male4.png", roleUser));
		User userStevenTurner = extractResultAndAddEvents(events, createUser(org.getId(), "Steven", "Turner", "steven.turner@gmail.com", genericPassword, "Student", "male5.png", roleUser));
		User userGeorgeYoung = extractResultAndAddEvents(events, createUser(org.getId(), "George", "Young", "george.young@gmail.com", genericPassword, "Student", "male6.png", roleUser));
		User userJosephGarcia = extractResultAndAddEvents(events, createUser(org.getId(), "Joseph", "Garcia", "joseph.garcia@gmail.com", genericPassword, "Student", "male8.png", roleUser));
		User userTimothyRivera = extractResultAndAddEvents(events, createUser(org.getId(), "Timothy", "Rivera", "timothy.rivera@gmail.com", genericPassword, "Student", "male9.png", roleUser));
		User userKevinHall = extractResultAndAddEvents(events, createUser(org.getId(), "Kevin", "Hall", "kevin.hall@gmail.com", genericPassword, "Student", "male10.png", roleUser));
		User userKennethCarter = extractResultAndAddEvents(events, createUser(org.getId(), "Kenneth", "Carter", "kenneth.carter@gmail.com", genericPassword, "Student", "male11.png", roleUser));
		User userAnthonyMoore = extractResultAndAddEvents(events, createUser(org.getId(), "Anthony", "Moore", "anthony.moore@gmail.com", genericPassword, "Student", "male12.png", roleUser));
		User userTaniaCortese = extractResultAndAddEvents(events, createUser(org.getId(), "Tania", "Cortese", "tania.cortese@gmail.com", genericPassword, "Student", "female1.png", roleUser));
		User userSonyaElston = extractResultAndAddEvents(events, createUser(org.getId(), "Sonya", "Elston", "sonya.elston@gmail.com", genericPassword, "Student", "female2.png", roleUser));
		User userLoriAbner = extractResultAndAddEvents(events, createUser(org.getId(), "Lori", "Abner", "lori.abner@gmail.com", genericPassword, "Student", "female3.png", roleUser));
		User userSamanthaDell = extractResultAndAddEvents(events, createUser(org.getId(), "Samantha", "Dell", "samantha.dell@gmail.com", genericPassword, "Student", "female4.png", roleUser));
		User userAkikoKido = extractResultAndAddEvents(events, createUser(org.getId(), "Akiko", "Kido", "akiko.kido@gmail.com", genericPassword, "Student", "female7.png", roleUser));
		User userHelenCampbell = extractResultAndAddEvents(events, createUser(org.getId(), "Helen", "Campbell", "helen.campbell@gmail.com", genericPassword, "Student", "female13.png", roleUser));
		User userSheriLaureano = extractResultAndAddEvents(events, createUser(org.getId(), "Sheri", "Laureano", "sheri.laureano@gmail.com", genericPassword, "Student", "female14.png", roleUser));
		User userAngelicaFallon = extractResultAndAddEvents(events, createUser(org.getId(), "Angelica", "Fallon", "angelica.fallon@gmail.com", genericPassword, "Student", "female16.png", roleUser));
		User userIdaFritz = extractResultAndAddEvents(events, createUser(org.getId(), "Ida", "Fritz", "ida.fritz@gmail.com", genericPassword, "Student", "female17.png", roleUser));
		User userRachelWiggins = extractResultAndAddEvents(events, createUser(org.getId(), "Rachel", "Wiggins", "rachel.wiggins@gmail.com", genericPassword, "Student", "female20.png", roleUser));

		// create 4 instructors
		User userPhilArmstrong = extractResultAndAddEvents(events, createUser(org.getId(), "Phil", "Armstrong", "phil.armstrong@gmail.com", genericPassword, "Teaching Assistant", "male7.png", roleInstructor));
		User userKarenWhite = extractResultAndAddEvents(events, createUser(org.getId(), "Karen", "White", "karen.white@gmail.com", genericPassword, "Teaching Assistant", "female10.png", roleInstructor));
		User userAnnaHallowell = extractResultAndAddEvents(events, createUser(org.getId(), "Anna", "Hallowell", "anna.hallowell@gmail.com", genericPassword, "Teaching Assistant", "female11.png", roleInstructor));
		User userErikaAmes = extractResultAndAddEvents(events, createUser(org.getId(), "Erika", "Ames", "erika.ames@gmail.com", genericPassword, "Teaching Assistant", "female12.png", roleInstructor));

		
		//////////////////////////////
		// Add roles to users
		//////////////////////////////

		// Nick Powell is Manager, Admin (already set when creating user) and Super Admin
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userNickPowell.getId());
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleSuperAdmin, userNickPowell.getId());

		// Karen White is Manager and Instructor (already set when user is defined)
		userKarenWhite = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userKarenWhite.getId());
		events.appendEvent(ServiceLocator.getInstance().getService(EventFactory.class).generateEventData(
				EventType.Edit_Profile, createUserContext(userKarenWhite), userKarenWhite, null, null, null));

		// Phil Armstrong is Instructor (already set when user is defined)
		// Anna Hallowell is Instructor (already set when user is defined)
		// Erika Ames is Instructor (already set when user is defined)

		// add managers to the unit School of Information Technologies
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userNickPowell.getId(), unit.getId(), roleManager.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKarenWhite.getId(), unit.getId(), roleManager.getId(), createUserContext(userKarenWhite)));

		// add instructors to the unit School of Information Technologies
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKarenWhite.getId(), unit.getId(), roleInstructor.getId(), createUserContext(userKarenWhite)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userPhilArmstrong.getId(), unit.getId(), roleInstructor.getId(), createUserContext(userPhilArmstrong)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAnnaHallowell.getId(), unit.getId(), roleInstructor.getId(), createUserContext(userAnnaHallowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userErikaAmes.getId(), unit.getId(), roleInstructor.getId(), createUserContext(userErikaAmes)));

		// add students to the unit School of Information Technologies
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userRichardAnderson.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKevinMitchell.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userPaulEdwards.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userStevenTurner.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userGeorgeYoung.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userJosephGarcia.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userTimothyRivera.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKevinHall.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKennethCarter.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAnthonyMoore.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userTaniaCortese.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSonyaElston.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userLoriAbner.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSamanthaDell.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAkikoKido.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userHelenCampbell.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSheriLaureano.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAngelicaFallon.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userIdaFritz.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userRachelWiggins.getId(), unit.getId(), roleUser.getId(), createUserContext(userNickPowell)));

        // list of all students from the School of Information Technologies
        List<User> schoolOfInformationTechnologiesStudents = Arrays.asList(userRichardAnderson, userKevinMitchell, userPaulEdwards, userStevenTurner, userGeorgeYoung, userJosephGarcia, userTimothyRivera, userKevinHall, userKennethCarter,
                userAnthonyMoore, userTaniaCortese, userSonyaElston, userLoriAbner, userSamanthaDell, userAkikoKido, userHelenCampbell, userSheriLaureano, userAngelicaFallon, userIdaFritz, userRachelWiggins);

		// ////////////////////////////
		// Create Credentials
		// ///////////////////////////////
		Credential1 cred1 = createCredential(events,
				"Basics of Social Network Analysis",
				"This credential defines social network analysis and its main analysis methods and "
						+ "introduces how to perform social network analysis and visualize analysis results in Gephi",
				userNickPowell,
				"network structure, data collection, learning analytics, network measures, network modularity, social network analysis",
				BlindAssessmentMode.BLIND);


		Competence1 comp1cred1;
		Activity1 act1comp1cred1 = null;
		Activity1 act2comp1cred1 = null;
		Activity1 act4comp1cred1 = null;
		try {
			comp1cred1 = createCompetence(events,
					userNickPowell,
					"Social Network Analysis",
					"Define social network analysis and its main analysis methods.",
					cred1.getId(),
					"centrality measures, data collection, modularity analysis, network centrality, network structure, social network analysis",
					LearningPathType.ACTIVITY);

			act1comp1cred1 = createActivity(events, org.getId(),
					userNickPowell,
					"Introduction to Social Network Analysis",
					"Introduction into social network analysis for week 3 of DALMOOC by Dragan Gasevic.",
					"https://www.youtube.com/watch?v=2uibqSdHSag",
					ActivityType.VIDEO,
					comp1cred1.getId(),
					0,
					5,
					ActivityResultType.TEXT,
					"Slides",
					"https://www.slideshare.net/dgasevic/introduction-into-social-network-analysis/");

			act2comp1cred1 = createActivity(events, org.getId(),
					userNickPowell,
					"Example dataset",
					null,
					"<p>Download the example dataset used in the videos and familiarize with the data.</p>",
					ActivityType.TEXT,
					comp1cred1.getId(),
					0,
					3,
					ActivityResultType.TEXT,
					"Example datasets used in the videos",
					"https://s3.amazonaws.com/prosoloedx2/files/3f86bdfd0e8357f7c60c36b38c8fc2c0/Example%20datasets%20used%20in%20the%20videos.pdf");

            createActivity(events, org.getId(),
                    userNickPowell,
                    "Network Modularity and Community Identification",
                    "Dragan Gasevic discusses network modularity and community identification in social network analysis for week 3 of DALMOOC. The presentation describes the notion of network modularity as a method used",
                    "https://www.slideshare.net/dgasevic/network-modularity-and-community-identification",
                    ActivityType.SLIDESHARE,
                    comp1cred1.getId(),
                    0,
                    6,
                    ActivityResultType.TEXT,
                    "Slides",
                    "http://www.slideshare.net/dgasevic/network-modularity-and-community-identification/1");

            act4comp1cred1 = createActivity(events, org.getId(),
                    userNickPowell,
                    "Network measures",
                    "Dragan Gasevic discusses network measures (degree centrality, betweenness centrality, closeness centrality, degree, diameter)  for week 3 of DALMOOC.",
                    "https://www.youtube.com/watch?v=Gq-4ErYLuLA",
                    ActivityType.VIDEO,
                    comp1cred1.getId(),
                    0,
                    8,
                    ActivityResultType.TEXT,
                    "Slides",
                    "http://www.slideshare.net/dgasevic/network-measures-used-in-social-network-analysis");

			createActivity(events, org.getId(),
					userNickPowell,
					"Assignment: Reflection and discussion on social network analysis",
					"",
					"<p>After the introduction into social network analysis, its main analysis techniques, and data collection "
							+ "methods, it would be useful to reflect on what you have learned so far. Please, prepare a reflection "
							+ "piece (about 300 words) in which you will address the following issues:</p><ul><li>Outline your "
							+ "understanding of social network structure and main methods for social network analysis (centrality, "
							+ "density, and modularity);</li><li>Discuss potential benefits of the use of social network analysis for "
							+ "the study of learning and learning contexts</li><li>Describe potential applications of social network "
							+ "analysis for the study of learning. Reflect on the methods that could be used for data collection, "
							+ "steps to be taken for the analysis, potential conclusions, and possible issues (e.g., incomplete "
							+ "network, triangulation with other types of analysis, or ethics) that would need to be addressed in "
							+ "the process.</li></ul><p>Please, share your reflection as a blog post (preferred as it allows for "
							+ "the broader reach). Once you have created your blog post, please, share the blog reference (URL) on "
							+ "Twitter with the <strong>#dalmooc</strong> hashtag and ProSolo.</p><ul><li>Once you have posted and "
							+ "shared your blog on social media, please, read and engage into conversation of the blogs posted by "
							+ "at least two our participants of the course. The conversation can be done as direct comments on the "
							+ "blogs and/or via other social media used in the course.</li><li>When connecting with other peers, "
							+ "try to compare the findings presented in their reports, connect their findings with the readings you "
							+ "found in the course and/or elsewhere in the web. Ideally, you will also reflect on the applicability "
							+ "of each other’s results in the real-world studies/contexts.</li></ul><p><em>Note: In case you do not "
							+ "have a blog and would not like to set up a blog, please, initiate a discussion thread on the edX forum, "
							+ "or create a ProSolo status post, and share the post reference (URL) on Twitter and ProSolo as described "
							+ "above.</em></p>",
					ActivityType.TEXT,
					comp1cred1.getId(),
					1,
					0,
					ActivityResultType.TEXT);
		} catch (Exception ex) {
			logger.error(ex);
		}

		Competence1 comp2cred1;
		try {
			comp2cred1 = createCompetence(events,
					userNickPowell,
					"Using Gephi for Social Network Analysis",
					"Perform social network analysis and visualize analysis results in Gephi",
					cred1.getId(),
					"community identification, gephi, network centrality, network density, network diameter, network visualization, social network analysis",
					LearningPathType.ACTIVITY);

			createActivity(events, org.getId(),
					userNickPowell,
					"Gephi Community Introduction",
					"A YouTube video introducing the Gephi tool",
					"https://www.youtube.com/watch?v=bXCBh6QH5W0",
					ActivityType.VIDEO,
					comp2cred1.getId(),
					0,
					4,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Download and install Gephi",
					"",
					"<p><strong>Windows</strong></p><ol><li>Make sure you have the latest version of JAVA runtime "
							+ "installed</li><li>Install Gephi</li><li>Open Gephi and try to open the <em>Les Miserables.gexf</em> "
							+ "sample</li><li>If you see a picture you are good to go, if not:</li><ol><li>If you see a grey window "
							+ "with no picture, go windows -&gt; graph and open the graph window</li><li>If no menus are working "
							+ "uninstall everything and try again</li></ol><li>If it doesn't work a second time, look and/or ask "
							+ "for a solution on the Gephi forum. Also, you may ask for assistance on the edX discussion forums, "
							+ "social media (Twitter and Pro Solo), and QuickHelper.</li></ol><p><strong>Mac</strong></p><ol><li>"
							+ "Install the latest version of JAVA</li><li>Re-install JAVA through the Mac site (don't ask me why "
							+ "you need to do this twice, it is some kind of magic)</li><li>Install Gephi</li><li>Open Gephi and "
							+ "try to open the <em>Les Miserables.gexf</em> sample</li><li>If you see a picture you are good to go, "
							+ "if not:</li><ol><li>If you see a grey window with no picture, go windows -&gt; graph and open the "
							+ "graph window</li><li>If no menus are working uninstall everything and try again</li></ol><li>If it "
							+ "doesn't work a second time, look and/or ask for a solution on the Gephi forum. Also, you may ask "
							+ "for assistance on the edX discussion forums, social media (Twitter and Pro Solo), and QuickHelper."
							+ "</li></ol>",
					ActivityType.TEXT,
					comp2cred1.getId(),
					0,
					15,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Gephi - An Introduction tour",
					"Dragan Gasevic gives an introductory tour of Gephi for week 3 of DALMOOC.",
					"https://www.youtube.com/watch?v=L0C_D68E1Q0",
					ActivityType.VIDEO,
					comp2cred1.getId(),
					0,
					17,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Gephi - Modularity Analysis",
					"Dragan Gasevic discusses modularity analysis in Gephi for week 3 of DALMOOC.",
					"https://www.youtube.com/watch?v=D1soIxZ61As",
					ActivityType.VIDEO,
					comp2cred1.getId(),
					0,
					11,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Gephi Tutorial Visualization",
					"Explore slide presentation: Gephi Tutorial Visualization",
					"http://www.slideshare.net/gephi/gephi-tutorial-visualization",
					ActivityType.SLIDESHARE,
					comp2cred1.getId(),
					0,
					15,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Hands-on -  Visualization of the results of social network analysis in Gephi",
					"",
					"<p>Hands-on activity – Visualization of the results of social network analysis in Gephi (example "
							+ "dataset)</p><p>After you have studied the resources on how to use Gephi, it is now time to perform "
							+ "some hands-on activities:</p><ul><li>Perform the following visualizations on the networks from the "
							+ "example dataset. The visualizations are to be performed in the Gephi projects, which you created in "
							+ "the previous hands-on activity when you performed the main network analyses (density, centrality, "
							+ "and modularity):</li><ul><li>Explore different layouts for the representation of the network (e.g., "
							+ "Fruchterman Reingold and Yinfan Hu) and experiment with their configuration parameters</li><li>Size "
							+ "the network nodes based on centrality measures</li><li>Size the network edges based on their weight"
							+ "</li><li>Explore how to visualize the labels of the network nodes and edges</li><li>Used different "
							+ "color to visualize the communities identified in the networks</li></ul><li>Share your experience "
							+ "(e.g., blogs and figures based on your visualizations) with other credential participants on social "
							+ "media (blog, Twitter, and Pro Solo, edX discussion forum).&nbsp; <br></li></ul>",
					ActivityType.TEXT,
					comp2cred1.getId(),
					1,
					0,
					ActivityResultType.TEXT);
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 cred2 = createCredential(events,
				"Sensemaking of Social Network Analysis for Learning",
				"This credential defines describes and critically reflects on possible approaches to the use of social network analysis for the study of learning. The credential also describes and interprets the results of social network analysis for the study of learning",
				userNickPowell,
				"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis",
				BlindAssessmentMode.OFF);


		Competence1 comp1cred2;
		try {
			comp1cred2 = createCompetence(events,
					userNickPowell,
					"Reflecting on approaches to the use of SNA for the study of learning",
					"Describe and critically reflect on possible approaches to the use of social network analysis for the study of learning",
					cred2.getId(),
					"academic performance, creative potential, learning analytics, learning design, MOOCs, sense of community, sensemaking, social network analysis",
					LearningPathType.ACTIVITY);

			createActivity(events, org.getId(),
					userNickPowell,
					"Introduction",
					"Introduction into sensemaking of social network analysis for the study of learning. Dragan Gasevic introduces us to week 4 of DALMOOC.",
					"https://www.youtube.com/watch?v=NPEeSArODQE",
					ActivityType.VIDEO,
					comp1cred2.getId(),
					0,
					4,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Social Network Analysis and Learning Design",
					"Dragan Gasevic discusses social network analysis and learning design for week 4 of DALMOOC.",
					"https://www.youtube.com/watch?v=-JuBDu_YVoo",
					ActivityType.VIDEO,
					comp1cred2.getId(),
					0,
					6,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Social Network Analysis and Sense of Community",
					"Dragan Gasevic discusses social network analysis and sense of community for week 4 of DALMOOC.",
					"https://www.youtube.com/watch?v=lUEeppG_6So",
					ActivityType.VIDEO,
					comp1cred2.getId(),
					0,
					5,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Social Network Analysis and Creative Potential",
					"Dragan Gasevic discusses social network analysis and creative potential for week 4 of DALMOOC.",
					"https://www.youtube.com/watch?v=VTGvvHpC5IQ",
					ActivityType.VIDEO,
					comp1cred2.getId(),
					0,
					4,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Social Network Analysis and Academic Peformance",
					"Dragan Gasevic discusses social network analysis and academic performance for week 4 of DALMOOC.",
					"https://www.youtube.com/watch?v=F9jLV7hS2AE",
					ActivityType.VIDEO,
					comp1cred2.getId(),
					0,
					6,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Social Network Analysis and Social Presence",
					"Dragan Gasevic discusses social network analysis and social presence for week 4 of DALMOOC.",
					"https://www.youtube.com/watch?v=bZhRuo8nz7A",
					ActivityType.VIDEO,
					comp1cred2.getId(),
					0,
					6,
					ActivityResultType.TEXT);

			createActivity(events, org.getId(),
					userNickPowell,
					"Hands-on activity: Integration of social network analysis in Gephi and Tableau analysis",
					"Dragan Gasevic discusses social network analysis and social presence for week 4 of DALMOOC.",
					"<p>Now that you have performed social network analysis in Gephi and started working on their interpretation of relevance for the understanding of learning, it is time to learn to integrate knowledge and skills gained in weeks 1-2 with Tableau. Specifically, in this hands-on activity, you are asked to:</p><ul><li>Export the results of social network analyses (centrality and modularity) of the networks available in the example dataset from Gephi – via the Data Laboratory tab of Gephi - in the format (i.e., CSV) that can be imported into Tableau</li><li>Plot the data to show the distribution of each centrality measure for each of the two networks</li><li>Plot the data to show the distribution of centrality measures across communities identified in each of the two networks</li><li>Share your experience (e.g., blogs and figures based on your visualizations from both Gephi and Tableau) with other course participants on social media (blog, Twitter, and Pro Solo, edX discussion forum).</li></ul>",
					ActivityType.TEXT,
					comp1cred2.getId(),
					0,
					40,
					ActivityResultType.TEXT);
		} catch (Exception ex) {
			logger.error(ex);
		}

		Competence1 comp2cred2;
		try {
			comp2cred2 = createCompetence(events,
					userNickPowell,
					"Interpreting the results of SNA",
					"Describe and interpret the results of social network analysis for the study of learning",
					cred2.getId(),
					"analytics interpretation, gephi, learning analytics, sensemaking, social network analysis, tableau", LearningPathType.EVIDENCE);

		} catch (Exception ex) {
			logger.error(ex);
		}


		Credential1 cred3 = createCredential(events,
				"Introduction to Learning Analytics",
				"The proliferation of data in digital environments has to date been largely unexplored in education. A new academic field - learning analytics - has developed to gain insight into learner generated data and how this can be used to improve learning and teaching practices",
				userNickPowell,
				"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis",
				BlindAssessmentMode.OFF);

		Competence1 comp1cred3;
		try {
			comp1cred3 = createCompetence(events,
					userNickPowell,
					"Tools for Learning Analytics",
					"Identify proprietary and open source tools commonly used in learning analytics",
					cred3.getId(),
					"academic performance, creative potential, social network analysis",
					LearningPathType.ACTIVITY);

			Activity1 act1comp1cred3 = createActivity(events, org.getId(),
					userNickPowell,
					"Getting Started With Data Analytics Tools",
					"A basic overview of the Data Anlytics tools by George Siemens",
					"https://www.youtube.com/watch?v=XOckgFlLqwU",
					ActivityType.VIDEO,
					comp1cred3.getId(),
					0,
					30,
					ActivityResultType.TEXT);
		} catch (Exception ex) {
			logger.error(ex);
		}


		Credential1 cred4 = createCredential(events,
				"Text Mining: Data Preparation",
				"As the process of preparing data for data mining / text mining can be very complex and requires a lot of time and thought, you should think at the beginning if it is realistic to achieve and if it is worth doing (e.g. when you can use the trained model for other studies where similar data is collected etc.).",
				userNickPowell,
				"data preparation, dalmooc, data",
				BlindAssessmentMode.OFF);

		Competence1 comp1cred4;
		try {
			comp1cred4 = createCompetence(events,
					userNickPowell,
					"Feature interpretation",
					"This starts when the model is already built. LightSide has a panel „Explore Results“ (normally used for error analysis) with which you also can look at feature weights. In the confusion matrix (called „Cell highlights“) you can select „feature weight“.",
					cred4.getId(),
					"feature interpretation",
					LearningPathType.EVIDENCE);
		} catch (Exception ex) {
			logger.error(ex);
		}

		Competence1 comp2cred4;
		try {
			comp2cred4 = createCompetence(events,
					userNickPowell,
					"Basic use of LightSIDE",
					"Prepare data for use in LightSIDE and use LightSIDE to extract a wide range of feature types",
					cred4.getId(),
					"academic performance, creative potential, social network analysis",
					LearningPathType.ACTIVITY);

			createActivity(events, org.getId(),
					userNickPowell,
					"Data Preparation",
					"Data Preparation in LightSIDE",
					"https://www.youtube.com/watch?v=jz5pwR0moL0",
					ActivityType.VIDEO,
					comp2cred4.getId(),
					0,
					45,
					ActivityResultType.NONE);
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 cred5 = createCredential(events,
				"Prediction modeling",
				"The credential introduces how to conduct prediction modeling effectively and appropriately and describe core uses of prediction modeling in education.",
				userNickPowell,
				"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis",
				BlindAssessmentMode.OFF);

		Competence1 comp1cred5;
		try {
			comp1cred5 = createCompetence(events,
					userNickPowell,
					"Basic of Prediction Modeling",
					"Conduct prediction modeling effectively and appropriately",
					cred5.getId(),
					"academic performance, creative potential, social network analysis",
					LearningPathType.ACTIVITY);

			createActivity(events, org.getId(),
					userNickPowell,
					"Introduction in prediction modeling and regressors",
					"Ryan Baker introduces prediction modeling and discusses regressors for week 5 of DALMOOC.",
					"https://www.youtube.com/watch?v=1ZkUyFtCNIk",
					ActivityType.VIDEO,
					comp1cred5.getId(),
					0,
					37,
					ActivityResultType.TEXT);
		} catch (Exception ex) {
			logger.error(ex);
		}

		/*
		 * Commenting on activities/ competences
		 */
		CommentData comment1 = commentOnActivity(events, userIdaFritz, act1comp1cred1, null, "10.06.2016. 15:24", "Very good presentation. Well suited for the novices like I am.");
		CommentData comment2 = commentOnActivity(events, userAnthonyMoore, act1comp1cred1, null, "12.06.2016. 09:50", "The video makes an important point of how individuals lay a data trail of interest that requires parties retrieving this information to proper understand the opportunities and confront “data overload” to best take advantage of this same data.");
		CommentData comment3 = commentOnActivity(events, userErikaAmes, act1comp1cred1, comment2, "13.06.2016. 13:02", "anthony - I would add to information overload and decision quality, the issue with multitasking and shorter attention spans (a la twitter)");
		CommentData comment4 = commentOnActivity(events, userKarenWhite, act2comp1cred1, comment2, "05.06.2016. 11:46", "The topics are well presented. Please take in account the fact that during the first week it is necessary for us, as learners, to become familiar with the dual-layer MOOC. This is important so every learner is building himself his knowledge");


		CommentData comment5 = commentOnActivity(events, userKevinHall, act1comp1cred1, null, "11.07.2016. 12:37", "Very good video to explain the meaning of learning analytics. Thanks");
		CommentData comment6 = commentOnActivity(events, userAnnaHallowell, act1comp1cred1, comment5, "11.07.2016. 17:42", "I also found this to be a concise introduction to learning analytics. :)");
		CommentData comment7 = commentOnActivity(events, userAkikoKido, act1comp1cred1, null, "12.07.2016. 08:17", "Nice video. Thanks.");
		CommentData comment8 = commentOnActivity(events, userIdaFritz, act1comp1cred1, null, "12.07.2016. 08:37", "Very concise, thanks");
		CommentData comment9 = commentOnActivity(events, userRichardAnderson, act1comp1cred1, null, "14.07.2016. 15:05", "This is short yet to the point in introducing learning analytics. Thanks");
		CommentData comment10 = commentOnActivity(events, userPhilArmstrong, act1comp1cred1, null, "12.07.2016. 16:21", "The video makes an important point of how individuals lay a data trail of interest that requires parties retrieving this information to proper understand the opportunities and confront “data overload” to best take advantage of this same data. In support of this point a study by Speier, Valacich, and Vessey (1999) state that “when information overload occurs, it is likely that a reduction in decision quality will occur” (p.338). Reference: Speier, C., Valacich, J. S., & Vessey, I. (1999). The influence of task interruption on individual decision making: An information overload perspective. Decision Sciences, 30(2), 337-360.");
		CommentData comment11 = commentOnActivity(events, userKevinHall, act1comp1cred1, comment10, "13.07.2016. 10:52", "I would add to information overload and decision quality, the issue with multitasking and shorter attention spans (a la twitter)");

		CommentData comment12 = commentOnActivity(events, userKevinHall, act4comp1cred1, null, "13.07.2016. 14:52", "I'm up to speed on the course design, but I wish I had found this video sooner!");
		CommentData comment13 = commentOnActivity(events, userPhilArmstrong, act4comp1cred1, null, "14.07.2016. 09:51", "I LIKE the idea of assignment bank. Definitely, each has different learning pace. I LOATHE the idea of group work. That is one thing I always wish to avoid in the university.");
		CommentData comment14 = commentOnActivity(events, userAnnaHallowell, act4comp1cred1, null, "14.07.2016. 12:43", "The topics are well presented. Please take in account the fact that during the first week it is necessary for us, as learners, to become familiar with the dual-layer MOOC. This is important so every learner is building himself his knowledge. ");
		CommentData comment15 = commentOnActivity(events, userGeorgeYoung, act4comp1cred1, null, "15.07.2016. 16:22", "Interesting structure. Its taken me a while to orient myself with the course setup, but am quite enjoying the use of prosolo as it helps to integrate the social media, content and course activities. The list of goals and competences (activities) has provided the best structure so far. ");
		CommentData comment16 = commentOnActivity(events, userKevinHall, act4comp1cred1, comment15, "16.07.2016. 20:37", "Great course.");

		/*
		 * Liking comments
		 */
		String commentContextMessage = "name:competence|id:1|context:/name:activity|id:1|context:/context:/name:comment|id:{0}/|name:target_activity|id:1//";

		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment5.getCommentId(), UserContextData.of(userAkikoKido.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment5.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment5.getCommentId(), UserContextData.of(userIdaFritz.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment5.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment5.getCommentId(), UserContextData.of(userPhilArmstrong.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment5.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment5.getCommentId(), UserContextData.of(userGeorgeYoung.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment5.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment5.getCommentId(), UserContextData.of(userAnnaHallowell.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment5.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment6.getCommentId(), UserContextData.of(userIdaFritz.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment6.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment6.getCommentId(), UserContextData.of(userKevinHall.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment6.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment6.getCommentId(), UserContextData.of(userGeorgeYoung.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment6.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment9.getCommentId(), UserContextData.of(userPhilArmstrong.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment9.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment9.getCommentId(), UserContextData.of(userAnnaHallowell.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment9.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment12.getCommentId(), UserContextData.of(userPhilArmstrong.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment12.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment12.getCommentId(), UserContextData.of(userAnnaHallowell.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment12.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment13.getCommentId(), UserContextData.of(userIdaFritz.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment13.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment13.getCommentId(), UserContextData.of(userRichardAnderson.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment13.getCommentId()), null))));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(comment14.getCommentId(), UserContextData.of(userGeorgeYoung.getId(), org.getId(), null, new PageContextData("/activity.xhtml", MessageFormat.format(commentContextMessage, comment14.getCommentId()), null))));


		////////////////////////////////
		// Create deliveries
		////////////////////////////////

		// Credential 1 delivery
		try {
			long date20DaysFromNow = LocalDateTime.now(Clock.systemUTC()).plusDays(20).atZone(ZoneOffset.ofTotalSeconds(0)).toInstant().toEpochMilli();
			Credential1 cred1Delivery = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(cred1.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(date20DaysFromNow), createUserContext(userNickPowell)));

			// add instructor to the delivery
			CredentialInstructor instructorKarenWhite = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(cred1Delivery.getId(), userKarenWhite.getId(), 0, createUserContext(userNickPowell)));

            // give learn privilege to all students from
            givePrivilegeToUsersOnDelivery(events, cred1Delivery, UserGroupPrivilege.Learn, userNickPowell, org, schoolOfInformationTechnologiesStudents);


            // enroll Lori Abner to the delivery
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(cred1Delivery.getId(), userLoriAbner.getId(), 0, createUserContext(userLoriAbner)));

			// Lori starts the first competency
			List<CompetenceData1> cred1CompetenciesLoriAbner = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(cred1Delivery.getId(), userLoriAbner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

			TargetCompetence1 cred1Comp1TargetLoriAbner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(cred1CompetenciesLoriAbner.get(0).getCompetenceId(), userLoriAbner.getId(), createUserContext(userLoriAbner)));

			// enroll other students
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(cred1Delivery.getId(), userGeorgeYoung.getId(), 0, createUserContext(userGeorgeYoung)));
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(cred1Delivery.getId(), userIdaFritz.getId(), 0, createUserContext(userIdaFritz)));
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(cred1Delivery.getId(), userErikaAmes.getId(), 0, createUserContext(userErikaAmes)));
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(cred1Delivery.getId(), userTaniaCortese.getId(), 0, createUserContext(userTaniaCortese)));
		} catch (IllegalDataStateException e) {
			e.printStackTrace();
		}


		// Credential 4 delivery
		try {
			long date90DaysFromNow = getDaysFromNow(90);
			Credential1 cred4Delivery = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(cred4.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(date90DaysFromNow), createUserContext(userNickPowell)));

			// add instructor to the delivery
			CredentialInstructor instructorKarenWhite = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(cred4Delivery.getId(), userKarenWhite.getId(), 0, createUserContext(userNickPowell)));

			// enroll Richard Anderson to the delivery
			enrollToDelivery(events, org, cred4Delivery, userRichardAnderson);

			// explicitly set Karen White as an instructor of Richard Anderson
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
					instructorKarenWhite.getId(), cred4Delivery.getId(), Arrays.asList(userRichardAnderson.getId()), null, createUserContext(userNickPowell)));

			// Richard starts both competencies
			List<CompetenceData1> cred4CompetenciesRichardAnderson = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(cred4Delivery.getId(), userRichardAnderson.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

			TargetCompetence1 cred4Comp1TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(cred4CompetenciesRichardAnderson.get(0).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
			TargetCompetence1 cred4Comp2TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(cred4CompetenciesRichardAnderson.get(1).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));

			// Richard completes first competency
			// create a piece of evidence and add to
			LearningEvidenceData evidence1Data = new LearningEvidenceData();
			evidence1Data.setType(LearningEvidenceType.LINK);
			evidence1Data.setTitle("Sample dataset manipulation R script");
			evidence1Data.setText("An R script with linear regressions built on a sample dataset.");
			evidence1Data.setUrl("http://example.com/r-script.R");
			evidence1Data.setTagsString("feature manipulation, R script");
			evidence1Data.setRelationToCompetence("The script demonstrates feature manipulation skills.");

			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					cred4Comp1TargetRichardAnderson.getId(), evidence1Data, createUserContext(userRichardAnderson)));

			// mark the competency as completed
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).completeCompetenceAndGetEvents(
					cred4Comp1TargetRichardAnderson.getId(), createUserContext(userRichardAnderson)));

			// Richard completes first competency
			List<ActivityData> cred2Comp2Activities = ServiceLocator.getInstance().getService(Activity1Manager.class).getTargetActivitiesData(cred4Comp2TargetRichardAnderson.getId());

			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Activity1Manager.class).completeActivityAndGetEvents(
					cred2Comp2Activities.get(0).getTargetActivityId(),
					cred4Comp2TargetRichardAnderson.getId(),
					createUserContext(userRichardAnderson)));

			// mark the competency as completed
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).completeCompetenceAndGetEvents(
					cred4Comp2TargetRichardAnderson.getId(), createUserContext(userRichardAnderson)));

			// the credential is completed at this point

            // enroll Lori Abner to the delivery
            extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(cred4Delivery.getId(), userLoriAbner.getId(), 0, createUserContext(userLoriAbner)));

            // Lori starts both competencies
            List<CompetenceData1> cred4CompetenciesLoriAbner = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(cred4Delivery.getId(), userLoriAbner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

            TargetCompetence1 cred4Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(cred4CompetenciesLoriAbner.get(0).getCompetenceId(), userLoriAbner.getId(), createUserContext(userLoriAbner)));
            TargetCompetence1 cred4Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(cred4CompetenciesLoriAbner.get(1).getCompetenceId(), userLoriAbner.getId(), createUserContext(userLoriAbner)));

            // give learn privilege to Akiko Kido
            givePrivilegeToUsersOnDelivery(events, cred4Delivery, UserGroupPrivilege.Learn, userNickPowell, org, Arrays.asList(userAkikoKido));
        } catch (IllegalDataStateException e) {
			e.printStackTrace();
			logger.error("Error", e);
		}


		/*
		 * Sending private messages
		 */
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userRichardAnderson.getId(), "Hi Richard");
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userRichardAnderson.getId(), "Can you help me with a task");
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userRichardAnderson.getId(), userKevinHall.getId(), "Sure. What's the problem?");
//
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userAnnaHallowell.getId(), "Hi Anna");
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userAnnaHallowell.getId(), "Do you have time to help me with something?");
//
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userIdaFritz.getId(), userGeorgeYoung.getId(), "Hi George");
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userIdaFritz.getId(), userGeorgeYoung.getId(), "Hi Ida");
//
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userAnnaHallowell.getId(), userPhilArmstrong.getId(), "Hello Phill");
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userPhilArmstrong.getId(), userAnnaHallowell.getId(), "Hi");
//		ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userPhilArmstrong.getId(), userAnnaHallowell.getId(), "Did you maybe have time to complete the latest assignment?");

		////////////////////////////////
		// Add follow relations
		////////////////////////////////
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userAnnaHallowell.getId(),  createUserContext(userKevinHall)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userGeorgeYoung.getId(),  createUserContext(userKevinHall)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userIdaFritz.getId(),  createUserContext(userKevinHall)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userKevinHall.getId(),  createUserContext(userIdaFritz)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userKevinHall.getId(),  createUserContext(userAnnaHallowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPhilArmstrong.getId(),  createUserContext(userAnnaHallowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userIdaFritz.getId(),  createUserContext(userAnnaHallowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPhilArmstrong.getId(),  createUserContext(userSheriLaureano)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPhilArmstrong.getId(),  createUserContext(userLoriAbner)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userKevinHall.getId(),  createUserContext(userLoriAbner)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userIdaFritz.getId(),  createUserContext(userTaniaCortese)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userSheriLaureano.getId(),  createUserContext(userJosephGarcia)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userKevinHall.getId(),  createUserContext(userAngelicaFallon)));

		ServiceLocator.getInstance().getService(EventFactory.class).generateEvents(events, new Class[]{NodeChangeObserver.class});

		try {
			logger.info("Reindexing all indices since we know some observers have failed");
			ServiceLocator.getInstance().getService(BulkDataAdministrationService.class).deleteAndReindexDBESIndexes();
		} catch (IndexingServiceNotAvailable indexingServiceNotAvailable) {
			logger.error(indexingServiceNotAvailable);
		}
	}

	private UserContextData createUserContext(User user) {
		return UserContextData.of(user.getId(), user.getOrganization().getId(), null, null);
	}

	private <T> T extractResultAndAddEvents(EventQueue events, Result<T> result) {
		events.appendEvents(result.getEventQueue());
		return result.getResult();
	}

	private CommentData commentOnActivity(EventQueue events, User userKevinHall, Activity1 act1comp1cred1, CommentData parent, String date, String commentText) {
		CommentData newComment = new CommentData();
		newComment.setCommentedResourceId(act1comp1cred1.getId());
		try {
			newComment.setDateCreated(dateFormatter.parse(date));
		} catch (ParseException e) {
			logger.error(e);
		}
		newComment.setComment(commentText);
		newComment.setCreator(new UserData(userKevinHall));
		newComment.setParent(parent);

		String learningContext= MessageFormat.format("name:competence|id:1|context:/name:activity|id:1|context:/context:/name:comment/|name:target_activity|id:1//", act1comp1cred1.getId());

		PageContextData context = new PageContextData("/activity.xhtml", learningContext, null );

		Comment1 comment = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).saveNewCommentAndGetEvents(newComment,
				CommentedResourceType.Activity, UserContextData.of(userKevinHall.getId(), userKevinHall.getOrganization().getId(), null, context)));

		newComment.setCommentId(comment.getId());

		return newComment;
	}

    private void givePrivilegeToUsersOnDelivery(EventQueue events, Credential1 delivery, UserGroupPrivilege userGroupPrivilege, User actor, Organization org, List<User> students) {
        List<ResourceVisibilityMember> studentsToAdd = new LinkedList<>();

        for (User student : students) {
            ResourceVisibilityMember resourceVisibilityMember = new ResourceVisibilityMember(0, student, userGroupPrivilege, false, true);
            resourceVisibilityMember.setStatus(ObjectStatus.CREATED);
            studentsToAdd.add(resourceVisibilityMember);
        }

        events.appendEvents(ServiceLocator.getInstance().getService(CredentialManager.class).updateCredentialVisibilityAndGetEvents(
                delivery.getId(), new LinkedList<>(), studentsToAdd,false, false,
                UserContextData.of(actor.getId(), org.getId(), null, null)));
    }

    private void givePrivilegeToGroupOnDelivery(EventQueue events, Credential1 delivery, UserGroupPrivilege userGroupPrivilege, User actor, Organization org, List<Long> groupIds) {
        List<ResourceVisibilityMember> groupsToAdd = new LinkedList<>();

        for (Long groupId : groupIds) {
            ResourceVisibilityMember resourceVisibilityMember = new ResourceVisibilityMember(0, groupId, null, 0, userGroupPrivilege, false, true);
            resourceVisibilityMember.setStatus(ObjectStatus.CREATED);
            groupsToAdd.add(resourceVisibilityMember);
        }

        events.appendEvents(ServiceLocator.getInstance().getService(CredentialManager.class).updateCredentialVisibilityAndGetEvents(
                delivery.getId(), groupsToAdd, new LinkedList<>(), false, false,
                UserContextData.of(actor.getId(), org.getId(), null, null)));
    }

	private long getDaysFromNow(int days) {
		return LocalDateTime.now(Clock.systemUTC()).plusDays(days).atZone(ZoneOffset.ofTotalSeconds(0)).toInstant().toEpochMilli();
	}

	private Result<User> createUser(long orgId, String name, String lastname, String emailAddress, String password, String fictitiousUser,
									String avatar, Role roleUser) {
		try {
			return ServiceLocator
					.getInstance()
					.getService(UserManager.class)
					.createNewUserAndGetEvents(orgId, name, lastname, emailAddress,
							true, password, fictitiousUser, getAvatarInputStream(avatar), avatar, Arrays.asList(roleUser.getId()),false);
		} catch (IllegalDataStateException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void enrollToDelivery(EventQueue events, Organization org, Credential1 delivery, User user) {
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(delivery.getId(), user.getId(), 0, UserContextData.of(user.getId(), org.getId(), null, null)));
	}

	private Activity1 createActivity(EventQueue events, long orgId, User userNickPowell, String title, String description, String url, ActivityType type,
									 long compId, int durationHours, int durationMinutes, ActivityResultType resultType, String... nameLink)
			throws DbConnectionException, IllegalDataStateException {
		ActivityData actData = new ActivityData(false);
		actData.setTitle(title);
		actData.setDescription(description);
		actData.setActivityType(type);
		actData.setStudentCanSeeOtherResponses(true);
		actData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
		actData.getAssessmentSettings().setMaxPoints(100);

		switch (type) {
			case VIDEO:
				actData.setVideoLink(url);
				break;
			case SLIDESHARE:
				actData.setSlidesLink(url);
				break;
			case TEXT:
				actData.setText(url);
				break;
			default:
				break;
		}
		actData.setType(LearningResourceType.UNIVERSITY_CREATED);
		actData.setCompetenceId(compId);
		actData.setDurationMinutes(durationMinutes);
		actData.setDurationHours(durationHours);
		actData.getResultData().setResultType(resultType);

		if (nameLink != null) {
			List<ResourceLinkData> activityLinks = new ArrayList<>();

			for (int i = 0; i < nameLink.length; i+=2) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setLinkName(nameLink[i]);
				rlData.setUrl(nameLink[i+1]);
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityLinks.add(rlData);
			}

			actData.setLinks(activityLinks);
		}

		Activity1 act = extractResultAndAddEvents(events, ServiceLocator
				.getInstance()
				.getService(Activity1Manager.class)
				.createActivity(
						actData, createUserContext(userNickPowell)));
		return act;
	}

	private Credential1 createCredential(EventQueue events, String title, String description, User userNickPowell, String tags, BlindAssessmentMode blindedAssessmentMode) {
		CredentialData credentialData = new CredentialData(false);
		credentialData.getIdData().setTitle(title);
		credentialData.setDescription(description);
		credentialData.setTagsString(tags);
		credentialData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
		credentialData.getAssessmentSettings().setMaxPoints(100);

		AssessmentTypeConfig instructorAssessment = new AssessmentTypeConfig(-1, AssessmentType.INSTRUCTOR_ASSESSMENT, true, true);
		AssessmentTypeConfig peerAssessment = new AssessmentTypeConfig(-1, AssessmentType.PEER_ASSESSMENT, true, false);
		peerAssessment.setBlindAssessmentMode(blindedAssessmentMode);
		AssessmentTypeConfig selfAssessment = new AssessmentTypeConfig(-1, AssessmentType.SELF_ASSESSMENT, true, false);
		credentialData.setAssessmentTypes(Arrays.asList(instructorAssessment, peerAssessment, selfAssessment));

		Credential1 credNP1 = extractResultAndAddEvents(events, ServiceLocator
				.getInstance()
				.getService(CredentialManager.class)
				.saveNewCredentialAndGetEvents(credentialData, createUserContext(userNickPowell)));

		return credNP1;
	}

	public Competence1 createCompetence(EventQueue events, User user, String title, String description, long credentialId, String tags, LearningPathType learningPathType) {

		CompetenceData1 compData = new CompetenceData1(false);
		compData.setTitle(title);
		compData.setDescription(description);
		compData.setTagsString(tags);
		compData.setPublished(false);
		compData.setType(LearningResourceType.UNIVERSITY_CREATED);
		compData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
		compData.getAssessmentSettings().setMaxPointsString(String.valueOf(100));
		compData.setLearningPathType(learningPathType);

		AssessmentTypeConfig instructorAssessment = new AssessmentTypeConfig(-1, AssessmentType.INSTRUCTOR_ASSESSMENT, true, true);
		AssessmentTypeConfig peerAssessment = new AssessmentTypeConfig(-1, AssessmentType.PEER_ASSESSMENT, true, false);
		AssessmentTypeConfig selfAssessment = new AssessmentTypeConfig(-1, AssessmentType.SELF_ASSESSMENT, true, false);
		compData.setAssessmentTypes(Arrays.asList(instructorAssessment, peerAssessment, selfAssessment));

		Competence1 comp;
		try {
			comp = extractResultAndAddEvents(events, ServiceLocator
					.getInstance()
					.getService(Competence1Manager.class)
					.saveNewCompetenceAndGetEvents(
							compData, credentialId, createUserContext(user)));
			return comp;
		} catch (DbConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalDataStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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

}
