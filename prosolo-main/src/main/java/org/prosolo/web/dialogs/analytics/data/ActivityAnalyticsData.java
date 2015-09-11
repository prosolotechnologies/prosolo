/**
 * 
 */
package org.prosolo.web.dialogs.analytics.data;

import java.io.Serializable;

/**
 * @author "Nikola Milikic"
 * 
 */
public class ActivityAnalyticsData implements Serializable {

	private static final long serialVersionUID = 3254746749872628920L;

	private int likes;
	private int dislikes;
	private int completedGoals;
	private int ongoingGoals;

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getDislikes() {
		return dislikes;
	}

	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}

	public int getCompletedGoals() {
		return completedGoals;
	}

	public void setCompletedGoals(int completedGoals) {
		this.completedGoals = completedGoals;
	}

	public int getOngoingGoals() {
		return ongoingGoals;
	}

	public void setOngoingGoals(int ongoingGoals) {
		this.ongoingGoals = ongoingGoals;
	}

}
