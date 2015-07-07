package org.prosolo.services.activityWall.filters;

import java.util.Set;
import java.util.TreeSet;

import org.prosolo.common.domainmodel.interfacesettings.FilterType;

/**
 * @author Zoran Jeremic Feb 4, 2015
 *
 */

public class CourseFilter extends Filter {
	private long courseId;
	private Set<String> hashtags;
	private Set<Long> targetCompetences;
	// private Long learningGoalId;
	private Set<Long> learningGoals;
	private Set<Long> targetLearningGoals;
	// private Long targetLearningGoalId;
	private Set<Long> targetActivities;
	
	public CourseFilter() {
		filterType = FilterType.COURSE;
		setHashtags(new TreeSet<String>());
		setTargetCompetences(new TreeSet<Long>());
		setTargetActivities(new TreeSet<Long>());
		setLearningGoals(new TreeSet<Long>());
		setTargetLearningGoals(new TreeSet<Long>());
	}
	
	public long getCourseId() {
		return courseId;
	}
	
	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}
	
	public Set<String> getHashtags() {
		return hashtags;
	}
	
	public void setHashtags(Set<String> hashtags) {
		this.hashtags = hashtags;
	}
	
	public void addHashtag(String hashtag) {
		this.hashtags.add(hashtag);
	}
	
	public boolean contains(String hashtag) {
		if (hashtags.contains(hashtag)) {
			return true;
		} else
			return false;
	}
	
	public Set<Long> getTargetCompetences() {
		return targetCompetences;
	}
	
	public void setTargetCompetences(Set<Long> targetCompetences) {
		this.targetCompetences = targetCompetences;
	}
	
	public void addTargetCompetence(Long targetCompetence) {
		this.targetCompetences.add(targetCompetence);
	}
	public void removeTargetCompetence(Long targetCompetence) {
		if(this.targetCompetences.contains(targetCompetence)){
			this.targetCompetences.remove(targetCompetence);
		}
	}
	
	public boolean containsTargetCompetence(Long tc) {
		if (targetCompetences.contains(tc)) {
			return true;
		} else
			return false;
	}
	
	public Set<Long> getTargetActivities() {
		return targetActivities;
	}
	
	public void setTargetActivities(Set<Long> targetActivities) {
		this.targetActivities = targetActivities;
	}
	
	public void addTargetActivity(Long targetActivity) {
		this.targetActivities.add(targetActivity);
	}
	public void removeTargetActivity(Long targetActivity) {
		if(this.targetActivities.contains(targetActivity)){
		this.targetActivities.remove(targetActivity);
		}
	}
	
	public boolean containsTargetActivity(Long ta) {
		if (targetActivities.contains(ta)) {
			return true;
		} else
			return false;
	}
	
	// public Long getLearningGoalId() {
	// return learningGoalId;
	// }
	//
	// public void setLearningGoalId(Long learningGoalId) {
	// this.learningGoalId = learningGoalId;
	// }
	//
	// public Long getTargetLearningGoalId() {
	// return targetLearningGoalId;
	// }
	//
	// public void setTargetLearningGoalId(Long targetLearningGoalId) {
	// this.targetLearningGoalId = targetLearningGoalId;
	// }
	
	public Set<Long> getTargetLearningGoals() {
		return targetLearningGoals;
	}
	
	public void setTargetLearningGoals(Set<Long> targetLearningGoals) {
		this.targetLearningGoals = targetLearningGoals;
	}
	
	public void addTargetLearningGoal(Long tlg) {
		this.targetLearningGoals.add(tlg);
	}
	
	public boolean containsTargetLearningGoal(Long tlg) {
		if (targetLearningGoals.contains(tlg)) {
			return true;
		} else
			return false;
	}
	
	public Set<Long> getLearningGoals() {
		return learningGoals;
	}
	
	public void setLearningGoals(Set<Long> learningGoals) {
		this.learningGoals = learningGoals;
	}
	
	public void addLearningGoal(Long lg) {
		this.learningGoals.add(lg);
	}
	
	public boolean containsLearningGoal(Long lg) {
		if (learningGoals.contains(lg)) {
			return true;
		} else
			return false;
	}
	
}
