package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-01-03
 * @since 1.2.0
 */
@Service ("org.prosolo.services.migration.CommonCustomMigrationService")
public class CommonCustomMigrationServiceImpl extends AbstractManagerImpl implements CommonCustomMigrationService {

    protected static Logger logger = Logger.getLogger(CommonCustomMigrationServiceImpl.class);

    @Inject private AssessmentManager assessmentManager;
    @Inject private Competence1Manager compManager;
    @Inject private CredentialManager credManager;
    @Inject private OrganizationManager orgManager;
    @Inject private EventFactory eventFactory;

    @Override
    public void migrateAssessments() throws DbConnectionException {
        logger.info("MIGRATION STARTED");
        EventQueue events = ServiceLocator.getInstance().getService(CommonCustomMigrationService.class)
               .migrateAssessmentsAndGetEvents();
        eventFactory.generateEvents(events);
        logger.info("MIGRATION FINISHED");
    }

    @Override
    @Transactional
    public EventQueue migrateAssessmentsAndGetEvents() {
        try {
            EventQueue eventQueue = EventQueue.newEventQueue();
            List<CredentialAssessment> credentialAssessments = getAllCredentialAssessments();

            for (CredentialAssessment ca : credentialAssessments) {
                long assessorId = ca.getAssessor() != null ? ca.getAssessor().getId() : 0;

                List<CompetenceData1> comps = compManager.getCompetencesForCredential(
                        ca.getTargetCredential().getCredential().getId(), ca.getStudent().getId(), false, false, true);

                for (CompetenceData1 cd : comps) {
                    // if competence assessment has not been created, create it
                    if (!compAssessmentCreated(ca, cd.getCompetenceId())) {
                        Result<CompetenceAssessment> res = assessmentManager.getOrCreateCompetenceAssessmentAndGetEvents(
                                cd, ca.getStudent().getId(), assessorId, null, ca.getType(), false, UserContextData.empty());
                        CredentialCompetenceAssessment cca = new CredentialCompetenceAssessment();
                        cca.setCredentialAssessment(ca);
                        cca.setCompetenceAssessment(res.getResult());
                        saveEntity(cca);
                        eventQueue.appendEvents(res.getEventQueue());
                    } else {
                        // if competence assessment has been created, check if all activity assessments have been created

                        CompetenceAssessment compAssessment = ca.getCompetenceAssessmentByCompetenceId(cd.getCompetenceId());

                        List<Long> participantIds = new ArrayList<>();
                        long studentId = compAssessment.getStudent().getId();
                        participantIds.add(studentId);
                        //for self assessment student and assessor are the same user

                        if (assessorId > 0 && assessorId != studentId) {
                            participantIds.add(assessorId);
                        }

                        int compPoints = 0;
                        boolean atLeastOneActivityGraded = false;

                        for (ActivityData activityData : cd.getActivities()) {
                            if (compAssessment.getActivityDiscussions().stream().noneMatch(aa -> aa.getActivity().getId() == activityData.getActivityId())) {
                                Result<ActivityAssessment> actAssessment = assessmentManager.createActivityAssessmentAndGetEvents(
                                        activityData, compAssessment.getId(), participantIds, compAssessment.getType(),
                                        UserContextData.empty(), persistence.currentManager());

                                eventQueue.appendEvents(actAssessment.getEventQueue());

                                if (cd.getAssessmentSettings().getGradingMode() == GradingMode.AUTOMATIC) {
                                    compPoints += actAssessment.getResult().getPoints() >= 0
                                            ? actAssessment.getResult().getPoints()
                                            : 0;
                                    if (actAssessment.getResult().getPoints() >= 0) {
                                        atLeastOneActivityGraded = true;
                                    }
                                }
                            }
                        }

                        if (cd.getAssessmentSettings().getGradingMode() == GradingMode.AUTOMATIC && atLeastOneActivityGraded) {
                            compAssessment.setPoints(compPoints);
                        } else {
                            compAssessment.setPoints(-1);
                        }

                        saveEntity(compAssessment);
                    }
                }
            }
            return eventQueue;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error migrating the data");
        }
    }

    private boolean compAssessmentCreated(CredentialAssessment credentialAssessment, long compId) {
        return credentialAssessment.getCompetenceAssessments().stream().anyMatch(cca -> cca.getCompetenceAssessment().getCompetence().getId() == compId);
    }

    private List<CredentialAssessment> getAllCredentialAssessments() {
        String q =
                "SELECT ca FROM CredentialAssessment ca";
        return persistence.currentManager()
                .createQuery(q)
                .list();
    }

    @Override
    @Transactional
    public void migrateAssessmentDiscussions() {
        logger.info("MIGRATION STARTED");
        migrateCompetenceAssessmentDiscussions();
        migrateCredentialAssessmentDiscussions();
        logger.info("MIGRATION FINISHED");
    }

