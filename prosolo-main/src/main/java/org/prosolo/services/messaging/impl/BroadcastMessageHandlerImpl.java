package org.prosolo.services.messaging.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.messaging.data.BroadcastMessage;
import org.prosolo.common.messaging.rabbitmq.WorkerException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.SocialActivityFiltering;
import org.prosolo.services.messaging.MessageHandler;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zoran Aug 3, 2015
 */
@Service("org.prosolo.services.messaging.BroadcastMessageHandler")
public class BroadcastMessageHandlerImpl  implements MessageHandler<BroadcastMessage>{
	private static Logger logger = Logger .getLogger(BroadcastMessageHandlerImpl.class.getName());
	@Autowired private SocialActivityFiltering socialActivityFiltering;
	@Autowired private DefaultManager defaultManager;
	
	@Override
	public void handle(BroadcastMessage message)  throws WorkerException {
		Session session = (Session) defaultManager.getPersistence().openSession();
		try{
		switch (message.getServiceType()) {
		case BROADCAST_SOCIAL_ACTIVITY:
			long socialActivityId = Long.parseLong(message.getParameters().get("socialActivityId"));
			SocialActivity1 socialActivity = defaultManager.loadResource(SocialActivity1.class, socialActivityId, session);
			socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
			socialActivityFiltering.checkSocialActivity(socialActivity, session);
			break;
		
		}
	} catch (Exception e) {
		logger.error("Exception in handling message", e);
			throw new WorkerException();
	} finally {
		HibernateUtil.close(session);
	}
		
	}

}
