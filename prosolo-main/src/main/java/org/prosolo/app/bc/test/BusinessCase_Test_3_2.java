package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-01-29
 * @since 1.3
 */
public class BusinessCase_Test_3_2 extends BusinessCase_Test_3 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_3_2.class.getName());

    @Override
    protected void createAdditionalDataTest3(EventQueue events) {
        ////////////////////////////////
        // Enroll users to deliveries
        ////////////////////////////////
        enrollToDelivery(events, organization, credential1Delivery1, userHelenCampbell);
        enrollToDelivery(events, organization, credential1Delivery1, userGeorgeYoung);
        
        ///////////////////////////////////////////
        // enroll in competencies from credentialWithActivities1Delivery1
        ///////////////////////////////////////////
        List<CompetenceData1> competenciesUserHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credentialWithActivities1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 comp1UserHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(competenciesUserHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

        List<CompetenceData1> competenciesUserGeorgeYoung = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credentialWithActivities1Delivery1.getId(), userGeorgeYoung.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 comp1UserGeorgeYoung = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(competenciesUserGeorgeYoung.get(0).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));


        ///////////////////////////////////////////
        // Add comments to competencies
        ///////////////////////////////////////////
        CommentData comment1 = createNewComment(events,
                userHelenCampbell,
                "Social network analysis (SNA) is the process of investigating social structures through the use of networks and graph theory.",
                comp1UserHelenCampbell.getCompetence().getId(),
                CommentedResourceType.Competence,
                null);

        likeComment(events, comment1, userGeorgeYoung);

        CommentData comment1Reply1 = createNewComment(events,
                userGeorgeYoung,
                "It characterizes networked structures in terms of nodes and the ties, edges, or links that connect them.",
                comp1UserGeorgeYoung.getCompetence().getId(),
                CommentedResourceType.Competence,
                comment1);

        likeComment(events, comment1Reply1, userHelenCampbell);

        CommentData comment2 = createNewComment(events,
                userGeorgeYoung,
                "Social network analysis has emerged as a key technique in modern sociology.",
                comp1UserGeorgeYoung.getCompetence().getId(),
                CommentedResourceType.Competence,
                null);

        CommentData comment3 = createNewComment(events,
                userHelenCampbell,
                "It has also gained a significant following in anthropology, biology, demography, communication studies, economics, geography, history, information science, organizational studies, political science, social psychology, development studies, sociolinguistics, and computer science and is now commonly available as a consumer tool.",
                comp1UserHelenCampbell.getCompetence().getId(),
                CommentedResourceType.Competence,
                null);
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 3.2";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
