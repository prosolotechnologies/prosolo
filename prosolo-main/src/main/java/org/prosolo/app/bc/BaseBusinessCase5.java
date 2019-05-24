package org.prosolo.app.bc;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.OperationForbiddenException;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricLevelData;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.nodes.data.statusWall.MediaType1;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2019-01-16
 * @since 1.3.0
 */
public abstract class BaseBusinessCase5 extends BaseBusinessCase {

    //units
    protected Unit unitSchoolOfEducation;
    protected Unit unitOfNursingAndMidwifery;
    protected List<User> schoolOfEducationInstructors;
    protected List<User> schoolOfEducationStudents;
    protected List<User> schoolOfNursingStudents;
    //user groups
    protected UserGroup userGroupArtsEducationStudents;
    protected UserGroup userGroupScienceEducationStudents;
    //learning stages
    protected LearningStageData graduateLearningStage;
    //rubrics
    protected RubricData rubricData;
    //credentials
    protected Credential1 credential1;
    protected Credential1 credential2;
    protected Credential1 credential3;
    protected Credential1 credential4;
    protected Credential1 credential5;
    protected Credential1 credential6;
    protected Credential1 credential7;
    //deliveries
    protected Credential1 credential1Delivery1;
    protected CredentialInstructor credential1Delivery1InstructorKarenWhite;
    protected CredentialInstructor credential1Delivery1InstructorPhilArmstrong;
    protected CredentialInstructor credential1Delivery1InstructorAnnaHallowell;
    protected CredentialInstructor credential1Delivery1InstructorErikaAmes;

    protected Credential1 credential2Delivery1;
    protected CredentialInstructor credential2Delivery1InstructorPhilArmstrong;
    protected Credential1 credential3Delivery1;
    protected CredentialInstructor credential3Delivery1InstructorPhilArmstrong;
    protected Credential1 credential4Delivery1;
    protected CredentialInstructor credential4Delivery1InstructorPhilArmstrong;
    protected Credential1 credential5Delivery1;
    protected CredentialInstructor credential5Delivery1InstructorPhilArmstrong;
    protected Credential1 credential6Delivery1;
    protected CredentialInstructor credential6Delivery1InstructorPhilArmstrong;
    protected Credential1 credential7Delivery1;
    protected CredentialInstructor credential7Delivery1InstructorPhilArmstrong;

    public static final String PDF_TEST_FILE = "https://devfiles.prosolo.ca/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf";
    public static final String PDF1_TEST_FILE = "https://devfiles.prosolo.ca/files/e05c20125f4c36ba7043b6d89f925715/New%20Mathematics%20teaching%20program.pdf";
    public static final String PPT_TEST_FILE = "https://devfiles.prosolo.ca/files/88b14f65b00d2921c4355ab427a202c9/Lesson%20notes%20from%20English%20language%20course.pptx";
    public static final String MOV_TEST_FILE = "https://devfiles.prosolo.ca/files/93b2ae57400b80de816555ce667fb9de/Meeting%20recording,%2015%20June,%202018.mov";
    public static final String MP3_TEST_FILE = "https://devfiles.prosolo.ca/files/f64720fff7409bf3b5f59e8f3e51a0c6/Student%20conference%20recording.mp3";

    @Override
    protected void createAdditionalData(EventQueue events) throws Exception {
        createLearningStages(events, "Graduate");
        // load learning stage from db in order to obtain its id
        graduateLearningStage = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationLearningStagesForLearningResource(organization.getId()).get(0).getLearningStage();
        ////////////////////////
        // create units
        ////////////////////////
        createUnitsAndAddUsers(events);
        ///////////////////////
        // Create unit groups
        ///////////////////////
        createUserGroupsAndAddUsers(events);
        ///////////////////////
        // create rubric
        ///////////////////////
        rubricData = createRubric(events, userNickPowell);
        //////////////////////////
        // Create credentials
        //////////////////////////
        createCredentials(events);
        //////////////////////////
        // create deliveries
        //////////////////////////
        createDeliveries(events);

        createAdditionalDataBC5(events);
    }

    protected abstract void createAdditionalDataBC5(EventQueue events) throws Exception;

