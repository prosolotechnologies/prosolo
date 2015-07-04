package org.prosolo.services.messaging;

import org.prosolo.bigdata.common.rabbitmq.AnalyticalServiceMessage;

 

/**
@author Zoran Jeremic Apr 12, 2015
 *
 */

public interface AnalyticalServiceMessageDistributer {


	void wrapMessageAndSend(AnalyticalServiceMessage message);

	void distributeMessage(AnalyticalServiceMessage message);

}

