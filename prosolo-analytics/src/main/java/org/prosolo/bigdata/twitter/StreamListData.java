package org.prosolo.bigdata.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class StreamListData  implements Serializable{
	private Integer streamId;
	private String hashtag;
	private List<Long> goalsIds;
	private List<Long> usersIds;
	public StreamListData(String hashtag){
		setGoalsIds(new ArrayList<Long>());
		setUsersIds(new ArrayList<Long>());
		setHashtag(hashtag);
	}
	public StreamListData(String hashtag, List<Long> goalsIds){
	
		this(hashtag);
		setGoalsIds(goalsIds);

		
	}
	public Integer getStreamId() {
		return streamId;
	}
	public void setStreamId(Integer streamId) {
		this.streamId = streamId;
	}
	public String getHashtag() {
		return hashtag;
	}
	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}
	public List<Long> getGoalsIds() {
		return goalsIds;
	}
	public void setGoalsIds(List<Long> goalsIds) {
		this.goalsIds = goalsIds;
	}
	public void addGoalId(Long goalId){
		if(!goalsIds.contains(goalId)){
			goalsIds.add(goalId);
		}
	}
	public List<Long> getUsersIds() {
		return usersIds;
	}
	public void setUsersIds(List<Long> usersIds) {
		this.usersIds = usersIds;
	}
	public void addUserId(Long userId){
		if(!usersIds.contains(userId)){
			usersIds.add(userId);
		}
	}
	public void addUsersIds(List<Long> newUsersIds){
		for(Long userId:newUsersIds){
			if(!usersIds.contains(userId)){
				usersIds.add(userId);
			}
		}		
	}
	public boolean isFreeToRemove(){
		if(usersIds.isEmpty() && goalsIds.isEmpty()){
			return true;
		}else return false;
	}
	public boolean removeUserId(Long userId){
		if(usersIds.contains(userId)){
			
			usersIds.remove(userId);
		}
		return isFreeToRemove();
	}
	public boolean removeLearningGoalId(Long lGoalId){
		if(goalsIds.contains(lGoalId)){
			goalsIds.remove(lGoalId);
		}
		return isFreeToRemove();
	}
}

