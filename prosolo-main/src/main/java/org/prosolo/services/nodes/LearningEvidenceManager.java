package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceLoadConfig;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-12-06
 * @since 1.2.0
 */
public interface LearningEvidenceManager {

    long postEvidence(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<LearningEvidence> postEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    CompetenceEvidence attachEvidenceToCompetence(long targetCompId, LearningEvidence evidence, String relationToCompetence) throws DbConnectionException;

    Result<LearningEvidenceData> postEvidenceAttachItToCompetenceAndGetEvents(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    LearningEvidenceData postEvidenceAndAttachItToCompetence(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    List<LearningEvidenceData> getUserEvidencesForACompetence(long targetCompId, LearningEvidenceLoadConfig loadConfig) throws DbConnectionException;

    void removeEvidenceFromCompetence(long compEvidenceId) throws DbConnectionException;

    List<LearningEvidence> getAllEvidences(long orgId, Session session) throws DbConnectionException;

    LearningEvidenceData getLearningEvidence(long evidenceId) throws DbConnectionException;

    PaginatedResult<LearningEvidenceData> getPaginatedUserEvidences(long userId, int offset, int limit) throws DbConnectionException;

    List<BasicObjectInfo> getCompetencesWithAddedEvidence(long evidenceId) throws DbConnectionException;

    List<String> getKeywordsFromAllUserEvidences(long userId) throws DbConnectionException;

    LearningEvidenceData getLearningEvidence(long evidenceId, LearningEvidenceLoadConfig loadConfig) throws DbConnectionException;

    Result<Void> deleteLearningEvidenceAndGetEvents(long evidenceId, UserContextData context) throws DbConnectionException;

    void deleteLearningEvidence(long evidenceId, UserContextData context) throws DbConnectionException;

    Result<LearningEvidence> updateEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    void updateEvidence(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    ResourceAccessData getResourceAccessRightsForEvidence(long evidenceId, long userId, ResourceAccessRequirements accessRequirements) throws DbConnectionException;

    /**
     * Returns true if competence evidence is published on profile as part of this
     * one competence.
     * If evidence is published on profile as a part of some other competence or if not published
     * at all, false is returned.
     *
     * @param compEvidenceId
     * @return
     * @throws DbConnectionException
     */
    boolean isCompetenceEvidencePublishedOnProfile(long compEvidenceId);

    /**
     * Returns evidence data for specified competence evidence id and null if competence
     * evidence with specified id does not exist.
     *
     * @param compEvidenceId
     * @param loadConfig
     * @return
     */
    LearningEvidenceData getCompetenceEvidenceData(long compEvidenceId, LearningEvidenceLoadConfig loadConfig);

    /**
     * Updates relation to competency of a provided competenceEvidence object (join class between a competency and
     * an evidence).
     *
     * @param competenceEvidenceId
     * @param newRelationText
     */
    void updateRelationToCompetency(long competenceEvidenceId, String newRelationText);
}
