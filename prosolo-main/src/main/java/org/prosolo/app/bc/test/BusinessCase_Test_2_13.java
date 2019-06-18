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
 * @date 2019-06-12
 * @since 1.3.2
 */
public class BusinessCase_Test_2_13 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_13.class.getName());

    private LearningEvidence evidence1Helen;
    private LearningEvidence evidence2Helen;

    private int tokensSpentPerRequest = 2;


    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        enableTokensPlugin(10, tokensSpentPerRequest, 2);

        setBlindAssessmentModeForCredentialCompetencies(events, credential6Delivery1.getId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);

        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userRichardAnderson.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userStevenTurner.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userJosephGarcia.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userTimothyRivera.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userKevinHall.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userKennethCarter.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userAnthonyMoore.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userLoriAbner.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userSamanthaDell.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userSheriLaureano.getId(), true);

        List<Long> roles = ServiceLocator.getInstance().getService(RoleManager.class)
                .getRolesByNames(new String[]{SystemRoleNames.USER, SystemRoleNames.INSTRUCTOR, SystemRoleNames.MANAGER, SystemRoleNames.ADMIN})
                .stream().map(Role::getId).collect(Collectors.toList());

        updateUserNumberOfTokens(events, userHelenCampbell.getId(), roles, 17);
        updateUserNumberOfTokens(events, userRichardAnderson.getId(), roles, 2);
        updateUserNumberOfTokens(events, userStevenTurner.getId(), roles, 3);
        updateUserNumberOfTokens(events, userJosephGarcia.getId(), roles, 4);
        updateUserNumberOfTokens(events, userTimothyRivera.getId(), roles, 5);
        updateUserNumberOfTokens(events, userKevinHall.getId(), roles, 6);
        updateUserNumberOfTokens(events, userKennethCarter.getId(), roles, 7);
        updateUserNumberOfTokens(events, userAnthonyMoore.getId(), roles, 8);
        updateUserNumberOfTokens(events, userLoriAbner.getId(), roles, 9);
        updateUserNumberOfTokens(events, userSamanthaDell.getId(), roles, 10);
        updateUserNumberOfTokens(events, userSheriLaureano.getId(), roles, 11);
        updateUserNumberOfTokens(events, userTaniaCortese.getId(), roles, 2);
        updateUserNumberOfTokens(events, userSonyaElston.getId(), roles, 2);
        updateUserNumberOfTokens(events, userAngelicaFallon.getId(), roles, 2);
        updateUserNumberOfTokens(events, userIdaFritz.getId(), roles, 2);


        createEvidenceForHelenCampbell(events);

        enrollTimothyRiveraToDelivery6(events);
        //these two users should have different enrollment date
        Thread.sleep(1500);
        enrollStevenTurnerToDelivery6(events);
        enrollKevinHallToDelivery6(events);
        enrollRichardAndersonToDelivery6(events);
        enrollJosephGarciaToDelivery6(events);
        enrollKennethCarterToDelivery6(events);
        enrollAnthonyMooreToDelivery6(events);
        enrollLoriAbnerToDelivery6(events);
        enrollSamanthaDellToDelivery6(events);
        enrollSheriLaureanoToDelivery6(events);
        enrollHelenCampbellToDelivery6(events);

        updateUserNumberOfTokens(events, userJosephGarcia.getId(), roles, 7);
        updateUserNumberOfTokens(events, userKevinHall.getId(), roles, 10);
        updateUserNumberOfTokens(events, userSamanthaDell.getId(), roles, 0);
        updateUserNumberOfTokens(events, userStevenTurner.getId(), roles, 10);
        updateUserNumberOfTokens(events, userTimothyRivera.getId(), roles, 10);
        updateUserNumberOfTokens(events, userRichardAnderson.getId(), roles, 30);

        enrollTaniaCorteseToDelivery6(events);
        enrollSonyaElstonToDelivery6(events);
        enrollAngelicaFallonToDelivery6(events);
        enrollIdaFritzToDelivery6(events);

        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userKennethCarter.getId(), false);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userAnthonyMoore.getId(), false);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userLoriAbner.getId(), false);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userSamanthaDell.getId(), false);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userSheriLaureano.getId(), false);

    }

    private void enrollRichardAndersonToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userRichardAnderson);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userRichardAnderson.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));

        CompetenceAssessment competenceAssessment1 = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userRichardAnderson, userKevinHall.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declineCompetenceAssessmentRequestAndGetEvents(competenceAssessment1.getId(), createUserContext(userKevinHall)));

        CompetenceAssessment competenceAssessment2 = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userRichardAnderson, userTimothyRivera.getId(), tokensSpentPerRequest);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declineCompetenceAssessmentRequestAndGetEvents(competenceAssessment2.getId(), createUserContext(userTimothyRivera)));

        Session session = (Session) ServiceLocator.getInstance().getService(DefaultManager.class).getPersistence().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            CompetenceAssessment ca1 = (CompetenceAssessment) session.load(CompetenceAssessment.class, competenceAssessment1.getId());
            ca1.setDateCreated(DateUtil.getNDaysBeforeNow(32));
            ca1.setQuitDate(DateUtil.getNDaysBeforeNow(31));

            CompetenceAssessment ca2 = (CompetenceAssessment) session.load(CompetenceAssessment.class, competenceAssessment2.getId());
            ca2.setDateCreated(DateUtil.getNDaysBeforeNow(33));
            ca2.setQuitDate(DateUtil.getNDaysBeforeNow(32));

            transaction.commit();
        } catch (Exception e) {
            getLogger().error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session);
        }
    }

    private void enrollStevenTurnerToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userStevenTurner);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
    }

    private void enrollJosephGarciaToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userJosephGarcia);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userJosephGarcia.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userJosephGarcia.getId(), createUserContext(userJosephGarcia)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userJosephGarcia.getId(), createUserContext(userJosephGarcia)));
    }

    private void enrollTimothyRiveraToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userTimothyRivera);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userTimothyRivera.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));
    }

    private void enrollKevinHallToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userKevinHall);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
    }

    private void enrollKennethCarterToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userKennethCarter);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userKennethCarter.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userKennethCarter.getId(), createUserContext(userKennethCarter)));
    }

    private void enrollAnthonyMooreToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userAnthonyMoore);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userAnthonyMoore.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userAnthonyMoore.getId(), createUserContext(userAnthonyMoore)));
    }

    private void enrollLoriAbnerToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userLoriAbner);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userLoriAbner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userLoriAbner.getId(), createUserContext(userLoriAbner)));
    }

    private void enrollSamanthaDellToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userSamanthaDell);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userSamanthaDell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userSamanthaDell.getId(), createUserContext(userSamanthaDell)));
    }

    private void enrollSheriLaureanoToDelivery6(EventQueue events) {
        enrollToDelivery(events, credential6Delivery1, userSheriLaureano);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userSheriLaureano.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(0).getCompetenceId(), userSheriLaureano.getId(), createUserContext(userSheriLaureano)));
    }

    private void enrollTaniaCorteseToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userTaniaCortese);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userTaniaCortese.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userTaniaCortese.getId(), createUserContext(userTaniaCortese)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userTaniaCortese, userStevenTurner.getId(), tokensSpentPerRequest);
    }

    private void enrollSonyaElstonToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userSonyaElston);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userSonyaElston.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userSonyaElston.getId(), createUserContext(userSonyaElston)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userSonyaElston, userTimothyRivera.getId(), tokensSpentPerRequest);
    }

    private void enrollAngelicaFallonToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userAngelicaFallon);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userAngelicaFallon.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userAngelicaFallon.getId(), createUserContext(userAngelicaFallon)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userAngelicaFallon, userStevenTurner.getId(), tokensSpentPerRequest);
    }

    private void enrollIdaFritzToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userIdaFritz);

        //enroll in competencies
        List<CompetenceData1> credential6Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userIdaFritz.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userIdaFritz.getId(), createUserContext(userIdaFritz)));

        CompetenceAssessment competenceAssessment = askPeerForCompetenceAssessment(events, credential6Delivery1.getId(), credential6Competencies.get(1).getCompetenceId(), userIdaFritz, userTimothyRivera.getId(), tokensSpentPerRequest);
    }

    private void enrollHelenCampbellToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userHelenCampbell);

        //enroll in competencies
        List<CompetenceData1> credential6CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1Helen.getId(), credential6Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2Helen.getId(), credential6Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");

        ServiceLocator.getInstance().getService(Competence1Manager.class).saveEvidenceSummary(credential6Comp1Target.getId(), "Evidence Summary from Helen Campbell for focus area 6.1");

        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential6Comp1Target.getId()),
                userHelenCampbell);

        CompetenceAssessment competenceAssessment1 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment1.getId(), createUserContext(userRichardAnderson)));
        gradeCompetenceAssessmentByRubric(events, competenceAssessment1.getId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
        approveCompetenceAssessment(events, competenceAssessment1.getId(), userRichardAnderson);

        CompetenceAssessment competenceAssessment2 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment2.getId(), createUserContext(userStevenTurner)));
        gradeCompetenceAssessmentByRubric(events, competenceAssessment2.getId(), AssessmentType.PEER_ASSESSMENT, userStevenTurner, rubricData.getLevels().get(3).getId());
        addCommentToCompetenceAssessmentDiscussion(events, competenceAssessment2.getId(), userStevenTurner, "More evidence is needed");

        CompetenceAssessment competenceAssessment3 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declineCompetenceAssessmentRequestAndGetEvents(competenceAssessment3.getId(), createUserContext(userJosephGarcia)));

        CompetenceAssessment competenceAssessment4 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);

        CompetenceAssessment competenceAssessment5 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).expireCompetenceAssessmentRequestAndGetEvents(competenceAssessment5.getId(), createUserContext(userKevinHall)));

        CompetenceAssessment competenceAssessment6 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment6.getId(), createUserContext(userKennethCarter)));

        CompetenceAssessment competenceAssessment7 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(competenceAssessment7.getId(), createUserContext(userAnthonyMoore)));
        gradeCompetenceAssessmentByRubric(events, competenceAssessment7.getId(), AssessmentType.PEER_ASSESSMENT, userAnthonyMoore, rubricData.getLevels().get(0).getId());
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).declinePendingCompetenceAssessmentAndGetEvents(competenceAssessment7.getId(), createUserContext(userAnthonyMoore)));

        CompetenceAssessment competenceAssessment8 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        CompetenceAssessment competenceAssessment9 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        CompetenceAssessment competenceAssessment10 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
        CompetenceAssessment competenceAssessment11 = askPeerFromPoolForCompetenceAssessment(events, credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, tokensSpentPerRequest, true);
    }

    private void createEvidenceForHelenCampbell(EventQueue events) {
        evidence1Helen = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://devfiles.prosolo.ca.s3-us-west-1.amazonaws.com/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                userHelenCampbell);
        evidence2Helen = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                userHelenCampbell);
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
