package org.prosolo.common.messaging.data;

import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;

import com.google.gson.JsonObject;

/**
@author Zoran Jeremic Apr 12, 2015
 *
 */

public class AnalyticalServiceMessage  extends SimpleMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9031443663136316404L;

	private DataType dataType;
	private DataName dataName;
	private JsonObject data;
	public DataType getDataType() {
		return dataType;
	}
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	public JsonObject getData() {
		return data;
	}
	public void setData(JsonObject data) {
		this.data = data;
	}
	public DataName getDataName() {
		return dataName;
	}
	public void setDataName(DataName dataName) {
		this.dataName = dataName;
	}

}

