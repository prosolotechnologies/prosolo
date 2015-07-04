package org.prosolo.bigdata.common.dal.pojo;

import java.io.Serializable;

/**
@author Zoran Jeremic Apr 19, 2015
 *
 */

public class ActivityAccessCount  implements Serializable, Comparable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5128704776961885483L;
	long activityId;
	long competenceId;
	long counter;
	public ActivityAccessCount(){
		
	}
	public ActivityAccessCount(long actId, long compId, long counter){
		this.setActivityId(actId);
		this.setCompetenceId(compId);
		this.setCounter(counter);
	}
	@Override
	public int compareTo(Object o) {
		ActivityAccessCount f = (ActivityAccessCount) o;
		if (this.getCounter() > f.getCounter()) {
			return -1;
		} else if (this.getCounter() < f.getCounter()) {
			return 1;
		} else {
			return 0;
		}
	}
	public long getActivityId() {
		return activityId;
	}
	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}
	public long getCompetenceId() {
		return competenceId;
	}
	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}
	public long getCounter() {
		return counter;
	}
	public void setCounter(long counter) {
		this.counter = counter;
	}
	
}

