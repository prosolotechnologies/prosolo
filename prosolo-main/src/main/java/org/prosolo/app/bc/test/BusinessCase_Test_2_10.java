package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.AssessorAssignmentMethod;
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
 * @author Nikola Milikic
 * @date 2019-01-22
 * @since 1.3
 */
public class BusinessCase_Test_2_10 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_10.class.getName());

    private LearningEvidence evidence1Helen;
    private LearningEvidence evidence2Helen;
    private LearningEvidence evidence3Helen;

    private LearningEvidence evidence1George;
    private LearningEvidence evidence2George;
    private LearningEvidence evidence3George;


    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        updateInstructorAssignMode(credential1Delivery1.getId(), AssessorAssignmentMethod.BY_STUDENTS);
        updateInstructorMaxNumberOfStudents(events, credential1Delivery1.getId(), credential1Delivery1InstructorKarenWhite.getId(), 1, createUserContext(userNickPowell));

        createEvidenceForGeorgeYoung(events);
        enrollGeorgYoungToDelivery1(events);

        createEvidenceForHelenCampbell(events);
        enrollHelenCampbellToDelivery1(events);
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
        TargetCompetence1 credential6Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6CompetenciesHelenCampbell.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1Helen.getId(), credential6Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2Helen.getId(), credential6Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");
        attachExistingEvidenceToCompetence(evidence3Helen.getId(), credential6Comp2Target.getId(), "Contains structure of the new version of a teaching program.");
        attachExistingEvidenceToCompetence(evidence3Helen.getId(), credential6Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidence2Helen.getId(), credential6Comp4Target.getId(), "Teaching strategies success analysis for the K-12 programme.");

        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential6Comp1Target.getId(),
                        credential6Comp2Target.getId(),
                        credential6Comp3Target.getId(),
                        credential6Comp4Target.getId()
                ),
                userHelenCampbell);

        //grade and approve instructor assessment
        long credential6Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getActiveInstructorCredentialAssessmentId(credential6Delivery1.getId(), userHelenCampbell.getId()).get();
        AssessmentDataFull instructorCredentialAssessmentData = getCredentialAssessmentData(credential6Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
        gradeCredentialAssessmentByRubric(events, instructorCredentialAssessmentData, userPhilArmstrong, rubricData.getLevels().get(2).getId());
        for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredentialAssessmentData.getCompetenceAssessmentData()) {
            long lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp1Target.getId()) {
                lvl = rubricData.getLevels().get(1).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp2Target.getId()) {
                lvl = rubricData.getLevels().get(3).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp3Target.getId()) {
                lvl = rubricData.getLevels().get(2).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential6Comp4Target.getId()) {
                lvl = rubricData.getLevels().get(0).getId();
            }
            gradeCompetenceAssessmentByRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
        }
        approveCredentialAssessment(events, instructorCredentialAssessmentData.getCredAssessmentId(), userPhilArmstrong);
    }

    private void enrollHelenCampbellToDelivery1(EventQueue events) throws Exception {
        enrollToDelivery(events, credential1Delivery1, userHelenCampbell);
        //enroll in competencies
        List<CompetenceData1> credential1CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1CompetenciesHelenCampbell.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1CompetenciesHelenCampbell.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1CompetenciesHelenCampbell.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1Helen.getId(), credential1Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2Helen.getId(), credential1Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");
        attachExistingEvidenceToCompetence(evidence3Helen.getId(), credential1Comp2Target.getId(), "Contains structure of the new version of a teaching program.");
        attachExistingEvidenceToCompetence(evidence3Helen.getId(), credential1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
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

    private void createEvidenceForGeorgeYoung(EventQueue events) {
        evidence1George = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://devfiles.prosolo.ca.s3-us-west-1.amazonaws.com/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                userGeorgeYoung);
        evidence2George = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                userGeorgeYoung);
        evidence3George = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF1_TEST_FILE,
                "teaching program",
                userGeorgeYoung);
    }

    private void enrollGeorgYoungToDelivery1(EventQueue events) throws Exception {
        enrollToDelivery(events, credential1Delivery1, userGeorgeYoung);
        assignInstructorToStudent(events, credential1Delivery1InstructorKarenWhite, List.of(userGeorgeYoung), credential1Delivery1);

        //enroll in competencies
        List<CompetenceData1> credential1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userGeorgeYoung.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Competencies.get(0).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        TargetCompetence1 credential1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Competencies.get(1).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        TargetCompetence1 credential1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Competencies.get(2).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        TargetCompetence1 credential1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Competencies.get(3).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        TargetCompetence1 credential1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Competencies.get(4).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));

        // add pieces of evidence to the all competencies
        attachExistingEvidenceToCompetence(evidence1George.getId(), credential1Comp1Target.getId(), "Learning plan incorporating teaching strategies.");
        attachExistingEvidenceToCompetence(evidence2George.getId(), credential1Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");
        attachExistingEvidenceToCompetence(evidence3George.getId(), credential1Comp2Target.getId(), "Contains structure of the new version of a teaching program.");
        attachExistingEvidenceToCompetence(evidence3George.getId(), credential1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential1Comp2Target.getId(),
                        credential1Comp3Target.getId()
                ),
                userGeorgeYoung);

        long credential1Delivery1InstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getActiveInstructorCredentialAssessmentId(credential1Delivery1.getId(), userGeorgeYoung.getId()).get();
        AssessmentDataFull instructorCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1InstructorAssessmentId, userKarenWhite.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
        for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredentialAssessmentData.getCompetenceAssessmentData()) {
            if (competenceAssessmentData.getTargetCompetenceId() > 0) {
                long lvl = 0;
                if (competenceAssessmentData.getTargetCompetenceId() == credential1Comp1Target.getId()) {
                    lvl = rubricData.getLevels().get(1).getId();
                } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Comp2Target.getId()) {
                    lvl = rubricData.getLevels().get(3).getId();
                } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Comp3Target.getId()) {
                    lvl = rubricData.getLevels().get(2).getId();
                } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Comp4Target.getId()) {
                    lvl = rubricData.getLevels().get(0).getId();
                }
                gradeCompetenceAssessmentByRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
                if (competenceAssessmentData.getTargetCompetenceId() == credential1Comp2Target.getId()) {
                    approveCompetenceAssessment(events, competenceAssessmentData.getCompetenceAssessmentId(), userGeorgeYoung);
                }
                if (competenceAssessmentData.getTargetCompetenceId() == credential1Comp5Target.getId()) {
                    addCommentToCompetenceAssessmentDiscussion(events, competenceAssessmentData.getCompetenceAssessmentId(), userKarenWhite, "More evidence is needed");
                }
            }
        }
        addCommentToCredentialAssessmentDiscussion(events, instructorCredentialAssessmentData.getCredAssessmentId(), userKarenWhite, "All focus areas need to be completed");

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
