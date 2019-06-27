package org.prosolo.services.assessment.data;

import lombok.*;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;

/**
 * Created by stefanvuckovic on 5/18/17.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentBasicData {

    private long credentialAssessmentId;
    private long credentialId;
    private long competenceAssessmentId;
    private long competenceId;
    private long activityAssessmentId;
    private long activityId;
    private long studentId;
    private long assessorId;
    private int grade;
    private AssessmentType type;
    private BlindAssessmentMode blindAssessmentMode;

}
