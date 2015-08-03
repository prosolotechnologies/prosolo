package org.prosolo.services.interaction;

 

import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.common.messaging.data.AnalyticalServiceMessage;

import com.google.gson.JsonObject;

/**
@author Zoran Jeremic Apr 10, 2015
 *
 */

public interface AnalyticalServiceDataFactory {

	AnalyticalServiceMessage createAnalyticalServiceMessage(DataName dataName, DataType dataType,
			JsonObject data);

 

}

