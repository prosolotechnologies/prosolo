package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-01-22
 * @since 1.3
 */
public class BusinessCase_Test_2_9 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_9.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        ////////////////////////////////////////////////////////////
        // Create two credentials with activity based competences
        ////////////////////////////////////////////////////////////
        Credential1 credentialWithActivities1 = createCredential(events,
                "Basics of Social Network Analysis",
                "This credential defines social network analysis and its main analysis methods and "
                        + "introduces how to perform social network analysis and visualize analysis results in Gephi",
                userNickPowell,
                "network structure, data collection, learning analytics, network measures, network modularity, social network analysis",
                rubricData.getId(),
                null);

        Competence1 comp1cred1 = createCompetence(events,
                userNickPowell,
                "Social Network Analysis",
                "Define social network analysis and its main analysis methods.",
                "centrality measures, data collection, modularity analysis, network centrality, network structure, social network analysis",
                credentialWithActivities1.getId(),
                rubricData.getId(),
                LearningPathType.ACTIVITY);

        createActivity(events,
                userNickPowell,
                "Introduction to Social Network Analysis",
                "Introduction into social network analysis for week 3 of DALMOOC by Dragan Gasevic.",
                "https://www.youtube.com/watch?v=2uibqSdHSag",
                ActivityType.VIDEO,
                comp1cred1.getId(),
                0,
                5,
                ActivityResultType.TEXT,
                "Slides",
                "https://www.slideshare.net/dgasevic/introduction-into-social-network-analysis/");

        createActivity(events,
                userNickPowell,
                "Example dataset",
                null,
                "<p>Download the example dataset used in the videos and familiarize with the data.</p>",
                ActivityType.TEXT,
                comp1cred1.getId(),
                0,
                3,
                ActivityResultType.TEXT,
                "Example datasets used in the videos",
                "https://s3.amazonaws.com/prosoloedx2/files/3f86bdfd0e8357f7c60c36b38c8fc2c0/Example%20datasets%20used%20in%20the%20videos.pdf");

        createActivity(events,
                userNickPowell,
                "Network Modularity and Community Identification",
                "Dragan Gasevic discusses network modularity and community identification in social network analysis for week 3 of DALMOOC. The presentation describes the notion of network modularity as a method used",
                "https://www.slideshare.net/dgasevic/network-modularity-and-community-identification",
                ActivityType.SLIDESHARE,
                comp1cred1.getId(),
                0,
                6,
                ActivityResultType.TEXT,
                "Slides",
                "http://www.slideshare.net/dgasevic/network-modularity-and-community-identification/1");

        createActivity(events,
                userNickPowell,
                "Network measures",
                "Dragan Gasevic discusses network measures (degree centrality, betweenness centrality, closeness centrality, degree, diameter)  for week 3 of DALMOOC.",
                "https://www.youtube.com/watch?v=Gq-4ErYLuLA",
                ActivityType.VIDEO,
                comp1cred1.getId(),
                0,
                8,
                ActivityResultType.TEXT,
                "Slides",
                "http://www.slideshare.net/dgasevic/network-measures-used-in-social-network-analysis");

        createActivity(events,
                userNickPowell,
                "Assignment: Reflection and discussion on social network analysis",
                "",
                "<p>After the introduction into social network analysis, its main analysis techniques, and data collection "
                        + "methods, it would be useful to reflect on what you have learned so far. Please, prepare a reflection "
                        + "piece (about 300 words) in which you will address the following issues:</p><ul><li>Outline your "
                        + "understanding of social network structure and main methods for social network analysis (centrality, "
                        + "density, and modularity);</li><li>Discuss potential benefits of the use of social network analysis for "
                        + "the study of learning and learning contexts</li><li>Describe potential applications of social network "
                        + "analysis for the study of learning. Reflect on the methods that could be used for data collection, "
                        + "steps to be taken for the analysis, potential conclusions, and possible issues (e.g., incomplete "
                        + "network, triangulation with other types of analysis, or ethics) that would need to be addressed in "
                        + "the process.</li></ul><p>Please, share your reflection as a blog post (preferred as it allows for "
                        + "the broader reach). Once you have created your blog post, please, share the blog reference (URL) on "
                        + "Twitter with the <strong>#dalmooc</strong> hashtag and ProSolo.</p><ul><li>Once you have posted and "
                        + "shared your blog on social media, please, read and engage into conversation of the blogs posted by "
                        + "at least two our participants of the course. The conversation can be done as direct comments on the "
                        + "blogs and/or via other social media used in the course.</li><li>When connecting with other peers, "
                        + "try to compare the findings presented in their reports, connect their findings with the readings you "
                        + "found in the course and/or elsewhere in the web. Ideally, you will also reflect on the applicability "
                        + "of each other’s results in the real-world studies/contexts.</li></ul><p><em>Note: In case you do not "
                        + "have a blog and would not like to set up a blog, please, initiate a discussion thread on the edX forum, "
                        + "or create a ProSolo status post, and share the post reference (URL) on Twitter and ProSolo as described "
                        + "above.</em></p>",
                ActivityType.TEXT,
                comp1cred1.getId(),
                1,
                0,
                ActivityResultType.TEXT);


        Credential1 credentialWithActivities2 = createCredential(events,
                "Sensemaking of Social Network Analysis for Learning",
                "This credential defines describes and critically reflects on possible approaches to the use of social network analysis for the study of learning. The credential also describes and interprets the results of social network analysis for the study of learning",
                userNickPowell,
                "academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis",
                rubricData.getId(),
                null);

        Competence1 comp1cred2 = createCompetence(events,
                userNickPowell,
                "Reflecting on approaches to the use of SNA for the study of learning",
                "Describe and critically reflect on possible approaches to the use of social network analysis for the study of learning",
                "academic performance, creative potential, learning analytics, learning design, MOOCs, sense of community, sensemaking, social network analysis",
                credentialWithActivities2.getId(),
                rubricData.getId(),
                LearningPathType.ACTIVITY);

        createActivity(events,
                userNickPowell,
                "Introduction",
                "Introduction into sensemaking of social network analysis for the study of learning. Dragan Gasevic introduces us to week 4 of DALMOOC.",
                "https://www.youtube.com/watch?v=NPEeSArODQE",
                ActivityType.VIDEO,
                comp1cred2.getId(),
                0,
                4,
                ActivityResultType.TEXT);

        createActivity(events,
                userNickPowell,
                "Social Network Analysis and Learning Design",
                "Dragan Gasevic discusses social network analysis and learning design for week 4 of DALMOOC.",
                "https://www.youtube.com/watch?v=-JuBDu_YVoo",
                ActivityType.VIDEO,
                comp1cred2.getId(),
                0,
                6,
                ActivityResultType.TEXT);

        createActivity(events,
                userNickPowell,
                "Social Network Analysis and Sense of Community",
                "Dragan Gasevic discusses social network analysis and sense of community for week 4 of DALMOOC.",
                "https://www.youtube.com/watch?v=lUEeppG_6So",
                ActivityType.VIDEO,
                comp1cred2.getId(),
                0,
                5,
                ActivityResultType.TEXT);

        createActivity(events,
                userNickPowell,
                "Social Network Analysis and Creative Potential",
                "Dragan Gasevic discusses social network analysis and creative potential for week 4 of DALMOOC.",
                "https://www.youtube.com/watch?v=VTGvvHpC5IQ",
                ActivityType.VIDEO,
                comp1cred2.getId(),
                0,
                4,
                ActivityResultType.TEXT);

        createActivity(events,
                userNickPowell,
                "Social Network Analysis and Academic Peformance",
                "Dragan Gasevic discusses social network analysis and academic performance for week 4 of DALMOOC.",
                "https://www.youtube.com/watch?v=F9jLV7hS2AE",
                ActivityType.VIDEO,
                comp1cred2.getId(),
                0,
                6,
                ActivityResultType.TEXT);

        createActivity(events,
                userNickPowell,
                "Social Network Analysis and Social Presence",
                "Dragan Gasevic discusses social network analysis and social presence for week 4 of DALMOOC.",
                "https://www.youtube.com/watch?v=bZhRuo8nz7A",
                ActivityType.VIDEO,
                comp1cred2.getId(),
                0,
                6,
                ActivityResultType.TEXT);

        createActivity(events,
                userNickPowell,
                "Hands-on activity: Integration of social network analysis in Gephi and Tableau analysis",
                "Dragan Gasevic discusses social network analysis and social presence for week 4 of DALMOOC.",
                "<p>Now that you have performed social network analysis in Gephi and started working on their interpretation of relevance for the understanding of learning, it is time to learn to integrate knowledge and skills gained in weeks 1-2 with Tableau. Specifically, in this hands-on activity, you are asked to:</p><ul><li>Export the results of social network analyses (centrality and modularity) of the networks available in the example dataset from Gephi – via the Data Laboratory tab of Gephi - in the format (i.e., CSV) that can be imported into Tableau</li><li>Plot the data to show the distribution of each centrality measure for each of the two networks</li><li>Plot the data to show the distribution of centrality measures across communities identified in each of the two networks</li><li>Share your experience (e.g., blogs and figures based on your visualizations from both Gephi and Tableau) with other course participants on social media (blog, Twitter, and Pro Solo, edX discussion forum).</li></ul>",
                ActivityType.TEXT,
                comp1cred2.getId(),
                0,
                40,
                ActivityResultType.TEXT);


        Credential1 credentialWithActivities3 = createCredential(events,
                "Introduction to Learning Analytics",
                "The proliferation of data in digital environments has to date been largely unexplored in education. A new academic field - learning analytics - has developed to gain insight into learner generated data and how this can be used to improve learning and teaching practices",
                userNickPowell,
                "academic performance, creative potential, dalmooc, learning design, MOOCs, sense of community, social network analysis",
                rubricData.getId(),
                null);

        Competence1 comp1cred3 = createCompetence(events,
                userNickPowell,
                "Tools for Learning Analytics",
                "Identify proprietary and open source tools commonly used in learning analytics",
                "academic performance, creative potential, social network analysis",
                credentialWithActivities3.getId(),
                rubricData.getId(),
                LearningPathType.ACTIVITY);

        Activity1 act1comp1cred3 = createActivity(events,
                userNickPowell,
                "Getting Started With Data Analytics Tools",
                "A basic overview of the Data Anlytics tools by George Siemens",
                "https://www.youtube.com/watch?v=XOckgFlLqwU",
                ActivityType.VIDEO,
                comp1cred3.getId(),
                0,
                30,
                ActivityResultType.TEXT);

        /////////////////////////
        // Create deliveries
        /////////////////////////
        Credential1 credentialWithActivities1Delivery1 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(credentialWithActivities1.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(getDaysFromNow(90)), createUserContext(userNickPowell)));
        Credential1 credentialWithActivities1Delivery2 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(credentialWithActivities2.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(getDaysFromNow(90)), createUserContext(userNickPowell)));
        Credential1 credentialWithActivities1Delivery3 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialManager.class).createCredentialDeliveryAndGetEvents(credentialWithActivities3.getId(), DateUtil.getDateFromMillis(new Date().getTime()), DateUtil.getDateFromMillis(getDaysFromNow(90)), createUserContext(userNickPowell)));

        // give learn privilege to all students from both user groups
        givePrivilegeToGroupOnDelivery(events, credentialWithActivities1Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGroupScienceEducationStudents, userGroupArtsEducationStudents));
        givePrivilegeToGroupOnDelivery(events, credentialWithActivities1Delivery2, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGroupScienceEducationStudents, userGroupArtsEducationStudents));
        givePrivilegeToGroupOnDelivery(events, credentialWithActivities1Delivery3, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGroupScienceEducationStudents, userGroupArtsEducationStudents));

        ////////////////////////////////
        // enroll users to Delivery 1
        ////////////////////////////////
        enrollToDelivery(events, organization, credential1Delivery1, userHelenCampbell);

        ///////////////////////////////////////////
        // enroll in competencies from Delivery 1
        ///////////////////////////////////////////
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


        ////////////////////////////////
        // enroll users to Delivery 1
        ////////////////////////////////
        enrollToDelivery(events, organization, credentialWithActivities1Delivery1, userHelenCampbell);

        ///////////////////////////////////////////
        // enroll in competencies from Delivery 1
        ///////////////////////////////////////////
        List<CompetenceData1> credentialWithActivities1Delivery1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credentialWithActivities1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
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
