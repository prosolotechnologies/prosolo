package org.prosolo.app.bc;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.app.RegistrationType;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Zoran Jeremic Oct 11, 2014
 *
 */
@Service("org.prosolo.app.bc.BusinessCase4_EDX")
public class BusinessCase4_EDX extends BusinessCase {

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy. HH:mm");
	
	public void initRepository() {
		try {
			System.out.println("BusinessCaseTest - initRepository() with BC 3");

			RegistrationKey regKey0 = new RegistrationKey();
			regKey0.setUid("reg793442b86584b46f7bd8a0dae72f31");
			regKey0.setRegistrationType(RegistrationType.NO_APPROVAL_ACCESS);
			ServiceLocator.getInstance().getService(RegistrationManager.class).saveEntity(regKey0);

			RegistrationKey regKey = new RegistrationKey();
			regKey.setUid(UUID.randomUUID().toString().replace("-", ""));
			regKey.setRegistrationType(RegistrationType.NO_APPROVAL_ACCESS);
			ServiceLocator.getInstance().getService(RegistrationManager.class).saveEntity(regKey);

			RegistrationKey regKey2 = new RegistrationKey();
			regKey2.setUid(UUID.randomUUID().toString().replace("-", ""));
			regKey2.setRegistrationType(RegistrationType.NO_APPROVAL_ACCESS);
			ServiceLocator.getInstance().getService(RegistrationManager.class).saveEntity(regKey2);


			// get ROLES
			Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName("User");
			Role roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName("Manager");
			Role roleInstructor = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName("Instructor");
			Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName("Admin");




			/*
			 * CREATING USERS
			 */
			String fictitiousUser = "System analyst";
			String password = "prosolo@2014";


			User userNickPowell = createUser("Nick", "Powell", "nick.powell@gmail.com", password, fictitiousUser, "male1.png", roleUser);
			User userRichardAnderson = createUser("Richard", "Anderson", "richard.anderson@gmail.com", password, fictitiousUser, "male2.png", roleUser);
			User userKevinMitchell = createUser("Kevin", "Mitchell", "kevin.mitchell@gmail.com", password, fictitiousUser, "male3.png", roleUser);
			User userPaulEdwards = createUser("Paul", "Edwards", "paul.edwards@gmail.com", password, fictitiousUser, "male4.png", roleUser);
			User userStevenTurner = createUser("Steven", "Turner", "steven.turner@gmail.com", password, fictitiousUser, "male5.png", roleUser);
			User userGeorgeYoung = createUser("George", "Young", "george.young@gmail.com", password, fictitiousUser, "male6.png", roleUser);
			User userPhillAmstrong = createUser("Phill", "Amstrong", "phill.amstrong@gmail.com", password, fictitiousUser, "male7.png", roleUser);
			User userJosephGarcia = createUser("Joseph", "Garcia", "joseph.garcia@gmail.com", password, fictitiousUser, "male8.png", roleUser);
			User userTimothyRivera = createUser("Timothy", "Rivera", "timothy.rivera@gmail.com", password, fictitiousUser, "male9.png", roleUser);
			User userKevinHall = createUser("Kevin", "Hall", "kevin.hall@gmail.com", password, fictitiousUser, "male10.png", roleUser);
			User userKennethCarter = createUser("Kenneth", "Carter", "kenneth.carter@gmail.com", password, fictitiousUser, "male11.png", roleUser);
			User userAnthonyMoore = createUser("Anthony", "Moore", "anthony.moore@gmail.com", password, fictitiousUser, "male12.png", roleUser);


			User userTaniaCortese = createUser("Tania", "Cortese", "tania.cortese@gmail.com", password, fictitiousUser, "female1.png", roleUser);
			User userSonyaElston = createUser("Sonya", "Elston", "sonya.elston@gmail.com", password, fictitiousUser, "female2.png", roleUser);
			User userLoriAbner = createUser("Lori", "Abner", "lori.abner@gmail.com", password, fictitiousUser, "female3.png", roleUser);
			User userSamanthaDell = createUser("Samantha", "Dell", "samantha.dell@gmail.com", password, fictitiousUser, "female4.png", roleUser);
			User userAkikoKido = createUser("Akiko", "Kido", "akiko.kido@gmail.com", password, fictitiousUser, "female7.png", roleUser);
			User userKarenWhite = createUser("Karen", "White", "karen.white@gmail.com", password, fictitiousUser, "female10.png", roleUser);
			User userAnnaHallowell = createUser("Anna", "Hallowell", "anna.hallowell@gmail.com", password, fictitiousUser, "female11.png", roleUser);
			User userErikaAmes = createUser("Erika", "Ames", "erika.ames@gmail.com", password, fictitiousUser, "female12.png", roleUser);
			User userHelenCampbell = createUser("Helen", "Campbell", "helen.campbell@gmail.com", password, fictitiousUser, "female13.png", roleUser);
			User userSheriLaureano = createUser("Sheri", "Laureano", "sheri.laureano@gmail.com", password, fictitiousUser, "female14.png", roleUser);
			User userAngelicaFallon = createUser("Angelica", "Fallon", "angelica.fallon@gmail.com", password, fictitiousUser, "female16.png", roleUser);
			User userIdaFritz = createUser("Ida", "Fritz", "ida.fritz@gmail.com", password, fictitiousUser, "female17.png", roleUser);
			User userRachelWiggins = createUser("Rachel", "Wiggins", "rachel.wiggins@gmail.com", password, fictitiousUser, "female20.png", roleUser);

			// allow users to be added to indexes
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e);
			}

