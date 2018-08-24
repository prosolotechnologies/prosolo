package org.prosolo.app.bc;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.observer.processor.UnitWelcomePostSocialActivityProcessor;
import org.prosolo.services.admin.BulkDataAdministrationService;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.htmlparser.LinkParser;
import org.prosolo.services.htmlparser.LinkParserFactory;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricLevelData;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.util.HTMLUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

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

		// fetch roles
		Role roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.USER);
		Role roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.MANAGER);
		Role roleInstructor = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.INSTRUCTOR);
		Role roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.ADMIN);
		Role roleSuperAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.SUPER_ADMIN);

		///////////////////////
		// Create users
		///////////////////////
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
		orgData.setAdmins(Collections.singletonList(new UserData(userNickPowell)));


		Organization org = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(OrganizationManager.class)
				.createNewOrganizationAndGetEvents(orgData, UserContextData.empty()));

		// create learning stage
		LearningStageData graduateLearningStage = new LearningStageData(false);
		graduateLearningStage.setTitle("Graduate");
		graduateLearningStage.setOrder(1);
		graduateLearningStage.setStatus(ObjectStatus.CREATED);	// this needs to be set in order for the stage to be created in the method createNewOrganizationAndGetEvents
		orgData.setId(org.getId());
		orgData.setLearningInStagesEnabled(true);
		orgData.addLearningStage(graduateLearningStage);

		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(OrganizationManager.class)
				.updateOrganizationAndGetEvents(orgData, UserContextData.empty()));


		// load learning stage from db in order to obtain its id
		graduateLearningStage = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationLearningStagesForLearningResource(org.getId()).get(0).getLearningStage();

		userNickPowell.setOrganization(org);

		// create org. unit School of Education
		Unit unitSchoolOfEducation = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
				.createNewUnitAndGetEvents("School of Education", org.getId(), 0, createUserContext(userNickPowell)));

		// create org. unit School of Nursing and Midwifery
		Unit unitOfNursingAndMidwifery = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
				.createNewUnitAndGetEvents("School of Nursing and Midwifery", org.getId(), 0, createUserContext(userNickPowell)));

		// explicitly generate welcome post social activity at this point to have the earliest timestamp
		Session session1 = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
		try {
			ServiceLocator.getInstance().getService(SocialActivityManager.class).saveUnitWelcomePostSocialActivityIfNotExists(unitSchoolOfEducation.getId(), session1);
			ServiceLocator.getInstance().getService(SocialActivityManager.class).saveUnitWelcomePostSocialActivityIfNotExists(unitOfNursingAndMidwifery.getId(), session1);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
		} finally {
			HibernateUtil.close(session1);
		}

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
		User userPhilArmstrong = extractResultAndAddEvents(events, createUser(org.getId(), "Phil", "Armstrong", "phil.armstrong@gmail.com", genericPassword, "Instructor", "male7.png", roleInstructor));
		User userKarenWhite = extractResultAndAddEvents(events, createUser(org.getId(), "Karen", "White", "karen.white@gmail.com", genericPassword, "Instructor", "female10.png", roleInstructor));
		User userAnnaHallowell = extractResultAndAddEvents(events, createUser(org.getId(), "Anna", "Hallowell", "anna.hallowell@gmail.com", genericPassword, "Instructor", "female11.png", roleInstructor));
		User userErikaAmes = extractResultAndAddEvents(events, createUser(org.getId(), "Erika", "Ames", "erika.ames@gmail.com", genericPassword, "Instructor", "female12.png", roleInstructor));


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


		// adding managers to the unit School of Education
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userNickPowell.getId(), unitSchoolOfEducation.getId(), roleManager.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKarenWhite.getId(), unitSchoolOfEducation.getId(), roleManager.getId(), createUserContext(userKarenWhite)));

		// adding instructors to the unit School of Education
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKarenWhite.getId(), unitSchoolOfEducation.getId(), roleInstructor.getId(), createUserContext(userKarenWhite)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userPhilArmstrong.getId(), unitSchoolOfEducation.getId(), roleInstructor.getId(), createUserContext(userPhilArmstrong)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAnnaHallowell.getId(), unitSchoolOfEducation.getId(), roleInstructor.getId(), createUserContext(userAnnaHallowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userErikaAmes.getId(), unitSchoolOfEducation.getId(), roleInstructor.getId(), createUserContext(userErikaAmes)));

		// list of all instructors from the School od Education
		List<User> schoolOfEducationInstructors = Arrays.asList(userKarenWhite, userPhilArmstrong, userAnnaHallowell, userErikaAmes);

		// adding students to the unit School of Education
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userHelenCampbell.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userRichardAnderson.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userStevenTurner.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userJosephGarcia.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userTimothyRivera.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKevinHall.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKennethCarter.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAnthonyMoore.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userTaniaCortese.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSonyaElston.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userLoriAbner.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSamanthaDell.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userSheriLaureano.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAngelicaFallon.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userIdaFritz.getId(), unitSchoolOfEducation.getId(), roleUser.getId(), createUserContext(userHelenCampbell)));

		// list of all students from the School od Education
		List<User> schoolOfEducationStudents = Arrays.asList(userHelenCampbell, userRichardAnderson, userStevenTurner, userJosephGarcia, userTimothyRivera, userKevinHall, userKennethCarter, userHelenCampbell, userAnthonyMoore,
				userTaniaCortese, userSonyaElston, userLoriAbner, userSamanthaDell, userSheriLaureano, userAngelicaFallon, userIdaFritz);

		// adding students to the unit School of Nursing and Midwifery
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userPaulEdwards.getId(), unitOfNursingAndMidwifery.getId(), roleUser.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userKevinMitchell.getId(), unitOfNursingAndMidwifery.getId(), roleUser.getId(), createUserContext(userKevinMitchell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userGeorgeYoung.getId(), unitOfNursingAndMidwifery.getId(), roleUser.getId(), createUserContext(userGeorgeYoung)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userRachelWiggins.getId(), unitOfNursingAndMidwifery.getId(), roleUser.getId(), createUserContext(userRachelWiggins)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(userAkikoKido.getId(), unitOfNursingAndMidwifery.getId(), roleUser.getId(), createUserContext(userAkikoKido)));

		// list of all students from the School of Nursing and Midwifery
		List<User> schoolOfNursingStudents = Arrays.asList(userPaulEdwards, userKevinMitchell, userGeorgeYoung, userRachelWiggins);

		////////////////////////////////
		// Add follow relations
		////////////////////////////////
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userLoriAbner.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userSamanthaDell.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userRachelWiggins.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userTimothyRivera)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userKevinMitchell) ));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userGeorgeYoung)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userRachelWiggins)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userStevenTurner.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userJosephGarcia.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userLoriAbner.getId(), createUserContext(userHelenCampbell)));

		///////////////////////
		// Create unit groups
		///////////////////////

		// create unit Arts Education Students
		UserGroup userGroupArtsEducationStudents = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).saveNewGroupAndGetEvents(unitSchoolOfEducation.getId(), "Arts Education Students", false, createUserContext(userNickPowell)));

		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userHelenCampbell.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userRichardAnderson.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userStevenTurner.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userJosephGarcia.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userTimothyRivera.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userKevinHall.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userKennethCarter.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userAnthonyMoore.getId(), createUserContext(userNickPowell)));

		// create unit Science Education Students
		UserGroup userGroupScienceEducationStudents = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).saveNewGroupAndGetEvents(unitSchoolOfEducation.getId(), "Science Education Students", false, createUserContext(userNickPowell)));

		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userTaniaCortese.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userSonyaElston.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userLoriAbner.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userSamanthaDell.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userSheriLaureano.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userAngelicaFallon.getId(), createUserContext(userNickPowell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userIdaFritz.getId(), createUserContext(userNickPowell)));


		// ////////////////////////////
		// Create Standards
		// ////////////////////////////

		// create rubric to be used for all standards and focus areas
		RubricData rubricData = createRubric(events, userNickPowell);

		try {
			ServiceLocator.getInstance().getService(RubricManager.class).saveRubricCriteriaAndLevels(rubricData, EditMode.FULL);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
		}

		Credential1 standard1 = createStandard7(events,
				"Standard 1 - Know students and how they learn",
				"Know students and how they learn.",
				userNickPowell,
				"characteristics of students, learning needs",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "1.1 Physical, social and intellectual development and characteristics of students",
						"Demonstrate knowledge and understanding of physical, social and intellectual development and characteristics of students and how these may affect learning." },
				new String[]{ "1.2 Understand how students learn",
						"Demonstrate knowledge and understanding of research into how students learn and the implications for teaching." },
				new String[]{ "1.3 Students with diverse linguistic, cultural, religious and socioeconomic backgrounds",
						"Demonstrate knowledge of teaching strategies that are responsive to the learning strengths and needs of students from diverse linguistic, cultural, religious and socioeconomic backgrounds." },
				new String[]{ "1.4 Strategies for teaching Aboriginal and Torres Strait Islander students",
						"Demonstrate broad knowledge and understanding of the impact of culture, cultural identity and linguistic background on the education of students from Aboriginal and Torres Strait Islander backgrounds." },
				new String[]{ "1.5 Differentiate teaching to meet the specific learning needs of students across the full range of abilities",
						"Demonstrate knowledge and understanding of strategies for differentiating teaching to meet the specific learning needs of students across the full range of abilities." },
				new String[]{ "1.6 Strategies to support full participation of students with disability",
						"Demonstrate broad knowledge and understanding of legislative requirements and teaching strategies that support participation and learning of students with disability." });

		Credential1 standard2 = createStandard7(events,
				"Standard 2 - Know the content and how to teach it",
				"Know the content and how to teach it.",
				userNickPowell,
				"learning content, teaching strategies",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "2.1 Content and teaching strategies of the teaching area",
						"Demonstrate knowledge and understanding of the concepts, substance and structure of the content and teaching strategies of the teaching area." },
				new String[]{ "2.2 Content selection and organisation",
						"Organise content into an effective learning and teaching sequence." },
				new String[]{ "2.3 Curriculum, assessment and reporting",
						"Use curriculum, assessment and reporting knowledge to design learning sequences and lesson plans." },
				new String[]{ "2.4 Understand and respect Aboriginal and Torres Strait Islander people to promote reconciliation between Indigenous and non-Indigenous Australians",
						"Demonstrate broad knowledge of, understanding of and respect for Aboriginal and Torres Strait Islander histories, cultures and languages." },
				new String[]{ "2.5 Literacy and numeracy strategies",
						"Know and understand literacy and numeracy teaching strategies and their application in teaching areas." },
				new String[]{ "2.6 Information and Communication Technology (ICT)",
						"Implement teaching strategies for using ICT to expand curriculum learning opportunities for students." });

		Credential1 standard3 = createStandard7(events,
				"Standard 3 - Plan for and implement effective teaching and learning",
				"Plan for and implement effective teaching and learning.",
				userNickPowell,
				"teaching strategies, effective learning, learning goals",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "3.1 Establish challenging learning goals",
						"Set learning goals that provide achievable challenges for students of varying abilities and characteristics." },
				new String[]{ "3.2 Plan, structure and sequence learning programs",
						"Plan lesson sequences using knowledge of student learning, content and effective teaching strategies." },
				new String[]{ "3.3 Use teaching strategies",
						"Include a range of teaching strategies." },
				new String[]{ "3.4 Select and use resources",
						"Demonstrate knowledge of a range of resources, including ICT, that engage students in their learning." },
				new String[]{ "3.5 Use effective classroom communication",
						"Demonstrate a range of verbal and non-verbal communication strategies to support student engagement." },
				new String[]{ "3.6 Evaluate and improve teaching programs",
						"Demonstrate broad knowledge of strategies that can be used to evaluate teaching programs to improve student learning." },
				new String[]{ "3.7 Engage parents/carers in the educative process",
						"Describe a broad range of strategies for involving parents/carers in the educative process." });

		Credential1 standard4 = createStandard7(events,
				"Standard 4 - Create and maintain supportive and safe learning environments",
				"Create and maintain supportive and safe learning environments.",
				userNickPowell,
				"student participation, classroom activities, challenging behaviour, student safety, safe learning environment",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "4.1 Support student participation",
						"Identify strategies to support inclusive student participation and engagement in classroom activities." },
				new String[]{ "4.2 Manage classroom activities",
						"Demonstrate the capacity to organise classroom activities and provide clear directions." },
				new String[]{ "4.3 Manage challenging behaviour",
						"Demonstrate knowledge of practical approaches to manage challenging behaviour." },
				new String[]{ "4.4 Maintain student safety",
						"Describe strategies that support students’ well-being and safety working within school and/or system, curriculum and legislative requirements." },
				new String[]{ "4.5 Use ICT safely, responsibly and ethically",
						"Demonstrate an understanding of the relevant issues and the strategies available to support the safe, responsible and ethical use of ICT in learning and teaching." });

		Credential1 standard5 = createStandard7(events,
				"Standard 5 - Assess, provide feedback and report on student learning",
				"Assess, provide feedback and report on student learning.",
				userNickPowell,
				"assessment, feedback reporting, student achievement",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "5.1 Assess student learning",
						"Demonstrate understanding of assessment strategies, including informal and formal, diagnostic, formative and summative approaches to assess student learning." },
				new String[]{ "5.2 Provide feedback to students on their learning",
						"Demonstrate an understanding of the purpose of providing timely and appropriate feedback to students about their learning." },
				new String[]{ "5.3 Make consistent and comparable judgements",
						"Demonstrate understanding of assessment moderation and its application to support consistent and comparable judgements of student learning." },
				new String[]{ "5.4 Interpret student data",
						"Demonstrate the capacity to interpret student assessment data to evaluate student learning and modify teaching practice." },
				new String[]{ "5.5 Report on student achievement",
						"Demonstrate understanding of a range of strategies for reporting to students and parents/carers and the purpose of keeping accurate and reliable records of student achievement." });

		Credential1 standard6 = createStandard7(events,
				"Standard 6 - Engage in professional learning",
				"Engage in professional learning.",
				userNickPowell,
				"professional learning, teaching practice",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "6.1 Identify and plan professional learning needs",
						"Demonstrate an understanding of the role of the Australian Professional Standards for Teachers in identifying professional learning needs." },
				new String[]{ "6.2 Engage in professional learning and improve practice",
						"Understand the relevant and appropriate sources of professional learning for teachers." },
				new String[]{ "6.3 Engage with colleagues and improve practice",
						"Seek and apply constructive feedback from supervisors and teachers to improve teaching practices." },
				new String[]{ "6.4 Apply professional learning and improve student learning",
						"Demonstrate an understanding of the rationale for continued professional learning and the implications for improved student learning." });

		Credential1 standard7 = createStandard7(events,
				"Standard 7 - Engage professionally with colleagues, parents/carers and the community",
				"Engage professionally with colleagues, parents/carers and the community.",
				userNickPowell,
				"professional ethics, policies, professional teaching networks",
				rubricData.getId(),
				graduateLearningStage,
				new String[]{ "7.1 Meet professional ethics and responsibilities",
						"Understand and apply the key principles described in codes of ethics and conduct for the teaching profession." },
				new String[]{ "7.2 Comply with legislative, administrative and organisational requirements",
						"Understand the relevant legislative, administrative and organisational policies and processes required for teachers according to school stage." },
				new String[]{ "7.3 Engage with the parents/carers",
						"Understand strategies for working effectively, sensitively and confidentially with parents/carers." },
				new String[]{ "7.4 Engage with professional teaching networks and broader communities",
						"Understand the role of external professionals and community representatives in broadening teachers’ professional knowledge and practice." });

		////////////////////////////////
		// Create deliveries
		////////////////////////////////
		try {
			long date90DaysFromNow = getDaysFromNow(90);
			Credential1 standard1Delivery = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(standard1.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(date90DaysFromNow), createUserContext(userNickPowell)));

			// give learn privilege to all students from
			givePrivilegeToGroupOnDelivery(events, standard1Delivery, UserGroupPrivilege.Learn, userNickPowell, org, Arrays.asList(userGroupScienceEducationStudents.getId(), userGroupArtsEducationStudents.getId()));

			CredentialInstructor instructorKarenWhite = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(standard1Delivery.getId(), userKarenWhite.getId(), 0, createUserContext(userNickPowell)));
			CredentialInstructor instructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(standard1Delivery.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));
			CredentialInstructor instructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(standard1Delivery.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));
			CredentialInstructor instructorErikaAmes = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(standard1Delivery.getId(), userErikaAmes.getId(), 0, createUserContext(userNickPowell)));

			// enroll some students to the delivery
			enrollToDelivery(events, org, standard1Delivery, userHelenCampbell);
			enrollToDelivery(events, org, standard1Delivery, userRichardAnderson);
			enrollToDelivery(events, org, standard1Delivery, userStevenTurner);
			enrollToDelivery(events, org, standard1Delivery, userJosephGarcia);
			enrollToDelivery(events, org, standard1Delivery, userTimothyRivera);
			enrollToDelivery(events, org, standard1Delivery, userKevinHall);

			// explicitly set Phil Armstrong as an instructor of Helen Campbell
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
					instructorPhilArmstrong.getId(), standard1Delivery.getId(), Arrays.asList(userHelenCampbell.getId()), null, createUserContext(userNickPowell)));

			// explicitly set Phil Armstrong as an instructor of Richard Anderson
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
					instructorPhilArmstrong.getId(), standard1Delivery.getId(), Arrays.asList(userRichardAnderson.getId()), null, createUserContext(userNickPowell)));

			//////////////////////////
			// Start all competencies
			//////////////////////////
			List<CompetenceData1> standard1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(standard1Delivery.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

			// we need a reference to the TargetCompetence1
			TargetCompetence1 standard1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
			TargetCompetence1 standard1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
			TargetCompetence1 standard1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
			TargetCompetence1 standard1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
			TargetCompetence1 standard1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
			TargetCompetence1 standard1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

			// add pieces of evidence to the all competencies
			LearningEvidenceData evidence1Data = new LearningEvidenceData();
			evidence1Data.setType(LearningEvidenceType.LINK);
			evidence1Data.setTitle("Learning Plan");
			evidence1Data.setText("Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics");
			evidence1Data.setUrl("https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf");
			evidence1Data.setTagsString("learning plan, teaching strategies");
			evidence1Data.setRelationToCompetence("Learning plan incorporating teaching strategies.");

			evidence1Data = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					standard1Comp1Target.getId(), evidence1Data, createUserContext(userHelenCampbell)));

			LearningEvidenceData evidence2Data = new LearningEvidenceData();
			evidence2Data.setType(LearningEvidenceType.LINK);
			evidence2Data.setTitle("Teaching Strategies Success Analysis");
			evidence2Data.setText("Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved");
			evidence2Data.setUrl("http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/");
			evidence2Data.setTagsString("teaching strategies");
			evidence2Data.setRelationToCompetence("Teaching strategies success analysis for the K-12 programme.");

			evidence2Data = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					standard1Comp1Target.getId(), evidence2Data, createUserContext(userHelenCampbell)));

			LearningEvidenceData evidence3Data = new LearningEvidenceData();
			evidence3Data.setType(LearningEvidenceType.FILE);
			evidence3Data.setTitle("New version of the Mathematics teaching program");
			evidence3Data.setText("A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.");
			evidence3Data.setUrl("https://s3.amazonaws.com/prosolo.nikola/files/a7db937ae4b4958ceb15fb82137c43fb/New%20Mathematics%20teaching%20program.pdf");
			evidence3Data.setTagsString("teaching program");
			evidence3Data.setRelationToCompetence("Contains structure of the new version of a teaching program.");

			evidence3Data = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					standard1Comp2Target.getId(), evidence3Data, createUserContext(userHelenCampbell)));

			LearningEvidenceData evidence4Data = new LearningEvidenceData();
			evidence4Data.setType(LearningEvidenceType.FILE);
			evidence4Data.setTitle("Recording of meeting with supervisor");
			evidence4Data.setText("Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.");
			evidence4Data.setUrl("https://s3.amazonaws.com/prosolo.nikola/files/6ce971e7edb9bb95a35abd501a2409c7/Meeting%20recording,%2015%20June,%202018.mov");
			evidence4Data.setTagsString("meeting logs");
			evidence4Data.setRelationToCompetence("Contains feedback on the new version of the teaching program.");

			evidence4Data = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					standard1Comp2Target.getId(), evidence4Data, createUserContext(userHelenCampbell)));

			LearningEvidence evidence3 = ServiceLocator.getInstance().getService(DefaultManager.class).loadResource(LearningEvidence.class, evidence3Data.getId(), true);
			ServiceLocator.getInstance().getService(LearningEvidenceManager.class).attachEvidenceToCompetence(standard1Comp3Target.getId(), evidence3, "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");


			LearningEvidenceData evidence5Data = new LearningEvidenceData();
			evidence5Data.setType(LearningEvidenceType.FILE);
			evidence5Data.setTitle("Lesson notes from English language course");
			evidence5Data.setText("Lesson notes from the English language course given on 21 April, 2018.");
			evidence5Data.setUrl("https://s3.amazonaws.com/prosolo.nikola/files/dedd108c4ea49314e6a9d9c0d8cfca5e/Lesson%20notes%20from%20English%20language%20course.pptx");
			evidence5Data.setTagsString("lesson notes, english language");
			evidence5Data.setRelationToCompetence("Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.");

			evidence5Data = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					standard1Comp4Target.getId(), evidence5Data, createUserContext(userHelenCampbell)));

			LearningEvidenceData evidence6Data = new LearningEvidenceData();
			evidence6Data.setType(LearningEvidenceType.FILE);
			evidence6Data.setTitle("Audio recording of student feedback");
			evidence6Data.setText("Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
					"the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.");
			evidence6Data.setUrl("https://s3.amazonaws.com/prosolo.nikola/files/05766a8aa68df0b97f6f5934c040adb1/Student%20conference%20recording.mp3");
			evidence6Data.setTagsString("student conference");
			evidence6Data.setRelationToCompetence("Student feedback on teaching activities to meet the specific learning strengths and needs.");

			evidence6Data = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
					standard1Comp5Target.getId(), evidence6Data, createUserContext(userHelenCampbell)));

			LearningEvidence evidence5 = ServiceLocator.getInstance().getService(DefaultManager.class).loadResource(LearningEvidence.class, evidence5Data.getId(), true);
			ServiceLocator.getInstance().getService(LearningEvidenceManager.class).attachEvidenceToCompetence(standard1Comp6Target.getId(), evidence5, "Lesson observation notes that record how the teaching strategies designed and implemented by\n" +
					"the teacher have been adjusted to support the learning needs of individual students with disability.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
		}

		// create deliveries for all other standards
		List<Credential1> otherStandards = Arrays.asList(standard2, standard3, standard4, standard5, standard6, standard7);

		for (Credential1 standard : otherStandards)
		try {
			long date90DaysFromNow = getDaysFromNow(90);
			Credential1 standardDelivery = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(standard.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(date90DaysFromNow), createUserContext(userNickPowell)));

			// all student from the School of Education can learn all deliveries
			givePrivilegeToUsersOnDelivery(events, standardDelivery, UserGroupPrivilege.Learn, userNickPowell, org, schoolOfEducationStudents);

			// Phil Armstrong is instructor at all deliveries
			extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(standardDelivery.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));
		} catch (IllegalDataStateException e) {
			e.printStackTrace();
			logger.error("Error", e);
		}

		//////////////////////////////////
		// Create Status wall posts
		//////////////////////////////////
		SocialActivityData1 newSocialActivity = new SocialActivityData1();
		newSocialActivity.setText("Market analysis and future prospects of Online Education market.");

		try {
			LinkParser parser = LinkParserFactory.buildParser(StringUtil.cleanHtml("https://www.marketwatch.com/press-release/online-education-market-2018-top-key-players-k12-inc-pearson-white-hat-managemen-georg-von-holtzbrinck-gmbh-co-2018-08-22"));
			AttachmentPreview1 attachmentPreview1 = parser.parse();
			newSocialActivity.setAttachmentPreview(attachmentPreview1);
		} catch (LinkParserException e) {
			e.printStackTrace();
			logger.error("Error", e);
		}

		PostSocialActivity1 postSocialActivity1 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(SocialActivityManager.class).createNewPostAndGetEvents(
				newSocialActivity, createUserContext(userLoriAbner)));

		// HACK: manually add minute to the lastEvent of the postSocialActivity1 so it would be listed on the Status Wall after UnitWelcomePostSocialActivity
		Session session2 = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
		try {
			postSocialActivity1 = (PostSocialActivity1) session2.merge(postSocialActivity1);
			postSocialActivity1.setLastAction(DateUtils.addMinutes(postSocialActivity1.getLastAction(), 5));
			session2.save(postSocialActivity1);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
		} finally {
			HibernateUtil.close(session2);
		}


		// fire all events
		ServiceLocator.getInstance().getService(EventFactory.class).generateEvents(events, new Class[]{NodeChangeObserver.class});

		try {
			logger.info("Reindexing all indices since we know some observers have failed");
			ServiceLocator.getInstance().getService(BulkDataAdministrationService.class).deleteAndReindexDBESIndexes();
		} catch (IndexingServiceNotAvailable indexingServiceNotAvailable) {
			logger.error(indexingServiceNotAvailable);
		}
	}

	private RubricData createRubric(EventQueue events, User creator) {
		Rubric rubric = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(RubricManager.class).createNewRubricAndGetEvents("Standard Performance Assessment Rubric", createUserContext(creator)));
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
		return rubricData;
	}

	private void enrollToDelivery(EventQueue events, Organization org, Credential1 delivery, User user) {
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(delivery.getId(), user.getId(), 0, UserContextData.of(user.getId(), org.getId(), null, null)));
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

	private Credential1 createStandard7(EventQueue events, String title, String description, User creator, String tags, long rubricId, LearningStageData graduateLearningStage, String[]... compData) {
		Credential1 standard = createCredential(events,
				title,
				description,
				creator,
				tags,
				rubricId,
				graduateLearningStage);

		try {
			for (String[] compDatum : compData) {
				createCompetence(events,
						creator,
						compDatum[0],
						compDatum[1],
						standard.getId(),
						rubricId);
			}
		} catch (Exception ex) {
			logger.error(ex);
		}

		return standard;
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
			logger.error("Error", e);
			return null;
		}
	}

	private Credential1 createCredential(EventQueue events, String title, String description, User user, String tags, long rubricId, LearningStageData learningStage) {
		CredentialData credentialData = new CredentialData(false);
		credentialData.setTitle(title);
		credentialData.setDescription(description);
		credentialData.setTagsString(tags);
		credentialData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
		credentialData.getAssessmentSettings().setRubricId(rubricId);
		credentialData.setLearningStageEnabled(true);
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

	private Competence1 createCompetence(EventQueue events, User user, String title, String description, long credentialId, long rubricId) {
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
			return extractResultAndAddEvents(events, ServiceLocator
					.getInstance()
					.getService(Competence1Manager.class)
					.saveNewCompetenceAndGetEvents(
							compData, credentialId, createUserContext(user)));
		} catch (DbConnectionException | IllegalDataStateException e) {
			logger.error(e);
			return null;
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
