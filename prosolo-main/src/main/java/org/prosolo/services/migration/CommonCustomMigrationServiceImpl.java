package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
                        ca.getTargetCredential().getCredential().getId(), ca.getAssessedStudent().getId(), false, false, true);
                for (CompetenceData1 cd : comps) {
                    if (!compAssessmentCreated(ca, cd.getCompetenceId())) {
                        Result<CompetenceAssessment> res = assessmentManager.getOrCreateCompetenceAssessmentAndGetEvents(
                                cd, ca.getAssessedStudent().getId(), assessorId, ca.getType(), UserContextData.empty());
                        CredentialCompetenceAssessment cca = new CredentialCompetenceAssessment();
                        cca.setCredentialAssessment(ca);
                        cca.setCompetenceAssessment(res.getResult());
                        saveEntity(cca);
                        eventQueue.appendEvents(res.getEventQueue());
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
        return credentialAssessment.getCompetenceAssessments().stream().filter(cca -> cca.getCompetenceAssessment().getCompetence().getId() == compId).findFirst().isPresent();
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
                participantIds.add(ca.getAssessedStudent().getId());
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

}
