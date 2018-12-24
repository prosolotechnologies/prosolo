package org.prosolo.web.assessments;

import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-12-14
 * @since 1.2.0
 */
public interface AssessmentCommentsAware extends CurrentAssessmentBeanAware {

    List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages();

    BlindAssessmentMode getCurrentBlindAssessmentMode();
}
