package org.prosolo.services.messaging;

import org.prosolo.common.messaging.data.AnalyticalServiceMessage;

 

/**
@author Zoran Jeremic Apr 12, 2015
 *
 */

public interface AnalyticalServiceMessageDistributer {


	void wrapMessageAndSend(AnalyticalServiceMessage message);

	void distributeMessage(AnalyticalServiceMessage message);

}

