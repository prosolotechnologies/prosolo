package org.prosolo.services.messaging.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.messaging.data.BroadcastMessage;
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
	public void handle(BroadcastMessage message) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		try{
		switch (message.getServiceType()) {
		case BROADCAST_SOCIAL_ACTIVITY:
			long socialActivityId = Long.parseLong(message.getParameters().get("socialActivityId"));
			SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, socialActivityId, session);
			socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
			socialActivityFiltering.checkSocialActivity(socialActivity);
			break;
		
		}
	} catch (Exception e) {
		logger.error("Exception in handling message", e);
	} finally {
		HibernateUtil.close(session);
	}
		
	}

}
