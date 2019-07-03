package org.prosolo.app.bc;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.OperationForbiddenException;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.AssessorAssignmentMethod;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.settings.AssessmentTokensPlugin;
import org.prosolo.common.domainmodel.organization.settings.OrganizationPlugin;
import org.prosolo.common.domainmodel.organization.settings.OrganizationPluginType;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.EventData;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.admin.BulkDataAdministrationService;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.assessment.data.factory.AssessmentDataFactory;
import org.prosolo.services.assessment.data.grading.*;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.htmlparser.LinkParser;
import org.prosolo.services.htmlparser.LinkParserFactory;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.organization.*;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricLevelData;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.util.roles.SystemRoleNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2019-01-15
 * @since 1.3.0
 */
public abstract class BaseBusinessCase implements BusinessCase {

    //organization
    protected Organization organization;
    //roles
    protected Role roleUser;
    protected Role roleManager;
    protected Role roleInstructor;
    protected Role roleAdmin;
    protected Role roleSuperAdmin;
    //users
    protected User userNickPowell;
    protected User userKevinMitchell;
    protected User userPaulEdwards;
    protected User userGeorgeYoung;
    protected User userRichardAnderson;
    protected User userStevenTurner;
    protected User userJosephGarcia;
    protected User userTimothyRivera;
    protected User userKevinHall;
    protected User userKennethCarter;
    protected User userAnthonyMoore;
    protected User userAkikoKido;
    protected User userTaniaCortese;
    protected User userSonyaElston;
    protected User userLoriAbner;
    protected User userSamanthaDell;
    protected User userSheriLaureano;
    protected User userAngelicaFallon;
    protected User userIdaFritz;
    protected User userRachelWiggins;
    protected User userHelenCampbell;
    protected User userPhilArmstrong;
    protected User userKarenWhite;
    protected User userAnnaHallowell;
    protected User userErikaAmes;

