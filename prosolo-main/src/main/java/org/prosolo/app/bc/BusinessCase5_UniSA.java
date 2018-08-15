package org.prosolo.app.bc;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.admin.BulkDataAdministrationService;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricLevelData;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author Nikola Milikic
 * @date 2018-04-25
 * @since 1.2
 */
@Service
public class BusinessCase5_UniSA {

    private static Logger logger = Logger.getLogger(BusinessCase5_UniSA.class.getName());

	public void initRepository() {
        logger.info("Initializing business case 5 - UniSA data");

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

		User userNickPowell = extractResultAndAddEvents(events, createUser(0,"Nick", "Powell", "nick.powell@gmail.com", genericPassword, "Teacher", "male1.png", roleUser));

		//generate event after roles are updated
		Map<String, String> params = null;
		events.appendEvent(ServiceLocator.getInstance().getService(EventFactory.class).generateEventData(
				EventType.USER_ROLES_UPDATED, UserContextData.ofActor(userNickPowell.getId()),
				userNickPowell, null, null, params));

		//create organization
		LearningStageData graduateLearningStage = new LearningStageData(false);
		graduateLearningStage.setTitle("Graduate");
		graduateLearningStage.setOrder(1);
		graduateLearningStage.setStatus(ObjectStatus.CREATED);	// this needs to be set in order for the stage to be created in the method createNewOrganizationAndGetEvents

		OrganizationData orgData = new OrganizationData();
		orgData.setTitle("University of South Australia");
		orgData.setAdmins(Collections.singletonList(new UserData(userNickPowell)));
		orgData.addLearningStage(graduateLearningStage);

		Organization org = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(OrganizationManager.class)
				.createNewOrganizationAndGetEvents(orgData, UserContextData.empty()));

		userNickPowell.setOrganization(org);

