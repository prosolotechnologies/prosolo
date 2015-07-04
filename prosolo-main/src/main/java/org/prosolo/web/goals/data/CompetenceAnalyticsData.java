package org.prosolo.web.goals.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.web.activitywall.data.UserData;

public class CompetenceAnalyticsData implements Serializable {
	
	private static final long serialVersionUID = -907208996157075307L;

	private List<UserData> usedBy;
	private int numOfUsersAchieving;
	private int numOfLikes;
	private int numOfDislikes;
	private String averageTime;
	
	public CompetenceAnalyticsData(){
		this.usedBy = new ArrayList<UserData>();
	}
	
	/* 
	 * GETTERS / SETTERS
	 */
	
	public int getNumOfUsedBy() {
		return usedBy.size();
	}

	public int getNumOfUsersAchieving() {
		return numOfUsersAchieving;
	}

	public void setNumOfUsersAchieving(int numOfUsersAchieving) {
		this.numOfUsersAchieving = numOfUsersAchieving;
	}

	public int getNumOfLikes() {
		return numOfLikes;
	}

	public void setNumOfLikes(int numOfLikes) {
		this.numOfLikes = numOfLikes;
	}

	public int getNumOfDislikes() {
		return numOfDislikes;
	}

	public void setNumOfDislikes(int numOfDislikes) {
		this.numOfDislikes = numOfDislikes;
	}

	public void setAverageTime(String averageTime) {
		this.averageTime = averageTime;
	}

	public String getAverageTime(){
		return averageTime;
	}

	public List<UserData> getUsedBy() {
		return usedBy;
	}

	public void setUsedBy(List<UserData> usedBy) {
		this.usedBy = usedBy;
	}
	
	public void addUsedBy(UserData user) {
		this.usedBy.add(user);
	}
	
}
