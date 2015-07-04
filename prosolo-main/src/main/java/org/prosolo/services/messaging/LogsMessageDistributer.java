package org.prosolo.services.messaging;

import org.prosolo.services.messaging.data.LogMessage;

import com.mongodb.DBObject;

/**
@author Zoran Jeremic Apr 4, 2015
 *
 */

public interface LogsMessageDistributer {

	void wrapMessageAndSend(LogMessage message);

 

	void distributeMessage(DBObject logObject);

}

