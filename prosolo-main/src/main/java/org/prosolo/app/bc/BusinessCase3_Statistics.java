package org.prosolo.app.bc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.app.RegistrationType;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.following.FollowedEntity;
import org.prosolo.common.domainmodel.user.following.FollowedUserEntity;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.springframework.stereotype.Service;

@Service("org.prosolo.app.bc.BusinessCase3_Statistics")
public class BusinessCase3_Statistics extends BusinessCase {

	public Map<String, Tag> allTags = new HashMap<String, Tag>();
	
	public void setFollowedUser(User user, User followedUser) {

		FollowedEntity fe = new FollowedUserEntity();
		fe.setUser(user);
		fe.setFollowedResource(followedUser);
		fe = ServiceLocator.getInstance().getService(DefaultManager.class)
				.saveEntity(fe);
		user = ServiceLocator.getInstance().getService(DefaultManager.class)
				.saveEntity(user);
	}

	public void initRepository() {
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
		String roleUserTitle = "User";
		String roleManagerTitle = "Manager";
		String roleAdminTitle = "Admin";
		Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleUserTitle);
		Role roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleManagerTitle);
		Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleAdminTitle);

		
		
		
		/*
		 * CREATING USERS
		 */
		String fictitiousUser = "System analyst";
		String password = "prosolo@2014";
		

		
		User userNickPowell = createUser("Nick", "Powell", "nick.powell@gmail.com", password, fictitiousUser, "male1.png", roleUser);
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, userNickPowell);
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userNickPowell);
		
		User userRichardAnderson = 	createUser("Richard", "Anderson", "richard.anderson@gmail.com", password, fictitiousUser, "male2.png", roleUser);
		User userKevinMitchell = 	createUser("Kevin", "Mitchell", "kevin.mitchell@gmail.com", password, fictitiousUser, "male3.png", roleUser);
		User userPaulEdwards = 		createUser("Paul", "Edwards", "paul.edwards@gmail.com", password, fictitiousUser, "male4.png", roleUser);
		User userStevenTurner = 	createUser("Steven", "Turner", "steven.turner@gmail.com", password, fictitiousUser, "male5.png", roleUser);
		User userGeorgeYoung = 		createUser("George", "Young", "george.young@gmail.com", password, fictitiousUser, "male6.png", roleUser);
		User userPhillAmstrong = 	createUser("Phill", "Amstrong", "phill.amstrong@gmail.com", password, fictitiousUser, "male7.png", roleUser);
		User userJosephGarcia = 	createUser("Joseph", "Garcia", "joseph.garcia@gmail.com", password, fictitiousUser, "male8.png", roleUser);
		User userTimothyRivera = 	createUser("Timothy", "Rivera", "timothy.rivera@gmail.com", password, fictitiousUser, "male9.png", roleUser);
		User userKevinHall = 		createUser("Kevin", "Hall", "kevin.hall@gmail.com", password, fictitiousUser, "male10.png", roleUser);
		User userKennethCarter = 	createUser("Kenneth", "Carter", "kenneth.carter@gmail.com", password, fictitiousUser, "male11.png", roleUser);
		User userAnthonyMoore = 	createUser("Anthony", "Moore", "anthony.moore@gmail.com", password, fictitiousUser, "male12.png", roleUser);
		
		
		User userTaniaCortese = 	createUser("Tania", "Cortese", "tania.cortese@gmail.com", password, fictitiousUser, "female1.png", roleUser);
		User userSonyaElston = 		createUser("Sonya", "Elston", "sonya.elston@gmail.com", password, fictitiousUser, "female2.png", roleUser);
		User userLoriAbner = 		createUser("Lori", "Abner", "lori.abner@gmail.com", password, fictitiousUser, "female3.png", roleUser);
		User userSamanthaDell = 	createUser("Samantha", "Dell", "samantha.dell@gmail.com", password, fictitiousUser, "female4.png", roleUser);
		User userAkikoKido = 		createUser("Akiko", "Kido", "akiko.kido@gmail.com", password, fictitiousUser, "female7.png", roleUser);
		User userKarenWhite = 		createUser("Karen", "White", "karen.white@gmail.com", password, fictitiousUser, "female10.png", roleUser);
		User userAnnaHallowell = 	createUser("Anna", "Hallowell", "anna.hallowell@gmail.com", password, fictitiousUser, "female11.png", roleUser);
		User userErikaAmes = 		createUser("Erika", "Ames", "erika.ames@gmail.com", password, fictitiousUser, "female12.png", roleUser);
		User userHelenCampbell = 	createUser("Helen", "Campbell", "helen.campbell@gmail.com", password, fictitiousUser, "female13.png", roleUser);
		User userSheriLaureano = 	createUser("Sheri", "Laureano", "sheri.laureano@gmail.com", password, fictitiousUser, "female14.png", roleUser);
		User userAngelicaFallon = 	createUser("Angelica", "Fallon", "angelica.fallon@gmail.com", password, fictitiousUser, "female16.png", roleUser);
		User userIdaFritz = 		createUser("Ida", "Fritz", "ida.fritz@gmail.com", password, fictitiousUser, "female17.png", roleUser);
		User userRachelWiggins = 	createUser("Rachel", "Wiggins", "rachel.wiggins@gmail.com", password, fictitiousUser, "female20.png", roleUser);

		/*
		 * END CRETAING USERS
		 */

		// ////////////////////////////
		// Credential for Nick Powell
		// ///////////////////////////////
		Credential1 cred1 = createCredential(
			"Preparing Statistical Data for Analysis", 
			"This section provides an example of the programming code needed to read "
				+ "in a multilevel data file, to create an incident-level aggregated flat file "
				+ "for summary-level analysis, and to prepare individual data segments for detailed "
				+ "analysis. For illustration purposes, a National Incident-Based Reporting System "
				+ "(NIBRS) data file obtained from the FBI is read into and restructured in SPSS, "
				+ "SAS, and Microsoft ACCESS. The concepts illustrated are applicable to state-level "
				+ "data sets and transferable to other software.",
			userNickPowell, 
			"data, statistics, exploring data");
		

		Competence1 comp1cred1 = null;
		try {
			comp1cred1 = createCompetence(
						userNickPowell,
						"Outline Descriptive statistics",
						"Descriptive statistics is the discipline of quantitatively "
								+ "describing the main features of a collection of data.Descriptive "
								+ "statistics are distinguished from inferential statistics (or inductive statistics), "
								+ "in that descriptive statistics aim to summarize a sample, rather than use the data to "
								+ "learn about the population that the sample of data is thought to represent.",
						cred1.getId(),
						"descriptive statistics, statistics");
			
			addCompetenceToCredential(cred1, comp1cred1, userNickPowell);
			
			Activity1 a1 = createActivity(
					userNickPowell, 
					"Read introduction to Descriptive statistics",
					"http://www.socialresearchmethods.net/kb/statdesc.php");
			
			Activity1 a2 = createActivity(
					userNickPowell, 
					"Univariate analysis",
					"http://www.slideshare.net/christineshearer/univariate-analysis");
			
			Activity1 a3 = createActivity(
					userNickPowell, 
					"Data collection",
					"http://en.wikipedia.org/wiki/Data_collection");
			
			Activity1 a4 = createActivity(
					userNickPowell, 
					"Probability through simulation",
					"http://www.stat.yale.edu/Courses/1997-98/101/sampinf.htm");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred1.getId(), a1);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred1.getId(), a2);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred1.getId(), a3);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred1.getId(), a4);
		} catch (EventException e) {
			logger.error(e);
		}
		
		
		

		// CREDENTIAL 1
		Credential1 cred2 = createCredential(
				"Learn how to explore data in statistics",
				"Learn the first steps in analyzing data: exploring it.In statistics, exploratory data analysis (EDA) "
						+ "is an approach to analyzing data sets to summarize their main characteristics in easy-to-understand form, "
						+ "often with visual graphs, without using a statistical model or having formulated a hypothesis. "
						+ "Exploratory data analysis was promoted by John Tukey to encourage statisticians visually to examine "
						+ "their data sets, to formulate hypotheses that could be tested on new data-sets.",
				userNickPowell,
				"data, statistics, exploring data");

		Competence1 comp1cred2 = null;
		try {
			comp1cred2 = createCompetence(
					userNickPowell,
					"Differentiate Parametric Data",
					"Familiarity with parametric tests and parametric data. "
							+ "Parametric statistics is a branch of statistics that assumes that "
							+ "the data has come from a type of probability distribution and makes "
							+ "inferences about the parameters of the distribution. Most well-known "
							+ "elementary statistical methods are parametric.",
					cred2.getId(),
					"parametric statistics, statistics");
			
			addCompetenceToCredential(cred2, comp1cred2, userNickPowell);
			
			Activity1 a5 = createActivity(
					userNickPowell, 
					"Parametric and Resampling Statistics",
					"http://www.uvm.edu/~dhowell/StatPages/Resampling/Resampling.html");
			
			Activity1 a6 = createActivity(
					userNickPowell, 
					"Read about Parametric statistics",
					"http://laboratory-manager.advanceweb.com/Columns/Interpreting-Statistics/Non-Parametric-Statistics.aspx");
			
			Activity1 a7 = createActivity(
					userNickPowell, 
					"Read about Probability distribution",
					"http://isomorphismes.tumblr.com/post/18913494015/probability-distributions");
			
			Activity1 a8 = createActivity(
					userNickPowell, 
					"List of probability distributions",
					"http://www.mathwave.com/articles/distribution-fitting-graphs.html");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred2.getId(), a5);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred2.getId(), a6);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred2.getId(), a7);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred2.getId(), a8);
		} catch (EventException e) {
			logger.error(e);
		}
	

		
		Competence1 comp2cred2 = null;
		try {
			comp2cred2 = createCompetence(
					userNickPowell,
					"Illustrate and Prepare Data",
					"Knowledge in Using frequency distributions, other graphs and "
							+ "descriptive statistics to screen our data. Statistical graphs "
							+ "present data and the results of statistical analysis, assist in "
							+ "the analysis of data, and occasionally are used to facilitate statistical "
							+ "computation. Presentation graphs include the familiar bar graph, pie chart, "
							+ "line graph, scatterplot, and statistical map. Data analysis employs these graphical "
							+ "forms as well as others.",
					cred2.getId(),
					"data, statistics");
			
			addCompetenceToCredential(cred2, comp2cred2, userNickPowell);
			
			Activity1 a9 = createActivity(
					userNickPowell, 
					"An Introductory Handbook to Probability, Statistics and Excel",
					"http://records.viu.ca/~johnstoi/maybe/maybe3.htm");
			
			Activity1 a10 = createActivity(
					userNickPowell, 
					"Box Plot: Display of Distribution",
					"http://www.physics.csbsju.edu/stats/box2.html");
			
			Activity1 a11 = createActivity(
					userNickPowell, 
					"Data Types",
					"http://wiki.stat.ucla.edu/socr/index.php/AP_Statistics_Curriculum_2007_EDA_DataTypes");
			
			Activity1 a12 = createActivity(
					userNickPowell, 
					"Probability through simulation",
					"http://wiki.stat.ucla.edu/socr/index.php/AP_Statistics_Curriculum_2007_Prob_Simul");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred2.getId(), a9);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred2.getId(), a10);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred2.getId(), a11);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred2.getId(), a12);
		} catch (EventException e) {
			logger.error(e);
		}
		
		
		
		////////////////////////////////////////////////
		///CREDENTIAL: 'Understanding of Applications of Learning Analytics in Education'
		////////////////////////////////////////////////
		
		Credential1 cred3 = createCredential(
				"Understanding of Applications of Learning Analytics in Education",
				"This is a credential provides a set of competences for the EdX Data Analytics and Learning MOOC",
				userNickPowell,
				"learning analytics, education");
		
		Competence1 comp1cred3;
		try {
			comp1cred3 = createCompetence(
					userNickPowell,
					"Define social network analysis",
					"Define networks and articulate why they are important for education and educational research.",
					cred3.getId(),
					"social network analysis, sna");
			
			addCompetenceToCredential(cred3, comp1cred3, userNickPowell);
		} catch (EventException e) {
			logger.error(e);
		}

			
		Competence1 comp2cred3;
		try {
			comp2cred3 = createCompetence(
					userNickPowell,
					"Perform social network analysis centrality measures using Gephi",
					"See the title. This also includes being able to import data in to Gephi.",
					cred3.getId(),
					"social network analysis, sna, centrality measures, gephi");
			
			addCompetenceToCredential(cred3, comp2cred3, userNickPowell);
			
			
			Activity1 a13 = createActivity(
					userNickPowell, 
					"Gephi",
					"https://gephi.org");
			
			Activity1 a14 = createActivity(
					userNickPowell, 
					"Gephi Demo 920",
					"http://www.youtube.com/watch?v=JgDYV5ArXgw");
			
			Activity1 a15 = createActivity(
					userNickPowell, 
					"Paper: 'Gephi: An Open Source Software for Exploring and Manipulating Networks'",
					"http://www.aaai.org/ocs/index.php/ICWSM/09/paper/view/154");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred3.getId(), a13);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred3.getId(), a14);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred3.getId(), a15);
		} catch (EventException e) {
			logger.error(e);
		}
		
		
		Competence1 comp3cred3;
		try {
			comp3cred3 = createCompetence(
					userNickPowell,
					"Interpret results of social network analysis",
					"Interpret detailed meaning of SNA result and importance of the position of actors in social networks for information flow. Discuss implications for educational research and practice. ",
					cred3.getId(),
					"social network analysis, sna");

			addCompetenceToCredential(cred3, comp3cred3, userNickPowell);
		} catch (EventException e) {
			logger.error(e);
		}
			
		
		
		
		///////////////////////////////////////
		// CREDENTIAL Learning Statistical Correlation
		///////////////////////////////////////
		Credential1 cred4 = createCredential(
				"Learning Statistical Correlation",
				"Learn how to identify relationship between two or "
						+ "more variable and what are the most usually used relationships. "
						+ "Correlation is a measure of relationship between two mathematical "
						+ "variables or measured data values, which includes the Pearson correlation "
						+ "coefficient as a special case.",
				userNickPowell,
				"statistics, correlation");

		// enroll phillAmstrong to cred 4
			
		Competence1 comp1cred4;
		try {
			comp1cred4 = createCompetence(
					userNickPowell,
					"Construct Bivariate Correlations",
					"A statistical test that measures the association or relationship between two "
							+ "continuous/interval/ordinal level variables. Bivariate correlation is a measure "
							+ "of the relationship between the two variables; it measures the strength of their "
							+ "relationship, which can range from absolute value 1 to 0. The stronger the relationship, "
							+ "the closer the value is to 1. The relationship can be positive or negative; in positive "
							+ "relationship, as one value increases, another value increases with it. In the negative "
							+ "relationship, as one value increases, the other one decreases.",
					cred4.getId(),
					"bivariate correlations");

			addCompetenceToCredential(cred4, comp1cred4, userNickPowell);

			Activity1 a16 = createActivity(
					userNickPowell,
					"Pearson's Correlation Coeeficient",
					"http://hsc.uwe.ac.uk/dataanalysis/quantinfasspear.asp");

			Activity1 a17 = createActivity(
					userNickPowell,
					"Instructions for Covariance, Correlation, and Bivariate Graphs",
					"http://www.math.uah.edu/stat/sample/Covariance.html");

			Activity1 a18 = createActivity(
					userNickPowell,
					"Coefficient of determination",
					"http://www.statisticshowto.com/articles/how-to-find-the-coefficient-of-determination/");

			Activity1 a19 = createActivity(
					userNickPowell,
					"Spearman's rank correlation coefficient",
					"http://udel.edu/~mcdonald/statspearman.html");

			Activity1 a20 = createActivity(
					userNickPowell,
					"Kendall tau rank correlation coefficient",
					"http://www.statisticssolutions.com/academic-solutions/resources/directory-of-statistical-analyses/kendalls-tau-and-spearmans-rank-correlation-coefficient/");

			Activity1 a21 = createActivity(
					userNickPowell,
					"Biserial and Point-Biserial Correlations",
					"http://www.apexdissertations.com/articles/point-biserial_correlation.html");

			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred4.getId(), a16);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred4.getId(), a17);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred4.getId(), a18);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred4.getId(), a19);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred4.getId(), a20);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred4.getId(), a21);
		} catch (EventException e) {
			logger.error(e);
		}
			
 
		Competence1 comp2cred4;
		try {
			comp2cred4 = createCompetence(
					userNickPowell,
					"Construct Partial Correlations",
					"Partial correlation is the relationship between two variables while controlling "
							+ "for a third variable. The purpose is to find the unique variance between two "
							+ "variables while eliminating the variance from a third variables.",
					cred4.getId(),
					"partial correlations, correlations");
			
			addCompetenceToCredential(cred4, comp2cred4, userNickPowell);

			Activity1 a22 = createActivity(
					userNickPowell,
					"Partial and Semi-Partial Correlations",
					"http://www.apexdissertations.com/articles/point-biserial_correlation.html");

			Activity1 a23 = createActivity(
					userNickPowell,
					"Partial Correlation Analysis",
					"http://explorable.com/partial-correlation-analysis.html");

			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred4.getId(), a22);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp2cred4.getId(), a23);
		} catch (EventException e) {
			logger.error(e);
		}

			
		
			
		
		///////////////////////////////////////
		// CREDENTIAL Learning Statistical Correlation
		///////////////////////////////////////
		Credential1 cred5 = createCredential(
				"Exploratory analysis of data",
				"Exploratory analysis of data makes use of graphical and numerical techniques to "
						+ "study patterns and departures from patterns. In examining distributions of data, "
						+ "students should be able to detect important characteristics, such as shape, location, "
						+ "variability, and unusual values. From careful observations of patterns in data, "
						+ "students can generate conjectures about relationships among variables. The notion of "
						+ "how one variable may be associated with another permeates almost all of statistics, from "
						+ "simple comparisons of proportions through linear regression. The difference between "
						+ "association and causation must accompany this conceptual development throughout.",
				userNickPowell,
				"data, statistics, exploring data");

		Competence1 comp1cred5;
		try {
			comp1cred5 = createCompetence(
					userNickPowell,
					"Analyze Data",
					"Know how to take raw data, extract meaningful information and use statistical tools.",
					cred5.getId(),
					"data analysis, data");
			
			addCompetenceToCredential(cred5, comp1cred5, userNickPowell);
			
			Activity1 a24 = createActivity(
					userNickPowell, 
					"Sampling activity",
					"http://exploringdata.net/sampling.htm");
			
			Activity1 a25 = createActivity(
					userNickPowell, 
					"Normal Distribution",
					"http://www.khanacademy.org/math/statistics/v/introduction-to-the-normal-distribution");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred5.getId(), a24);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred5.getId(), a25);
		} catch (EventException e) {
			logger.error(e);
		}
		
		
		///////////////////////////////////////
		// CREDENTIAL Statistics 2 – Inference and Association
		///////////////////////////////////////
		Credential1 cred6 = createCredential(
				"Statistics 2 – Inference and Association",
				"This course, the second in a three-course sequence, "
						+ "provides an easy introduction to inference and association through a series of practical applications, "
						+ "based on the resampling/simulation approach. Once you have completed this course you will be able to "
						+ "test hypotheses and compute confidence intervals regarding proportions or means, computer correlations and "
						+ "fit simple linear regressions.  Topics covered also include chi-square goodness-of-fit and paired comparisons.",
				userNickPowell,
				"inference, statistics, association");
			
		Competence1 comp1cred6;
		try {
			comp1cred6 = createCompetence(
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
					cred6.getId(),
					"statistics, data, analysis");

			addCompetenceToCredential(cred6, comp1cred6, userNickPowell);
		} catch (EventException e) {
			logger.error(e);
		}

		
		///////////////////////////////////////
		// CREDENTIAL Spatial Analysis Techniques in R taught by Dave Unwin
		///////////////////////////////////////
		Credential1 cred7 = createCredential(
				"Spatial Analysis Techniques in R taught by Dave Unwin",
				"This course will teach users how to implement spatial statistical "
						+ "analysis procedures using R software. Topics covered include point pattern analysis, "
						+ "identifying clusters, measures of spatial association, geographically weighted regression "
						+ "and surface procession.",
				userNickPowell,
				"r, statistics, spatial analysis");
		
		Competence1 comp1cred7;
		try {
			comp1cred7 = createCompetence(
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
					cred7.getId(),
					"data analysis, data, statistics");

			addCompetenceToCredential(cred7, comp1cred7, userNickPowell);
		} catch (EventException e) {
			logger.error(e);
		}
			

		///////////////////////////////////////
		// CREDENTIAL Spatial Analysis Techniques in R taught by Dave Unwin
		///////////////////////////////////////
		Credential1 cred8 = createCredential(
			"Learning Parametric statistics",
			"Parametric statistics is a branch of statistics that assumes that the data has come from a type of "
				+ "probability distribution and makes inferences about the parameters of the distribution. Most "
				+ "well-known elementary statistical methods are parametric",
			userNickPowell,
			"statistics, parametric statistics");

		Competence1 comp1cred8;
		try {
			comp1cred8 = createCompetence(
					userNickPowell,
					"Parametric and Non-parametric statistics",
					"In the literal meaning of the terms, a parametric statistical test is one that makes assumptions about the "
					+ "parameters (defining properties) of the population distribution(s) from which one's data are drawn, while "
					+ "a non-parametric test is one that makes no such assumptions. In this strict sense, \"non-parametric\" is "
					+ "essentially a null category, since virtually all statistical tests assume one thing or another about the "
					+ "properties of the source population(s).",
					cred8.getId(),
					"parametric statistics, non-parametric statistics, statistics");
			
			addCompetenceToCredential(cred8, comp1cred8, userNickPowell);
			
			Activity1 ab1 = createActivity(
					userNickPowell, 
					"Parametric statistics, From Wikipedia, the free encyclopedia",
					"http://www.mathsisfun.com/data/standard-normal-distribution.html");
			
			Activity1 ab2 = createActivity(
					userNickPowell, 
					"Non-parametric statistics, From Wikipedia, the free encyclopedia",
					"http://laboratory-manager.advanceweb.com/Columns/Interpreting-Statistics/Non-Parametric-Statistics.aspx");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred8.getId(), ab1);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred8.getId(), ab2);
		} catch (EventException e) {
			logger.error(e);
		}
		
		
		///////////////////////////////////////
		// CREDENTIAL Preparing Data for Analysis
		///////////////////////////////////////
		Credential1 cred9 = createCredential(
				"Preparing Data for Analysis",
				"This section provides an example of the programming code needed to read "
						+ "in a multilevel data file, to create an incident-level aggregated flat file "
						+ "for summary-level analysis, and to prepare individual data segments for detailed "
						+ "analysis. For illustration purposes, a National Incident-Based Reporting System "
						+ "(NIBRS) data file obtained from the FBI is read into and restructured in SPSS, "
						+ "SAS, and Microsoft ACCESS. The concepts illustrated are applicable to state-level "
						+ "data sets and transferable to other software.",
				userNickPowell,
				"statistics, data analysis, data");

		Competence1 comp1cred9;
		try {
			comp1cred9 = createCompetence(
					userNickPowell,
					"Data Preparation",
					"In the literal meaning of the terms, a parametric statistical test is one that makes assumptions about the "
							+ "parameters (defining properties) of the population distribution(s) from which one's data are drawn, while "
							+ "a non-parametric test is one that makes no such assumptions. In this strict sense, \"non-parametric\" is "
							+ "essentially a null category, since virtually all statistical tests assume one thing or another about the "
							+ "properties of the source population(s).",
							cred9.getId(),
							"data preparation, data, statistics");
			
			addCompetenceToCredential(cred9, comp1cred9, userNickPowell);
			
			
			Activity1 ba1 = createActivity(
					userNickPowell, 
					"Extracting Data from Incident-Based Systems and NIBRS",
					"http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/index.shtml");
			
			Activity1 ba2 = createActivity(
					userNickPowell, 
					"PREPARING A FILE FOR ANALYSIS",
					"http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/preparing_data.shtml");
			
			Activity1 ba3 = createActivity(
					userNickPowell, 
					"Reading a Multilevel Data File",
					"http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/reading_data.shtml");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred9.getId(), ba1);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred9.getId(), ba2);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred9.getId(), ba3);
		} catch (EventException e) {
			logger.error(e);
		}
 

		///////////////////////////////////////
		// CREDENTIAL Drawing conclusions from data
		///////////////////////////////////////
		Credential1 cred10 = createCredential(
				"Drawing conclusions from data",
				"How well do measurements of mercury concentrations in ten "
						+ "cans of tuna reflect the composition of the factory's entire output? "
						+ "Why can't you just use the average of these measurements? "
						+ "How much better would the results of 100 such tests be? This "
						+ "final lesson on measurement will examine these questions and introduce "
						+ "you to some of the methods of dealing with data. This stuff is important "
						+ "not only for scientists, but also for any intelligent citizen who wishes "
						+ "to independenly evaluate the flood of numbers served up by advertisers, "
						+ "politicians,  experts , and yes— by other scientists.",
				userNickPowell,
				"statistics, data analysis, data");
 
		Competence1 comp1cred10;
		try {
			comp1cred10 = createCompetence(
					userNickPowell,
					"Data Analysis",
					"Analysis of data is a process of inspecting, cleaning, transforming, and modeling data with the goal of "
							+ "discovering useful information, suggesting conclusions, and supporting decision-making. Data analysis "
							+ "has multiple facets and approaches, encompassing diverse techniques under a variety of names, in"
							+ " different business, science, and social science domains.",
					cred10.getId(),
					"data analysis, data, statistics");
			
			addCompetenceToCredential(cred10, comp1cred10, userNickPowell);
			
			
			Activity1 bb1 = createActivity(
					userNickPowell, 
					"Drawing conclusions from data",
					"http://www.chem1.com/acad/webtext/matmeasure/mm5.html");
			
			Activity1 bb2 = createActivity(
					userNickPowell, 
					"Understanding the units of scientific measurement",
					"http://www.chem1.com/acad/webtext/matmeasure/mm1.html");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred10.getId(), bb1);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred10.getId(), bb2);
		} catch (EventException e) {
			logger.error(e);
		}
		

		///////////////////////////////////////
		// CREDENTIAL Understanding Descriptive Statistics
		///////////////////////////////////////
		Credential1 cred11 = createCredential(
				"Understanding Descriptive Statistics",
				"How well do measurements of mercury concentrations in ten "
						+ "cans of tuna reflect the composition of the factory's entire output? "
						+ "Why can't you just use the average of these measurements? "
						+ "How much better would the results of 100 such tests be? This "
						+ "final lesson on measurement will examine these questions and introduce "
						+ "you to some of the methods of dealing with data. This stuff is important "
						+ "not only for scientists, but also for any intelligent citizen who wishes "
						+ "to independenly evaluate the flood of numbers served up by advertisers, "
						+ "politicians,  experts , and yes— by other scientists.",
				userNickPowell,
				"statistics, descriptive statistics");

		Competence1 comp1cred11 = null;
		try {
			comp1cred11 = createCompetence(
					userNickPowell,
					"Descriptive Statistics",
					"Descriptive statistics are used to describe the basic features of the data in a study. "
							+ "They provide simple summaries about the sample and the measures. Together with simple graphics "
							+ "analysis, they form the basis of virtually every quantitative analysis of data.",
					cred11.getId(),
					"descriptive statistics, statistics");
			
			addCompetenceToCredential(cred11, comp1cred11, userNickPowell);
			
			
			Activity1 bc1 = createActivity(
					userNickPowell,
					"Understanding Descriptive Statistics",
					"http://www.nationalatlas.gov/articles/mapping/a_statistics.html");
			
			Activity1 bc2 = createActivity(
					userNickPowell,
					"Teaching Prediction Intervals",
					"http://www.amstat.org/publications/jse/secure/v8n3/preston.cfm");
			
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred11.getId(), bc1);
			ServiceLocator.getInstance().getService(Competence1Manager.class).addActivityToCompetence(comp1cred11.getId(), bc2);
		} catch (EventException e) {
			logger.error(e);
		}
			
			
		///////////////////////////////////////
		// CREDENTIAL Understanding Descriptive Statistics
		///////////////////////////////////////
		Credential1 cred12 = createCredential(
				"Learning Descriptive statistics",
				"Descriptive statistics is the discipline of quantitatively describing the main features"
						+ " of a collection of data.Descriptive statistics are distinguished from inferential "
						+ "statistics (or inductive statistics), in that descriptive statistics aim to summarize "
						+ "a sample, rather than use the data to learn about the population that the sample of "
						+ "data is thought to represent. This generally means that descriptive statistics, unlike "
						+ "inferential statistics, are not developed on the basis of probability theory.",
				userNickPowell,
				"statistics, descriptive statistics");

		addCompetenceToCredential(cred12, comp1cred11, userNickPowell);

 
		try {
			ServiceLocator
					.getInstance()
					.getService(PostManager.class)
					.createNewPost(userNickPowell,
							"Learning parametric data.", VisibilityType.PUBLIC, null, null, true, null, null, null, null);

			ServiceLocator
					.getInstance()
					.getService(PostManager.class)
					.createNewPost(
							userNickPowell,
							"Can anybody recommend me a good book for SPSS basics? Thanks!",
							VisibilityType.PUBLIC, null, null, true, null, null, null, null);
		} catch (EventException e) {
			logger.error(e);
		}
 	}

	private void addCompetenceToCredential(Credential1 credential, Competence1 competence, User user) {
		ServiceLocator
				.getInstance()
				.getService(CredentialManager.class).addCompetenceToCredential(credential.getId(), competence);
		
		CredentialManager credentialManager = ServiceLocator
				.getInstance()
				.getService(CredentialManager.class);
		
		CredentialData credentialData = credentialManager.getCredentialDataForEdit(credential.getId(), user.getId(), true);
		credentialData.setPublished(true);
		
		credentialManager.updateCredential(credentialData, user, org.prosolo.services.nodes.data.Role.User);
	}

	private User createUser(String name, String lastname, String emailAddress, String password, String fictitiousUser,
			String avatar, Role roleUser) {
		try {
			User newUser = ServiceLocator
					.getInstance()
					.getService(UserManager.class)
					.createNewUser(name, lastname, emailAddress,
							true, password, fictitiousUser, getAvatarInputStream(avatar), avatar);
			
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

	private Activity1 createActivity(User userNickPowell, String title, String... links) {
		ActivityData actData = new ActivityData(false);
		actData.setTitle(title);
		actData.setPublished(true);
		actData.setActivityType(ActivityType.TEXT);
		actData.setType(LearningResourceType.UNIVERSITY_CREATED);
		
		if (links != null) {
			List<ResourceLinkData> activityLinks = new ArrayList<>();
			
			for (String link : links) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setLinkName(link);
				rlData.setUrl(link);
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
						userNickPowell.getId());
		return act;
	}

	private Credential1 createCredential(String title, String description, User userNickPowell, String tags) {
		CredentialData credentialData = new CredentialData(false);
		credentialData.setTitle(title);
		credentialData.setDescription(description);
		credentialData.setTagsString(tags);
		credentialData.setPublished(true);
		credentialData.setType(LearningResourceType.UNIVERSITY_CREATED);
		
		Credential1 credNP1 = ServiceLocator
				.getInstance()
				.getService(CredentialManager.class)
				.saveNewCredential(credentialData, userNickPowell);
		
		return credNP1;
	}

	public Competence1 createCompetence(User user, String title, String description, long credentialId, String tags)
			throws EventException {
		
		CompetenceData1 compData = new CompetenceData1(false);
		compData.setTitle(title);
		compData.setDescription(description);
		compData.setTagsString(tags);
		compData.setPublished(true);
		compData.setType(LearningResourceType.UNIVERSITY_CREATED);
		
		Competence1 comp = ServiceLocator
				.getInstance()
				.getService(Competence1Manager.class)
				.saveNewCompetence(
						compData,
						user,
						credentialId);
		
		return comp;
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
