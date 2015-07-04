package org.prosolo.bigdata.common.rest.pojo;

import java.util.ArrayList;
import java.util.List;

/**
@author Zoran Jeremic Apr 20, 2015
 *
 */

public class RequestParametersObject extends RequestObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8370294766577709068L;
	private Long objectId;
	
	
	public RequestParametersObject(){
		
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}



	
}

