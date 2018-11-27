package org.prosolo.services.user.data.profile;

import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.services.nodes.data.BasicObjectInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class CompetenceEvidenceProfileData implements Serializable {

    private static final long serialVersionUID = -6875110248347473987L;


    private final long evidenceId;
    private final String title;
    private final LearningEvidenceType type;
    //date when evidence is added to the competence
    private final long dateAttached;

    public CompetenceEvidenceProfileData(long evidenceId, String title, LearningEvidenceType type, long dateAttached) {
        this.evidenceId = evidenceId;
        this.title = title;
        this.type = type;
        this.dateAttached = dateAttached;
    }
}
