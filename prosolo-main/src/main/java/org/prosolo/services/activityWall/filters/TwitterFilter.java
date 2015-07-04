package org.prosolo.services.activityWall.filters;

import java.util.Set;
import java.util.TreeSet;

import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.interfacesettings.FilterType;

/**
 * @author Zoran Jeremic Jan 25, 2015
 *
 */

public class TwitterFilter extends Filter {
	private Set<Tag> hashtags;// = tagManager.getUserHashtags(user)
	
	public TwitterFilter() {
		setHashtags(new TreeSet<Tag>());
		this.setFilterType(FilterType.TWITTER);
	}
	
	public Set<Tag> getHashtags() {
		return hashtags;
	}
	
	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}
	
	public void addHashtag(Tag hashtag) {
		this.hashtags.add(hashtag);
	}
	public void removeHashtag(Tag hashtag) {
		if(this.hashtags.contains(hashtag)){
			this.hashtags.remove(hashtag);
		}
		
	}
}
