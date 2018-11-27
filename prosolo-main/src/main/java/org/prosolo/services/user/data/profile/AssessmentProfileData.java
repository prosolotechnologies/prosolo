package org.prosolo.services.user.data.profile;

import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.user.data.UserBasicData;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class AssessmentProfileData implements Serializable {

    private static final long serialVersionUID = -3608777079207808629L;

    private final UserBasicData userBasicData;
    private final BlindAssessmentMode blindAssessmentMode;
    private final long dateApproved;
    private final AssessmentGradeSummary assessmentGradeSummary;

    public AssessmentProfileData(UserBasicData userBasicData, BlindAssessmentMode blindAssessmentMode, long dateApproved, AssessmentGradeSummary assessmentGradeSummary) {
        this.userBasicData = userBasicData;
        this.blindAssessmentMode = blindAssessmentMode;
        this.dateApproved = dateApproved;
        this.assessmentGradeSummary = assessmentGradeSummary;
    }

    public UserBasicData getUserBasicData() {
        return userBasicData;
    }

    public BlindAssessmentMode getBlindAssessmentMode() {
        return blindAssessmentMode;
    }

    public long getDateApproved() {
        return dateApproved;
    }

    public AssessmentGradeSummary getAssessmentGradeSummary() {
        return assessmentGradeSummary;
    }
}
