package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.user.data.factory.UserBasicDataFactory;
import org.prosolo.services.user.data.profile.AssessmentByTypeProfileOptionsData;
import org.prosolo.services.user.data.profile.AssessmentProfileData;

import javax.inject.Inject;

/**
 * @author stefanvuckovic
 * @date 2018-11-30
 * @since 1.2.0
 */
public class ProfileDataFactory {

    @Inject private UserBasicDataFactory userBasicDataFactory;

    protected AssessmentProfileData getCredentialAssessmentProfileData(CredentialAssessment ca, AssessmentGradeSummary gradeSummary, BlindAssessmentMode blindAssessmentMode) {
        return new AssessmentProfileData(
                ca.getId(),
                userBasicDataFactory.getBasicUserData(ca.getAssessor()),
                blindAssessmentMode,
                DateUtil.getMillisFromDate(ca.getDateApproved()),
                gradeSummary);
    }

    protected AssessmentProfileData getCompetenceAssessmentProfileData(CompetenceAssessment ca, AssessmentGradeSummary gradeSummary, BlindAssessmentMode blindAssessmentMode) {
        return new AssessmentProfileData(
                ca.getId(),
                userBasicDataFactory.getBasicUserData(ca.getAssessor()),
                blindAssessmentMode,
                DateUtil.getMillisFromDate(ca.getDateApproved()),
                gradeSummary);
    }

    /**
     * Compares two assessment types and returns negative integer if first is 'less' than second, zero if equal and
     * positive if 'greater'.
     *
     * @param type1
     * @param type2
     * @return
     */
    protected int compareAssessmentTypes(AssessmentType type1, AssessmentType type2) {
        return getSortNumberForAssessmentType(type1) - getSortNumberForAssessmentType(type2);
    }

    private int getSortNumberForAssessmentType(AssessmentType type) {
        switch (type) {
            case INSTRUCTOR_ASSESSMENT:
                return 1;
            case PEER_ASSESSMENT:
                return 2;
            case SELF_ASSESSMENT:
                return 3;
            default:
                return 4;
        }
    }
}
