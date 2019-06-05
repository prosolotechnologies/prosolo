package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.AssessmentDAO;
import org.prosolo.bigdata.dal.persistence.impl.AssessmentDAOImpl;
import org.prosolo.bigdata.scala.messaging.AppEventDistributer;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.util.date.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Job that assigns available assessor to unassigned peer assessment requests
 * and invalidates assessment request that should be expired (14 days without assessor accepting/declining the request)
 *
 * @author stefanvuckovic
 * @date 2019-04-23
 * @since 1.3.2
 */
public class PeerAssessmentMaintenanceJob implements Job {

	private static Logger logger = Logger
			.getLogger(PeerAssessmentMaintenanceJob.class.getName());

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Peer assessment maintenance job started");
		AssessmentDAO assessmentDAO = new AssessmentDAOImpl();
		assignUnassignedAssessments(assessmentDAO);
		expireOldAssessmentRequests(assessmentDAO);
		logger.info("Peer assessment maintenance job completed");
	}

	private void assignUnassignedAssessments(AssessmentDAO assessmentDAO) {
		logger.info("Assigning unassigned peer assessments started");
		List<Long> assessmentIds = assessmentDAO.getIdsOfUnassignedCompetencePeerAssessmentRequests();
		for (long assessmentId : assessmentIds) {
			EventQueue events = assessmentDAO.assignAssessorFromAssessorPoolToCompetencePeerAssessmentAndGetEvents(assessmentId);
			if (!events.isEmpty()) {
				AppEventDistributer.distributeMessage(events);
			}
		}
		logger.info("Assigning unassigned peer assessments finished");
	}

	private void expireOldAssessmentRequests(AssessmentDAO assessmentDAO) {
		logger.info("Invalidating old peer assessment requests started");
		List<Long> assessmentsToExpireIds = assessmentDAO.getIdsOfAssignedCompetencePeerAssessmentRequestsOlderThanSpecified(DateUtil.getNDaysBeforeNow(14));
		for (long id : assessmentsToExpireIds) {
			EventQueue eventQueue = assessmentDAO.expireCompetenceAssessmentRequest(id);
			if (!eventQueue.isEmpty()) {
				AppEventDistributer.distributeMessage(eventQueue);
			}
		}
		logger.info("Invalidating old peer assessment requests finished");
	}

}
