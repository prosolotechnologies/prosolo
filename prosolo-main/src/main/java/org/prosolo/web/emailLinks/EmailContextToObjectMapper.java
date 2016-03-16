package org.prosolo.web.emailLinks;

import org.prosolo.web.emailLinks.data.ObjectData;

public enum EmailContextToObjectMapper {

	PERSONAL_FEEDS(new ObjectData("FeedEntry", "link")), 
	FRIENDS_FEEDS(new ObjectData("FeedEntry", "link")), 
	COURSE_FEEDS(new ObjectData("FeedEntry", "link")), 
	PERSONAL_TWEETS(new ObjectData("TwitterPostSocialActivity", "postUrl")), 
	COURSE_TWEETS(new ObjectData("TwitterPostSocialActivity", "postUrl"));
	
    private ObjectData objectData; 
    
    EmailContextToObjectMapper(ObjectData objectType) {
        this.objectData = objectType;
    }

	public ObjectData getObjectData() {
		return objectData;
	}
}
