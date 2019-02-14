package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.services.user.data.UserBasicData;
import org.prosolo.services.user.data.profile.grade.GradeData;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class AssessmentProfileData implements Serializable {

    private static final long serialVersionUID = -3608777079207808629L;

    private final long assessmentId;
    private final UserBasicData userBasicData;
    private final BlindAssessmentMode blindAssessmentMode;
    private final long dateApproved;
    private final GradeData assessmentGradeSummary;

}
