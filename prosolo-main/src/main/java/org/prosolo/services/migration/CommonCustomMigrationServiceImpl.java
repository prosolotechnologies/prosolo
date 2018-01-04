package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialCompetenceAssessment;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

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

}
