package org.prosolo.bigdata.common.rest.pojo;

import java.util.HashMap;
import java.util.Map;

/**
@author Zoran Jeremic May 10, 2015
 *
 */

public class RequestListParametersObject extends RequestObject {
	private Map<String,Long> parameters;
	
	
	public RequestListParametersObject(){
		parameters=new HashMap<String,Long>();
	}
	public Map<String,Long> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String,Long> parameters) {
		this.parameters = parameters;
	}
	public void addParameter(String key, Long value){
		this.parameters.put(key, value);
	}
	public Long getParameter(String key){
		if(parameters.containsKey(key)){
			return this.parameters.get(key);
		}else{
			return null;
		}
	}
 
 
}

