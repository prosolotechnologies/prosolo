package org.prosolo.bigdata.common.dal.pojo;

import java.util.HashMap;
import java.util.Map;

/**
@author Zoran Jeremic May 31, 2015
 *
 */

public class MostActiveUsersForLearningGoal {
	long date;
	long learninggoal;
	Map<Long, Long> users;//user id, counter of user activities
	
	
	public MostActiveUsersForLearningGoal(){
		users=new HashMap<Long,Long>();
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public long getLearninggoal() {
		return learninggoal;
	}
	public void setLearninggoal(long learninggoal) {
		this.learninggoal = learninggoal;
	}
	public Map<Long, Long> getUsers() {
		return users;
	}
	public void setUsers(Map<Long, Long> users) {
		this.users = users;
	}
	public void addUser(Long userid, Long counter){
		System.out.println("Adding user:"+userid+" counter:"+counter+" has:"+users.size());
		users.put(userid, counter);
	}

}

