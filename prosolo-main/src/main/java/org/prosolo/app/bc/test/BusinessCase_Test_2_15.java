package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.event.EventQueue;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-06-27
 * @since 1.3.2
 */
public class BusinessCase_Test_2_15 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_15.class.getName());

    private LearningEvidence evidence1Helen;
    private LearningEvidence evidence2Helen;
    private LearningEvidence evidence3Helen;


    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        createEvidenceForHelenCampbell(events);
        enrollHelenCampbellToDelivery6(events);

    }

    private void enrollHelenCampbellToDelivery6(EventQueue events) throws Exception {
        enrollToDelivery(events, credential6Delivery1, userHelenCampbell);

        //enroll in competencies
        List<CompetenceData1> credential6CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential6Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential6Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential6Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential6Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1Helen.getId(), credential6Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2Helen.getId(), credential6Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");
        attachExistingEvidenceToCompetence(evidence3Helen.getId(), credential6Comp2Target.getId(), "Contains structure of the new version of a teaching program.");
        attachExistingEvidenceToCompetence(evidence3Helen.getId(), credential6Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        ServiceLocator.getInstance().getService(Competence1Manager.class).saveEvidenceSummary(credential6Comp2Target.getId(), "Evidence Summary from Helen Campbell for focus area 6.2 Engage in professional learning and improve practice");

        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential6Comp1Target.getId(),
                        credential6Comp2Target.getId(),
                        credential6Comp3Target.getId()
                ),
                userHelenCampbell);

        long selfAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getSelfCredentialAssessmentId(credential6Delivery1.getId(), userHelenCampbell.getId()).get();
        AssessmentDataFull assessment = getCredentialAssessmentData(selfAssessmentId, userHelenCampbell.getId(), AssessmentType.SELF_ASSESSMENT);
        for (CompetenceAssessmentDataFull competenceAssessmentData : assessment.getCompetenceAssessmentData()) {
            long lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp2Target.getId()) {
                lvl = rubricData.getLevels().get(3).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp3Target.getId()) {
                lvl = rubricData.getLevels().get(2).getId();
            }
            if (lvl > 0) {
                gradeCompetenceAssessmentByRubric(events, competenceAssessmentData, userHelenCampbell, lvl);
            }
            if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp2Target.getId()) {
                addCommentToCompetenceAssessmentDiscussion(events, competenceAssessmentData.getCompetenceAssessmentId(), userHelenCampbell, "Reminder: upload more evidence");
            }
            if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp3Target.getId()) {
               approveCompetenceAssessment(events, competenceAssessmentData.getCompetenceAssessmentId(), userHelenCampbell);
            }
        }
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
        evidence3Helen = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF1_TEST_FILE,
                "teaching program",
                userHelenCampbell);
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.11";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