		// create org. unit School of Education
		Unit unit1 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
				.createNewUnitAndGetEvents("School of Education", org.getId(), 0, createUserContext(userNickPowell)));

		// create org. unit School of Nursing and Midwifery
		Unit unit2 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
				.createNewUnitAndGetEvents("School of Nursing and Midwifery", org.getId(), 0, createUserContext(userNickPowell)));

		// create 20 users
		User userKevinMitchell = extractResultAndAddEvents(events, createUser(org.getId(), "Kevin", "Mitchell", "kevin.mitchell@gmail.com", genericPassword, "Student", "male3.png", roleUser));
		User userPaulEdwards = extractResultAndAddEvents(events, createUser(org.getId(), "Paul", "Edwards", "paul.edwards@gmail.com", genericPassword, "Student", "male4.png", roleUser));
		User userGeorgeYoung = extractResultAndAddEvents(events, createUser(org.getId(), "George", "Young", "george.young@gmail.com", genericPassword, "Student", "male6.png", roleUser));
		User userRichardAnderson = extractResultAndAddEvents(events, createUser(org.getId(), "Richard", "Anderson", "richard.anderson@gmail.com", genericPassword, "Student", "male2.png", roleUser));
		User userStevenTurner = extractResultAndAddEvents(events, createUser(org.getId(), "Steven", "Turner", "steven.turner@gmail.com", genericPassword, "Student", "male5.png", roleUser));
		User userJosephGarcia = extractResultAndAddEvents(events, createUser(org.getId(), "Joseph", "Garcia", "joseph.garcia@gmail.com", genericPassword, "Student", "male8.png", roleUser));
		User userTimothyRivera = extractResultAndAddEvents(events, createUser(org.getId(), "Timothy", "Rivera", "timothy.rivera@gmail.com", genericPassword, "Student", "male9.png", roleUser));
		User userKevinHall = extractResultAndAddEvents(events, createUser(org.getId(), "Kevin", "Hall", "kevin.hall@gmail.com", genericPassword, "Student", "male10.png", roleUser));
		User userKennethCarter = extractResultAndAddEvents(events, createUser(org.getId(), "Kenneth", "Carter", "kenneth.carter@gmail.com", genericPassword, "Student", "male11.png", roleUser));
		User userAnthonyMoore = extractResultAndAddEvents(events, createUser(org.getId(), "Anthony", "Moore", "anthony.moore@gmail.com", genericPassword, "Student", "male12.png", roleUser));
		User userAkikoKido = extractResultAndAddEvents(events, createUser(org.getId(), "Akiko", "Kido", "akiko.kido@gmail.com", genericPassword, "Student", "female7.png", roleUser));
		User userTaniaCortese = extractResultAndAddEvents(events, createUser(org.getId(), "Tania", "Cortese", "tania.cortese@gmail.com", genericPassword, "Student", "female1.png", roleUser));
		User userSonyaElston = extractResultAndAddEvents(events, createUser(org.getId(), "Sonya", "Elston", "sonya.elston@gmail.com", genericPassword, "Student", "female2.png", roleUser));
		User userLoriAbner = extractResultAndAddEvents(events, createUser(org.getId(), "Lori", "Abner", "lori.abner@gmail.com", genericPassword, "Student", "female3.png", roleUser));
		User userSamanthaDell = extractResultAndAddEvents(events, createUser(org.getId(), "Samantha", "Dell", "samantha.dell@gmail.com", genericPassword, "Student", "female4.png", roleUser));
		User userSheriLaureano = extractResultAndAddEvents(events, createUser(org.getId(), "Sheri", "Laureano", "sheri.laureano@gmail.com", genericPassword, "Student", "female14.png", roleUser));
		User userAngelicaFallon = extractResultAndAddEvents(events, createUser(org.getId(), "Angelica", "Fallon", "angelica.fallon@gmail.com", genericPassword, "Student", "female16.png", roleUser));
		User userIdaFritz = extractResultAndAddEvents(events, createUser(org.getId(), "Ida", "Fritz", "ida.fritz@gmail.com", genericPassword, "Student", "female17.png", roleUser));
		User userRachelWiggins = extractResultAndAddEvents(events, createUser(org.getId(), "Rachel", "Wiggins", "rachel.wiggins@gmail.com", genericPassword, "Student", "female20.png", roleUser));
		User userHelenCampbell = extractResultAndAddEvents(events, createUser(org.getId(), "Helen", "Campbell", "helen.campbell@gmail.com", genericPassword, "Student", "female13.png", roleUser));

		// create 4 instructors
		User userPhillArmstrong = extractResultAndAddEvents(events, createUser(org.getId(), "Phill", "Armstrong", "phill.armstrong@gmail.com", genericPassword, "Instructor", "male7.png", roleInstructor));
		User userKarenWhite = extractResultAndAddEvents(events, createUser(org.getId(), "Karen", "White", "karen.white@gmail.com", genericPassword, "Instructor", "female10.png", roleInstructor));
		User userAnnaHallowell = extractResultAndAddEvents(events, createUser(org.getId(), "Anna", "Hallowell", "anna.hallowell@gmail.com", genericPassword, "Instructor", "female11.png", roleInstructor));
		User userErikaAmes = extractResultAndAddEvents(events, createUser(org.getId(), "Erika", "Ames", "erika.ames@gmail.com", genericPassword, "Instructor", "female12.png", roleInstructor));


		// Adding roles to users

		// Nick Powell is Manager, Admin and Super Admin
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userNickPowell.getId());
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleAdmin, userNickPowell.getId());
		userNickPowell = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleSuperAdmin, userNickPowell.getId());

		// Karen White is Manager and Instructor (already set when user is defined)
		userKarenWhite = ServiceLocator.getInstance().getService(RoleManager.class).assignRoleToUser(roleManager, userKarenWhite.getId());
		events.appendEvent(ServiceLocator.getInstance().getService(EventFactory.class).generateEventData(
				EventType.Edit_Profile, createUserContext(userKarenWhite), userKarenWhite, null, null, null));

		// Phill Armstrong is Instructor (already set when user is defined)
		// Anna Hallowell is Instructor (already set when user is defined)
		// Erika Ames is Instructor (already set when user is defined)


		// adding managers to the unit School of Education
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userNickPowell.getId(), unit1.getId(), roleManager.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKarenWhite.getId(), unit1.getId(), roleManager.getId(), createUserContext(userKarenWhite)));

		// adding instructors to the unit School of Education
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKarenWhite.getId(), unit1.getId(), roleInstructor.getId(), createUserContext(userKarenWhite)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userPhillArmstrong.getId(), unit1.getId(), roleInstructor.getId(), createUserContext(userPhillArmstrong)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAnnaHallowell.getId(), unit1.getId(), roleInstructor.getId(), createUserContext(userAnnaHallowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userErikaAmes.getId(), unit1.getId(), roleInstructor.getId(), createUserContext(userErikaAmes)));

		// adding students to the unit School of Education
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userHelenCampbell.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userRichardAnderson.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userStevenTurner.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userJosephGarcia.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userTimothyRivera.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKevinHall.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKennethCarter.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userHelenCampbell.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAnthonyMoore.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userTaniaCortese.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSonyaElston.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userLoriAbner.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSamanthaDell.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSheriLaureano.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAngelicaFallon.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userIdaFritz.getId(), unit1.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));

		// adding students to the unit School of Nursing and Midwifery
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userPaulEdwards.getId(), unit2.getId(), roleUser.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKevinMitchell.getId(), unit2.getId(), roleUser.getId(), createUserContext(userKevinMitchell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userGeorgeYoung.getId(), unit2.getId(), roleUser.getId(), createUserContext(userGeorgeYoung)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userRachelWiggins.getId(), unit2.getId(), roleUser.getId(), createUserContext(userAkikoKido)));

		// adding follow relations
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userLoriAbner.getId(), UserContextData.of(userPaulEdwards.getId(), org.getId(), null, null)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userSamanthaDell.getId(), UserContextData.of(userPaulEdwards.getId(), org.getId(), null, null)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userRachelWiggins.getId(), UserContextData.of(userPaulEdwards.getId(), org.getId(), null, null)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), UserContextData.of(userTimothyRivera.getId(), org.getId(), null, null)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), UserContextData.of(userKevinMitchell.getId(), org.getId(), null, null)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), UserContextData.of(userGeorgeYoung.getId(), org.getId(), null, null)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), UserContextData.of(userRachelWiggins.getId(), org.getId(), null, null)));


		/*
		 * END CREATING USERS
		 */



		// ////////////////////////////
		// CREATING STANDARDS
		// ///////////////////////////////
		Rubric rubric = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(RubricManager.class).createNewRubricAndGetEvents("Standard Performance Assessment Rubric", createUserContext(userNickPowell)));
		RubricData rubricData = ServiceLocator.getInstance().getService(RubricManager.class).getRubricData(rubric.getId(), true, true, 0, true, true);
		rubricData.setRubricType(RubricType.DESCRIPTIVE);
		rubricData.setReadyToUse(true);

		RubricCriterionData criterion = new RubricCriterionData(ObjectStatus.CREATED);
		criterion.setName("Performance");
		criterion.setOrder(1);
		rubricData.addNewCriterion(criterion);

		RubricLevelData level1 = new RubricLevelData(ObjectStatus.CREATED);
		level1.setName("Outstanding Performance");
		level1.setOrder(1);
		rubricData.addNewLevel(level1);

		RubricLevelData level2 = new RubricLevelData(ObjectStatus.CREATED);
		level2.setName("Meet the Standard at Enhanced Level");
		level2.setOrder(2);
		rubricData.addNewLevel(level2);

		RubricLevelData level3 = new RubricLevelData(ObjectStatus.CREATED);
		level3.setName("Meet the Standard at Threshold Level");
		level3.setOrder(3);
		rubricData.addNewLevel(level3);

		RubricLevelData level4 = new RubricLevelData(ObjectStatus.CREATED);
		level4.setName("Working Towards Graduate Level");
		level4.setOrder(4);
		rubricData.addNewLevel(level4);

		try {
			ServiceLocator.getInstance().getService(RubricManager.class).saveRubricCriteriaAndLevels(rubricData, EditMode.FULL);
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard1 = createCredential(events,
				"Standard 1 - Know students and how they learn",
				"Know students and how they learn.",
				userNickPowell,
				"characteristics of students, learning needs",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"1.1 Physical, social and intellectual development and characteristics of students",
					"Demonstrate knowledge and understanding of physical, social and intellectual development and characteristics of students and how these may affect learning.",
					standard1.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"1.2 Understand how students learn",
					"Demonstrate knowledge and understanding of research into how students learn and the implications for teaching.",
					standard1.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"1.3 Students with diverse linguistic, cultural, religious and socioeconomic backgrounds",
					"Demonstrate knowledge of teaching strategies that are responsive to the learning strengths and needs of students from diverse linguistic, cultural, religious and socioeconomic backgrounds.",
					standard1.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"1.4 Strategies for teaching Aboriginal and Torres Strait Islander students",
					"Demonstrate broad knowledge and understanding of the impact of culture, cultural identity and linguistic background on the education of students from Aboriginal and Torres Strait Islander backgrounds.",
					standard1.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"1.5 Differentiate teaching to meet the specific learning needs of students across the full range of abilities",
					"Demonstrate knowledge and understanding of strategies for differentiating teaching to meet the specific learning needs of students across the full range of abilities.",
					standard1.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"1.6 Strategies to support full participation of students with disability",
					"Demonstrate broad knowledge and understanding of legislative requirements and teaching strategies that support participation and learning of students with disability.",
					standard1.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard2 = createCredential(events,
				"Standard 2 - Know the content and how to teach it",
				"Know the content and how to teach it.",
				userNickPowell,
				"learning content, teaching strategies",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"2.1 Content and teaching strategies of the teaching area",
					"Demonstrate knowledge and understanding of the concepts, substance and structure of the content and teaching strategies of the teaching area.",
					standard2.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"2.2 Content selection and organisation",
					"Organise content into an effective learning and teaching sequence.",
					standard2.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"2.3 Curriculum, assessment and reporting",
					"Use curriculum, assessment and reporting knowledge to design learning sequences and lesson plans.",
					standard2.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"2.4 Understand and respect Aboriginal and Torres Strait Islander people to promote reconciliation between Indigenous and non-Indigenous Australians",
					"Demonstrate broad knowledge of, understanding of and respect for Aboriginal and Torres Strait Islander histories, cultures and languages.",
					standard2.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"2.5 Literacy and numeracy strategies",
					"Know and understand literacy and numeracy teaching strategies and their application in teaching areas.",
					standard2.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"2.6 Information and Communication Technology (ICT)",
					"Implement teaching strategies for using ICT to expand curriculum learning opportunities for students.",
					standard2.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard3 = createCredential(events,
				"Standard 3 - Plan for and implement effective teaching and learning",
				"Plan for and implement effective teaching and learning.",
				userNickPowell,
				"teaching strategies, effective learning, learning goals",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"3.1 Establish challenging learning goals",
					"Set learning goals that provide achievable challenges for students of varying abilities and characteristics.",
					standard3.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"3.2 Plan, structure and sequence learning programs",
					"Plan lesson sequences using knowledge of student learning, content and effective teaching strategies.",
					standard3.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"3.3 Use teaching strategies",
					"Include a range of teaching strategies.",
					standard3.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"3.4 Select and use resources",
					"Demonstrate knowledge of a range of resources, including ICT, that engage students in their learning.",
					standard3.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"3.5 Use effective classroom communication",
					"Demonstrate a range of verbal and non-verbal communication strategies to support student engagement.",
					standard3.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"3.6 Evaluate and improve teaching programs",
					"Demonstrate broad knowledge of strategies that can be used to evaluate teaching programs to improve student learning.",
					standard3.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"3.7 Engage parents/carers in the educative process",
					"Describe a broad range of strategies for involving parents/carers in the educative process.",
					standard3.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard4 = createCredential(events,
				"Standard 4 - Create and maintain supportive and safe learning environments",
				"Create and maintain supportive and safe learning environments.",
				userNickPowell,
				"student participation, classroom activities, challenging behaviour, student safety, safe learning environment",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"4.1 Support student participation",
					"Identify strategies to support inclusive student participation and engagement in classroom activities.",
					standard4.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"4.2 Manage classroom activities",
					"Demonstrate the capacity to organise classroom activities and provide clear directions.",
					standard4.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"4.3 Manage challenging behaviour",
					"Demonstrate knowledge of practical approaches to manage challenging behaviour.",
					standard4.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"4.4 Maintain student safety",
					"Describe strategies that support students’ well-being and safety working within school and/or system, curriculum and legislative requirements.",
					standard4.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"4.5 Use ICT safely, responsibly and ethically",
					"Demonstrate an understanding of the relevant issues and the strategies available to support the safe, responsible and ethical use of ICT in learning and teaching.",
					standard4.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard5 = createCredential(events,
				"Standard 5 - Assess, provide feedback and report on student learning",
				"Assess, provide feedback and report on student learning.",
				userNickPowell,
				"assessment, feedback reporting, student achievement",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"5.1 Assess student learning",
					"Demonstrate understanding of assessment strategies, including informal and formal, diagnostic, formative and summative approaches to assess student learning.",
					standard5.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"5.2 Provide feedback to students on their learning",
					"Demonstrate an understanding of the purpose of providing timely and appropriate feedback to students about their learning.",
					standard5.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"5.3 Make consistent and comparable judgements",
					"Demonstrate understanding of assessment moderation and its application to support consistent and comparable judgements of student learning.",
					standard5.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"5.4 Interpret student data",
					"Demonstrate the capacity to interpret student assessment data to evaluate student learning and modify teaching practice.",
					standard5.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"5.5 Report on student achievement",
					"Demonstrate understanding of a range of strategies for reporting to students and parents/carers and the purpose of keeping accurate and reliable records of student achievement.",
					standard5.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard6 = createCredential(events,
				"Standard 6 - Engage in professional learning",
				"Engage in professional learning.",
				userNickPowell,
				"professional learning, teaching practice",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"6.1 Identify and plan professional learning needs",
					"Demonstrate an understanding of the role of the Australian Professional Standards for Teachers in identifying professional learning needs.",
					standard6.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"6.2 Engage in professional learning and improve practice",
					"Understand the relevant and appropriate sources of professional learning for teachers.",
					standard6.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"6.3 Engage with colleagues and improve practice",
					"Seek and apply constructive feedback from supervisors and teachers to improve teaching practices.",
					standard6.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"6.4 Apply professional learning and improve student learning",
					"Demonstrate an understanding of the rationale for continued professional learning and the implications for improved student learning.",
					standard6.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

		Credential1 standard7 = createCredential(events,
				"Standard 7 - Engage professionally with colleagues, parents/carers and the community",
				"Engage professionally with colleagues, parents/carers and the community.",
				userNickPowell,
				"professional ethics, policies, professional teaching networks",
				rubricData.getId(),
				graduateLearningStage);

		try {
			createCompetence(events,
					userNickPowell,
					"7.1 Meet professional ethics and responsibilities",
					"Understand and apply the key principles described in codes of ethics and conduct for the teaching profession.",
					standard7.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"7.2 Comply with legislative, administrative and organisational requirements",
					"Understand the relevant legislative, administrative and organisational policies and processes required for teachers according to school stage.",
					standard7.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"7.3 Engage with the parents/carers",
					"Understand strategies for working effectively, sensitively and confidentially with parents/carers.",
					standard7.getId(),
					rubricData.getId());

			createCompetence(events,
					userNickPowell,
					"7.4 Engage with professional teaching networks and broader communities",
					"Understand the role of external professionals and community representatives in broadening teachers’ professional knowledge and practice.",
					standard7.getId(),
					rubricData.getId());
		} catch (Exception ex) {
			logger.error(ex);
		}

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

	private Result<User> createUser(long orgId, String name, String lastname, String emailAddress, String password, String position,
									String avatar, Role roleUser) {
		try {
			return ServiceLocator
					.getInstance()
					.getService(UserManager.class)
					.createNewUserAndGetEvents(orgId, name, lastname, emailAddress,
							true, password, position, getAvatarInputStream(avatar), avatar, Collections.singletonList(roleUser.getId()),false);
		} catch (IllegalDataStateException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Credential1 createCredential(EventQueue events, String title, String description, User user, String tags, long rubricId, LearningStageData learningStage) {
		CredentialData credentialData = new CredentialData(false);
		credentialData.setTitle(title);
		credentialData.setDescription(description);
		credentialData.setTagsString(tags);
		credentialData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
		credentialData.getAssessmentSettings().setRubricId(rubricId);
		credentialData.setLearningStage(learningStage);
		credentialData.setAssessorAssignment(CredentialData.AssessorAssignmentMethodData.AUTOMATIC);

		AssessmentTypeConfig instructorAssessment = new AssessmentTypeConfig(-1, AssessmentType.INSTRUCTOR_ASSESSMENT, true, true);
		AssessmentTypeConfig peerAssessment = new AssessmentTypeConfig(-1, AssessmentType.PEER_ASSESSMENT, true, false);
		AssessmentTypeConfig selfAssessment = new AssessmentTypeConfig(-1, AssessmentType.SELF_ASSESSMENT, true, false);
		credentialData.setAssessmentTypes(Arrays.asList(instructorAssessment, peerAssessment, selfAssessment));

		return extractResultAndAddEvents(events, ServiceLocator
				.getInstance()
				.getService(CredentialManager.class)
				.saveNewCredentialAndGetEvents(credentialData, createUserContext(user)));
	}

	private void createCompetence(EventQueue events, User user, String title, String description, long credentialId, long rubricId) {
		CompetenceData1 compData = new CompetenceData1(false);
		compData.setTitle(title);
		compData.setDescription(description);
		compData.setPublished(false);
		compData.setType(LearningResourceType.UNIVERSITY_CREATED);
		compData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
		compData.getAssessmentSettings().setRubricId(rubricId);
		compData.setLearningPathType(LearningPathType.EVIDENCE);

		AssessmentTypeConfig instructorAssessment = new AssessmentTypeConfig(-1, AssessmentType.INSTRUCTOR_ASSESSMENT, true, true);
		AssessmentTypeConfig peerAssessment = new AssessmentTypeConfig(-1, AssessmentType.PEER_ASSESSMENT, true, false);
		AssessmentTypeConfig selfAssessment = new AssessmentTypeConfig(-1, AssessmentType.SELF_ASSESSMENT, true, false);
		compData.setAssessmentTypes(Arrays.asList(instructorAssessment, peerAssessment, selfAssessment));

		try {
			extractResultAndAddEvents(events, ServiceLocator
					.getInstance()
					.getService(Competence1Manager.class)
					.saveNewCompetenceAndGetEvents(
							compData, credentialId, createUserContext(user)));
		} catch (DbConnectionException | IllegalDataStateException e) {
			logger.error(e);
		}
	}

	private InputStream getAvatarInputStream(String avatarName) {
		URL url = Thread.currentThread().getContextClassLoader()
				.getResource("test_avatars120x120/" + avatarName);

		try {
			return new FileInputStream(new File(url.getFile()));
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		return null;
	}

}
