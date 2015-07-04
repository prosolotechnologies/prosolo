package org.prosolo.bigdata.common.rest.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
@author Zoran Jeremic May 10, 2015
 *
 */

public class RequestObject implements Serializable {
	private int limit;
	private List<Long> ignoredIds;
	
	public RequestObject(){
		ignoredIds=new ArrayList<Long>();
	}
	
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	public List<Long> getIgnoredIds() {
		return ignoredIds;
	}

	public void setIgnoredIds(List<Long> ignoredIds) {
		this.ignoredIds = ignoredIds;
	}
}