    @Override
    public void initRepository() {
        try {
            getLogger().info(getBusinessCaseInitLog());

            EventQueue events = EventQueue.newEventQueue();

            // fetch roles
            roleUser = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.USER);
            roleManager = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.MANAGER);
            roleInstructor = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.INSTRUCTOR);
            roleAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.ADMIN);
            roleSuperAdmin = ServiceLocator.getInstance().getService(RoleManager.class).getRoleByName(SystemRoleNames.SUPER_ADMIN);

            ///////////////////////
            // Create users and organization
            ///////////////////////
            createOrganizationAndUsers(events);

            createAdditionalData(events);

            // fire all events
            /*
            TODO hack - since there is a race between observer that creates profile settings
            data based on Registered event and notification observer that creates follow user notification,
            Registered events are separated and fired before other events.
             */
            Map<Boolean, List<EventData>> eventsMap = events.getEvents().stream().collect(Collectors.partitioningBy(ev -> ev.getEventType() == EventType.Registered));
            EventQueue eventQueue1 = EventQueue.newEventQueue();
            eventQueue1.appendEvents(eventsMap.get(true));
            ServiceLocator.getInstance().getService(EventFactory.class).generateAndPublishEvents(eventQueue1, new Class[]{NodeChangeObserver.class});

            Thread.sleep(2000);

            EventQueue eventQueue2 = EventQueue.newEventQueue();
            eventQueue2.appendEvents(eventsMap.get(false));
            ServiceLocator.getInstance().getService(EventFactory.class).generateAndPublishEvents(eventQueue2, new Class[]{NodeChangeObserver.class});

            getLogger().info("Reindexing all indices since we know some observers have failed");
            ServiceLocator.getInstance().getService(BulkDataAdministrationService.class).deleteAndReindexDBESIndexes();
        } catch (Exception e) {
            getLogger().error("error", e);
            throw new RuntimeException("Error initializing business case data");
        }
    }

    private void createOrganizationAndUsers(EventQueue events) {
        String genericPassword = "prosolo@2018";

        userNickPowell = extractResultAndAddEvents(events, createUser(0, "Nick", "Powell", "nick.powell@gmail.com", genericPassword, "Teacher", "male1.png", roleAdmin));

        //generate event after roles are updated
        Map<String, String> params = null;
        events.appendEvent(ServiceLocator.getInstance().getService(EventFactory.class).generateEventData(
                EventType.USER_ROLES_UPDATED, UserContextData.ofActor(userNickPowell.getId()),
                userNickPowell, null, null, params));

        //create organization
        organization = createOrganization(events);

        userNickPowell.setOrganization(organization);

        // create 20 students
        userKevinMitchell = extractResultAndAddEvents(events, createUser(organization.getId(), "Kevin", "Mitchell", "kevin.mitchell@gmail.com", genericPassword, "Student", "male3.png", roleUser));
        userPaulEdwards = extractResultAndAddEvents(events, createUser(organization.getId(), "Paul", "Edwards", "paul.edwards@gmail.com", genericPassword, "Student", "male4.png", roleUser));
        userGeorgeYoung = extractResultAndAddEvents(events, createUser(organization.getId(), "George", "Young", "george.young@gmail.com", genericPassword, "Student", "male6.png", roleUser));
        userRichardAnderson = extractResultAndAddEvents(events, createUser(organization.getId(), "Richard", "Anderson", "richard.anderson@gmail.com", genericPassword, "Student", "male2.png", roleUser));
        userStevenTurner = extractResultAndAddEvents(events, createUser(organization.getId(), "Steven", "Turner", "steven.turner@gmail.com", genericPassword, "Student", "male5.png", roleUser));
        userJosephGarcia = extractResultAndAddEvents(events, createUser(organization.getId(), "Joseph", "Garcia", "joseph.garcia@gmail.com", genericPassword, "Student", "male8.png", roleUser));
        userTimothyRivera = extractResultAndAddEvents(events, createUser(organization.getId(), "Timothy", "Rivera", "timothy.rivera@gmail.com", genericPassword, "Student", "male9.png", roleUser));
        userKevinHall = extractResultAndAddEvents(events, createUser(organization.getId(), "Kevin", "Hall", "kevin.hall@gmail.com", genericPassword, "Student", "male10.png", roleUser));
        userKennethCarter = extractResultAndAddEvents(events, createUser(organization.getId(), "Kenneth", "Carter", "kenneth.carter@gmail.com", genericPassword, "Student", "male11.png", roleUser));
        userAnthonyMoore = extractResultAndAddEvents(events, createUser(organization.getId(), "Anthony", "Moore", "anthony.moore@gmail.com", genericPassword, "Student", "male12.png", roleUser));
        userAkikoKido = extractResultAndAddEvents(events, createUser(organization.getId(), "Akiko", "Kido", "akiko.kido@gmail.com", genericPassword, "Student", "female7.png", roleUser));
        userTaniaCortese = extractResultAndAddEvents(events, createUser(organization.getId(), "Tania", "Cortese", "tania.cortese@gmail.com", genericPassword, "Student", "female1.png", roleUser));
        userSonyaElston = extractResultAndAddEvents(events, createUser(organization.getId(), "Sonya", "Elston", "sonya.elston@gmail.com", genericPassword, "Student", "female2.png", roleUser));
        userLoriAbner = extractResultAndAddEvents(events, createUser(organization.getId(), "Lori", "Abner", "lori.abner@gmail.com", genericPassword, "Student", "female3.png", roleUser));
        userSamanthaDell = extractResultAndAddEvents(events, createUser(organization.getId(), "Samantha", "Dell", "samantha.dell@gmail.com", genericPassword, "Student", "female4.png", roleUser));
        userSheriLaureano = extractResultAndAddEvents(events, createUser(organization.getId(), "Sheri", "Laureano", "sheri.laureano@gmail.com", genericPassword, "Student", "female14.png", roleUser));
        userAngelicaFallon = extractResultAndAddEvents(events, createUser(organization.getId(), "Angelica", "Fallon", "angelica.fallon@gmail.com", genericPassword, "Student", "female16.png", roleUser));
        userIdaFritz = extractResultAndAddEvents(events, createUser(organization.getId(), "Ida", "Fritz", "ida.fritz@gmail.com", genericPassword, "Student", "female17.png", roleUser));
        userRachelWiggins = extractResultAndAddEvents(events, createUser(organization.getId(), "Rachel", "Wiggins", "rachel.wiggins@gmail.com", genericPassword, "Student", "female20.png", roleUser));
        userHelenCampbell = extractResultAndAddEvents(events, createUser(organization.getId(), "Helen", "Campbell", "helen.campbell@gmail.com", genericPassword, "Student", "female13.png", roleUser));

        // create 4 instructors
        userPhilArmstrong = extractResultAndAddEvents(events, createUser(organization.getId(), "Phil", "Armstrong", "phil.armstrong@gmail.com", genericPassword, "Teaching Assistant", "male7.png", roleInstructor));
        userKarenWhite = extractResultAndAddEvents(events, createUser(organization.getId(), "Karen", "White", "karen.white@gmail.com", genericPassword, "Teaching Assistant", "female10.png", roleInstructor));
        userAnnaHallowell = extractResultAndAddEvents(events, createUser(organization.getId(), "Anna", "Hallowell", "anna.hallowell@gmail.com", genericPassword, "Teaching Assistant", "female11.png", roleInstructor));
        userErikaAmes = extractResultAndAddEvents(events, createUser(organization.getId(), "Erika", "Ames", "erika.ames@gmail.com", genericPassword, "Teaching Assistant", "female12.png", roleInstructor));


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
    }

    protected SocialActivity1 createSocialActivity(EventQueue events, User user, String text, String url) {
        SocialActivityData1 newSocialActivity = new SocialActivityData1();
        newSocialActivity.setText(text);

        if (url != null) {
            try {
                LinkParser parser = LinkParserFactory.buildParser(StringUtil.cleanHtml(url));
                AttachmentPreview attachmentPreview = parser.parse();
                newSocialActivity.setAttachmentPreview(attachmentPreview);
            } catch (LinkParserException e) {
                e.printStackTrace();
                getLogger().error("Error", e);
            }
        }

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

        return postSocialActivity1;
    }

    private Organization createOrganization(EventQueue events) {
        OrganizationBasicData orgData = new OrganizationBasicData();
        orgData.setTitle("Desert Winds University");
        orgData.setAdmins(List.of(new UserData(userNickPowell)));

        return extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(OrganizationManager.class)
                .createNewOrganizationAndGetEvents(orgData, UserContextData.empty()));
    }

    protected UserContextData createUserContext(User user) {
        return UserContextData.of(user.getId(), user.getOrganization().getId(), null, null, null);
    }

    protected UserContextData createUserContext(User user, PageContextData context) {
        return UserContextData.of(user.getId(), user.getOrganization().getId(), null, null, context);
    }

    protected <T> T extractResultAndAddEvents(EventQueue events, Result<T> result) {
        events.appendEvents(result.getEventQueue());
        return result.getResult();
    }

    protected Result<User> createUser(long orgId, String name, String lastname, String emailAddress, String password, String position,
                                      String avatar, Role roleUser) {
        try {
            return ServiceLocator
                    .getInstance()
                    .getService(UserManager.class)
                    .createNewUserAndGetEvents(orgId, name, lastname, emailAddress,
                            true, password, position, getAvatarInputStream(avatar), avatar, Collections.singletonList(roleUser.getId()), false);
        } catch (IllegalDataStateException e) {
            e.printStackTrace();
            getLogger().error("Error", e);
            return null;
        }
    }

    private InputStream getAvatarInputStream(String avatarName) {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource("test_avatars120x120/" + avatarName);

        try {
            return new FileInputStream(new File(url.getFile()));
        } catch (Exception e) {
            getLogger().error(e.getLocalizedMessage());
        }
        return null;
    }

    protected RubricData createRubric(EventQueue events, User creator, String rubricName, RubricType rubricType, List<RubricLevelData> levels, List<RubricCriterionData> criteria) throws OperationForbiddenException {
        Rubric rubric = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(RubricManager.class).createNewRubricAndGetEvents(rubricName, createUserContext(creator)));
        RubricData rubricData = ServiceLocator.getInstance().getService(RubricManager.class).getRubricData(rubric.getId(), true, true, 0, true, true);
        rubricData.setRubricType(rubricType);
        rubricData.setReadyToUse(true);

        for (RubricCriterionData criterion : criteria) {
            criterion.setStatus(ObjectStatus.CREATED);
            rubricData.addNewCriterion(criterion);
        }

        for (RubricLevelData level : levels) {
            level.setStatus(ObjectStatus.CREATED);
            rubricData.addNewLevel(level);
        }

        ServiceLocator.getInstance().getService(RubricManager.class).saveRubricCriteriaAndLevels(rubricData, EditMode.FULL);
        return rubricData;
    }

    protected Credential1 createCredential(EventQueue events, String title, String description, User user, String tags, long rubricId, LearningStageData learningStage) {
        CredentialData credentialData = new CredentialData(false);
        credentialData.getIdData().setTitle(title);
        credentialData.setDescription(description);
        credentialData.setTagsString(tags);
        credentialData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
        credentialData.getAssessmentSettings().setRubricId(rubricId);

        if (learningStage != null) {
            credentialData.setLearningStageEnabled(true);
            credentialData.setLearningStage(learningStage);
        }
        credentialData.setAssessorAssignment(CredentialData.AssessorAssignmentMethodData.AUTOMATIC);

        AssessmentTypeConfig instructorAssessment = new AssessmentTypeConfig(-1, AssessmentType.INSTRUCTOR_ASSESSMENT, true, true);
        AssessmentTypeConfig peerAssessment = new AssessmentTypeConfig(-1, AssessmentType.PEER_ASSESSMENT, false, false);
        AssessmentTypeConfig selfAssessment = new AssessmentTypeConfig(-1, AssessmentType.SELF_ASSESSMENT, true, false);
        credentialData.setAssessmentTypes(List.of(instructorAssessment, peerAssessment, selfAssessment));

        return extractResultAndAddEvents(events, ServiceLocator
                .getInstance()
                .getService(CredentialManager.class)
                .saveNewCredentialAndGetEvents(credentialData, createUserContext(user)));
    }

    protected Competence1 createCompetence(EventQueue events, User user, String title, String description, String tagsCsv, long credentialId, long rubricId, LearningPathType learningPathType) {
        CompetenceData1 compData = new CompetenceData1(false);
        compData.setTitle(title);
        compData.setDescription(description);
        compData.setTagsString(tagsCsv);
        compData.setPublished(false);
        compData.setType(LearningResourceType.UNIVERSITY_CREATED);
        compData.getAssessmentSettings().setGradingMode(GradingMode.MANUAL);
        compData.getAssessmentSettings().setRubricId(rubricId);
        compData.setLearningPathType(learningPathType);

        AssessmentTypeConfig instructorAssessment = new AssessmentTypeConfig(-1, AssessmentType.INSTRUCTOR_ASSESSMENT, true, true);
        AssessmentTypeConfig peerAssessment = new AssessmentTypeConfig(-1, AssessmentType.PEER_ASSESSMENT, true, false);
        AssessmentTypeConfig selfAssessment = new AssessmentTypeConfig(-1, AssessmentType.SELF_ASSESSMENT, true, false);
        compData.setAssessmentTypes(List.of(instructorAssessment, peerAssessment, selfAssessment));

        try {
            return extractResultAndAddEvents(events, ServiceLocator
                    .getInstance()
                    .getService(Competence1Manager.class)
                    .saveNewCompetenceAndGetEvents(
                            compData, credentialId, createUserContext(user)));
        } catch (DbConnectionException | IllegalDataStateException e) {
            getLogger().error(e);
            return null;
        }
    }

    protected Activity1 createActivity(EventQueue events, User user, String title, String description, String url, ActivityType type,
                                     long compId, int durationHours, int durationMinutes, org.prosolo.services.nodes.data.ActivityResultType resultType, String... nameLink)
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
                        actData, createUserContext(user)));
        return act;
    }

    protected void enrollToDelivery(EventQueue events, Credential1 delivery, User user) {
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).enrollInCredentialAndGetEvents(delivery.getId(), user.getId(), 0, UserContextData.of(user.getId(), organization.getId(), null, null, null)));
    }

    protected LearningEvidence createEvidence(EventQueue events, LearningEvidenceType type, String title, String description, String url, String tagsCsv, User user) {
        LearningEvidenceData evidence1Data = new LearningEvidenceData();
        evidence1Data.setType(type);
        evidence1Data.setTitle(title);
        evidence1Data.setText(description);
        evidence1Data.setUrl(url);
        evidence1Data.setTagsString(tagsCsv);

        return extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAndGetEvents(
                evidence1Data, createUserContext(user)));
    }
    protected void enrollToCompetencies(EventQueue events, long deliveryId, List<CompetenceData1> competences, User user) {
        for (CompetenceData1 cd : competences) {
            extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(deliveryId, cd.getCompetenceId(), user.getId(), createUserContext(user)));
        }
    }

    protected LearningEvidenceData addNewEvidenceAndAttachToCompetence(EventQueue events, LearningEvidenceType type, String title, String description, String url, String tagsCsv, String relationToCompetence, long targetCompId, User user) {
        LearningEvidenceData evidence1Data = new LearningEvidenceData();
        evidence1Data.setType(type);
        evidence1Data.setTitle(title);
        evidence1Data.setText(description);
        evidence1Data.setUrl(url);
        evidence1Data.setTagsString(tagsCsv);
        evidence1Data.setRelationToCompetence(relationToCompetence);

        return extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(LearningEvidenceManager.class).postEvidenceAttachItToCompetenceAndGetEvents(
                targetCompId, evidence1Data, createUserContext(user)));
    }

    protected void attachExistingEvidenceToCompetence(long evidenceId, long targetCompId, String relationToCompetence) throws Exception {
        LearningEvidence evidence = ServiceLocator.getInstance().getService(DefaultManager.class).loadResource(LearningEvidence.class, evidenceId, true);
        ServiceLocator.getInstance().getService(LearningEvidenceManager.class).attachEvidenceToCompetence(targetCompId, evidence, relationToCompetence);
    }

    protected void givePrivilegeToUsersOnDelivery(EventQueue events, Credential1 delivery, UserGroupPrivilege userGroupPrivilege, User actor, Organization org, List<User> students) {
        List<ResourceVisibilityMember> studentsToAdd = new LinkedList<>();

        for (User student : students) {
            ResourceVisibilityMember resourceVisibilityMember = new ResourceVisibilityMember(0, student, userGroupPrivilege, false, true);
            resourceVisibilityMember.setStatus(ObjectStatus.CREATED);
            studentsToAdd.add(resourceVisibilityMember);
        }

        events.appendEvents(ServiceLocator.getInstance().getService(CredentialManager.class).updateCredentialVisibilityAndGetEvents(
                delivery.getId(), new LinkedList<>(), studentsToAdd,false, false,
                UserContextData.of(actor.getId(), org.getId(), null, null, null)));
    }

    protected void givePrivilegeToGroupOnDelivery(EventQueue events, Credential1 delivery, UserGroupPrivilege userGroupPrivilege, User actor, Organization org, List<UserGroup> groups) {
        List<ResourceVisibilityMember> groupsToAdd = new LinkedList<>();

        for (UserGroup group : groups) {
            ResourceVisibilityMember resourceVisibilityMember = new ResourceVisibilityMember(0, group.getId(), null, 0, userGroupPrivilege, false, true);
            resourceVisibilityMember.setStatus(ObjectStatus.CREATED);
            groupsToAdd.add(resourceVisibilityMember);
        }

        events.appendEvents(ServiceLocator.getInstance().getService(CredentialManager.class).updateCredentialVisibilityAndGetEvents(
                delivery.getId(), groupsToAdd, new LinkedList<>(), false, false,
                UserContextData.of(actor.getId(), org.getId(), null, null, null)));
    }

    protected void givePrivilegeToUsersForCompetency(EventQueue events, long competenceId, UserGroupPrivilege userGroupPrivilege, User actor, List<User> students) {
        List<ResourceVisibilityMember> studentsToAdd = new LinkedList<>();

        for (User student : students) {
            ResourceVisibilityMember resourceVisibilityMember = new ResourceVisibilityMember(0, student, userGroupPrivilege, false, true);
            resourceVisibilityMember.setStatus(ObjectStatus.CREATED);
            studentsToAdd.add(resourceVisibilityMember);
        }

        events.appendEvents(ServiceLocator.getInstance().getService(Competence1Manager.class).updateCompetenceVisibilityAndGetEvents(
                competenceId, new LinkedList<>(), studentsToAdd,false, false,
                createUserContext(actor)));
    }

    protected void bookmarkCredential(EventQueue events, long credId, User user) {
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class)
                .bookmarkCredentialAndGetEvents(credId, createUserContext(user)));
    }

    protected long getDaysFromNow(int days) {
        return LocalDateTime.now(Clock.systemUTC()).plusDays(days).atZone(ZoneOffset.ofTotalSeconds(0)).toInstant().toEpochMilli();
    }

    protected long getDaysBeforeNow(int days) {
        return LocalDateTime.now(Clock.systemUTC()).minusDays(days).atZone(ZoneOffset.ofTotalSeconds(0)).toInstant().toEpochMilli();
    }

    protected void createLearningStages(EventQueue events, String... stages) {
        OrganizationPlugin learningStagesPlugin = organization.getPlugins().stream().filter(p -> p.getType() == OrganizationPluginType.LEARNING_STAGES).findAny().get();
        LearningStagesPluginData learningStagesPluginData = new LearningStagesPluginData();
        learningStagesPluginData.setPluginId(learningStagesPlugin.getId());
        learningStagesPluginData.setEnabled(true);
        int order = 1;
        for (String stage : stages) {
            LearningStageData stageData = new LearningStageData(false);
            stageData.setTitle(stage);
            stageData.setOrder(order++);
            stageData.setStatus(ObjectStatus.CREATED);    // this needs to be set in order for the stage to be created in the method createNewOrganizationAndGetEvents
            learningStagesPluginData.addLearningStage(stageData);
        }


        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(OrganizationManager.class)
               .updateLearningStagesPluginAndGetEvents(organization.getId(), learningStagesPluginData, UserContextData.empty()));
    }

    protected void createCredentialCategories(EventQueue events, String... categories) {
        OrganizationPlugin credentialCategoriesPlugin = organization.getPlugins().stream().filter(p -> p.getType() == OrganizationPluginType.CREDENTIAL_CATEGORIES).findAny().get();

        CredentialCategoriesPluginData credentialCategoriesPluginData = new CredentialCategoriesPluginData();
        credentialCategoriesPluginData.setPluginId(credentialCategoriesPlugin.getId());

        for (String category : categories) {
            CredentialCategoryData categoryData = new CredentialCategoryData(false);
            categoryData.setTitle(category);
            categoryData.setStatus(ObjectStatus.CREATED);    // this needs to be set in order for the stage to be created in the method createNewOrganizationAndGetEvents
            credentialCategoriesPluginData.addCredentialCategory(categoryData);
        }

        ServiceLocator.getInstance().getService(OrganizationManager.class)
                .updateCredentialCategoriesPlugin(organization.getId(), credentialCategoriesPluginData);
    }

    protected void addUsersToUnitWithRole(EventQueue events, long unitId, List<Long> users, long roleId, UserContextData userContext) {
        for (Long user : users) {
            extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UnitManager.class).addUserToUnitWithRoleAndGetEvents(user, unitId, roleId, userContext));
        }
    }

    protected void markCompetencyAsCompleted(EventQueue events, long targetCompetenceId, long competenceId, long credentialId, User user) {
        String learningContext= MessageFormat.format("name:CREDENTIAL|id:{0}|context:/name:COMPETENCE|id:{1}|context:/name:TARGET_COMPETENCE|id:{2}//", credentialId+"", competenceId+"", targetCompetenceId+"");

        PageContextData context = new PageContextData("/competence.xhtml", learningContext, null);

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).completeCompetenceAndGetEvents(targetCompetenceId, createUserContext(user, context)));
    }


    protected void assignCategoryToCredential(EventQueue events, long credId, CredentialCategoryData category, User user) throws Exception {
        CredentialManager credManager = ServiceLocator.getInstance().getService(CredentialManager.class);
        CredentialData credentialData = credManager.getCredentialDataForEdit(credId);
        credentialData.setCategory(category);
        extractResultAndAddEvents(events, credManager.updateCredentialData(credentialData, createUserContext(user)));
    }

    protected CompetenceAssessmentData askPeerForCompetenceAssessment(EventQueue events, long deliveryId, long compId, User student, long peerId, int numberOfTokensToSpend) throws Exception {
        return extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).requestCompetenceAssessmentGetEventsAndReturnCompetenceAssessmentData(deliveryId, compId, student.getId(), peerId, numberOfTokensToSpend, createUserContext(student)));
    }

    protected void updateCompetenceBlindAssessmentMode(EventQueue events, long compId, BlindAssessmentMode blindAssessmentMode, User userEditor) throws Exception {
        Competence1Manager compManager = ServiceLocator.getInstance().getService(Competence1Manager.class);
        RestrictedAccessResult<CompetenceData1> competenceForEdit = compManager.getCompetenceForEdit(0, compId, userEditor.getId(), AccessMode.MANAGER);
        competenceForEdit.getResource().getAssessmentTypes()
                .stream()
                .filter(conf -> conf.getType() == AssessmentType.PEER_ASSESSMENT)
                .findFirst().get().setBlindAssessmentMode(blindAssessmentMode);
        extractResultAndAddEvents(events, compManager.updateCompetenceData(competenceForEdit.getResource(), createUserContext(userEditor)));
    }

    protected AssessmentDataFull getCredentialAssessmentData(long credentialAssessmentId, long actorId, AssessmentType assessmentType) {
        AssessmentDataFull credentialAssessmentData = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getFullAssessmentDataForAssessmentType(credentialAssessmentId,
                        actorId, assessmentType, AssessmentLoadConfig.of(true, true, true));
        if (credentialAssessmentData.getGradeData().getGradingMode() == org.prosolo.services.assessment.data.grading.GradingMode.MANUAL_RUBRIC) {
            ((RubricGradeData) credentialAssessmentData.getGradeData()).setRubricCriteria(
                    ServiceLocator.getInstance().getService(RubricManager.class).getRubricDataForCredential(
                            credentialAssessmentData.getCredentialId(),
                            credentialAssessmentData.getCredAssessmentId(),
                            true));
        }
        for (CompetenceAssessmentDataFull competenceAssessmentData : credentialAssessmentData.getCompetenceAssessmentData()) {
            if (competenceAssessmentData.getGradeData().getGradingMode() == org.prosolo.services.assessment.data.grading.GradingMode.MANUAL_RUBRIC) {
                ((RubricGradeData) competenceAssessmentData.getGradeData()).setRubricCriteria(
                        ServiceLocator.getInstance().getService(RubricManager.class).getRubricDataForCompetence(
                                competenceAssessmentData.getCompetenceId(),
                                competenceAssessmentData.getCompetenceAssessmentId(),
                                true));
            }
        }
        return credentialAssessmentData;
    }

    protected void gradeCredentialAssessmentWithRubric(EventQueue events, long credentialAssessmentId, AssessmentType assessmentType, User actor, long... lvls) throws Exception {
        AssessmentDataFull credAssessmentData = getCredentialAssessmentData(credentialAssessmentId, actor.getId(), assessmentType);
        gradeCredentialAssessmentWithRubric(events, credAssessmentData, actor, assessmentType, lvls);
    }

    protected void gradeCredentialAssessmentWithRubric(EventQueue events, AssessmentDataFull credentialAssessmentData, User actor, AssessmentType assessmentType, long... lvls) {
        gradeWithRubric(credentialAssessmentData.getGradeData(), lvls);

        ApplicationPage page = getApplicationPageForCredentialAssessmentType(assessmentType);

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class)
                .updateGradeForCredentialAssessmentAndGetEvents(credentialAssessmentData.getCredAssessmentId(), credentialAssessmentData.getGradeData(), createUserContext(actor, new PageContextData(page.getUrl(), null, null))));
    }

    private ApplicationPage getApplicationPageForCredentialAssessmentType(AssessmentType type) {
        ApplicationPage page;
        if (type == AssessmentType.PEER_ASSESSMENT) {
            page = ApplicationPage.MY_ASSESSMENTS_CREDENTIAL_ASSESSMENT;
        } else if (type == AssessmentType.INSTRUCTOR_ASSESSMENT) {
            page = ApplicationPage.MANAGE_CREDENTIAL_ASSESSMENT;
        } else {
            page = ApplicationPage.CREDENTIAL_SELF_ASSESSMENT;
        }
        return page;
    }

    protected CompetenceAssessmentDataFull getCompetenceAssessmentData(long compAssessmentId, long actorId, AssessmentType assessmentType) {
        CompetenceAssessmentDataFull competenceAssessmentData = ServiceLocator.getInstance().getService(AssessmentManager.class).getCompetenceAssessmentData(
                compAssessmentId, actorId, assessmentType, AssessmentLoadConfig.of(true, true, true));
        //init grade data
        if (competenceAssessmentData.getGradeData().getGradingMode() == org.prosolo.services.assessment.data.grading.GradingMode.MANUAL_RUBRIC) {
            ((RubricGradeData) competenceAssessmentData.getGradeData()).setRubricCriteria(
                    ServiceLocator.getInstance().getService(RubricManager.class).getRubricDataForCompetence(
                            competenceAssessmentData.getCompetenceId(),
                            competenceAssessmentData.getCompetenceAssessmentId(),
                            true));
        }
        return competenceAssessmentData;
    }

    protected void gradeCompetenceAssessmentWithRubric(EventQueue events, long competenceAssessmentId, AssessmentType assessmentType, User actor, long... lvls) throws Exception {
        CompetenceAssessmentDataFull competenceAssessmentData = getCompetenceAssessmentData(competenceAssessmentId, actor.getId(), assessmentType);
        gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, actor, lvls);
    }

    protected void gradeCompetenceAssessmentWithRubric(EventQueue events, CompetenceAssessmentDataFull competenceAssessmentData, User actor, long... lvls) throws Exception {
        gradeWithRubric(competenceAssessmentData.getGradeData(), lvls);

        ApplicationPage page = getApplicationPageForCompetencyAssessmentType(competenceAssessmentData.getType());

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class)
                .updateGradeForCompetenceAssessmentAndGetEvents(competenceAssessmentData.getCompetenceAssessmentId(), competenceAssessmentData.getGradeData(), createUserContext(actor, new PageContextData(page.getUrl(), null, null))));
    }

    private ApplicationPage getApplicationPageForCompetencyAssessmentType(AssessmentType type) {
        ApplicationPage page;
        if (type == AssessmentType.PEER_ASSESSMENT) {
            page = ApplicationPage.MY_ASSESSMENTS_COMPETENCE_ASSESSMENT;
        } else if (type == AssessmentType.INSTRUCTOR_ASSESSMENT) {
            page = ApplicationPage.MANAGE_CREDENTIAL_ASSESSMENT;
        } else {
            page = ApplicationPage.CREDENTIAL_SELF_ASSESSMENT;
        }
        return page;
    }

    protected void gradeWithRubric(GradeData gradeData, long... lvls) {
        if (gradeData.getGradingMode() == org.prosolo.services.assessment.data.grading.GradingMode.MANUAL_RUBRIC) {
            RubricGradeData<? extends RubricCriteriaGradeData<? extends RubricCriterionGradeData>> rubricGradeData = (RubricGradeData) gradeData;
            List<? extends RubricCriterionGradeData> criteria = rubricGradeData.getRubricCriteria().getCriteria();
            for (int i = 0; i < criteria.size(); i++) {
                criteria.get(i).setLevelId(lvls[i]);
            }
        }
    }

    protected void updateManualSimpleGrade(GradeData gradeData, int grade) {
        if (gradeData.getGradingMode() == org.prosolo.services.assessment.data.grading.GradingMode.MANUAL_SIMPLE) {
            ManualSimpleGradeData manualGrade = (ManualSimpleGradeData) gradeData;
            manualGrade.setNewGrade(grade);
        }
    }

    protected void approveCredentialAssessment(EventQueue events, long credentialAssessmentId, User actor) throws Exception {
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class)
                .approveCredentialAndGetEvents(credentialAssessmentId, "Review", createUserContext(actor)));
    }

    protected void approveCredentialAssessment(EventQueue events, long credentialAssessmentId, long credentialId, User actor) throws Exception {
        String learningContext= MessageFormat.format("name:CREDENTIAL|id:{0}/context:/name:CREDENTIAL_ASSESSMENT|id:{1}/", credentialId+"", credentialAssessmentId+"");

        PageContextData context = new PageContextData("/manage/credential-assessment.xhtml", learningContext, null);

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class)
                .approveCredentialAndGetEvents(credentialAssessmentId, "Review", createUserContext(actor, context)));
    }

    protected void approveCompetenceAssessment(EventQueue events, long competenceAssessmentId, long credentialId, long competenceId, User actor) {
        String learningContext= MessageFormat.format("name:CREDENTIAL|id:{0}/context:/name:COMPETENCE|id:{1}|context:/name:COMPETENCE_ASSESSMENT|id:{2}//", credentialId+"", competenceId+"", competenceAssessmentId+"");

        PageContextData context = new PageContextData("/manage/credential-assessment.xhtml", learningContext, null);

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class)
                .approveCompetenceAndGetEvents(competenceAssessmentId, true, createUserContext(actor, context)));
    }

    protected void approveCompetenceAssessment(EventQueue events, long competenceAssessmentId, User actor) {
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class)
                .approveCompetenceAndGetEvents(competenceAssessmentId, true, createUserContext(actor)));
    }

    protected void completeActivity(EventQueue events, long targetCompetenceId, long targetActivityId, User actor) {
        extractResultAndAddEvents(
                events,
                ServiceLocator.getInstance().getService(Activity1Manager.class).completeActivityAndGetEvents(
                        targetActivityId,
                        targetCompetenceId,
                        createUserContext(actor)));
    }

    protected void updateInstructorMaxNumberOfStudents(EventQueue events, long credId, long instructorId, int maxNumberOfStudents, UserContextData context) {
        InstructorData id = new InstructorData(false);
        id.setMaxNumberOfStudents(-1);
        id.setInstructorId(instructorId);
        id.startObservingChanges();
        id.setMaxNumberOfStudents(maxNumberOfStudents);
        extractResultAndAddEvents(
                events,
                ServiceLocator.getInstance().getService(CredentialInstructorManager.class)
                        .updateInstructorAndStudentsAssignedAndGetEvents(credId, id, null, null, context));
    }

    protected void updateInstructorAssignMode(long credentialId, AssessorAssignmentMethod assessorAssignmentMethod) {
        Session session = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Credential1 cred = (Credential1) session.load(Credential1.class, credentialId);
            cred.setAssessorAssignmentMethod(assessorAssignmentMethod);
            transaction.commit();
        } catch (Exception e) {
            getLogger().error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session);
        }
    }

    protected void addCommentToCredentialAssessmentDiscussion(EventQueue events, long credentialAssessmentId, AssessmentType type, User sender, String comment) {
        ApplicationPage page = getApplicationPageForCredentialAssessmentType(type);
        extractResultAndAddEvents(
                events,
                ServiceLocator.getInstance().getService(AssessmentManager.class).addCommentToCredentialAssessmentAndGetEvents(
                        credentialAssessmentId,
                        sender.getId(),
                        comment,
                        createUserContext(sender, new PageContextData(page.getUrl(), null, null))));
    }

    protected void addCommentToCompetenceAssessmentDiscussion(EventQueue events, long compAssessmentId, AssessmentType type, User sender, String comment) {
        ApplicationPage page = getApplicationPageForCompetencyAssessmentType(type);
        extractResultAndAddEvents(
                events,
                ServiceLocator.getInstance().getService(AssessmentManager.class).addCommentToCompetenceAssessmentAndGetEvents(
                        compAssessmentId,
                        sender.getId(),
                        comment,
                        createUserContext(sender, new PageContextData(page.getUrl(), null, null))));
    }

    protected void enableTokensPlugin(int initialNumberOfTokens, int tokensSpentPerRequest, int tokensEarnedPerAssessment) {
        AssessmentTokensPlugin tokensPlugin = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationPlugin(AssessmentTokensPlugin.class, organization.getId());
        AssessmentTokensPluginData tokensPluginData = new AssessmentTokensPluginData(tokensPlugin);
        tokensPluginData.setEnabled(true);
        tokensPluginData.setInitialNumberOfTokensGiven(initialNumberOfTokens);
        tokensPluginData.setNumberOfSpentTokensPerRequest(tokensSpentPerRequest);
        tokensPluginData.setNumberOfEarnedTokensPerAssessment(tokensEarnedPerAssessment);
        ServiceLocator.getInstance().getService(OrganizationManager.class).updateAssessmentTokensPlugin(tokensPluginData);
    }

    protected CompetenceAssessmentData askPeerFromPoolForCompetenceAssessment(EventQueue events, long deliveryId, long compId, User student, int numberOfTokensToSpend, boolean tokensEnabled) throws Exception {
        UserData assessor = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getPeerFromAvailableAssessorsPoolForCompetenceAssessment(
                        deliveryId,
                        compId,
                        student.getId(),
                        tokensEnabled);
        return askPeerForCompetenceAssessment(events, deliveryId, compId, student, assessor != null ? assessor.getId() : 0, numberOfTokensToSpend);
    }

    protected void updateUserNumberOfTokens(EventQueue events, long userId, List<Long> allRoles, int numberOfTokens) {
        UserData user = ServiceLocator.getInstance().getService(UserManager.class).getUserWithRoles(userId, organization.getId());
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserManager.class)
                .updateUserAndGetEvents(
                        user.getId(),
                        user.getName(),
                        user.getLastName(),
                        user.getEmail(),
                        true,
                        false,
                        user.getPassword(),
                        user.getPosition(),
                        numberOfTokens,
                        user.getRoleIds(),
                        allRoles,
                        createUserContext(userNickPowell)));
    }

    protected void setBlindAssessmentModeForCredentialCompetencies(EventQueue events, long credentialId, BlindAssessmentMode assessmentMode, User actor) throws Exception {
        List<Long> ids = ServiceLocator.getInstance().getService(CredentialManager.class).getIdsOfAllCompetencesInACredential(credentialId);
        for (long id : ids) {
            updateCompetenceBlindAssessmentMode(events, id, assessmentMode, actor);
        }
    }


    protected abstract String getBusinessCaseInitLog();
    protected abstract void createAdditionalData(EventQueue events) throws Exception;
    public abstract Logger getLogger();

}