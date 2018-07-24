package org.prosolo.services.nodes.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceDataFactory;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2017-12-06
 * @since 1.2.0
 */
@Service("org.prosolo.services.nodes.LearningEvidenceManager")
public class LearningEvidenceManagerImpl extends AbstractManagerImpl implements LearningEvidenceManager {

    private static final long serialVersionUID = -3272110803065555720L;

    @Inject private TagManager tagManager;
    @Inject private EventFactory eventFactory;
    @Inject private LearningEvidenceManager self;
    @Inject private LearningEvidenceDataFactory learningEvidenceDataFactory;

    @Override
    //nt
    public LearningEvidenceData postEvidenceAndAttachItToCompetence(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        Result<LearningEvidenceData> res = self.postEvidenceAttachItToCompetenceAndGetEvents(targetCompId, evidence, context);
        eventFactory.generateEvents(res.getEventQueue());
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<LearningEvidenceData> postEvidenceAttachItToCompetenceAndGetEvents(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<LearningEvidenceData> res = new Result<>();
            LearningEvidence ev;
            //if id is not greater than zero, it means that evidence does not exist and it should be created
            if (evidence.getId() <= 0) {
                Result<LearningEvidence> evPostResult = postEvidenceAndGetEvents(evidence, context);
                res.appendEvents(evPostResult.getEventQueue());
                ev = evPostResult.getResult();
            } else {
                ev = (LearningEvidence) persistence.currentManager().load(LearningEvidence.class, evidence.getId());
            }

            CompetenceEvidence ce = attachEvidenceToCompetence(targetCompId, ev, evidence.getRelationToCompetence());
            res.setResult(learningEvidenceDataFactory.getCompetenceLearningEvidenceData(ev, ce, ev.getTags()));
            return res;
        } catch (DbConnectionException|ConstraintViolationException|DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error saving the learning evidence");
        }
    }

    @Override
    //nt
    public long postEvidence(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        Result<LearningEvidence> res = self.postEvidenceAndGetEvents(evidence, context);
        eventFactory.generateEvents(res.getEventQueue());
        return res.getResult().getId();
    }

    @Override
    @Transactional
    public Result<LearningEvidence> postEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            LearningEvidence ev = new LearningEvidence();

            ev.setOrganization((Organization) persistence.currentManager().load(Organization.class, context.getOrganizationId()));
            ev.setUser((User) persistence.currentManager().load(User.class, context.getActorId()));
            ev.setDateCreated(new Date());
            setEvidenceData(ev, evidence);
            saveEntity(ev);

