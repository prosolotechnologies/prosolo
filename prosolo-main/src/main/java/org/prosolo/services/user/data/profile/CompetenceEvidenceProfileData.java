package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
@AllArgsConstructor
@Getter
public class CompetenceEvidenceProfileData implements Serializable {

    private static final long serialVersionUID = -6875110248347473987L;


    private final long evidenceId;
    private final long competenceEvidenceId;
    private final String title;
    private final LearningEvidenceType type;
    private final String url;
    private final long dateCreated;

}
