package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-01-17
 * @since 1.3
 */
public class BusinessCase_Test_2_6 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_6.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        ///////////////////////////
        // enroll users to delivery
        ///////////////////////////
        enrollToDelivery(events, credential1Delivery1, userHelenCampbell);

        ////////////////////////////
        // enroll in competencies
        ////////////////////////////
        List<CompetenceData1> standard1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

        // we need a reference to the TargetCompetence1
        TargetCompetence1 credential1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
        TargetCompetence1 credential1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        // add pieces of evidence to the all competencies
        LearningEvidence evidence1 = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://devfiles.prosolo.ca.s3-us-west-1.amazonaws.com/files/9367681195e4cfc492320693c754fa5f/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                userHelenCampbell);
        attachExistingEvidenceToCompetence(evidence1.getId(), credential1Comp1Target.getId(), "Learning plan incorporating teaching strategies.");


        LearningEvidence evidence2 = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                userHelenCampbell);
        attachExistingEvidenceToCompetence(evidence2.getId(), credential1Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");


        LearningEvidence evidence3 = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF1_TEST_FILE,
                "teaching program",
                userHelenCampbell);
        attachExistingEvidenceToCompetence(evidence3.getId(), credential1Comp2Target.getId(), "Contains structure of the new version of a teaching program.");
        attachExistingEvidenceToCompetence(evidence3.getId(), credential1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");


        LearningEvidence evidence4 = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "Recording of meeting with supervisor",
                "Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
                MOV_TEST_FILE,
                "meeting logs",
                userHelenCampbell);
        // not connected to any competency

        LearningEvidence evidence5 = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "Lesson notes from English language course",
                "Lesson notes from the English language course given on 21 April, 2018.",
                PPT_TEST_FILE,
                "lesson notes, english language",
                userHelenCampbell);
        attachExistingEvidenceToCompetence(evidence5.getId(), credential1Comp4Target.getId(), "Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.");
        attachExistingEvidenceToCompetence(evidence5.getId(), credential1Comp5Target.getId(), "Lesson observation notes and discussion about meeting the specific learning needs of students.");
        attachExistingEvidenceToCompetence(evidence5.getId(), credential1Comp6Target.getId(), "Lesson observation notes that record how the teaching strategies designed and implemented by\n" +
                "the teacher have been adjusted to support the learning needs of individual students with disability.");


        LearningEvidence evidence6 = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "Audio recording of student feedback",
                "Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
                        "the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
                MP3_TEST_FILE,
                "student conference",
                userHelenCampbell);
        attachExistingEvidenceToCompetence(evidence6.getId(), credential1Comp5Target.getId(), "Student feedback on teaching activities to meet the specific learning strengths and needs.");
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.6";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
