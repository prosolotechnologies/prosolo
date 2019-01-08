package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.user.data.factory.UserBasicDataFactory;
import org.prosolo.services.user.data.profile.AssessmentProfileData;
import org.prosolo.services.user.data.profile.CompetenceEvidenceProfileData;
import org.prosolo.services.user.data.profile.grade.GradeData;

import javax.inject.Inject;

/**
 * @author stefanvuckovic
 * @date 2018-11-30
 * @since 1.2.0
 */
public class ProfileDataFactory {

    @Inject private UserBasicDataFactory userBasicDataFactory;

    protected AssessmentProfileData getCredentialAssessmentProfileData(CredentialAssessment ca, GradeData gradeSummary) {
        return new AssessmentProfileData(
                ca.getId(),
                userBasicDataFactory.getBasicUserData(ca.getAssessor()),
                ca.getBlindAssessmentMode(),
                DateUtil.getMillisFromDate(ca.getDateApproved()),
                gradeSummary);
    }

    protected AssessmentProfileData getCompetenceAssessmentProfileData(CompetenceAssessment ca, GradeData gradeSummary) {
        return new AssessmentProfileData(
                ca.getId(),
                userBasicDataFactory.getBasicUserData(ca.getAssessor()),
                ca.getBlindAssessmentMode(),
                DateUtil.getMillisFromDate(ca.getDateApproved()),
                gradeSummary);
    }

    protected CompetenceEvidenceProfileData getCompetenceEvidenceProfileData(CompetenceEvidence ce) {
        return new CompetenceEvidenceProfileData(
                ce.getEvidence().getId(),
                ce.getId(),
                ce.getEvidence().getTitle(),
                ce.getEvidence().getType(),
                ce.getEvidence().getUrl(),
                DateUtil.getMillisFromDate(ce.getEvidence().getDateCreated()));
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
