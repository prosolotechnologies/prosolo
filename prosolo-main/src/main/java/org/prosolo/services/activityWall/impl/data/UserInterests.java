/**
 * 
 */
package org.prosolo.services.activityWall.impl.data;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 * 
 */
public class UserInterests implements Comparable<UserInterests> {

	private User user;
	private List<HashtagInterest> hashtagInterests;
	
	public UserInterests() {
		hashtagInterests = new ArrayList<HashtagInterest>();
	}
	
	public UserInterests(User user, List<HashtagInterest> hashtagInterests) {
		this.user = user;
		this.hashtagInterests = hashtagInterests;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<HashtagInterest> getHashtagInterests() {
		return hashtagInterests;
	}

	public void setHashtagInterests(List<HashtagInterest> hashtagInterests) {
		this.hashtagInterests = hashtagInterests;
	}
	
	public void addHashtagInterest(HashtagInterest hashtagInterest) {
		this.hashtagInterests.add(hashtagInterest);
	}

	@Override
	public int compareTo(UserInterests o) {
		return user.compareTo(o.getUser());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		UserInterests ui = (UserInterests) obj;
		
		return this.getUser().getId() == ui.getUser().getId();
	}

	@Override
	public String toString() {
		return "UserInterests [user=" + user + ", hashtagInterests="
				+ hashtagInterests + "]";
	}
	
}
