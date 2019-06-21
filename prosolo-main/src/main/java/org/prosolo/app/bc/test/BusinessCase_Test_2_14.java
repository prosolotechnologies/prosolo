package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.util.roles.SystemRoleNames;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2019-06-18
 * @since 1.3.2
 */
public class BusinessCase_Test_2_14 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_14.class.getName());

    private int tokensSpentPerRequest = 2;

    private long credential6comp2Id;
    private long credential6comp3Id;

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        enableTokensPlugin(10, tokensSpentPerRequest, 2);

        setBlindAssessmentModeForCredentialCompetencies(events, credential6Delivery1.getId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);

        List<Long> roles = ServiceLocator.getInstance().getService(RoleManager.class)
                .getRolesByNames(new String[]{SystemRoleNames.USER, SystemRoleNames.INSTRUCTOR, SystemRoleNames.MANAGER, SystemRoleNames.ADMIN})
                .stream().map(Role::getId).collect(Collectors.toList());

        updateUserNumberOfTokens(events, userHelenCampbell.getId(), roles, 10);
        updateUserNumberOfTokens(events, userRichardAnderson.getId(), roles, 10);
        updateUserNumberOfTokens(events, userStevenTurner.getId(), roles, 10);
        updateUserNumberOfTokens(events, userJosephGarcia.getId(), roles, 10);
        updateUserNumberOfTokens(events, userTimothyRivera.getId(), roles, 10);
        updateUserNumberOfTokens(events, userKevinHall.getId(), roles, 10);
        updateUserNumberOfTokens(events, userKennethCarter.getId(), roles, 10);
        updateUserNumberOfTokens(events, userAnthonyMoore.getId(), roles, 10);
        updateUserNumberOfTokens(events, userLoriAbner.getId(), roles, 10);
        updateUserNumberOfTokens(events, userSamanthaDell.getId(), roles, 10);
        updateUserNumberOfTokens(events, userSheriLaureano.getId(), roles, 10);
        updateUserNumberOfTokens(events, userTaniaCortese.getId(), roles, 10);
        updateUserNumberOfTokens(events, userSonyaElston.getId(), roles, 10);
        updateUserNumberOfTokens(events, userAngelicaFallon.getId(), roles, 2);
        updateUserNumberOfTokens(events, userIdaFritz.getId(), roles, 10);

        enrollRichardAndersonToDelivery6(events);
        enrollTimothyRiveraToDelivery6(events);
        enrollStevenTurnerToDelivery6(events);
        //steven should enroll before sonya and richard before kevin
        Thread.sleep(1500);
        enrollSonyaElstonToDelivery6(events);
        enrollKevinHallToDelivery6(events);
        enrollJosephGarciaToDelivery6(events);
        enrollKennethCarterToDelivery6(events);
        enrollAnthonyMooreToDelivery6(events);
        enrollSamanthaDellToDelivery6(events);
        enrollSheriLaureanoToDelivery6(events);
        enrollHelenCampbellToDelivery6(events);
        enrollTaniaCorteseToDelivery6(events);
        enrollAngelicaFallonToDelivery6(events);
        enrollIdaFritzToDelivery6(events);

        askForAssessmentOfCompetency2(events);
        askForAssessmentOfCompetency3(events);

        updateUserNumberOfTokens(events, userIdaFritz.getId(), roles, 3);
        updateUserNumberOfTokens(events, userRichardAnderson.getId(), roles, 5);
        updateUserNumberOfTokens(events, userKevinHall.getId(), roles, 7);
        updateUserNumberOfTokens(events, userSonyaElston.getId(), roles, 8);
        updateUserNumberOfTokens(events, userStevenTurner.getId(), roles, 8);
        updateUserNumberOfTokens(events, userJosephGarcia.getId(), roles, 15);

        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userRichardAnderson.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userIdaFritz.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userKevinHall.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userSonyaElston.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userStevenTurner.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userJosephGarcia.getId(), true);
    }

    private void askForAssessmentOfCompetency3(EventQueue events) throws Exception {
        CompetenceAssessment competenceAssessment1 = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp3Id, userRichardAnderson, userJosephGarcia.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declineCompetenceAssessmentRequestAndGetEvents(competenceAssessment1.getId(), createUserContext(userJosephGarcia)));
        CompetenceAssessment competenceAssessment2 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp3Id, userRichardAnderson, tokensSpentPerRequest, true);

        CompetenceAssessment competenceAssessment3 = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp3Id, userJosephGarcia, userRichardAnderson.getId(), tokensSpentPerRequest);
        Session session = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            CompetenceAssessment ca3 = (CompetenceAssessment) session.load(CompetenceAssessment.class, competenceAssessment3.getId());
            ca3.setDateCreated(DateUtil.getNDaysBeforeNow(15));

            transaction.commit();
        } catch (Exception e) {
            getLogger().error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session);
        }
    }

    private void askForAssessmentOfCompetency2(EventQueue events) throws Exception {
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userJosephGarcia, userIdaFritz.getId(), 0);
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userJosephGarcia, userSonyaElston.getId(), 0);
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userJosephGarcia, userStevenTurner.getId(), 0);

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userJosephGarcia, userRichardAnderson.getId(), 0);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declineCompetenceAssessmentRequestAndGetEvents(competenceAssessment.getId(), createUserContext(userRichardAnderson)));

        Session session = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            CompetenceAssessment ca = (CompetenceAssessment) session.load(CompetenceAssessment.class, competenceAssessment.getId());
            ca.setDateCreated(DateUtil.getNDaysBeforeNow(32));
            ca.setQuitDate(DateUtil.getNDaysBeforeNow(31));

            transaction.commit();
        } catch (Exception e) {
            getLogger().error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session);
        }
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userTimothyRivera, userRichardAnderson.getId(), 0);
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userTimothyRivera, userKevinHall.getId(), 0);
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userTimothyRivera, userIdaFritz.getId(), 0);
        askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userKevinHall, userRichardAnderson.getId(), 0);

        askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userRichardAnderson, 0, true);
        askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userJosephGarcia, 0, true);
        askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6comp2Id, userTimothyRivera, 0, true);
    }

    private void enrollRichardAndersonToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userRichardAnderson);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userRichardAnderson.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        credential6comp2Id = credential6Competencies.get(1).getCompetenceId();
        credential6comp3Id = credential6Competencies.get(2).getCompetenceId();
        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
        TargetCompetence1 credential6Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(2).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
    }

    private void enrollStevenTurnerToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userStevenTurner);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userStevenTurner, userRichardAnderson.getId(), tokensSpentPerRequest);
    }

    private void enrollJosephGarciaToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userJosephGarcia);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userJosephGarcia.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userJosephGarcia.getId(), createUserContext(userJosephGarcia)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userJosephGarcia.getId(), createUserContext(userJosephGarcia)));
        TargetCompetence1 credential6Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(2).getCompetenceId(), userJosephGarcia.getId(), createUserContext(userJosephGarcia)));

        CompetenceAssessment competenceAssessment1 = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userJosephGarcia, userRichardAnderson.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declineCompetenceAssessmentRequestAndGetEvents(competenceAssessment1.getId(), createUserContext(userRichardAnderson)));
    }

    private void enrollTimothyRiveraToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userTimothyRivera);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userTimothyRivera.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userTimothyRivera, userRichardAnderson.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).expireCompetenceAssessmentRequestAndGetEvents(competenceAssessment.getId(), createUserContext(userNickPowell)));
    }

    private void enrollKevinHallToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userKevinHall);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userKevinHall, userRichardAnderson.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment.getId(), createUserContext(userRichardAnderson)));
        addCommentToCompetenceAssessmentDiscussion(events, competenceAssessment.getId(), userKevinHall, "I plan to upload more evidence soon");
    }

    private void enrollKennethCarterToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userKennethCarter);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userKennethCarter.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userKennethCarter.getId(), createUserContext(userKennethCarter)));

        LearningEvidence evidence1 = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan 3",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://devfiles.prosolo.ca.s3-us-west-1.amazonaws.com/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                userKennethCarter);
        LearningEvidence evidence2 = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis 3",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                userKennethCarter);
        // add pieces of evidence to the competency
        attachExistingEvidenceToCompetence(evidence1.getId(), credential6Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2.getId(), credential6Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userKennethCarter, userRichardAnderson.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment.getId(), createUserContext(userRichardAnderson)));
        gradeCompetenceAssessmentByRubric(events, competenceAssessment.getId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(0).getId());
        addCommentToCompetenceAssessmentDiscussion(events, competenceAssessment.getId(), userKennethCarter, "I plan to upload more evidence soon");
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declinePendingCompetenceAssessmentAndGetEvents(competenceAssessment.getId(), createUserContext(userRichardAnderson)));
    }

    private void enrollAnthonyMooreToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userAnthonyMoore);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userAnthonyMoore.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userAnthonyMoore.getId(), createUserContext(userAnthonyMoore)));

        LearningEvidence evidence1 = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan 2",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://devfiles.prosolo.ca.s3-us-west-1.amazonaws.com/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                userAnthonyMoore);
        LearningEvidence evidence2 = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis 2",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                userAnthonyMoore);
        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1.getId(), credential6Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2.getId(), credential6Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userAnthonyMoore, userRichardAnderson.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment.getId(), createUserContext(userRichardAnderson)));
        gradeCompetenceAssessmentByRubric(events, competenceAssessment.getId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
        approveCompetenceAssessment(events, competenceAssessment.getId(), userRichardAnderson);
    }

