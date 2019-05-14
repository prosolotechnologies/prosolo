package org.prosolo.bigdata.dal.persistence.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.dal.persistence.AssessmentDAO;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventData;
import org.prosolo.common.event.EventFactory;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AssessmentDAOImpl implements AssessmentDAO {

    private static Logger logger = Logger.getLogger(AssessmentDAOImpl.class);

    private EventFactory eventFactory;

    public AssessmentDAOImpl() {
        this.eventFactory = new EventFactory();
    }

    @Override
    public EventQueue assignAssessorFromAssessorPoolToCompetencePeerAssessmentAndGetEvents(long compAssessmentId) {
        EventQueue events = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction t = null;
        try {
            t = session.beginTransaction();
            CompetenceAssessment ca = (CompetenceAssessment) session.load(CompetenceAssessment.class, compAssessmentId);
            Organization org = ca.getStudent().getOrganization();
            long assessorId = getPeerFromAvailableAssessorsPoolForCompetenceAssessment(
                    ca.getTargetCredential().getCredential().getId(),
                    ca.getCompetence().getId(),
                    ca.getStudent().getId(),
                    org.isAssessmentTokensEnabled(),
                    session);
            if (assessorId > 0) {
                events = assignAssessorToCompetenceAssessment(ca, assessorId, org.getId(), session);
            }
            t.commit();
        } catch (Exception ex) {
            logger.error("error", ex);
            if (t != null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
        if (events != null) {
            return events;
        }
        return EventQueue.newEventQueue();
    }

    private EventQueue assignAssessorToCompetenceAssessment(CompetenceAssessment ca, long assessorId, long organizationId, Session session) {
        if (assessorId > 0) {
            ca.setAssessor((User) session.load(User.class, assessorId));

            User eventObj = new User();
            eventObj.setId(assessorId);
            CompetenceAssessment eventTarget = new CompetenceAssessment();
            eventTarget.setId(ca.getId());
            return EventQueue.of(eventFactory.generateEventData(EventType.ASSESSOR_ASSIGNED_TO_ASSESSMENT, UserContextData.ofOrganization(organizationId), eventObj, eventTarget, null, null));
        }
        return EventQueue.newEventQueue();
    }

    @Override
    public List<Long> getIdsOfUnassignedCompetencePeerAssessmentRequests() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction t = null;
        String query =
                "SELECT ca.id " +
                "FROM CompetenceAssessment ca " +
                "WHERE ca.type = :type " +
                "AND ca.status = :status " +
                "AND ca.assessor IS NULL ";
        List<Long> result = null;
        try {
            t = session.beginTransaction();
            result = (List<Long>) session.createQuery(query)
                    .setString("type", AssessmentType.PEER_ASSESSMENT.name())
                    .setString("status", AssessmentStatus.REQUESTED.name())
                    .list();
            t.commit();
        } catch (Exception ex) {
            logger.error("error", ex);
            if (t != null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
        if (result != null) {
            return result;
        }
        return new ArrayList<>();
    }

    //TODO: Same method exists in main application: org.prosolo.services.assessment.impl.AssessmentManagerImpl.getPeerFromAvailableAssessorsPoolForCompetenceAssessment
    private long getPeerFromAvailableAssessorsPoolForCompetenceAssessment(long credId, long compId, long userId, boolean orderByTokens, Session session) {
        try {
            String query =
                    "SELECT user.id " +
                            "FROM TargetCompetence1 tComp " +
                            "INNER JOIN tComp.user user " +
                            "LEFT JOIN user.competenceAssessmentsWithAssessorRole ca " +
                            "WITH ca.type = :aType " +
                            "AND ca.competence.id = :compId " +
                            "AND ca.status IN (:activeStatuses) " +
                            "LEFT JOIN ca.targetCredential tCred " +
                            "WITH tCred.credential.id = :credId " +
                            "WHERE tComp.competence.id = :compId " +
                            "AND user.id != :userId " +
                            "AND user.availableForAssessments is TRUE " +
                            "AND user.id NOT IN ( " +
                            "SELECT assessment.assessor.id " +
                            "FROM CompetenceAssessment assessment " +
                            "WHERE assessment.student.id = :userId " +
                            "AND assessment.competence.id = :compId " +
                            "AND assessment.targetCredential.credential.id = :credId " +
                            "AND assessment.assessor IS NOT NULL " +
                            "AND assessment.type = :aType " +
                            "AND (assessment.status IN (:activeStatuses) OR assessment.quitDate > :monthAgo) " +
                            ") " +
                            "AND user.id IN (" +
                            "SELECT tc.user.id " +
                            "FROM TargetCredential1 tc " +
                            "WHERE tc.credential.id = :credId" +
                            ") " +
                            "GROUP BY user.id " +
                            "ORDER BY ";
            if (orderByTokens) {
                query += "user.numberOfTokens, ";
            }
            query +=
                    "COUNT(tCred.id), tComp.dateCreated";

            Long res = (Long) session
                    .createQuery(query)
                    .setLong("compId", compId)
                    .setLong("userId", userId)
                    .setString("aType", AssessmentType.PEER_ASSESSMENT.name())
                    .setLong("credId", credId)
                    .setParameterList("activeStatuses", AssessmentStatus.getActiveStatuses())
                    .setTimestamp("monthAgo", DateUtil.getNDaysFromNow(30))
                    .setMaxResults(1)
                    .uniqueResult();

            return res != null ? res.longValue() : 0;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving peer from the pool of available peer assessors");
        }
    }

}