            Result<LearningEvidence> result = new Result<>();
            result.setResult(ev);
            LearningEvidence eventObject = new LearningEvidence();
            eventObject.setId(ev.getId());
            result.appendEvent(eventFactory.generateEventData(EventType.Create, context, eventObject, null, null, null));
            return result;
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error saving the learning evidence");
        }
    }

    private void setEvidenceData(LearningEvidence evidence, LearningEvidenceData evidenceData) {
        evidence.setTitle(evidenceData.getTitle());
        evidence.setDescription(evidenceData.getText());
        evidence.setType(evidenceData.getType());
        if (evidence.getType() != LearningEvidenceType.TEXT) {
            evidence.setUrl(evidenceData.getUrl());
        }
        evidence.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(evidenceData.getTagsString())));
    }

    @Override
    @Transactional
    public CompetenceEvidence attachEvidenceToCompetence(long targetCompId, LearningEvidence evidence, String relationToCompetence) throws DbConnectionException {
        try {
            CompetenceEvidence ce = new CompetenceEvidence();
            TargetCompetence1 targetCompetence = (TargetCompetence1) persistence.currentManager()
                    .load(TargetCompetence1.class, targetCompId);
            ce.setCompetence(targetCompetence);
            ce.setEvidence(evidence);
            ce.setDateCreated(new Date());
            ce.setDescription(relationToCompetence);
            saveEntity(ce);
            return ce;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error attaching evidence to competence");
        }
    }

    @Override
    @Transactional
    public List<LearningEvidenceData> getUserEvidencesForACompetence(long targetCompId, boolean loadTags) throws DbConnectionException {
        try {
            String query =
                    "SELECT distinct ce from CompetenceEvidence ce " +
                    "INNER JOIN fetch ce.evidence le ";
            if (loadTags) {
                query +=
                        "LEFT JOIN fetch le.tags ";
            }
            query +=
                    "WHERE ce.competence.id = :tcId " +
                    "AND ce.deleted IS FALSE " +
                    "ORDER BY ce.dateCreated ASC";

            @SuppressWarnings("unchecked")
            List<CompetenceEvidence> evidence = persistence.currentManager()
                    .createQuery(query)
                    .setLong("tcId", targetCompId)
                    .list();
            List<LearningEvidenceData> evidenceData = new ArrayList<>();
            for (CompetenceEvidence ce : evidence) {
                evidenceData.add(learningEvidenceDataFactory.getCompetenceLearningEvidenceData(
                        ce.getEvidence(), ce, loadTags ? ce.getEvidence().getTags() : null));
            }
            return evidenceData;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading user evidence");
        }
    }

    @Override
    @Transactional
    public void removeEvidenceFromCompetence(long compEvidenceId) throws DbConnectionException {
        try {
            CompetenceEvidence ce = (CompetenceEvidence) persistence.currentManager().load(CompetenceEvidence.class, compEvidenceId);
            ce.setDeleted(true);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error removing evidence from a competence");
        }
    }

    @Override
    @Transactional
    public List<LearningEvidence> getAllEvidences(long orgId, Session session) throws DbConnectionException {
        try {
            String query =
                    "SELECT le from LearningEvidence le " +
                    "WHERE le.deleted IS FALSE ";
            if (orgId > 0) {
                query += "AND le.organization.id = :orgId";
            }

            Query q = session.createQuery(query);
            if (orgId > 0) {
                q.setLong("orgId", orgId);
            }
            @SuppressWarnings("unchecked")
            List<LearningEvidence> evidences = q.list();
            return evidences;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the evidence");
        }
    }

    @Override
    @Transactional
    public LearningEvidenceData getLearningEvidence(long evidenceId) throws DbConnectionException {
        try {
            LearningEvidence le = (LearningEvidence) persistence.currentManager().load(LearningEvidence.class, evidenceId);
            return learningEvidenceDataFactory.getLearningEvidenceData(le, null, null);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the evidence");
        }
    }

    @Override
    @Transactional (readOnly = true)
    public PaginatedResult<LearningEvidenceData> getPaginatedUserEvidences(long userId, int offset, int limit) throws DbConnectionException {
        try {
            PaginatedResult<LearningEvidenceData> res = new PaginatedResult<>();
            res.setHitsNumber(countUserEvidences(userId));
            if (res.getHitsNumber() > 0) {
                res.setFoundNodes(getUserEvidences(userId, offset, limit));
            }
            return res;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the user evidences");
        }
    }

    private List<LearningEvidenceData> getUserEvidences(long userId, int offset, int limit) {
        String query =
                "SELECT le FROM LearningEvidence le " +
                "LEFT JOIN fetch le.tags " +
                "WHERE le.user.id = :userId " +
                "AND le.deleted IS FALSE " +
                "ORDER BY le.dateCreated DESC";

        @SuppressWarnings("unchecked")
        List<LearningEvidence> evidences = persistence.currentManager()
                .createQuery(query)
                .setLong("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();

        return evidences.stream()
                .map(ev -> learningEvidenceDataFactory.getLearningEvidenceData(ev, ev.getTags(), getCompetencesWithAddedEvidence(ev.getId())))
                .collect(Collectors.toList());
    }

    private long countUserEvidences(long userId) {
        String query =
                "SELECT COUNT(le.id) FROM LearningEvidence le " +
                "WHERE le.user.id = :userId " +
                "AND le.deleted IS FALSE";

        return (Long) persistence.currentManager()
                .createQuery(query)
                .setLong("userId", userId)
                .uniqueResult();
    }

    @Transactional (readOnly = true)
    @Override
    public List<BasicObjectInfo> getCompetencesWithAddedEvidence(long evidenceId) throws DbConnectionException {
        try {
            String query =
                    "SELECT comp, ce.description " +
                            "FROM CompetenceEvidence ce " +
                            "INNER JOIN ce.competence tc " +
                            "INNER JOIN tc.competence comp " +
                            "WHERE ce.evidence.id = :evId " +
                            "AND ce.deleted IS FALSE";

            @SuppressWarnings("unchecked")
            List<Object[]> competences = persistence.currentManager()
                    .createQuery(query)
                    .setLong("evId", evidenceId)
                    .list();

            List<BasicObjectInfo> comps = new ArrayList<>();
            for (Object[] row : competences) {
                Competence1 comp = (Competence1) row[0];
                comps.add(new BasicObjectInfo(comp.getId(), comp.getTitle(), (String) row[1]));
            }
            return comps;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the competences evidence is added to");
        }
    }

    @Transactional (readOnly = true)
    @Override
    public List<String> getKeywordsFromAllUserEvidences(long userId) throws DbConnectionException {
        try {
            String query =
                    "SELECT DISTINCT t.title " +
                    "FROM LearningEvidence le " +
                    "INNER JOIN le.tags t " +
                    "WHERE le.user.id = :userId " +
                    "AND le.deleted IS FALSE";

            @SuppressWarnings("unchecked")
            List<String> tags = persistence.currentManager()
                    .createQuery(query)
                    .setLong("userId", userId)
                    .list();

            return tags;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the evidences keywords");
        }
    }

    @Transactional (readOnly = true)
    @Override
    public LearningEvidenceData getLearningEvidence(long evidenceId, boolean loadTags, boolean loadCompetencesWithEvidence) throws DbConnectionException {
        try {
            String query =
                    "SELECT le FROM LearningEvidence le ";
            if (loadTags) {
               query +=  "LEFT JOIN fetch le.tags ";
            }
            query += "WHERE le.id = :evId " +
                     "AND le.deleted IS FALSE";

            LearningEvidence evidence = (LearningEvidence) persistence.currentManager()
                    .createQuery(query)
                    .setLong("evId", evidenceId)
                    .uniqueResult();

            if (evidence == null) {
                return null;
            }

            Set<Tag> tags = loadTags ? evidence.getTags() : null;
            List<BasicObjectInfo> competences = loadCompetencesWithEvidence ? getCompetencesWithAddedEvidence(evidence.getId()) : Collections.emptyList();
            return learningEvidenceDataFactory.getLearningEvidenceData(evidence, tags, competences);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the learning evidence");
        }
    }

    @Override
    //nt
    public void deleteLearningEvidence(long evidenceId, UserContextData context) throws DbConnectionException {
        eventFactory.generateEvents(self.deleteLearningEvidenceAndGetEvents(evidenceId, context).getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> deleteLearningEvidenceAndGetEvents(long evidenceId, UserContextData context) throws DbConnectionException {
        try {
            //TODO check if this is the desired behavior
            deleteAllCompetenceEvidencesForEvidence(evidenceId);
            LearningEvidence le = (LearningEvidence) persistence.currentManager().load(LearningEvidence.class, evidenceId);
            le.setDeleted(true);

            LearningEvidence obj = new LearningEvidence();
            obj.setId(evidenceId);

            Result<Void> res = new Result<>();
            res.appendEvent(eventFactory.generateEventData(EventType.Delete, context, obj, null, null, null));
            return res;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error deleting the learning evidence");
        }
    }

    private void deleteAllCompetenceEvidencesForEvidence(long evidenceId) {
        String q =
                "UPDATE CompetenceEvidence ce SET ce.deleted = :deleted " +
                "WHERE ce.evidence.id = :evId";

        persistence.currentManager()
                .createQuery(q)
                .setBoolean("deleted", true)
                .setLong("evId", evidenceId)
                .executeUpdate();
    }

    @Override
    //nt
    public void updateEvidence(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        Result<LearningEvidence> res = self.updateEvidenceAndGetEvents(evidence, context);
        eventFactory.generateEvents(res.getEventQueue());
    }

    @Override
    @Transactional
    public Result<LearningEvidence> updateEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            LearningEvidence ev = (LearningEvidence) persistence.currentManager().load(LearningEvidence.class, evidence.getId());
            setEvidenceData(ev, evidence);

            Result<LearningEvidence> result = new Result<>();
            result.setResult(ev);
            LearningEvidence eventObject = new LearningEvidence();
            eventObject.setId(ev.getId());
            result.appendEvent(eventFactory.generateEventData(EventType.Edit, context, eventObject, null, null, null));
            return result;
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error updating the learning evidence");
        }
    }

    @Override
    @Transactional
    public ResourceAccessData getResourceAccessRightsForEvidence(long evidenceId, long userId, ResourceAccessRequirements accessRequirements) throws DbConnectionException {
        try {
            LearningEvidence le = (LearningEvidence) persistence.currentManager()
                .load(LearningEvidence.class, evidenceId);
            if (userId > 0) {
                //if user is evidence owner resource access data with full privileges is returned
                if (accessRequirements.getAccessMode() == AccessMode.USER && le.getUser().getId() == userId) {
                    return new ResourceAccessData(true, true, true, false, false);
                }

                //check if user is assessor on at least one competence where this evidence is added
                String query =
                        "SELECT ca.id FROM CompetenceAssessment ca " +
                                "INNER JOIN ca.competence comp " +
                                "INNER JOIN comp.targetCompetences tc " +
                                "WITH tc.user.id = :studentId " +
                                "INNER JOIN tc.evidences ce " +
                                "WHERE ce.evidence.id = :evId " +
                                "AND ce.deleted IS FALSE " +
                                "AND ca.assessor.id = :userId " +
                                "AND ca.type = :type";

                AssessmentType aType = accessRequirements.getAccessMode() == AccessMode.USER
                        ? AssessmentType.PEER_ASSESSMENT
                        : AssessmentType.INSTRUCTOR_ASSESSMENT;

                Long id = (Long) persistence.currentManager()
                        .createQuery(query)
                        .setLong("studentId", le.getUser().getId())
                        .setLong("evId", evidenceId)
                        .setLong("userId", userId)
                        .setString("type", aType.name())
                        .setMaxResults(1)
                        .uniqueResult();

                if (id != null) {
                    return new ResourceAccessData(true, true, false, false, false);
                }
            }

            if (accessRequirements.getAccessMode() == AccessMode.USER) {
                boolean canAccess = hasUserAllowedHisEvidenceToBeSeenByOtherStudents(le);
                return new ResourceAccessData(canAccess, canAccess, false, false, false);
            }
            return new ResourceAccessData(false, false, false, false, false);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error determining learning evidence access rights");
        }
    }

    /**
     * Returns true if evidence display is enabled by user on at least one credential with competence for which this evidence is attached
     * @param le
     * @return
     */
    private boolean hasUserAllowedHisEvidenceToBeSeenByOtherStudents(LearningEvidence le) {
        String q =
                "SELECT tCred.id FROM CompetenceEvidence ce " +
                "INNER JOIN ce.competence tc " +
                "INNER JOIN tc.competence comp " +
                "INNER JOIN comp.credentialCompetences cc " +
                "INNER JOIN cc.credential cred " +
                "INNER JOIN cred.targetCredentials tCred " +
                    "WITH tCred.user.id = :userId " +
                    "AND tCred.evidenceDisplayed IS TRUE " +
                "WHERE ce.evidence.id = :evidenceId";

        Long res = (Long) persistence.currentManager().createQuery(q)
                .setLong("userId", le.getUser().getId())
                .setLong("evidenceId", le.getId())
                .setMaxResults(1)
                .uniqueResult();

        return res != null;
    }

}
