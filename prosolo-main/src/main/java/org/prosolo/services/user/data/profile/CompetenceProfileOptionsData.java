package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.services.common.data.SelectableData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-20
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CompetenceProfileOptionsData implements Serializable {

    private static final long serialVersionUID = -6284496293208098696L;

    private final long id;
    private final String title;
    private final LearningPathType learningPathType;
    private final List<SelectableData<CompetenceEvidenceProfileData>> evidence;
    private final List<AssessmentByTypeProfileOptionsData> assessments;

}