			// Adding roles to the users

			try {
				userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, userNickPowell);
				userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userNickPowell);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit_Profile, userNickPowell.getId(), userNickPowell);

				userPhillAmstrong = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleInstructor, userPhillAmstrong);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit_Profile, userNickPowell.getId(), userPhillAmstrong);

				userAnnaHallowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleInstructor, userAnnaHallowell);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit_Profile, userNickPowell.getId(), userAnnaHallowell);

				userTimothyRivera = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleInstructor, userTimothyRivera);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit_Profile, userNickPowell.getId(), userTimothyRivera);

				userErikaAmes = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleInstructor, userErikaAmes);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit_Profile, userNickPowell.getId(), userErikaAmes);

				userKarenWhite = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userKarenWhite);
				userKarenWhite = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleInstructor, userKarenWhite);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit_Profile, userNickPowell.getId(), userKarenWhite);
			} catch (EventException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}


			/*
			 * END CRETAING USERS
			 */

			// ////////////////////////////
			// Credential for Nick Powell
			// ///////////////////////////////
			Credential1 cred1 = createCredential(
					"Basics of Social Network Analysis",
					"This credential defines social network analysis and its main analysis methods and "
							+ "introduces how to peform social network analysis and visualize analysis results in Gephi",
					userNickPowell,
					"network structure, data collection, learning analytics, network measures, network modularity, social network analysis");


			Competence1 comp1cred1 = null;
			Activity1 act1comp1cred1 = null;
			Activity1 act2comp1cred1 = null;
			Activity1 act4comp1cred1 = null;
			try {
				comp1cred1 = createCompetence(
						userNickPowell,
						"Social Network Analysis",
						"Define social network analysis and its main analysis methods.",
						cred1.getId(),
						"centrality measures, data collection, modularity analysis, network centrality, network structure, social network analysis");

				act1comp1cred1 = createActivity(
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

				act2comp1cred1 = createActivity(
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

				createActivity(
						userNickPowell,
						"CCK11 dataset",
						"",
						"<p>Download the CCK11 dataset and familiarize with the data</p>",
						ActivityType.TEXT,
						comp1cred1.getId(),
						0,
						3,
						ActivityResultType.TEXT,
						"CCK11 dataset for social network analysis",
						"https://s3.amazonaws.com/prosoloedx2/files/3d9a5e10d63678812f87b21ed593659a/CCK11%20dataset%20for%20social%20network%20analysis.pdf");

				act4comp1cred1 = createActivity(
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

				createActivity(
						userNickPowell,
						"Network Modularity and Community Identification",
						"Dragan Gasevic discusses network modularity and community identification in social network analysis for week 3 of DALMOOC.",
						"https://www.youtube.com/watch?v=2_Q7uPAl34M",
						ActivityType.VIDEO,
						comp1cred1.getId(),
						0,
						6,
						ActivityResultType.TEXT,
						"Slides",
						"http://www.slideshare.net/dgasevic/network-modularity-and-community-identification/1");

				createActivity(
						userNickPowell,
						"Assignment: Reflection and discussion on social network analysis",
						"",
						"<p>After the introduction into social network analysis, its main analysis techniques, and data collection "
								+ "methods, it would be useful to reflect on what you have learned so far. Please, prepare a reflection "
								+ "piece (about 300 words) in which you will address the following issues:</p><ul><li>Outline your "
								+ "understanding of social network structure and main methods for social network analysis (centrality, "
								+ "density, and modularity);</li><li>Discus potential benefits of the use of social network analysis for "
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


				publishCredential(cred1, cred1.getCreatedBy());
			} catch (EventException e) {
				logger.error(e);
			} catch (Exception ex) {
				logger.error(ex);
			}

			Competence1 comp2cred1 = null;
			try {
				comp2cred1 = createCompetence(
						userNickPowell,
						"Using Gephi for Social Network Analysis",
						"Perform social network analysis and visualize analysis results in Gephi",
						cred1.getId(),
						"community identification, gephi, network centrality, network density, network diameter, network visualization, social network analysis");

				createActivity(
						userNickPowell,
						"Gephi Community Introduction",
						"A YouTube video introducing the Gephi tool",
						"https://www.youtube.com/watch?v=bXCBh6QH5W0",
						ActivityType.VIDEO,
						comp2cred1.getId(),
						0,
						4,
						ActivityResultType.TEXT);

				createActivity(
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

				createActivity(
						userNickPowell,
						"Gephi - An Introduction tour",
						"Dragan Gasevic gives an introductory tour of Gephi for week 3 of DALMOOC.",
						"https://www.youtube.com/watch?v=L0C_D68E1Q0",
						ActivityType.VIDEO,
						comp2cred1.getId(),
						0,
						17,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Gephi - Modularity Analysis",
						"Dragan Gasevic discusses modularity analysis in Gephi for week 3 of DALMOOC.",
						"https://www.youtube.com/watch?v=D1soIxZ61As",
						ActivityType.VIDEO,
						comp2cred1.getId(),
						0,
						11,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Gephi - Modularity tutorial",
						"A quick tutorial by Jennifer Golbeck  on how to use gephi's modularity feature to detect communities and color code them in graphs.",
						"https://www.youtube.com/watch?v=7LMnpM0p4cM",
						ActivityType.VIDEO,
						comp2cred1.getId(),
						0,
						9,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Gephi Tutorial Quick start",
						"Explore slide presentation: Gephi Tutorial Quick start",
						"http://www.slideshare.net/gephi/gephi-quick-start",
						ActivityType.SLIDESHARE,
						comp2cred1.getId(),
						0,
						10,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Gephi Tutorial Visualization",
						"Explore slide presentation: Gephi Tutorial Visualization",
						"http://www.slideshare.net/gephi/gephi-tutorial-visualization",
						ActivityType.SLIDESHARE,
						comp2cred1.getId(),
						0,
						15,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Hands-on - Import the example dataset and perform the SNA analysis methods",
						"",
						"<p><strong>Hands-on activity: Import the example dataset into Gephi and perform the SNA analysis "
								+ "methods</strong></p><p>After you have studied the resources on how to visualize social networks "
								+ "and performed main analysis method in Gephi, it is now time to perform some hands-on activities:"
								+ "</p><p></p><ul><li>Download the example dataset available used in this course for the description "
								+ "of social network measures and use both files (example_1.csv and example_2.csv from example dataset)."
								+ "</li><li>Perform the following operations on the network in Gephi as undirected files:</li><ul><li>"
								+ "Compute the density measure of the networks</li><li>Compute centrality measures (betweenness and "
								+ "degree) introduced in the course</li><li>Apply the Giant Component filter to filter out all the "
								+ "disconnected nodes and identify communities by using the modularity algorithm.</li></ul><li>Save "
								+ "the results of your analysis as Gephi projects, one separate project for either of the two examples."
								+ "</li><li>Share your experience with other course participants on social media (blog, Twitter, and "
								+ "ProSolo, edX discussion forum)<br></li></ul>",
						ActivityType.TEXT,
						comp2cred1.getId(),
						1,
						0,
						ActivityResultType.TEXT);

				createActivity(
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

				publishCredential(cred1, cred1.getCreatedBy());
			} catch (EventException e) {
				logger.error(e);
			} catch (Exception ex) {
				logger.error(ex);
			}


			Credential1 cred2 = createCredential(
					"Sensemaking of Social Network Analysis for Learning",
					"This credential defines describes and critically reflects on possible approaches to the use of social network analysis for the study of learning. The credential also describes and interprets the results of social network analysis for the study of learning",
					userNickPowell,
					"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis");


			Competence1 comp1cred2 = null;
			try {
				comp1cred2 = createCompetence(
						userNickPowell,
						"Reflecting on approaches to the use of SNA for the study of learning",
						"Describe and critically reflect on possible approaches to the use of social network analysis for the study of learning",
						cred2.getId(),
						"academic performance, creative potential, learning analytics, learning design, MOOCs, sense of community, sensemaking, social network analysis");

				createActivity(
						userNickPowell,
						"Introduction",
						"Introduction into sensemaking of social network analysis for the study of learning. Dragan Gasevic introduces us to week 4 of DALMOOC.",
						"https://www.youtube.com/watch?v=NPEeSArODQE",
						ActivityType.VIDEO,
						comp1cred2.getId(),
						0,
						4,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Social Network Analysis and Learning Design",
						"Dragan Gasevic discusses social network analysis and learning design for week 4 of DALMOOC.",
						"https://www.youtube.com/watch?v=-JuBDu_YVoo",
						ActivityType.VIDEO,
						comp1cred2.getId(),
						0,
						6,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Social Network Analysis and Sense of Community",
						"Dragan Gasevic discusses social network analysis and sense of community for week 4 of DALMOOC.",
						"https://www.youtube.com/watch?v=lUEeppG_6So",
						ActivityType.VIDEO,
						comp1cred2.getId(),
						0,
						5,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Social Network Analysis and Creative Potential",
						"Dragan Gasevic discusses social network analysis and creative potential for week 4 of DALMOOC.",
						"https://www.youtube.com/watch?v=VTGvvHpC5IQ",
						ActivityType.VIDEO,
						comp1cred2.getId(),
						0,
						4,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Social Network Analysis and Academic Peformance",
						"Dragan Gasevic discusses social network analysis and academic performance for week 4 of DALMOOC.",
						"https://www.youtube.com/watch?v=F9jLV7hS2AE",
						ActivityType.VIDEO,
						comp1cred2.getId(),
						0,
						6,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Social Network Analysis and Social Presence",
						"Dragan Gasevic discusses social network analysis and social presence for week 4 of DALMOOC.",
						"https://www.youtube.com/watch?v=bZhRuo8nz7A",
						ActivityType.VIDEO,
						comp1cred2.getId(),
						0,
						6,
						ActivityResultType.TEXT);

				createActivity(
						userNickPowell,
						"Hands-on activity: Integration of social network analysis in Gephi and Tableau analysis",
						"Dragan Gasevic discusses social network analysis and social presence for week 4 of DALMOOC.",
						"<p>Now that you have performed social network analysis in Gephi and started working on their interpretation of relevance for the understanding of learning, it is time to learn to integrate knowledge and skills gained in weeks 1-2 with Tableau. Specifically, in this hands-on activity, you are asked to:</p><ul><li>Export the results of social network analyses (centrality and modularity) of the networks available in the example dataset from Gephi – via the Data Laboratory tab of Gephi - in the format (i.e., CSV) that can be imported into Tableau</li><li>Plot the data to show the distribution of each centrality measure for each of the two networks</li><li>Plot the data to show the distribution of centrality measures across communities identified in each of the two networks</li><li>Share your experience (e.g., blogs and figures based on your visualizations from both Gephi and Tableau) with other course participants on social media (blog, Twitter, and Pro Solo, edX discussion forum).</li></ul>",
						ActivityType.TEXT,
						comp1cred2.getId(),
						0,
						40,
						ActivityResultType.TEXT);

				publishCredential(cred2, cred2.getCreatedBy());
			} catch (EventException e) {
				logger.error(e);
			} catch (Exception ex) {
				logger.error(ex);
			}

			Competence1 comp2cred2 = null;
			try {
				comp2cred2 = createCompetence(
						userNickPowell,
						"Interpreting the results of SNA",
						"Describe and interpret the results of social network analysis for the study of learning",
						cred2.getId(),
						"analytics interpretation, gephi, learning analytics, sensemaking, social network analysis, tableau");

				createActivity(
						userNickPowell,
						"Bazaar assignment: Collaborative reflection on the interpretation of the results of social network analysis",
						"",
						"<p>Now that you have been learned about different perspectives how social network analysis can inform learning research and practice, you will collaboratively reflect with a partner on what you have learned and what ideas you have. Before you engage into this collaborative activity, it will be useful if you have imported the blogs and Twitter social networks (both Week 6 and Week 12) from the dataset for social network analysis into Gephi, computed density and centrality measures, and performed modularity analysis. <br>We would like you to do this portion of the assignment online with a partner student we will assign to you.&nbsp; You will use the Collaborative Chat tool.&nbsp; To access the chat tool, paste the following URL (https://bit.ly/dalchat4) into your browser.&nbsp; You will log in using your EdX id.&nbsp; When you log in, you will enter a lobby program that will assign you to a partner. If it turns out that a partner student is not available, after 5 minutes it will suggest that you try again later.</p><p>When you are matched with a partner, you will be given a link to the chat room.&nbsp; Click the link to enter, and follow the instructions in the chat.&nbsp; The collaborative exercise will require about 30 minutes to complete.</p><p>Instructions for the chat activity will come up in the right hand panel, and you can chat with your partner in the left hand panel. A computer agent will provide prompts to structure the chat activity.</p>",
						ActivityType.TEXT,
						comp2cred2.getId(),
						0,
						20,
						ActivityResultType.TEXT);

				publishCredential(cred2, cred2.getCreatedBy());
			} catch (EventException e) {
				logger.error(e);
			} catch (Exception ex) {
				logger.error(ex);
			}


			Credential1 cred3 = createCredential(
					"Introduction to Learning Analytics",
					"The proliferation of data in digital environments has to date been largely unexplored in education. A new academic field - learning analytics - has developed to gain insight into learner generated data and how this can be used to improve learning and teaching practices",
					userNickPowell,
					"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis");

			Competence1 comp1cred3 = null;
			try {
				comp1cred3 = createCompetence(
						userNickPowell,
						"Tools for Learning Analytics",
						"Identify proprietary and open source tools commonly used in learning analytics",
						cred3.getId(),
						"academic performance, creative potential, social network analysis");

				Activity1 act1comp1cred3 = createActivity(
						userNickPowell,
						"Getting Started With Data Analytics Tools",
						"A basic overview of the Data Anlytics tools by George Siemens",
						"https://www.youtube.com/watch?v=XOckgFlLqwU",
						ActivityType.VIDEO,
						comp1cred3.getId(),
						0,
						30,
						ActivityResultType.TEXT);

				publishCredential(cred3, cred2.getCreatedBy());
			} catch (EventException e1) {
				logger.error(e1);
			} catch (Exception ex) {
				logger.error(ex);
			}


			Credential1 cred4 = createCredential(
					"Text mining nuts and bolts",
					"This credential introduces how to i) prepare data for use in LightSIDE and use LightSIDE to extract a wide range of feature types; ii) build and evaluate models using alternative feature spaces; iii) compare the performance of different models; iv) inspect models and interpret the weights assigned to different features as well as to reason about what these weights signify and whether they make sense; v) examine texts from different categories and notice characteristics they might want to include in feature space for models and then use this reasoning to start to make tentative decisions about what kinds of features to include in their models",
					userNickPowell,
					"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis");

			Competence1 comp1cred4 = null;
			try {
				comp1cred4 = createCompetence(
						userNickPowell,
						"Basic use of LightSIDE",
						"Prepare data for use in LightSIDE and use LightSIDE to extract a wide range of feature types",
						cred4.getId(),
						"academic performance, creative potential, social network analysis");

				createActivity(
						userNickPowell,
						"Data Preparation",
						"Data Preparation in LightSIDE",
						"https://www.youtube.com/watch?v=jz5pwR0moL0",
						ActivityType.VIDEO,
						comp1cred4.getId(),
						0,
						45,
						ActivityResultType.TEXT);

				publishCredential(cred4, cred4.getCreatedBy());
			} catch (EventException e1) {
				logger.error(e1);
			} catch (Exception ex) {
				logger.error(ex);
			}

			Credential1 cred5 = createCredential(
					"Prediction modeling",
					"The credential introduces how to conduct prediction modeling effectively and appropriately and describe core uses of prediction modeling in education.",
					userNickPowell,
					"academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis");

			Competence1 comp1cred5 = null;
			try {
				comp1cred5 = createCompetence(
						userNickPowell,
						"Basic of Prediction Modeling",
						"Conduct prediction modeling effectively and appropriately",
						cred5.getId(),
						"academic performance, creative potential, social network analysis");

				createActivity(
						userNickPowell,
						"Introduction in prediction modeling and regressors",
						"Ryan Baker introduces prediction modeling and discusses regressors for week 5 of DALMOOC.",
						"https://www.youtube.com/watch?v=1ZkUyFtCNIk",
						ActivityType.VIDEO,
						comp1cred5.getId(),
						0,
						37,
						ActivityResultType.TEXT);

				publishCredential(cred5, cred5.getCreatedBy());
			} catch (EventException e1) {
				logger.error(e1);
			} catch (Exception ex) {
				logger.error(ex);
			}


			try {
				ServiceLocator
						.getInstance()
						.getService(PostManager.class)
						.createNewPost(userNickPowell.getId(),
								"Learning parametric data.", VisibilityType.PUBLIC, null, null, true, null, null, null, null);

				ServiceLocator
						.getInstance()
						.getService(PostManager.class)
						.createNewPost(
								userNickPowell.getId(),
								"Can anybody recommend me a good book for SPSS basics? Thanks!",
								VisibilityType.PUBLIC, null, null, true, null, null, null, null);
			} catch (EventException | ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}

			try {
				/*
				 * Enrolling students to credentials
				 */
				ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredential(cred1.getId(), userKevinHall.getId(), new LearningContextData());
				ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredential(cred1.getId(), userAnnaHallowell.getId(), new LearningContextData());
				ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredential(cred1.getId(), userAkikoKido.getId(), new LearningContextData());
				ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredential(cred1.getId(), userRichardAnderson.getId(), new LearningContextData());
				ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredential(cred1.getId(), userIdaFritz.getId(), new LearningContextData());

			} catch (EventException e) {
				logger.error(e);
			}
			/*
			 * Adding instructors
			 */

			try {
				Map<String, String> params = new HashMap<>();
				params.put("dateAssigned", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

				ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredential(cred1.getId(), userPhillAmstrong.getId(), 10, 0, null);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL,
						userNickPowell.getId(), userPhillAmstrong, cred1, null, null, null, params);

				ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredential(cred1.getId(), userKarenWhite.getId(), 0, 0, null);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL,
						userNickPowell.getId(), userKarenWhite, cred1, null, null, null, params);

				ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredential(cred1.getId(), userErikaAmes.getId(), 0, 0, null);
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL,
						userNickPowell.getId(), userErikaAmes, cred1, null, null, null, params);

			} catch (EventException e) {
				logger.error(e);
			}

			ServiceLocator.getInstance().getService(DefaultManager.class).flush();
			/*
			 * Assigning instructors to students
			 */
			//		ServiceLocator.getInstance().getService(CredentialInstructorManager.class).assignStudentToInstructor(userAnnaHallowell.getId(), userPhillAmstrong.getId(), cred1.getId());
			//		ServiceLocator.getInstance().getService(CredentialInstructorManager.class).assignStudentToInstructor(userAkikoKido.getId(), userKarenWhite.getId(), cred1.getId());
			//		ServiceLocator.getInstance().getService(CredentialInstructorManager.class).assignStudentToInstructor(userKevinHall.getId(), userKarenWhite.getId(), cred1.getId());
			//		ServiceLocator.getInstance().getService(CredentialInstructorManager.class).assignStudentToInstructor(userRichardAnderson.getId(), userKarenWhite.getId(), cred1.getId());
			//		ServiceLocator.getInstance().getService(CredentialInstructorManager.class).assignStudentToInstructor(userIdaFritz.getId(), userErikaAmes.getId(), cred1.getId());

			/*
			 * Kevin Hall learning cred1
			 */

			//		int activitiesToComplete = 3;
			//		comLoop: for (CompetenceData1 compData : cred1DataKevinHall.getCompetences()) {
			//			for (ActivityData actData : compData.getActivities()) {
			//				ServiceLocator.getInstance().getService(Activity1Manager.class).completeActivity(
			//						actData.getTargetActivityId(),
			//						compData.getCompetenceId(),
			//						userKevinHall.getId(),
			//						 new LearningContextData());
			//
			//				if (activitiesToComplete-- == 0) {
			//					break comLoop;
			//				}
			//			}
			//		}

			/*
			 * Commenting on activities/ competences
			 */
			CommentData comment1 = commentOnActivity(userIdaFritz, act1comp1cred1, null, "10.06.2016. 15:24", "Very good presentation. Well suited for the novices like I am.");
			CommentData comment2 = commentOnActivity(userAnthonyMoore, act1comp1cred1, null, "12.06.2016. 09:50", "The video makes an important point of how individuals lay a data trail of interest that requires parties retrieving this information to proper understand the opportunities and confront “data overload” to best take advantage of this same data.");
			CommentData comment3 = commentOnActivity(userErikaAmes, act1comp1cred1, comment2, "13.06.2016. 13:02", "anthony - I would add to information overload and decision quality, the issue with multitasking and shorter attention spans (a la twitter)");
			CommentData comment4 = commentOnActivity(userKarenWhite, act2comp1cred1, comment2, "05.06.2016. 11:46", "The topics are well presented. Please take in account the fact that during the first week it is necessary for us, as learners, to become familiar with the dual-layer MOOC. This is important so every learner is building himself his knowledge");


			CommentData comment5 = commentOnActivity(userKevinHall, act1comp1cred1, null, "11.07.2016. 12:37", "Very good video to explain the meaning of learning analytics. Thanks");
			CommentData comment6 = commentOnActivity(userAnnaHallowell, act1comp1cred1, comment5, "11.07.2016. 17:42", "I also found this to be a concise introduction to learning analytics. :)");
			CommentData comment7 = commentOnActivity(userAkikoKido, act1comp1cred1, null, "12.07.2016. 08:17", "Nice video. Thanks.");
			CommentData comment8 = commentOnActivity(userIdaFritz, act1comp1cred1, null, "12.07.2016. 08:37", "Very concise, thanks");
			CommentData comment9 = commentOnActivity(userRichardAnderson, act1comp1cred1, null, "14.07.2016. 15:05", "This is short yet to the point in introducing learning analytics. Thanks");
			CommentData comment10 = commentOnActivity(userPhillAmstrong, act1comp1cred1, null, "12.07.2016. 16:21", "The video makes an important point of how individuals lay a data trail of interest that requires parties retrieving this information to proper understand the opportunities and confront “data overload” to best take advantage of this same data. In support of this point a study by Speier, Valacich, and Vessey (1999) state that “when information overload occurs, it is likely that a reduction in decision quality will occur” (p.338). Reference: Speier, C., Valacich, J. S., & Vessey, I. (1999). The influence of task interruption on individual decision making: An information overload perspective. Decision Sciences, 30(2), 337-360.");
			CommentData comment11 = commentOnActivity(userKevinHall, act1comp1cred1, comment10, "13.07.2016. 10:52", "I would add to information overload and decision quality, the issue with multitasking and shorter attention spans (a la twitter)");

			CommentData comment12 = commentOnActivity(userKevinHall, act4comp1cred1, null, "13.07.2016. 14:52", "I'm up to speed on the course design, but I wish I had found this video sooner!");
			CommentData comment13 = commentOnActivity(userPhillAmstrong, act4comp1cred1, null, "14.07.2016. 09:51", "I LIKE the idea of assignment bank. Definitely, each has different learning pace. I LOATHE the idea of group work. That is one thing I always wish to avoid in the university.");
			CommentData comment14 = commentOnActivity(userAnnaHallowell, act4comp1cred1, null, "14.07.2016. 12:43", "The topics are well presented. Please take in account the fact that during the first week it is necessary for us, as learners, to become familiar with the dual-layer MOOC. This is important so every learner is building himself his knowledge. ");
			CommentData comment15 = commentOnActivity(userGeorgeYoung, act4comp1cred1, null, "15.07.2016. 16:22", "Interesting structure. Its taken me a while to orient myself with the course setup, but am quite enjoying the use of prosolo as it helps to integrate the social media, content and course activities. The list of goals and competences (activities) has provided the best structure so far. ");
			CommentData comment16 = commentOnActivity(userKevinHall, act4comp1cred1, comment15, "16.07.2016. 20:37", "Great course.");

			/*
			 * Liking comments
			 */
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userAkikoKido.getId(), comment5.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userIdaFritz.getId(), comment5.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userPhillAmstrong.getId(), comment5.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userGeorgeYoung.getId(), comment5.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userAnnaHallowell.getId(), comment5.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userIdaFritz.getId(), comment6.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userKevinHall.getId(), comment6.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userGeorgeYoung.getId(), comment6.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userPhillAmstrong.getId(), comment9.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userAnnaHallowell.getId(), comment9.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userPhillAmstrong.getId(), comment12.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userAnnaHallowell.getId(), comment12.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userIdaFritz.getId(), comment13.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userRichardAnderson.getId(), comment13.getCommentId(), new LearningContextData());
			ServiceLocator.getInstance().getService(CommentManager.class).likeComment(userGeorgeYoung.getId(), comment14.getCommentId(), new LearningContextData());

			/*
			 * Sending private messages
			 */
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userRichardAnderson.getId(), "Hi Richard");
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userRichardAnderson.getId(), "Can you help me with a task");
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userRichardAnderson.getId(), userKevinHall.getId(), "Sure. What's the problem?");

			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userAnnaHallowell.getId(), "Hi Anna");
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userKevinHall.getId(), userAnnaHallowell.getId(), "Do you have time to help me with something?");

			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userIdaFritz.getId(), userGeorgeYoung.getId(), "Hi George");
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userIdaFritz.getId(), userGeorgeYoung.getId(), "Hi Ida");

			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userAnnaHallowell.getId(), userPhillAmstrong.getId(), "Hello Phill");
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userPhillAmstrong.getId(), userAnnaHallowell.getId(), "Hi");
			ServiceLocator.getInstance().getService(MessagingManager.class).sendMessage(userPhillAmstrong.getId(), userAnnaHallowell.getId(), "Did you maybe have time to complete the latest assignment?");

			/*
			 * User following
			 */
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userKevinHall.getId(), userPhillAmstrong.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userKevinHall.getId(), userAnnaHallowell.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userKevinHall.getId(), userGeorgeYoung.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userKevinHall.getId(), userIdaFritz.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userIdaFritz.getId(), userKevinHall.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userAnnaHallowell.getId(), userKevinHall.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userAnnaHallowell.getId(), userPhillAmstrong.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userAnnaHallowell.getId(), userIdaFritz.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userSheriLaureano.getId(), userPhillAmstrong.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userLoriAbner.getId(), userPhillAmstrong.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userLoriAbner.getId(), userKevinHall.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userTaniaCortese.getId(), userIdaFritz.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userJosephGarcia.getId(), userSheriLaureano.getId(), new LearningContextData());
			ServiceLocator.getInstance().getService(FollowResourceManager.class).followUser(userAngelicaFallon.getId(), userKevinHall.getId(), new LearningContextData());
		} catch (EventException e) {
			logger.error(e);
		}
	}

	private CommentData commentOnActivity(User userKevinHall, Activity1 act1comp1cred1, CommentData parent, String date, String commentText) {
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
		
		LearningContextData context = new LearningContextData("/activity.xhtml", null, null);
		
		Comment1 comment = ServiceLocator.getInstance().getService(CommentManager.class).saveNewComment(newComment, userKevinHall.getId(), 
				CommentedResourceType.Activity, context);
		
		newComment.setCommentId(comment.getId());
		
		return newComment;
	}
	
	private void publishCredential(Credential1 cred, User creator) throws DbConnectionException, StaleDataException {
//		CredentialManager credentialManager = ServiceLocator
//				.getInstance()
//				.getService(CredentialManager.class);
//		
//		RestrictedAccessResult<CredentialData> res = credentialManager.getCredentialData(cred.getId(), false, 
//				true, creator.getId(), ResourceAccessRequirements.of(AccessMode.MANAGER));
//		CredentialData credentialData = res.getResource();
//		
//		if (credentialData == null) {
//			RestrictedAccessResult<CredentialData> res1 = credentialManager.getCredentialData(cred.getId(), false, 
//					true, creator.getId(), ResourceAccessRequirements.of(AccessMode.MANAGER));
//			CredentialData credentialData1 = res.getResource();
//			System.out.println(credentialData1);
//		}
//		
//		if (credentialData != null) {
//			//credentialData.setPublished(true);
//			
//			credentialManager.updateCredential(credentialData, creator.getId(), null);
//			
//			try {
//				ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(EventType.Edit, creator.getId(), cred);
//			} catch (EventException e) {
//				e.printStackTrace();
//			}
//		} else {
//			logger.error("Could not load credential " + cred.getId());
//		}
	}

	private User createUser(String name, String lastname, String emailAddress, String password, String fictitiousUser,
			String avatar, Role roleUser) {
		try {
			User newUser = ServiceLocator
					.getInstance()
					.getService(UserManager.class)
					.createNewUser(name, lastname, emailAddress,
							true, password, fictitiousUser, getAvatarInputStream(avatar), avatar, null);
			
			newUser = ServiceLocator
					.getInstance()
					.getService(RoleManager.class)
					.assignRoleToUser(roleUser, newUser);
			
			return newUser;
		} catch (UserAlreadyRegisteredException e) {
			logger.error(e.getLocalizedMessage());
		} catch (EventException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	private Activity1 createActivity(User userNickPowell, String title, String description, String url, ActivityType type, 
			long compId, int durationHours, int durationMinutes, ActivityResultType resultType, String... nameLink) 
		throws DbConnectionException, IllegalDataStateException, EventException {
		ActivityData actData = new ActivityData(false);
		actData.setTitle(title);
		actData.setDescription(description);
		actData.setActivityType(type);
		actData.setStudentCanSeeOtherResponses(true);
		
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
		
		Activity1 act = ServiceLocator
				.getInstance()
				.getService(Activity1Manager.class)
				.saveNewActivity(
						actData,
						userNickPowell.getId(), null);
		return act;
	}

	private Credential1 createCredential(String title, String description, User userNickPowell, String tags)
			throws EventException {
		CredentialData credentialData = new CredentialData(false);
		credentialData.setTitle(title);
		credentialData.setDescription(description);
		credentialData.setTagsString(tags);
		
		Credential1 credNP1 = ServiceLocator
				.getInstance()
				.getService(CredentialManager.class)
				.saveNewCredential(credentialData, userNickPowell.getId(), null);
		
		return credNP1;
	}

	public Competence1 createCompetence(User user, String title, String description, long credentialId, String tags)
			throws EventException {
		
		CompetenceData1 compData = new CompetenceData1(false);
		compData.setTitle(title);
		compData.setDescription(description);
		compData.setTagsString(tags);
		compData.setPublished(false);
		compData.setType(LearningResourceType.UNIVERSITY_CREATED);
		
		Competence1 comp;
		try {
			comp = ServiceLocator
					.getInstance()
					.getService(Competence1Manager.class)
					.saveNewCompetence(
							compData,
							user.getId(),
							credentialId, null);
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
