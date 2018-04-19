package org.prosolo.app.bc;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.app.RegistrationType;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.following.FollowedEntity;
import org.prosolo.common.domainmodel.user.following.FollowedUserEntity;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

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
			String roleUserTitle = SystemRoleNames.USER;
			String roleManagerTitle = SystemRoleNames.MANAGER;
			String roleAdminTitle = SystemRoleNames.ADMIN;
			Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleUserTitle);
			Role roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleManagerTitle);
			Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(roleAdminTitle);


			/*
			 * CREATING USERS
			 */
			String fictitiousUser = "System analyst";
			String password = "prosolo@2014";


			User userNickPowell = createUser(0,"Nick", "Powell", "nick.powell@gmail.com", password, fictitiousUser, "male1.png", roleUser);
			userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, userNickPowell.getId());
			userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userNickPowell.getId());

			//generate event after roles are updated
			Map<String, String> params = null;
			ServiceLocator.getInstance().getService(EventFactory.class).generateEvent(
					EventType.USER_ROLES_UPDATED, UserContextData.ofActor(userNickPowell.getId()), userNickPowell, null, null, params);
			
			//create organization
			OrganizationData orgData = new OrganizationData();
			orgData.setTitle("Org 1");
			orgData.setAdmins(Arrays.asList(new UserData(userNickPowell)));
			Organization org = ServiceLocator.getInstance().getService(OrganizationManager.class)
					.createNewOrganization(orgData, UserContextData.empty());

			//to give time indexes to be created after ogranization is saved
			Thread.sleep(1500);

			User userRichardAnderson = createUser(org.getId(),"Richard", "Anderson", "richard.anderson@gmail.com", password, fictitiousUser, "male2.png", roleUser);
			User userKevinMitchell = createUser(org.getId(),"Kevin", "Mitchell", "kevin.mitchell@gmail.com", password, fictitiousUser, "male3.png", roleUser);
			User userPaulEdwards = createUser(org.getId(),"Paul", "Edwards", "paul.edwards@gmail.com", password, fictitiousUser, "male4.png", roleUser);
			User userStevenTurner = createUser(org.getId(),"Steven", "Turner", "steven.turner@gmail.com", password, fictitiousUser, "male5.png", roleUser);
			User userGeorgeYoung = createUser(org.getId(),"George", "Young", "george.young@gmail.com", password, fictitiousUser, "male6.png", roleUser);
			User userPhillAmstrong = createUser(org.getId(),"Phill", "Amstrong", "phill.amstrong@gmail.com", password, fictitiousUser, "male7.png", roleUser);
			User userJosephGarcia = createUser(org.getId(),"Joseph", "Garcia", "joseph.garcia@gmail.com", password, fictitiousUser, "male8.png", roleUser);
			User userTimothyRivera = createUser(org.getId(),"Timothy", "Rivera", "timothy.rivera@gmail.com", password, fictitiousUser, "male9.png", roleUser);
			User userKevinHall = createUser(org.getId(),"Kevin", "Hall", "kevin.hall@gmail.com", password, fictitiousUser, "male10.png", roleUser);
			User userKennethCarter = createUser(org.getId(),"Kenneth", "Carter", "kenneth.carter@gmail.com", password, fictitiousUser, "male11.png", roleUser);
			User userAnthonyMoore = createUser(org.getId(),"Anthony", "Moore", "anthony.moore@gmail.com", password, fictitiousUser, "male12.png", roleUser);


			User userTaniaCortese = createUser(org.getId(),"Tania", "Cortese", "tania.cortese@gmail.com", password, fictitiousUser, "female1.png", roleUser);
			User userSonyaElston = createUser(org.getId(),"Sonya", "Elston", "sonya.elston@gmail.com", password, fictitiousUser, "female2.png", roleUser);
			User userLoriAbner = createUser(org.getId(),"Lori", "Abner", "lori.abner@gmail.com", password, fictitiousUser, "female3.png", roleUser);
			User userSamanthaDell = createUser(org.getId(),"Samantha", "Dell", "samantha.dell@gmail.com", password, fictitiousUser, "female4.png", roleUser);
			User userAkikoKido = createUser(org.getId(),"Akiko", "Kido", "akiko.kido@gmail.com", password, fictitiousUser, "female7.png", roleUser);
			User userKarenWhite = createUser(org.getId(),"Karen", "White", "karen.white@gmail.com", password, fictitiousUser, "female10.png", roleUser);
			User userAnnaHallowell = createUser(org.getId(),"Anna", "Hallowell", "anna.hallowell@gmail.com", password, fictitiousUser, "female11.png", roleUser);
			User userErikaAmes = createUser(org.getId(),"Erika", "Ames", "erika.ames@gmail.com", password, fictitiousUser, "female12.png", roleUser);
			User userHelenCampbell = createUser(org.getId(),"Helen", "Campbell", "helen.campbell@gmail.com", password, fictitiousUser, "female13.png", roleUser);
			User userSheriLaureano = createUser(org.getId(),"Sheri", "Laureano", "sheri.laureano@gmail.com", password, fictitiousUser, "female14.png", roleUser);
			User userAngelicaFallon = createUser(org.getId(),"Angelica", "Fallon", "angelica.fallon@gmail.com", password, fictitiousUser, "female16.png", roleUser);
			User userIdaFritz = createUser(org.getId(),"Ida", "Fritz", "ida.fritz@gmail.com", password, fictitiousUser, "female17.png", roleUser);
			User userRachelWiggins = createUser(org.getId(),"Rachel", "Wiggins", "rachel.wiggins@gmail.com", password, fictitiousUser, "female20.png", roleUser);

			/*
			 * END CRETAING USERS
			 */

			// ////////////////////////////
			// Credential for Nick Powell
			// ///////////////////////////////
			Credential1 cred1 = createCredential(org.getId(),
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
				comp1cred1 = createCompetence(org.getId(),
						userNickPowell,
						"Outline Descriptive statistics",
						"Descriptive statistics is the discipline of quantitatively "
								+ "describing the main features of a collection of data.Descriptive "
								+ "statistics are distinguished from inferential statistics (or inductive statistics), "
								+ "in that descriptive statistics aim to summarize a sample, rather than use the data to "
								+ "learn about the population that the sample of data is thought to represent.",
						cred1.getId(),
						"descriptive statistics, statistics");

				createActivity(org.getId(),
						userNickPowell,
						"Read introduction to Descriptive statistics",
						comp1cred1.getId(),
						"http://www.socialresearchmethods.net/kb/statdesc.php");

				createActivity(org.getId(),
						userNickPowell,
						"Univariate analysis",
						comp1cred1.getId(),
						"http://www.slideshare.net/christineshearer/univariate-analysis");

				createActivity(org.getId(),
						userNickPowell,
						"Data collection",
						comp1cred1.getId(),
						"http://en.wikipedia.org/wiki/Data_collection");

				createActivity(org.getId(),
						userNickPowell,
						"Probability through simulation",
						comp1cred1.getId(),
						"http://www.stat.yale.edu/Courses/1997-98/101/sampinf.htm");

				publishCredential(cred1, cred1.getCreatedBy());
			} catch (Exception ex) {
				logger.error("Error", ex);
			}


			// CREDENTIAL 1
			Credential1 cred2 = createCredential(org.getId(),
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
				comp1cred2 = createCompetence(org.getId(),
						userNickPowell,
						"Differentiate Parametric Data",
						"Familiarity with parametric tests and parametric data. "
								+ "Parametric statistics is a branch of statistics that assumes that "
								+ "the data has come from a type of probability distribution and makes "
								+ "inferences about the parameters of the distribution. Most well-known "
								+ "elementary statistical methods are parametric.",
						cred2.getId(),
						"parametric statistics, statistics");

				createActivity(org.getId(),
						userNickPowell,
						"Parametric and Resampling Statistics",
						comp1cred2.getId(),
						"http://www.uvm.edu/~dhowell/StatPages/Resampling/Resampling.html");

				createActivity(org.getId(),
						userNickPowell,
						"Read about Parametric statistics",
						comp1cred2.getId(),
						"http://laboratory-manager.advanceweb.com/Columns/Interpreting-Statistics/Non-Parametric-Statistics.aspx");

				createActivity(org.getId(),
						userNickPowell,
						"Read about Probability distribution",
						comp1cred2.getId(),
						"http://isomorphismes.tumblr.com/post/18913494015/probability-distributions");

				createActivity(org.getId(),
						userNickPowell,
						"List of probability distributions",
						comp1cred2.getId(),
						"http://www.mathwave.com/articles/distribution-fitting-graphs.html");
			} catch (Exception ex) {
				logger.error("Error", ex);
			}


			Competence1 comp2cred2 = null;
			try {
				comp2cred2 = createCompetence(org.getId(),
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

				createActivity(org.getId(),
						userNickPowell,
						"An Introductory Handbook to Probability, Statistics and Excel",
						comp2cred2.getId(),
						"http://records.viu.ca/~johnstoi/maybe/maybe3.htm");

				createActivity(org.getId(),
						userNickPowell,
						"Box Plot: Display of Distribution",
						comp2cred2.getId(),
						"http://www.physics.csbsju.edu/stats/box2.html");

				createActivity(org.getId(),
						userNickPowell,
						"Data Types",
						comp2cred2.getId(),
						"http://wiki.stat.ucla.edu/socr/index.php/AP_Statistics_Curriculum_2007_EDA_DataTypes");

				createActivity(org.getId(),
						userNickPowell,
						"Probability through simulation",
						comp2cred2.getId(),
						"http://wiki.stat.ucla.edu/socr/index.php/AP_Statistics_Curriculum_2007_Prob_Simul");

				publishCredential(cred2, cred2.getCreatedBy());

				addCompetenceToCredential(org.getId(),cred2, comp1cred1, userNickPowell);
			} catch (Exception ex) {
				logger.error(ex);
				ex.printStackTrace();
			}


			////////////////////////////////////////////////
			///CREDENTIAL: 'Understanding of Applications of Learning Analytics in Education'
			////////////////////////////////////////////////

			Credential1 cred3 = createCredential(org.getId(),
					"Understanding of Applications of Learning Analytics in Education",
					"This is a credential provides a set of competences for the EdX Data Analytics and Learning MOOC",
					userNickPowell,
					"learning analytics, education");

			Competence1 comp1cred3;
			try {
				comp1cred3 = createCompetence(org.getId(),
						userNickPowell,
						"Define social network analysis",
						"Define networks and articulate why they are important for education and educational research.",
						cred3.getId(),
						"social network analysis, sna");

				createActivity(org.getId(),
						userNickPowell,
						"An Introductory Handbook to Probability, Statistics and Excel 2",
						comp1cred3.getId(),
						"http://records.viu.ca/~johnstoi/maybe/maybe3.htm");
			} catch (Exception ex) {
				logger.error("Error", ex);
			}


			Competence1 comp2cred3;
			try {
				comp2cred3 = createCompetence(org.getId(),
						userNickPowell,
						"Perform social network analysis centrality measures using Gephi",
						"See the title. This also includes being able to import data in to Gephi.",
						cred3.getId(),
						"social network analysis, sna, centrality measures, gephi");

				createActivity(org.getId(),
						userNickPowell,
						"Gephi",
						comp2cred3.getId(),
						"https://gephi.org");

				createActivity(org.getId(),
						userNickPowell,
						"Gephi Demo 920",
						comp2cred3.getId(),
						"http://www.youtube.com/watch?v=JgDYV5ArXgw");

				createActivity(org.getId(),
						userNickPowell,
						"Paper: 'Gephi: An Open Source Software for Exploring and Manipulating Networks'",
						comp2cred3.getId(),
						"http://www.aaai.org/ocs/index.php/ICWSM/09/paper/view/154");
			} catch (Exception ex) {
				logger.error("Error", ex);
			}


			Competence1 comp3cred3;
			try {
				comp3cred3 = createCompetence(org.getId(),
						userNickPowell,
						"Interpret results of social network analysis",
						"Interpret detailed meaning of SNA result and importance of the position of actors in social networks for information flow. Discuss implications for educational research and practice. ",
						cred3.getId(),
						"social network analysis, sna");

				createActivity(org.getId(),
						userNickPowell,
						"An Introductory Handbook to Probability, Statistics and Excel 3",
						comp3cred3.getId(),
						"http://records.viu.ca/~johnstoi/maybe/maybe3.htm");

				publishCredential(cred3, cred3.getCreatedBy());

				addCompetenceToCredential(org.getId(),cred3, comp1cred1, userNickPowell);
			} catch (Exception ex) {
				logger.error("Error", ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Learning Statistical Correlation
			///////////////////////////////////////
			Credential1 cred4 = createCredential(org.getId(),
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
				comp1cred4 = createCompetence(org.getId(),
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

				createActivity(org.getId(),
						userNickPowell,
						"Pearson's Correlation Coeeficient",
						comp1cred4.getId(),
						"http://hsc.uwe.ac.uk/dataanalysis/quantinfasspear.asp");

				createActivity(org.getId(),
						userNickPowell,
						"Instructions for Covariance, Correlation, and Bivariate Graphs",
						comp1cred4.getId(),
						"http://www.math.uah.edu/stat/sample/Covariance.html");

				createActivity(org.getId(),
						userNickPowell,
						"Coefficient of determination",
						comp1cred4.getId(),
						"http://www.statisticshowto.com/articles/how-to-find-the-coefficient-of-determination/");

				createActivity(org.getId(),
						userNickPowell,
						"Spearman's rank correlation coefficient",
						comp1cred4.getId(),
						"http://udel.edu/~mcdonald/statspearman.html");

				createActivity(org.getId(),
						userNickPowell,
						"Kendall tau rank correlation coefficient",
						comp1cred4.getId(),
						"http://www.statisticssolutions.com/academic-solutions/resources/directory-of-statistical-analyses/kendalls-tau-and-spearmans-rank-correlation-coefficient/");

				createActivity(org.getId(),
						userNickPowell,
						"Biserial and Point-Biserial Correlations",
						comp1cred4.getId(),
						"http://www.apexdissertations.com/articles/point-biserial_correlation.html");

			} catch (Exception ex) {
				logger.error(ex);
			}


			Competence1 comp2cred4;
			try {
				comp2cred4 = createCompetence(org.getId(),
						userNickPowell,
						"Construct Partial Correlations",
						"Partial correlation is the relationship between two variables while controlling "
								+ "for a third variable. The purpose is to find the unique variance between two "
								+ "variables while eliminating the variance from a third variables.",
						cred4.getId(),
						"partial correlations, correlations");

				createActivity(org.getId(),
						userNickPowell,
						"Partial and Semi-Partial Correlations",
						comp2cred4.getId(),
						"http://www.apexdissertations.com/articles/point-biserial_correlation.html");

				createActivity(org.getId(),
						userNickPowell,
						"Partial Correlation Analysis",
						comp2cred4.getId(),
						"http://explorable.com/partial-correlation-analysis.html");

				publishCredential(cred4, cred4.getCreatedBy());

				addCompetenceToCredential(org.getId(), cred4, comp1cred1, userNickPowell);
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Learning Statistical Correlation
			///////////////////////////////////////
			Credential1 cred5 = createCredential(org.getId(),
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
				comp1cred5 = createCompetence(org.getId(),
						userNickPowell,
						"Analyze Data",
						"Know how to take raw data, extract meaningful information and use statistical tools.",
						cred5.getId(),
						"data analysis, data");

				createActivity(org.getId(),
						userNickPowell,
						"Sampling activity",
						comp1cred5.getId(),
						"http://exploringdata.net/sampling.htm");

				createActivity(org.getId(),
						userNickPowell,
						"Normal Distribution",
						comp1cred5.getId(),
						"http://www.khanacademy.org/math/statistics/v/introduction-to-the-normal-distribution");

				publishCredential(cred5, cred5.getCreatedBy());
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Statistics 2 – Inference and Association
			///////////////////////////////////////
			Credential1 cred6 = createCredential(org.getId(),
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
				comp1cred6 = createCompetence(org.getId(),
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

				createActivity(org.getId(),
						userNickPowell,
						"An Introductory Handbook to Probability, Statistics and Excel 4",
						comp1cred6.getId(),
						"http://records.viu.ca/~johnstoi/maybe/maybe3.htm");

				publishCredential(cred6, cred6.getCreatedBy());
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Spatial Analysis Techniques in R taught by Dave Unwin
			///////////////////////////////////////
			Credential1 cred7 = createCredential(org.getId(),
					"Spatial Analysis Techniques in R taught by Dave Unwin",
					"This course will teach users how to implement spatial statistical "
							+ "analysis procedures using R software. Topics covered include point pattern analysis, "
							+ "identifying clusters, measures of spatial association, geographically weighted regression "
							+ "and surface procession.",
					userNickPowell,
					"r, statistics, spatial analysis");

			Competence1 comp1cred7;
			try {
				comp1cred7 = createCompetence(org.getId(),
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

				createActivity(org.getId(),
						userNickPowell,
						"An Introductory Handbook to Probability, Statistics and Excel 5",
						comp1cred7.getId(),
						"http://records.viu.ca/~johnstoi/maybe/maybe3.htm");

				publishCredential(cred7, cred7.getCreatedBy());
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Spatial Analysis Techniques in R taught by Dave Unwin
			///////////////////////////////////////
			Credential1 cred8 = createCredential(org.getId(),
					"Learning Parametric statistics",
					"Parametric statistics is a branch of statistics that assumes that the data has come from a type of "
							+ "probability distribution and makes inferences about the parameters of the distribution. Most "
							+ "well-known elementary statistical methods are parametric",
					userNickPowell,
					"statistics, parametric statistics");

			Competence1 comp1cred8;
			try {
				comp1cred8 = createCompetence(org.getId(),
						userNickPowell,
						"Parametric and Non-parametric statistics",
						"In the literal meaning of the terms, a parametric statistical test is one that makes assumptions about the "
								+ "parameters (defining properties) of the population distribution(s) from which one's data are drawn, while "
								+ "a non-parametric test is one that makes no such assumptions. In this strict sense, \"non-parametric\" is "
								+ "essentially a null category, since virtually all statistical tests assume one thing or another about the "
								+ "properties of the source population(s).",
						cred8.getId(),
						"parametric statistics, non-parametric statistics, statistics");

				createActivity(org.getId(),
						userNickPowell,
						"Parametric statistics, From Wikipedia, the free encyclopedia",
						comp1cred8.getId(),
						"http://www.mathsisfun.com/data/standard-normal-distribution.html");

				createActivity(org.getId(),
						userNickPowell,
						"Non-parametric statistics, From Wikipedia, the free encyclopedia",
						comp1cred8.getId(),
						"http://laboratory-manager.advanceweb.com/Columns/Interpreting-Statistics/Non-Parametric-Statistics.aspx");

				publishCredential(cred8, cred8.getCreatedBy());
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Preparing Data for Analysis
			///////////////////////////////////////
			Credential1 cred9 = createCredential(org.getId(),
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
				comp1cred9 = createCompetence(org.getId(),
						userNickPowell,
						"Data Preparation",
						"In the literal meaning of the terms, a parametric statistical test is one that makes assumptions about the "
								+ "parameters (defining properties) of the population distribution(s) from which one's data are drawn, while "
								+ "a non-parametric test is one that makes no such assumptions. In this strict sense, \"non-parametric\" is "
								+ "essentially a null category, since virtually all statistical tests assume one thing or another about the "
								+ "properties of the source population(s).",
						cred9.getId(),
						"data preparation, data, statistics");


				createActivity(org.getId(),
						userNickPowell,
						"Extracting Data from Incident-Based Systems and NIBRS",
						comp1cred9.getId(),
						"http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/index.shtml");

				createActivity(org.getId(),
						userNickPowell,
						"Preparing a File for Analysis",
						comp1cred9.getId(),
						"http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/preparing_data.shtml");

				createActivity(org.getId(),
						userNickPowell,
						"Reading a Multilevel Data File",
						comp1cred9.getId(),
						"http://www.jrsa.org/ibrrc/using-data/preparing_data/preparing-file/reading_data.shtml");

				publishCredential(cred9, cred9.getCreatedBy());
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Drawing conclusions from data
			///////////////////////////////////////
			Credential1 cred10 = createCredential(org.getId(),
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
				comp1cred10 = createCompetence(org.getId(),
						userNickPowell,
						"Data Analysis",
						"Analysis of data is a process of inspecting, cleaning, transforming, and modeling data with the goal of "
								+ "discovering useful information, suggesting conclusions, and supporting decision-making. Data analysis "
								+ "has multiple facets and approaches, encompassing diverse techniques under a variety of names, in"
								+ " different business, science, and social science domains.",
						cred10.getId(),
						"data analysis, data, statistics");

				createActivity(org.getId(),
						userNickPowell,
						"Drawing conclusions from data",
						comp1cred10.getId(),
						"http://www.chem1.com/acad/webtext/matmeasure/mm5.html");

				createActivity(org.getId(),
						userNickPowell,
						"Understanding the units of scientific measurement",
						comp1cred10.getId(),
						"http://www.chem1.com/acad/webtext/matmeasure/mm1.html");

				publishCredential(cred10, cred10.getCreatedBy());
			} catch (Exception ex) {
				logger.error(ex);
			}


			///////////////////////////////////////
			// CREDENTIAL Understanding Descriptive Statistics
			///////////////////////////////////////
			Credential1 cred11 = createCredential(org.getId(),
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
					"Teaching Prediction Intervals");
			
			publishCredential(cred11, cred11.getCreatedBy());
		} catch (Exception ex) {
			logger.error("Error", ex);
		}
 	}
	
	private void publishCredential(Credential1 cred, User creator) throws DbConnectionException, StaleDataException {
		//we no longer have published status for credential
//		//to make sure that all documents will be indexed in ES before we try to update them.
//		try {
//			Thread.sleep(1500);
//		} catch (InterruptedException e) {
//			logger.error(e);
//		}
//		CredentialManager credentialManager = ServiceLocator
//				.getInstance()
//				.getService(CredentialManager.class);
//		
//		RestrictedAccessResult<CredentialData> res = credentialManager.getCredentialData(cred.getId(), false, 
//				true, creator.getId(), ResourceAccessRequirements.of(AccessMode.MANAGER));
//		CredentialData credentialData = res.getResource();
//		if(credentialData != null) {
//			//credentialData.setPublished(true);
//			credentialManager.updateCredential(credentialData, creator.getId(), null);
//		}
	}

	private void addCompetenceToCredential(long orgId, Credential1 credential, Competence1 competence, User user) {
		EventQueue ev = ServiceLocator
				.getInstance()
				.getService(CredentialManager.class).addCompetenceToCredential(credential.getId(), competence, 
						UserContextData.ofOrganization(orgId));
		try {
			if(ev != null) {
				ServiceLocator.getInstance().getService(EventFactory.class).generateEvents(ev);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	private User createUser(long organizationId, String name, String lastname, String emailAddress, String password, String fictitiousUser,
		String avatar, Role roleUser) {
		User newUser = ServiceLocator
				.getInstance()
				.getService(UserManager.class)
				.createNewUser(organizationId, name, lastname, emailAddress,
						true, password, fictitiousUser, getAvatarInputStream(avatar), avatar, null);

		newUser = ServiceLocator
				.getInstance()
				.getService(RoleManager.class)
				.assignRoleToUser(roleUser, newUser.getId());

		return newUser;
	}

	private Activity1 createActivity(long orgId, User userNickPowell, String title, long compId, String... links)
		throws DbConnectionException, IllegalDataStateException {
		ActivityData actData = new ActivityData(false);
		actData.setTitle(title);
		actData.setActivityType(ActivityType.TEXT);
		actData.setType(LearningResourceType.UNIVERSITY_CREATED);
		actData.setCompetenceId(compId);
		
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
						actData, UserContextData.of(userNickPowell.getId(), orgId, null, null));
		return act;
	}

	private Credential1 createCredential(long orgId, String title, String description, User userNickPowell, String tags) {
		CredentialData credentialData = new CredentialData(false);
		credentialData.setTitle(title);
		credentialData.setDescription(description);
		credentialData.setTagsString(tags);
		
		Credential1 credNP1 = ServiceLocator
				.getInstance()
				.getService(CredentialManager.class)
				.saveNewCredential(credentialData, UserContextData.of(userNickPowell.getId(), orgId, null, null));
		
		return credNP1;
	}

	public Competence1 createCompetence(long orgId, User user, String title, String description, long credentialId, String tags) {
		
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
							compData, credentialId, UserContextData.of(user.getId(), orgId, null, null));
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
