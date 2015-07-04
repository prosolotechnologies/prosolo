package org.prosolo.common.domainmodel.user.preferences;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;

/**
 * This class represents the user's preferences for certain domain topics (i.e.,
 * topics of a specific subject domain); the preferred topics are represented as
 * concepts of an appropriate domain ontology or as keywords (i.e., user defined
 * tags)
 * 
 */

@Entity
public class TopicPreference extends UserPreference {
	
	private static final long serialVersionUID = 5665056041154266750L;
	
	/**
	 * keywords/tags that represents topic preferences of the given user
	 */
	private Set<Tag> preferredKeywords;
	private Set<Tag> preferredHashtags;
	
	public TopicPreference() {
		preferredKeywords = new HashSet<Tag>();
		preferredHashtags = new HashSet<Tag>();
	}
	
	@ManyToMany(targetEntity = Tag.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "user_topic_preference_preferred_hashtags_tag")
	public Set<Tag> getPreferredHashtags() {
		return preferredHashtags;
	}
	
	public void setPreferredHashtags(Set<Tag> preferredHashtags) {
		this.preferredHashtags = preferredHashtags;
	}
	
	@ManyToMany(targetEntity = Tag.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "user_topic_preference_preferred_keywords_tag")
	public Set<Tag> getPreferredKeywords() {
		return preferredKeywords;
	}
	
	public void setPreferredKeywords(Set<Tag> preferredKeywords) {
		this.preferredKeywords = preferredKeywords;
	}
	
	public void addPreferredKeyword(Tag keyword) {
		if (keyword != null && !getPreferredKeywords().contains(keyword))
			getPreferredKeywords().add(keyword);
		else
			System.err.println("Did not succeed in adding new keyword (tag) to the \"preferred keywords\" collection");
	}
	
}
