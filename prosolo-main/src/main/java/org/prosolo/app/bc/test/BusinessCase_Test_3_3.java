package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.AssessmentRequestData;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.studentProfile.observations.ObservationManager;
import org.prosolo.services.studentProfile.observations.SuggestionManager;
import org.prosolo.services.studentProfile.observations.SymptomManager;

import java.util.Date;
import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-01-29
 * @since 1.3
 */
public class BusinessCase_Test_3_3 extends BusinessCase_Test_3 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_3_3.class.getName());

    @Override
    protected void createAdditionalDataTest3(EventQueue events) throws Exception {
        Competence1Manager compManager = ServiceLocator.getInstance().getService(Competence1Manager.class);

        ////////////////////////////////
        // Enroll users to deliveries
        ////////////////////////////////
        enrollToDelivery(events, credential1Delivery1, userGeorgeYoung);
        enrollToDelivery(events, credential1Delivery1, userHelenCampbell);
        enrollToDelivery(events, credential1Delivery1, userRichardAnderson);
        enrollToDelivery(events, credential1Delivery1, userKevinHall);
        enrollToDelivery(events, credential1Delivery1, userRachelWiggins);

        enrollToDelivery(events, credential2Delivery1, userHelenCampbell);

        enrollToDelivery(events, credentialWithActivities1, userHelenCampbell);

        ////////////////////////////////////
        // Add instructors to students
        ////////////////////////////////////
        assignInstructorToStudent(events, credential1Delivery1InstructorAnnaHallowell, userHelenCampbell, credential1Delivery1);
        assignInstructorToStudent(events, credential1Delivery1InstructorAnnaHallowell, userGeorgeYoung, credential1Delivery1);
        assignInstructorToStudent(events, credential1Delivery1InstructorAnnaHallowell, userRichardAnderson, credential1Delivery1);
        assignInstructorToStudent(events, credential1Delivery1InstructorAnnaHallowell, userKevinHall, credential1Delivery1);

        assignInstructorToStudent(events, credentialWithActivities1Delivery1InstructorAnnaHallowell, userHelenCampbell, credentialWithActivities1Delivery1);

        assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userHelenCampbell, credential2Delivery1);


        ////////////////////////////////////
        // Students enroll to competencies
        ////////////////////////////////////

        // enroll helen campbell
        List<CompetenceData1> credential1Delivery1CompetencesHelen = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential1Delivery1Comp1TargetHelen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesHelen.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Delivery1Comp2TargetHelen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesHelen.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Delivery1Comp3TargetHelen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesHelen.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Delivery1Comp4TargetHelen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesHelen.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Delivery1Comp5TargetHelen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesHelen.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Delivery1Comp6TargetHelen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesHelen.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));


        // Helen Campbell starts activity-based competencies
        List<CompetenceData1> credentialWithActivities1Delivery1CompetenciesHelen = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credentialWithActivities1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credentialWithActivities1Delivery1Competency1Helen = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credentialWithActivities1Delivery1CompetenciesHelen.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));


        // enroll George Young
        List<CompetenceData1> credential1Delivery1CompetencesGeorge = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userGeorgeYoung.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        enrollToCompetencies(events, credential1Delivery1CompetencesGeorge, userGeorgeYoung);

        //enroll Richard Anderson
        List<CompetenceData1> credential1Delivery1CompetencesRichard = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userRichardAnderson.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential1Delivery1Comp1TargetRichard = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesRichard.get(0).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
        TargetCompetence1 credential1Delivery1Comp2TargetRichard = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1CompetencesRichard.get(1).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));




        ///////////////////////////////////
        // add evidence to competencies
        //////////////////////////////////
        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                "Learning plan incorporating teaching strategies.",
                credential1Delivery1Comp1TargetHelen.getId(),
                userHelenCampbell);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential1Delivery1Comp1TargetHelen.getId(),
                userHelenCampbell);

        LearningEvidenceData evidenceData3 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential1Delivery1Comp2TargetHelen.getId(),
                userHelenCampbell);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program 3",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential1Delivery1Comp1TargetRichard.getId(),
                userRichardAnderson);


        //////////////////////////////////////
        // Complete activities
        //////////////////////////////////////
        // Helen Campbell completes first activity
        List<ActivityData> credentialWithActivities1Delivery1ActivitiesHelen = ServiceLocator.getInstance().getService(Activity1Manager.class).getTargetActivitiesData(credentialWithActivities1Delivery1Competency1Helen.getId());

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Activity1Manager.class).completeActivityAndGetEvents(
                credentialWithActivities1Delivery1ActivitiesHelen.get(0).getTargetActivityId(),
                credentialWithActivities1Delivery1Competency1Helen.getId(),
                createUserContext(userRichardAnderson)));

        //////////////////////////////////////
        // Complete competencies
        //////////////////////////////////////
        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential1Delivery1Comp1TargetHelen.getId(),
                        credential1Delivery1Comp2TargetHelen.getId(),
                        credential1Delivery1Comp3TargetHelen.getId(),
                        credential1Delivery1Comp4TargetHelen.getId(),
                        credential1Delivery1Comp5TargetHelen.getId(),
                        credential1Delivery1Comp6TargetHelen.getId()
                ),
                userHelenCampbell);

        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential1Delivery1Comp1TargetRichard.getId()
                ),
                userRichardAnderson);

        // grade and approve instructor assessment
        long credential1Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getInstructorCredentialAssessmentId(credential1Delivery1.getId(), userHelenCampbell.getId()).get();
        AssessmentDataFull instructorCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1HelenCampbellInstructorAssessmentId, userAnnaHallowell.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
        gradeCredentialAssessmentByRubric(events, instructorCredentialAssessmentData, userAnnaHallowell, 3);
        for (CompetenceAssessmentData competenceAssessmentData : instructorCredentialAssessmentData.getCompetenceAssessmentData()) {
            int lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp1TargetHelen.getId()) {
                lvl = 2;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp2TargetHelen.getId()) {
                lvl = 4;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp3TargetHelen.getId()) {
                lvl = 2;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp4TargetHelen.getId()) {
                lvl = 1;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp5TargetHelen.getId()) {
                lvl = 4;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp6TargetHelen.getId()) {
                lvl = 3;
            }
            gradeCompetenceAssessmentByRubric(events, competenceAssessmentData, userAnnaHallowell, lvl);
        }
        approveCredentialAssessment(events, instructorCredentialAssessmentData.getCredAssessmentId(), userAnnaHallowell);


        /////////////////////////////////
        // Ask for credential assessment
        /////////////////////////////////
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).notifyAssessorToAssessCredentialAndGetEvents(
                AssessmentNotificationData.of(
                        credential1Delivery1.getId(),
                        userAnnaHallowell.getId(),
                        userRichardAnderson.getId(),
                        AssessmentType.INSTRUCTOR_ASSESSMENT),
                createUserContext(userRichardAnderson)));

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).notifyAssessorToAssessCredentialAndGetEvents(
                AssessmentNotificationData.of(
                        credential1Delivery1.getId(),
                        userAnnaHallowell.getId(),
                        userKevinHall.getId(),
                        AssessmentType.INSTRUCTOR_ASSESSMENT),
                createUserContext(userKevinHall)));

        ////////////////////////////////////////////////
        // Define observation symptoms and suggestions
        ////////////////////////////////////////////////
        Symptom symptom1 = ServiceLocator.getInstance().getService(SymptomManager.class).saveSymptom(0, "Lacks social interaction");
        Symptom symptom2 = ServiceLocator.getInstance().getService(SymptomManager.class).saveSymptom(0, "No assessments");

        Suggestion suggestion1 = ServiceLocator.getInstance().getService(SuggestionManager.class).saveSuggestion(0, "Work with others");
        Suggestion suggestion2 = ServiceLocator.getInstance().getService(SuggestionManager.class).saveSuggestion(0, "Use assessment feature");

        /////////////////////////////////
        // Create observations
        /////////////////////////////////
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(ObservationManager.class).saveObservationAndGetEvents(
                0,
                new Date(getDaysBeforeNow(5)),
                "You should try engaging more in discussion with other students.",
                "Lacks social interaction.",
                List.of(symptom1.getId()),
                List.of(suggestion1.getId()),
                createUserContext(userAnnaHallowell),
                userHelenCampbell.getId()));

        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(ObservationManager.class).saveObservationAndGetEvents(
                0,
                new Date(getDaysBeforeNow(10)),
                "Start asking for peer assessments.",
                "She does not have any peer assessment yet.",
                List.of(symptom2.getId()),
                List.of(suggestion2.getId()),
                createUserContext(userAnnaHallowell),
                userHelenCampbell.getId()));
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 3.3";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
