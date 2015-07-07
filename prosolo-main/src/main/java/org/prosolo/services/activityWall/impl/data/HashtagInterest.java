/**
 * 
 */
package org.prosolo.services.activityWall.impl.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.LearningGoal;

/**
 * @author "Nikola Milikic"
 * 
 */
public class HashtagInterest {

	private LearningGoal goal;
	private Set<Tag> hashtags;
	
	public HashtagInterest() {
		hashtags = new HashSet<Tag>();
	}
	
	public HashtagInterest(Collection<Tag> hashtags) {
		this();
		this.hashtags.addAll(hashtags);
	}
	
	public HashtagInterest(LearningGoal goal, Collection<Tag> hashtags) {
		this(hashtags);
		this.goal = goal;
	}

	public LearningGoal getGoal() {
		return goal;
	}

	public void setGoal(LearningGoal goal) {
		this.goal = goal;
	}

	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

}
