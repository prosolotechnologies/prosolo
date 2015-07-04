package org.prosolo.services.messaging;

import java.util.Map;

import org.prosolo.services.messaging.data.ServiceType;
import org.prosolo.services.messaging.data.SessionMessage;


/**
@author Zoran Jeremic Sep 9, 2014
 */

public interface SessionMessageDistributer {

	//void distributeMessage(ServiceType serviceType, long receiverId, long resourceId);

	void wrapMessageAndSend(SessionMessage message);

	void distributeMessage(ServiceType serviceType, long receiverId,
			long resourceId, String resourceType, Map<String, String> parameters);

}
