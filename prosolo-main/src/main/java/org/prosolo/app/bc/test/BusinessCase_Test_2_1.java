package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;

import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-01-17
 * @since 1.3
 */
public class BusinessCase_Test_2_1 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_1.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        /////////////////////////////////////////////////////////////////////
        // Assign instructors and students to the 1st standard delivery
        /////////////////////////////////////////////////////////////////////
        try {
            // enroll students to the delivery
            enrollToDelivery(events, credential1Delivery1, userHelenCampbell);
            enrollToDelivery(events, credential1Delivery1, userRichardAnderson);
            enrollToDelivery(events, credential1Delivery1, userStevenTurner);
            enrollToDelivery(events, credential1Delivery1, userJosephGarcia);
            enrollToDelivery(events, credential1Delivery1, userTimothyRivera);
            enrollToDelivery(events, credential1Delivery1, userKevinHall);

            // set Phil Armstrong as an instructor to Helen Campbell and Richard Anderson
            assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, List.of(userHelenCampbell, userRichardAnderson), credential1Delivery1);

            //////////////////////////
            // Start all competencies
            //////////////////////////
            List<CompetenceData1> standard1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());

            // we need a reference to the TargetCompetence1
            TargetCompetence1 standard1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
            TargetCompetence1 standard1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
            TargetCompetence1 standard1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
            TargetCompetence1 standard1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
            TargetCompetence1 standard1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
            TargetCompetence1 standard1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

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

        /////////////////////////////////////////////////////////////////////
        // Assign instructors and students to the 2st standard delivery
        /////////////////////////////////////////////////////////////////////
        try {
            // enroll students to the delivery
            enrollToDelivery(events, credential2Delivery1, userHelenCampbell);
            enrollToDelivery(events, credential2Delivery1, userStevenTurner);

            // set Phil Armstrong as an instructor to Helen Campbell and Steven Turner
            assignInstructorToStudent(events, credential2Delivery1InstructorPhilArmstrong, List.of(userHelenCampbell, userStevenTurner), credential2Delivery1);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error", e);
        }

        /////////////////////////////////////////////////////////////////////
        // Assign instructors and students to the 3rd standard delivery
        /////////////////////////////////////////////////////////////////////
        try {
            // enroll students to the delivery
            enrollToDelivery(events, credential3Delivery1, userHelenCampbell);

            // set Phil Armstrong as an instructor to Helen Campbell
            assignInstructorToStudent(events, credential3Delivery1InstructorPhilArmstrong, userHelenCampbell, credential3Delivery1);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error", e);
        }

        //////////////////////////////////
        // Create Status wall posts
        //////////////////////////////////
        createSocialActivity(events, userLoriAbner, "Market analysis and future prospects of Online Education market.", "https://www.marketwatch.com/press-release/online-education-market-2018-top-key-players-k12-inc-pearson-white-hat-managemen-georg-von-holtzbrinck-gmbh-co-2018-08-22");

        // post with no attachment nor link
        createSocialActivity(events, userHelenCampbell, "Make sure students understand that just because someone else says something, it’s not necessarily a fact. It’s most likely just his/her opinion. To simply agree with someone else’s opinion is to consider it a fact and thus make it real. For example, believing others who say “You can’t play soccer very well” can either convince you to agree with them and continue being poor at soccer OR motivate you to believe “I’m better now than before and I’ll improve with even more practice!” One’s attitude of others’ opinions can either 1) encourage and help us grow and improve or 2) discourage and inhibit us from growing.", null);

        // post with attachment
        createSocialActivityWithAttachment(events, userHelenCampbell, "The K to 12 Philippine Basic Education Curriculum Framework", "The K to 12 Curriculum", "http://industry.gov.ph/wp-content/uploads/2015/05/6th-TID-Usec.-Ocampos-Presentation-on-K-to-12.pdf");
        // post with SlideShare presentation
        createSocialActivity(events, userHelenCampbell, "", "https://www.slideshare.net/fordemm/stem-presentation-cloonan-13-final");
        // post with YouTube video
        createSocialActivity(events, userHelenCampbell, "", "https://www.youtube.com/watch?v=5GWhwUN9iaY");
        // post with no thumbnail
        createSocialActivity(events, userHelenCampbell, "", "https://www.edu.gov.mb.ca/k12/learnres/index.html");
        // post with small thumbnail
        createSocialActivity(events, userHelenCampbell, "Open educational resources are tools and supports that are available at no cost. They’re designed to support learning for K–12 students and adult learners.", "https://www.openschool.bc.ca/k12/");
        // post with large thumbnail
        createSocialActivity(events, userHelenCampbell, "", "https://www.teachermagazine.com.au/articles/numeracy-is-everyones-business");
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.1";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
