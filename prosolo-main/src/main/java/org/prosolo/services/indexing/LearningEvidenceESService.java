package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.credential.LearningEvidence;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
public interface LearningEvidenceESService {

    void saveEvidence(LearningEvidence evidence);

    void deleteEvidence(long orgId, long evidenceId);
}