//    private void enrollLoriAbnerToDelivery6(EventQueue events) throws Exception {
//        enrollToDelivery(events, credential6Delivery1, userLoriAbner);
//
//        //enroll in competencies
//        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userLoriAbner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
//
//        // we need a reference to the TargetCompetence1
//        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userLoriAbner.getId(), createUserContext(userLoriAbner)));
//
//        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userLoriAbner, userRichardAnderson.getId(), tokensSpentPerRequest);
//    }

    private void enrollSamanthaDellToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userSamanthaDell);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userSamanthaDell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userSamanthaDell.getId(), createUserContext(userSamanthaDell)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userSamanthaDell, userRichardAnderson.getId(), tokensSpentPerRequest);
    }

    private void enrollSheriLaureanoToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userSheriLaureano);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userSheriLaureano.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userSheriLaureano.getId(), createUserContext(userSheriLaureano)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userSheriLaureano, userRichardAnderson.getId(), tokensSpentPerRequest);
    }

    private void enrollTaniaCorteseToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userTaniaCortese);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userTaniaCortese.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userTaniaCortese.getId(), createUserContext(userTaniaCortese)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userTaniaCortese, userRichardAnderson.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment.getId(), createUserContext(userRichardAnderson)));
        gradeCompetenceAssessmentByRubric(events, competenceAssessment.getId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(1).getId());
        addCommentToCompetenceAssessmentDiscussion(events, competenceAssessment.getId(), userTaniaCortese, "I plan to upload more evidence soon");
    }

    private void enrollSonyaElstonToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userSonyaElston);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userSonyaElston.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userSonyaElston.getId(), createUserContext(userSonyaElston)));

        //CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userSonyaElston, userTimothyRivera.getId(), tokensSpentPerRequest);
    }

    private void enrollAngelicaFallonToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userAngelicaFallon);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userAngelicaFallon.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userAngelicaFallon.getId(), createUserContext(userAngelicaFallon)));
    }

    private void enrollIdaFritzToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userIdaFritz);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userIdaFritz.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userIdaFritz.getId(), createUserContext(userIdaFritz)));
    }

    private void enrollHelenCampbellToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userHelenCampbell);

        //enroll in competencies
        List<CompetenceData1> credential6CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        //create evidence
        LearningEvidence evidence1Helen = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://devfiles.prosolo.ca.s3-us-west-1.amazonaws.com/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                userHelenCampbell);
        LearningEvidence evidence2Helen = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                userHelenCampbell);
        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1Helen.getId(), credential6Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2Helen.getId(), credential6Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");

        ServiceLocator.getInstance().getService(Competence1Manager.class).saveEvidenceSummary(credential6Comp1Target.getId(), "Evidence Summary from Helen Campbell for focus area 6.1");

        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential6Comp1Target.getId()),
                userHelenCampbell);

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), tokensSpentPerRequest);
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.10";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
