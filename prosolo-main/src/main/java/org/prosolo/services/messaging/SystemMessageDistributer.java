package org.prosolo.services.messaging;

import java.util.Map;

import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.common.messaging.data.SystemMessage;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */

public interface SystemMessageDistributer {

	void distributeMessage(ServiceType serviceType,
			Map<String, String> parameters);

	void wrapMessageAndSend(SystemMessage message);

}
