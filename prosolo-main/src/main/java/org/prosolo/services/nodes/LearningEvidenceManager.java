package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-12-06
 * @since 1.2.0
 */
public interface LearningEvidenceManager {

    Result<LearningEvidence> postEvidenceAndGetEvents(LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    CompetenceEvidence attachEvidenceToCompetence(long targetCompId, LearningEvidence evidence) throws DbConnectionException;

    Result<LearningEvidenceData> postEvidenceAttachItToCompetenceAndGetEvents(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    LearningEvidenceData postEvidenceAndAttachItToCompetence(long targetCompId, LearningEvidenceData evidence, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    List<LearningEvidenceData> getUserEvidencesForACompetence(long targetCompId, boolean loadTags) throws DbConnectionException;

    void removeEvidenceFromCompetence(long compEvidenceId) throws DbConnectionException;

    List<LearningEvidence> getAllEvidences(long orgId, Session session) throws DbConnectionException;

    LearningEvidenceData getLearningEvidence(long evidenceId) throws DbConnectionException;
}