    private void migrateCompetenceAssessmentDiscussions() {
        try {
            List<CompetenceAssessment> competenceAssessments = getAllCompetenceAssessments();
            for (CompetenceAssessment ca : competenceAssessments) {
                List<Long> participantIds = new ArrayList<>();
                participantIds.add(ca.getStudent().getId());
                if (ca.getAssessor() != null) {
                    participantIds.add(ca.getAssessor().getId());
                }
                Date now = new Date();
                for (Long userId : participantIds) {
                    CompetenceAssessmentDiscussionParticipant participant = new CompetenceAssessmentDiscussionParticipant();
                    User user = loadResource(User.class, userId);
                    participant.setAssessment(ca);
                    participant.setDateCreated(now);
                    participant.setRead(true);

                    participant.setParticipant(user);
                    saveEntity(participant);
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error migrating the data");
        }
    }

    private void migrateCredentialAssessmentDiscussions() {
        try {
            List<CredentialAssessment> assessments = getAllCredentialAssessments();
            for (CredentialAssessment ca : assessments) {
                List<Long> participantIds = new ArrayList<>();
                participantIds.add(ca.getStudent().getId());
                if (ca.getAssessor() != null) {
                    participantIds.add(ca.getAssessor().getId());
                }
                Date now = new Date();
                for (Long userId : participantIds) {
                    CredentialAssessmentDiscussionParticipant participant = new CredentialAssessmentDiscussionParticipant();
                    User user = loadResource(User.class, userId);
                    participant.setAssessment(ca);
                    participant.setDateCreated(now);
                    participant.setRead(true);

                    participant.setParticipant(user);
                    saveEntity(participant);
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error migrating the data");
        }
    }

    private List<CompetenceAssessment> getAllCompetenceAssessments() {
        String q =
                "SELECT ca FROM CompetenceAssessment ca";
        return persistence.currentManager()
                .createQuery(q)
                .list();
    }

    @Override
    @Transactional
    public void migrateCompetenceAssessmentPoints() throws DbConnectionException {
        try {
            List<CompetenceAssessment> competenceAssessments = getAllCompetenceAssessments();
            for (CompetenceAssessment ca : competenceAssessments) {
                if (ca.getCompetence().getGradingMode() == GradingMode.AUTOMATIC) {
                    assessmentManager.updateScoreForCompetenceAssessmentIfNeeded(ca.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error migrating the data");
        }
    }

    @Override
    @Transactional
    public void createSelfAssessments(UserContextData context) {
        // load all organizations
        PaginatedResult<OrganizationData> allOrganizations = orgManager.getAllOrganizations(-1, -1, false);

        for (OrganizationData org : allOrganizations.getFoundNodes()) {
            // get all credentials in the organization
            List<Credential1> allCredentials = credManager.getAllCredentials(org.getId(), persistence.currentManager());

            // create assessment configs where they don't exist
            for (Credential1 cred : allCredentials) {
                if (cred.getAssessmentConfig() == null || cred.getAssessmentConfig().isEmpty()) {
                    cred.setAssessmentConfig(new HashSet<>());

                    for (AssessmentType assessmentType : AssessmentType.values()) {
                        CredentialAssessmentConfig cac = new CredentialAssessmentConfig();
                        cac.setCredential(cred);
                        cac.setAssessmentType(assessmentType);
                        cac.setEnabled(true);
                        saveEntity(cac);

                        cred.getAssessmentConfig().add(cac);
                    }
                }
            }

            // get all users
            List<User> orgUsers = orgManager.getOrganizationUsers(org.getId(), false, persistence.currentManager(), null);

            for (Credential1 cred : allCredentials) {
                if (cred.getAssessmentConfig()
                        .stream().filter(config -> config.getAssessmentType() == AssessmentType.SELF_ASSESSMENT)
                        .findFirst().get()
                        .isEnabled()) {

                    List<TargetCredential1> targetCredentials = credManager.getTargetCredentialsForUsers(orgUsers.stream().map(User::getId).collect(Collectors.toList()), cred.getId());

                    for (TargetCredential1 targetCredential : targetCredentials) {
                        try {
                            assessmentManager.createSelfAssessmentAndGetEvents(targetCredential, context);
                        } catch (IllegalDataStateException e) {
                            logger.error("Error", e);
                        }
                    }
                }
            }

            // create assessment configs if they don't exist
            List<Competence1> competences = compManager.getAllCompetences(org.getId(), persistence.currentManager());
            for (Competence1 competence : competences) {
                if (competence.getAssessmentConfig() == null ||
                        competence.getAssessmentConfig().isEmpty()) {
                    for (AssessmentType atc : AssessmentType.values()) {
                        CompetenceAssessmentConfig cac = new CompetenceAssessmentConfig();
                        cac.setCompetence(competence);
                        cac.setAssessmentType(atc);
                        cac.setEnabled(true);
                        saveEntity(cac);
                    }
                }
            }
        }
    }

}
