package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-01-29
 * @since 1.3
 */
public abstract class BusinessCase_Test_3 extends BaseBusinessCase5 {

    // credentials
    protected Credential1 credentialWithActivities1;

    // deliveries
    protected Credential1 credentialWithActivities1Delivery1;
    protected Credential1 credential2Delivery2;
    protected Credential1 credential3Delivery2;
    protected Credential1 credential4Delivery2;

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        ////////////////////////////////////////////////////////////
        // Create two credentials with activity based competences
        ////////////////////////////////////////////////////////////
        credentialWithActivities1 = createCredential(events,
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


        /////////////////////////
        // Ongoing deliveries
        /////////////////////////
        // add Anna Hallowell as an instructor
        // Anna Hallowell is already added to the credential1Delivery1
        CredentialInstructor credential2Delivery1InstructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential2Delivery1.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));



        //////////////////////////////
        // Pending deliveries
        //////////////////////////////
        credential3Delivery2 = createDelivery(events, credential3, getDaysFromNow(5), getDaysFromNow(25), userNickPowell);
        // give learn privilege to all students from the School of Education
        givePrivilegeToGroupOnDelivery(events, credential3Delivery2, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGroupScienceEducationStudents));
        // add Anna Hallowell as an instructor
        CredentialInstructor credential3Delivery2InstructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential3Delivery2.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));

        credential4Delivery2 = createDelivery(events, credential4, getDaysFromNow(3), getDaysFromNow(13), userNickPowell);
        // nobody has the Learn privilege

        // add Anna Hallowell as an instructor
        CredentialInstructor credential4Delivery2InstructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential4Delivery2.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));


        /////////////////////////
        // Past deliveries
        /////////////////////////

        // credentialWithActivities1
        credentialWithActivities1Delivery1 = createDelivery(events, credentialWithActivities1, getDaysBeforeNow(30), getDaysBeforeNow(1), userNickPowell);
        // give learn privilege to all students from the School of Education
        givePrivilegeToGroupOnDelivery(events, credentialWithActivities1Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGroupScienceEducationStudents));
        givePrivilegeToUsersOnDelivery(events, credentialWithActivities1Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userAkikoKido));
        // add Anna Hallowell as an instructor
        CredentialInstructor credentialWithActivities1Delivery1InstructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credentialWithActivities1Delivery1.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));

        // credential2
        credential2Delivery2 = createDelivery(events, credential2, getDaysBeforeNow(20), getDaysBeforeNow(5), userNickPowell);
        // give learn privilege to all students from the School of Education
        givePrivilegeToUsersOnDelivery(events, credential2Delivery2, UserGroupPrivilege.Learn, userNickPowell, organization, schoolOfEducationStudents);
        // add Anna Hallowell as an instructor
        CredentialInstructor credential2Delivery2InstructorAnnaHallowell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).addInstructorToCredentialAndGetEvents(credential2Delivery2.getId(), userAnnaHallowell.getId(), 0, createUserContext(userNickPowell)));


        createAdditionalDataTest3(events);
    }

    protected abstract void createAdditionalDataTest3(EventQueue events) throws Exception;

}
