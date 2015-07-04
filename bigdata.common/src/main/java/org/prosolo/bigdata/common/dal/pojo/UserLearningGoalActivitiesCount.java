package org.prosolo.bigdata.common.dal.pojo;

import java.io.Serializable;

/**
@author Zoran Jeremic May 24, 2015
 *
 */

public class UserLearningGoalActivitiesCount implements Serializable, Comparable{
	long learningGoalId;
	long date;
	long userid;
	long counter;
	
	public UserLearningGoalActivitiesCount(){
		
	}
public UserLearningGoalActivitiesCount(Long lgId, Long userId, Long date, Long count){
		this.setLearningGoalId(lgId);
		this.setUserid(userId);
		this.setDate(date);
		this.setCounter(count);
	}
	public long getLearningGoalId() {
		return learningGoalId;
	}

	public void setLearningGoalId(long learningGoalId) {
		this.learningGoalId = learningGoalId;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}


	
	@Override
	public int compareTo(Object o) {
		UserLearningGoalActivitiesCount f = (UserLearningGoalActivitiesCount) o;
		if (this.getCounter() > f.getCounter()) {
			return -1;
		} else if (this.getCounter() < f.getCounter()) {
			return 1;
		} else {
			return 0;
		}
	}
}

