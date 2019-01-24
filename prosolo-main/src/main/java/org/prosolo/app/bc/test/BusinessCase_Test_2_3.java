package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.util.Arrays;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-01-16
 * @since 1.2.0
 */
public class BusinessCase_Test_2_3 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_3.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        Competence1Manager compManager = ServiceLocator.getInstance().getService(Competence1Manager.class);
        ///////////////////////////
        // create credential category and set it for credentials
        ///////////////////////////
        createCredentialCategories(events, "Category 1");
        CredentialCategoryData category = ServiceLocator.getInstance().getService(OrganizationManager.class)
                .getOrganizationCredentialCategoriesData(organization.getId()).get(0);
        assignCategoryToCredential(events, credential2.getId(), category, userNickPowell);
        assignCategoryToCredential(events, credential3.getId(), category, userNickPowell);
        ///////////////////////////
        // enroll users to deliveries
        ///////////////////////////
        enrollToDelivery(events, organization, credential1Delivery1, userRichardAnderson);
        enrollToDelivery(events, organization, credential1Delivery1, userHelenCampbell);
        enrollToDelivery(events, organization, credential1Delivery1, userStevenTurner);
        enrollToDelivery(events, organization, credential1Delivery1, userKevinHall);
        enrollToDelivery(events, organization, credential2Delivery1, userKevinHall);
        enrollToDelivery(events, organization, credential3Delivery1, userKevinHall);
        enrollToDelivery(events, organization, credential4Delivery1, userKevinHall);
        ////////////////////////////
        // assign students to instructor
        ////////////////////////////
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
                credential1Delivery1InstructorPhilArmstrong.getId(), credential1Delivery1.getId(), Arrays.asList(userKevinHall.getId()), null, createUserContext(userNickPowell)));
        ////////////////////////////
        // enroll in competencies
        ////////////////////////////
        //enroll steven turner
        List<CompetenceData1> credential1Delivery1CompetencesSteven = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        enrollToCompetencies(events, credential1Delivery1CompetencesSteven, userStevenTurner);

        //enroll helen campbell
        List<CompetenceData1> credential1Delivery1CompetencesHelen = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        enrollToCompetencies(events, credential1Delivery1CompetencesHelen, userHelenCampbell);
        //enroll kevin hall
        List<CompetenceData1> credential1Delivery1Competences = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential1Delivery1Comp1Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1Competences.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp2Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1Competences.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp3Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1Competences.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp4Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1Competences.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp5Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1Competences.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp6Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1Competences.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        List<CompetenceData1> credential2Delivery1Competences = compManager.getCompetencesForCredential(credential2Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential2Delivery1Comp1Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp2Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp3Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp4Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp5Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp6Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        List<CompetenceData1> credential3Delivery1Competences = compManager.getCompetencesForCredential(credential3Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential3Delivery1Comp1Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp2Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp3Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp4Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp5Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp6Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp7Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1Competences.get(6).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        ///////////////////////////
        // add evidence to competencies
        ///////////////////////////
        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                "Learning plan incorporating teaching strategies.",
                credential2Delivery1Comp1Target.getId(),
                userKevinHall);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential2Delivery1Comp1Target.getId(),
                userKevinHall);

        LearningEvidenceData evidenceData3 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential2Delivery1Comp1Target.getId(),
                userKevinHall);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "Recording of meeting with supervisor",
                "Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
                MOV_TEST_FILE,
                "meeting logs",
                "Contains feedback on the new version of the teaching program.",
                credential2Delivery1Comp2Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential2Delivery1Comp2Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        LearningEvidenceData evidenceData5 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "Lesson notes from English language course",
                "Lesson notes from the English language course given on 21 April, 2018.",
                PPT_TEST_FILE,
                "lesson notes, english language",
                "Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.",
                credential2Delivery1Comp3Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential2Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential2Delivery1Comp5Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential2Delivery1Comp6Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "Audio recording of student feedback",
                "Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
                        "the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
                MP3_TEST_FILE,
                "student conference",
                "Student feedback on teaching activities to meet the specific learning strengths and needs.",
                credential3Delivery1Comp1Target.getId(),
                userKevinHall);

        LearningEvidenceData evidenceData7 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program 2",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential3Delivery1Comp2Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential3Delivery1Comp2Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis 2",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential3Delivery1Comp3Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential3Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential3Delivery1Comp5Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData7.getId(), credential3Delivery1Comp6Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData7.getId(), credential3Delivery1Comp7Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan 2",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                "Learning plan incorporating teaching strategies.",
                credential1Delivery1Comp1Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential1Delivery1Comp1Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis 3",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential1Delivery1Comp2Target.getId(),
                userKevinHall);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program 3",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential1Delivery1Comp3Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential1Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential1Delivery1Comp5Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData7.getId(), credential1Delivery1Comp6Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        //////////////////////////////////////
        // complete competencies
        //////////////////////////////////////
        markCompetenciesAsCompleted(
                events,
                List.of(
                        credential1Delivery1Comp1Target.getId(),
                        credential1Delivery1Comp2Target.getId(),
                        credential1Delivery1Comp3Target.getId(),
                        credential1Delivery1Comp4Target.getId(),
                        credential1Delivery1Comp5Target.getId(),
                        credential1Delivery1Comp6Target.getId(),
                        credential2Delivery1Comp1Target.getId(),
                        credential2Delivery1Comp2Target.getId(),
                        credential2Delivery1Comp3Target.getId(),
                        credential2Delivery1Comp4Target.getId(),
                        credential2Delivery1Comp5Target.getId(),
                        credential2Delivery1Comp6Target.getId(),
                        credential3Delivery1Comp1Target.getId(),
                        credential3Delivery1Comp2Target.getId(),
                        credential3Delivery1Comp3Target.getId(),
                        credential3Delivery1Comp4Target.getId(),
                        credential3Delivery1Comp5Target.getId(),
                        credential3Delivery1Comp6Target.getId(),
                        credential3Delivery1Comp7Target.getId()
                ),
                userKevinHall);

        ////////////////////////////////
        // ask for competence assessment
        ////////////////////////////////
        CompetenceAssessment peerComp2AssessmentByHelen = askPeerForCompetenceAssessment(events, credential1Delivery1Comp2Target.getCompetence().getId(), userKevinHall, userHelenCampbell.getId());
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp2Target.getCompetence().getId(), BlindAssessmentMode.BLIND, userNickPowell);
        CompetenceAssessment peerComp2AssessmentByRichard = askPeerForCompetenceAssessment(events, credential1Delivery1Comp2Target.getCompetence().getId(), userKevinHall, userRichardAnderson.getId());
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp2Target.getCompetence().getId(), BlindAssessmentMode.OFF, userNickPowell);

        CompetenceAssessment peerComp4AssessmentByRichard = askPeerForCompetenceAssessment(events, credential1Delivery1Comp4Target.getCompetence().getId(), userKevinHall, userRichardAnderson.getId());
        CompetenceAssessment peerComp4AssessmentBySteven = askPeerForCompetenceAssessment(events, credential1Delivery1Comp4Target.getCompetence().getId(), userKevinHall, userStevenTurner.getId());
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp4Target.getCompetence().getId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);
        CompetenceAssessment peerComp4AssessmentByHelen = askPeerForCompetenceAssessment(events, credential1Delivery1Comp4Target.getCompetence().getId(), userKevinHall, userHelenCampbell.getId());
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp4Target.getCompetence().getId(), BlindAssessmentMode.OFF, userNickPowell);

        ////////////////////////////////
        // grade and approve assessment
        ////////////////////////////////
        gradeCompetenceAssessmentByRubric(events, peerComp2AssessmentByHelen.getId(), AssessmentType.PEER_ASSESSMENT, userHelenCampbell, 3);
        approveCompetenceAssessment(events, peerComp2AssessmentByHelen.getId(), userHelenCampbell);
        gradeCompetenceAssessmentByRubric(events, peerComp2AssessmentByRichard.getId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, 4);
        approveCompetenceAssessment(events, peerComp2AssessmentByRichard.getId(), userRichardAnderson);

        gradeCompetenceAssessmentByRubric(events, peerComp4AssessmentByRichard.getId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, 2);
        approveCompetenceAssessment(events, peerComp4AssessmentByRichard.getId(), userRichardAnderson);
        gradeCompetenceAssessmentByRubric(events, peerComp4AssessmentByHelen.getId(), AssessmentType.PEER_ASSESSMENT, userHelenCampbell, 1);
        approveCompetenceAssessment(events, peerComp4AssessmentByHelen.getId(), userHelenCampbell);
        gradeCompetenceAssessmentByRubric(events, peerComp4AssessmentBySteven.getId(), AssessmentType.PEER_ASSESSMENT, userStevenTurner, 3);

        //grade and approve instructor assessment
        long credential1Delivery1KevinHallInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getInstructorCredentialAssessmentId(credential1Delivery1.getId(), userKevinHall.getId()).get();
        AssessmentDataFull instructorCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1KevinHallInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
        gradeCredentialAssessmentByRubric(events, instructorCredentialAssessmentData, userPhilArmstrong, 3);
        for (CompetenceAssessmentData competenceAssessmentData : instructorCredentialAssessmentData.getCompetenceAssessmentData()) {
            int lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp1Target.getId()) {
                lvl = 2;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp2Target.getId()) {
                lvl = 4;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp3Target.getId()) {
                lvl = 2;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp4Target.getId()) {
                lvl = 1;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp5Target.getId()) {
                lvl = 4;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp6Target.getId()) {
                lvl = 3;
            }
            gradeCompetenceAssessmentByRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
        }
        approveCredentialAssessment(events, instructorCredentialAssessmentData.getCredAssessmentId(), userPhilArmstrong);

        //grade and approve self assessment
        long credential1Delivery1KevinHallSelfAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getSelfCredentialAssessmentId(credential1Delivery1.getId(), userKevinHall.getId()).get();
        AssessmentDataFull selfCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1KevinHallSelfAssessmentId, userKevinHall.getId(), AssessmentType.SELF_ASSESSMENT);
        gradeCredentialAssessmentByRubric(events, selfCredentialAssessmentData, userKevinHall, 2);
        for (CompetenceAssessmentData competenceAssessmentData : selfCredentialAssessmentData.getCompetenceAssessmentData()) {
            int lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp1Target.getId()) {
                lvl = 1;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp2Target.getId()) {
                lvl = 3;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp3Target.getId()) {
                lvl = 4;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp4Target.getId()) {
                lvl = 2;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp5Target.getId()) {
                lvl = 1;
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp6Target.getId()) {
                lvl = 2;
            }
            gradeCompetenceAssessmentByRubric(events, competenceAssessmentData, userKevinHall, lvl);
        }
        approveCredentialAssessment(events, selfCredentialAssessmentData.getCredAssessmentId(), userKevinHall);
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.3";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
