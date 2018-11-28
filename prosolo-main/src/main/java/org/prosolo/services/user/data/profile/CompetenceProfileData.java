package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.services.common.data.LazyInitData;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CompetenceProfileData implements Serializable {

    private static final long serialVersionUID = 4566644824168327473L;

    private final long id;
    private final String title;
    private final LearningPathType learningPathType;
    private final LazyInitData<CompetenceEvidenceProfileData> evidence;
    private final LazyInitData<AssessmentByTypeProfileData> assessments;

}
