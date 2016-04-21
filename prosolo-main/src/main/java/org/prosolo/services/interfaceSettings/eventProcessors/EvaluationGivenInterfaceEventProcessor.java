package org.prosolo.services.interfaceSettings.eventProcessors;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.notifications.EvaluationUpdater;
import org.prosolo.web.ApplicationBean;

public class EvaluationGivenInterfaceEventProcessor extends InterfaceEventProcessor {

	private static Logger logger = Logger.getLogger(EvaluationGivenInterfaceEventProcessor.class);
	
	private EvaluationUpdater evaluationUpdater;
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	
	
	public EvaluationGivenInterfaceEventProcessor(Session session, Event event, BaseEntity object,
			EvaluationUpdater evaluationUpdater, SessionMessageDistributer messageDistributer,
			ApplicationBean applicationBean) {
		super(session, event, object);
		this.evaluationUpdater = evaluationUpdater;
		this.messageDistributer = messageDistributer;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		EvaluationSubmission evSubmission = (EvaluationSubmission) object;	
		User receiver = evSubmission.getRequest().getMaker();

		/*
		 * If user assessed is online, update the number of assessments for this resource.
		 * This number of assessments is being updated in session scoped beans used for displaying
		 * number of evaluations on Learn and Profile pages.
		 */
		long receiverId = receiver.getId();
		
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			messageDistributer.distributeMessage(
				ServiceType.UPDATE_EVALUAIION_DATA,
				receiverId, 
				evSubmission.getId(), 
				null, 
				null);
		} else {
			HttpSession userSession = applicationBean.getUserSession(receiverId);
			
			try {
				evaluationUpdater.updateEvaluationData(
					evSubmission.getId(),
					userSession,
					session);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	
}
