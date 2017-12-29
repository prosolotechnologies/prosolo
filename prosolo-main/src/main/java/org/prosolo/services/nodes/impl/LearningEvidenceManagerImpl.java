package org.prosolo.services.nodes.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceDataFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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

            CompetenceEvidence ce = attachEvidenceToCompetence(targetCompId, ev);
            res.setResult(learningEvidenceDataFactory.getLearningEvidenceData(ev, ce, ev.getTags()));
            return res;
        } catch (DbConnectionException|ConstraintViolationException|DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error saving the learning evidence");
        }
    }

    @Override
    @Transactional
    public Result<LearningEvidence> postEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            LearningEvidence ev = new LearningEvidence();

            ev.setOrganization((Organization) persistence.currentManager().load(Organization.class, context.getOrganizationId()));
            ev.setTitle(evidence.getTitle());
            ev.setDescription(evidence.getText());
            ev.setType(evidence.getType());
            if (ev.getType() != LearningEvidenceType.TEXT) {
                ev.setUrl(evidence.getUrl());
            }
            ev.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(evidence.getTagsString())));
            ev.setUser((User) persistence.currentManager().load(User.class, context.getActorId()));
            ev.setDateCreated(new Date());
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
            throw new DbConnectionException("Error saving the competence evidence");
        }
    }

    @Override
    @Transactional
    public CompetenceEvidence attachEvidenceToCompetence(long targetCompId, LearningEvidence evidence) throws DbConnectionException {
        try {
            CompetenceEvidence ce = new CompetenceEvidence();
            TargetCompetence1 targetCompetence = (TargetCompetence1) persistence.currentManager()
                    .load(TargetCompetence1.class, targetCompId);
            ce.setCompetence(targetCompetence);
            ce.setEvidence(evidence);
            ce.setDateCreated(new Date());
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
                evidenceData.add(learningEvidenceDataFactory.getLearningEvidenceData(
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

}
