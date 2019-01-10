package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.services.common.data.LazyInitCollection;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CredentialProfileData implements Serializable {

    private static final long serialVersionUID = 2006037751655590239L;

    private final long credentialProfileConfigId;
    private final long targetCredentialId;
    private final long credentialId;
    private final String title;
    private final String description;
    private final List<String> keywords;
    private final long dateCompleted;
    private final LazyInitCollection<AssessmentByTypeProfileData> assessments;
    private final LazyInitCollection<CompetenceProfileData> competences;
    private final CredentialCategoryData category;

}
