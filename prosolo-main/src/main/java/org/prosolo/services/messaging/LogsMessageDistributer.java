package org.prosolo.services.messaging;

import org.prosolo.common.messaging.data.LogMessage;

import org.json.simple.JSONObject;

/**
@author Zoran Jeremic Apr 4, 2015
 *
 */

public interface LogsMessageDistributer {

	void wrapMessageAndSend(LogMessage message);

 

	void distributeMessage(JSONObject logObject);

}

