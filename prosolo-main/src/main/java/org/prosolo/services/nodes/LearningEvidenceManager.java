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

    CompetenceEvidence attachEvidenceToCompetence(long targetCompId, LearningEvidence evidence) throws DbConnectionException;

    Result<LearningEvidenceData> postEvidenceAttachItToCompetenceAndGetEvents(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    LearningEvidenceData postEvidenceAndAttachItToCompetence(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    List<LearningEvidenceData> getUserEvidencesForACompetence(long targetCompId, boolean loadTags) throws DbConnectionException;

    void removeEvidenceFromCompetence(long compEvidenceId) throws DbConnectionException;

    List<LearningEvidence> getAllEvidences(long orgId, Session session) throws DbConnectionException;

    LearningEvidenceData getLearningEvidence(long evidenceId) throws DbConnectionException;

    PaginatedResult<LearningEvidenceData> getPaginatedUserEvidences(long userId, int offset, int limit) throws DbConnectionException;

    List<BasicObjectInfo> getCompetencesWithAddedEvidence(long evidenceId) throws DbConnectionException;

    List<String> getKeywordsFromAllUserEvidences(long userId) throws DbConnectionException;

    LearningEvidenceData getLearningEvidence(long evidenceId, boolean loadTags, boolean loadCompetencesWithEvidence) throws DbConnectionException;

    Result<Void> deleteLearningEvidenceAndGetEvents(long evidenceId, UserContextData context) throws DbConnectionException;

    void deleteLearningEvidence(long evidenceId, UserContextData context) throws DbConnectionException;

    Result<LearningEvidence> updateEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    void updateEvidence(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

}
