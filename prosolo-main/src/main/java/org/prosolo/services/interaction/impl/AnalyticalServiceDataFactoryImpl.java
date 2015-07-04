package org.prosolo.services.interaction.impl;

import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.common.rabbitmq.AnalyticalServiceMessage;
import org.prosolo.services.interaction.AnalyticalServiceDataFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

/**
@author Zoran Jeremic Apr 10, 2015
 *
 */
@Service("org.prosolo.services.interaction.AnalyticalServiceDataFactory")
public class AnalyticalServiceDataFactoryImpl implements AnalyticalServiceDataFactory{
 

	@Override
	public AnalyticalServiceMessage createAnalyticalServiceMessage(
			DataName dataName, DataType dataType, JsonObject data) {
		AnalyticalServiceMessage message=new AnalyticalServiceMessage();
		message.setDataName(dataName);
		message.setDataType(dataType);
		message.setData(data);
		return message;
	}
}

