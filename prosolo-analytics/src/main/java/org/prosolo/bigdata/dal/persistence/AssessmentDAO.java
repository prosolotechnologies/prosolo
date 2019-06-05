package org.prosolo.bigdata.dal.persistence;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.EventData;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.util.date.DateUtil;

import java.util.Date;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-04-23
 * @since 1.3
 */
public interface AssessmentDAO {

    EventQueue assignAssessorFromAssessorPoolToCompetencePeerAssessmentAndGetEvents(long compAssessmentId);
    List<Long> getIdsOfUnassignedCompetencePeerAssessmentRequests();

    List<Long> getIdsOfAssignedCompetencePeerAssessmentRequestsOlderThanSpecified(Date olderThan);
    EventQueue expireCompetenceAssessmentRequest(long competenceAssessmentId);
}
