package org.prosolo.bigdata.events.pojo;

import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;

import com.google.gson.JsonObject;

/**
 * @author Zoran Jeremic Apr 12, 2015
 *
 */

public class AnalyticsEvent extends DefaultEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -866430130883375786L;
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