    protected void createDeliveries(EventQueue events) throws Exception {
        //delivery 1
        credential1Delivery1 = createDelivery(events, credential1, new Date().getTime(), getDaysFromNow(90), userNickPowell);

        // give learn privilege to all students from both student groups
        givePrivilegeToGroupOnDelivery(events, credential1Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGroupScienceEducationStudents, userGroupArtsEducationStudents));

        credential1Delivery1InstructorKarenWhite = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential1Delivery1.getId(), userKarenWhite.getId(), 0, createUserContext(userNickPowell)));
        credential1Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential1Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));
        credential1Delivery1InstructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential1Delivery1.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));
        credential1Delivery1InstructorErikaAmes = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential1Delivery1.getId(), userErikaAmes.getId(), 0, createUserContext(userNickPowell)));

        //delivery 2
        credential2Delivery1 = createDelivery(events, credential2, getDaysBeforeNow(1), getDaysFromNow(90), userNickPowell);
        // give learn privilege to all students from the student group School of Education
        givePrivilegeToUsersOnDelivery(events, credential2Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        credential2Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential2Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));

        //delivery 3
        credential3Delivery1 = createDelivery(events, credential3, new Date().getTime(), getDaysFromNow(90), userNickPowell);
        // give learn privilege to all students from the student group School of Education
        givePrivilegeToUsersOnDelivery(events, credential3Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        credential3Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential3Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));

        //delivery 4
        credential4Delivery1 = createDelivery(events, credential4, new Date().getTime(), getDaysFromNow(90), userNickPowell);
        // give learn privilege to all students from the student group School of Education
        givePrivilegeToUsersOnDelivery(events, credential4Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        credential4Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential4Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));

        //delivery 5
        credential5Delivery1 = createDelivery(events, credential5, new Date().getTime(), getDaysFromNow(90), userNickPowell);
        // give learn privilege to all students from the student group School of Education
        givePrivilegeToUsersOnDelivery(events, credential5Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        credential5Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential5Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));

        //delivery 6
        credential6Delivery1 = createDelivery(events, credential6, new Date().getTime(), getDaysFromNow(90), userNickPowell);
        // give learn privilege to all students from the student group School of Education
        givePrivilegeToUsersOnDelivery(events, credential6Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        credential6Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential6Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));

        //delivery 7
        credential7Delivery1 = createDelivery(events, credential7, new Date().getTime(), getDaysFromNow(90), userNickPowell);
        // give learn privilege to all students from the student group School of Education
        givePrivilegeToUsersOnDelivery(events, credential7Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        credential7Delivery1InstructorPhilArmstrong = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential7Delivery1.getId(), userPhilArmstrong.getId(), 0, createUserContext(userNickPowell)));
    }

    protected void createCredentials(EventQueue events) {
        credential1 = createStandard(events,
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

        credential2 = createStandard(events,
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

        credential3 = createStandard(events,
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

        credential4 = createStandard(events,
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

        credential5 = createStandard(events,
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

        credential6 = createStandard(events,
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

        credential7 = createStandard(events,
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
    }

    private Credential1 createStandard(EventQueue events, String title, String description, User creator, String tags, long rubricId, LearningStageData graduateLearningStage, String[]... compData) {
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
                        null,
                        standard.getId(),
                        rubricId,
                        LearningPathType.EVIDENCE);
            }
        } catch (Exception ex) {
            getLogger().error(ex);
        }

        return standard;
    }

    protected Credential1 createDelivery(EventQueue events, Credential1 delivery, long startMillis, long endMillis, User user) throws IllegalDataStateException {
        return extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(delivery.getId(), DateUtil.getDateFromMillis(startMillis), DateUtil.getDateFromMillis(endMillis), createUserContext(user)));
    }

    private RubricData createRubric(EventQueue events, User creator) throws OperationForbiddenException {
        List<RubricLevelData> levels = new ArrayList<>();
        RubricLevelData level1 = new RubricLevelData(ObjectStatus.CREATED);
        level1.setName("Working Towards Graduate Level");
        level1.setOrder(1);
        levels.add(level1);

        RubricLevelData level2 = new RubricLevelData(ObjectStatus.CREATED);
        level2.setName("Meet the Standard at Threshold Level");
        level2.setOrder(2);
        levels.add(level2);

        RubricLevelData level3 = new RubricLevelData(ObjectStatus.CREATED);
        level3.setName("Meet the Standard at Enhanced Level");
        level3.setOrder(3);
        levels.add(level3);

        RubricLevelData level4 = new RubricLevelData(ObjectStatus.CREATED);
        level4.setName("Outstanding Performance");
        level4.setOrder(4);
        levels.add(level4);

        List<RubricCriterionData> criteria = new ArrayList<>();
        RubricCriterionData criterion = new RubricCriterionData(ObjectStatus.CREATED);
        criterion.setName("Performance");
        criterion.setOrder(1);
        criteria.add(criterion);

        return createRubric(events, creator, "Standard Performance Assessment Rubric", RubricType.DESCRIPTIVE, levels, criteria);
    }

    private void createUnitsAndAddUsers(EventQueue events) {
        // create org. unit School of Education
        unitSchoolOfEducation = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
                .createNewUnitAndGetEvents("School of Education", organization.getId(), 0, createUserContext(userNickPowell)));

        // create org. unit School of Nursing and Midwifery
        unitOfNursingAndMidwifery = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class)
                .createNewUnitAndGetEvents("School of Nursing and Midwifery", organization.getId(), 0, createUserContext(userNickPowell)));

        // add managers to the unit School of Education
        addUsersToUnitWithRole(events, unitSchoolOfEducation.getId(), List.of(userNickPowell.getId(), userKarenWhite.getId()), roleManager.getId(), createUserContext(userNickPowell));
        // add instructors to the unit School of Education
        addUsersToUnitWithRole(events, unitSchoolOfEducation.getId(), List.of(userKarenWhite.getId(), userPhilArmstrong.getId(), userAnnaHallowell.getId(), userErikaAmes.getId()), roleInstructor.getId(), createUserContext(userKarenWhite));

        // list of all instructors from the School od Education
        schoolOfEducationInstructors = List.of(userKarenWhite, userPhilArmstrong, userAnnaHallowell, userErikaAmes);
        // add students to the unit School of Education
        addUsersToUnitWithRole(events, unitSchoolOfEducation.getId(),
                List.of(
                        userHelenCampbell.getId(),
                        userRichardAnderson.getId(),
                        userStevenTurner.getId(),
                        userJosephGarcia.getId(),
                        userTimothyRivera.getId(),
                        userKevinHall.getId(),
                        userKennethCarter.getId(),
                        userAnthonyMoore.getId(),
                        userTaniaCortese.getId(),
                        userSonyaElston.getId(),
                        userLoriAbner.getId(),
                        userSamanthaDell.getId(),
                        userSheriLaureano.getId(),
                        userAngelicaFallon.getId(),
                        userIdaFritz.getId()),
                roleUser.getId(), createUserContext(userKarenWhite));
        // list of all students from the School od Education
        schoolOfEducationStudents = List.of(userHelenCampbell, userRichardAnderson, userStevenTurner, userJosephGarcia, userTimothyRivera, userKevinHall, userKennethCarter, userAnthonyMoore,
                userTaniaCortese, userSonyaElston, userLoriAbner, userSamanthaDell, userSheriLaureano, userAngelicaFallon, userIdaFritz);

        // adding students to the unit School of Nursing and Midwifery
        addUsersToUnitWithRole(events, unitOfNursingAndMidwifery.getId(),
                List.of(
                        userPaulEdwards.getId(),
                        userKevinMitchell.getId(),
                        userGeorgeYoung.getId(),
                        userRachelWiggins.getId(),
                        userAkikoKido.getId()),
                roleUser.getId(), createUserContext(userKarenWhite));

        // list of all students from the School of Nursing and Midwifery
        schoolOfNursingStudents = List.of(userPaulEdwards, userKevinMitchell, userGeorgeYoung, userRachelWiggins, userAkikoKido);

        // explicitly generate welcome post social activity at this point to have the earliest timestamp
        Session session1 = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
        Transaction transaction = null;
        try {
            transaction = session1.beginTransaction();
            ServiceLocator.getInstance().getService(SocialActivityManager.class).saveUnitWelcomePostSocialActivityIfNotExists(unitSchoolOfEducation.getId(), session1);
            ServiceLocator.getInstance().getService(SocialActivityManager.class).saveUnitWelcomePostSocialActivityIfNotExists(unitOfNursingAndMidwifery.getId(), session1);
            transaction.commit();
        } catch (Exception e) {
            getLogger().error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session1);
        }
    }

    private void createUserGroupsAndAddUsers(EventQueue events) {
        // create unit Arts Education Students
        userGroupArtsEducationStudents = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).saveNewGroupAndGetEvents(unitSchoolOfEducation.getId(), "Arts Education Students", false, createUserContext(userNickPowell)));

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userHelenCampbell.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userRichardAnderson.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userStevenTurner.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userJosephGarcia.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userTimothyRivera.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userKevinHall.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userKennethCarter.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupArtsEducationStudents.getId(), userAnthonyMoore.getId(), createUserContext(userNickPowell)));

        // create unit Science Education Students
        userGroupScienceEducationStudents = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).saveNewGroupAndGetEvents(unitSchoolOfEducation.getId(), "Science Education Students", false, createUserContext(userNickPowell)));

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userTaniaCortese.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userSonyaElston.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userLoriAbner.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userSamanthaDell.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userSheriLaureano.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userAngelicaFallon.getId(), createUserContext(userNickPowell)));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserGroupManager.class).addUserToTheGroupAndGetEvents(userGroupScienceEducationStudents.getId(), userIdaFritz.getId(), createUserContext(userNickPowell)));
    }

    protected void createSocialActivityWithAttachment(EventQueue events, User user, String text, String attachmentTitle, String attachmentUrl) {
        SocialActivityData1 newSocialActivity = new SocialActivityData1();
        newSocialActivity.setText(text);

        if (attachmentUrl != null) {
            AttachmentPreview uploadedFilePreview = new AttachmentPreview();
            uploadedFilePreview.setInitialized(true);
            uploadedFilePreview.setMediaType(MediaType1.File_Other);
            uploadedFilePreview.setContentType(ContentType1.FILE);
            uploadedFilePreview.setLink(attachmentUrl);
            uploadedFilePreview.setTitle(attachmentTitle);

            newSocialActivity.setAttachmentPreview(uploadedFilePreview);
        }

        postStatus(events, user, newSocialActivity);
    }

    private void postStatus(EventQueue events, User user, SocialActivityData1 newSocialActivity) {
        PostSocialActivity1 postSocialActivity1 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(SocialActivityManager.class).createNewPostAndGetEvents(
                newSocialActivity, createUserContext(user)));

        // HACK: manually add 5 minutes to the lastEvent of the postSocialActivity1 so it would be listed on the Status Wall after UnitWelcomePostSocialActivity
        Session session2 = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
        try {
            postSocialActivity1 = (PostSocialActivity1) session2.merge(postSocialActivity1);
            postSocialActivity1.setLastAction(DateUtils.addMinutes(postSocialActivity1.getLastAction(), 5));
            postSocialActivity1.setDateCreated(DateUtils.addMinutes(postSocialActivity1.getDateCreated(), 5));
            session2.flush();
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().error("Error", e);
        } finally {
            HibernateUtil.close(session2);
        }
    }

    protected void assignInstructorToStudent(EventQueue events, CredentialInstructor instructor, List<User> students, Credential1 delivery) {
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
                instructor.getId(),
                delivery.getId(),
                students.stream().map(u -> u.getId()).collect(Collectors.toList()),
                null,
                createUserContext(userNickPowell)));
    }

    protected void assignInstructorToStudent(EventQueue events, CredentialInstructor instructor, User student, Credential1 delivery) {
        assignInstructorToStudent(events, instructor, List.of(student), delivery);
    }

    protected CommentData createNewComment(EventQueue events, User user, String text, long commentedResourceId, CommentedResourceType commentedResourceType, CommentData parent, boolean isManagerComment) {
        CommentData newComment = new CommentData();
        newComment.setCommentedResourceId(commentedResourceId);
        newComment.setDateCreated(new Date());
        newComment.setComment(text);
        newComment.setCreator(new UserData(user));
        newComment.setParent(parent);
        newComment.setManagerComment(isManagerComment);

        Comment1 comment = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).saveNewCommentAndGetEvents(newComment,
                commentedResourceType, UserContextData.of(user.getId(), user.getOrganization().getId(), null, null, null)));

        newComment.setCommentId(comment.getId());

        return newComment;
    }

    protected void likeComment(EventQueue events, CommentData commentData, User user) {
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CommentManager.class).likeCommentAndGetEvents(commentData.getCommentId(), UserContextData.of(user.getId(), user.getOrganization().getId(), null, null, new PageContextData("/activity.xhtml", null, null))));
    }

}
