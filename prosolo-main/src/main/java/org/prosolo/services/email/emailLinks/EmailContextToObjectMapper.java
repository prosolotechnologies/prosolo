package org.prosolo.services.email.emailLinks;

import org.prosolo.services.email.emailLinks.data.LinkObjectGeneralInfo;

public enum EmailContextToObjectMapper {

	PERSONAL_FEEDS(new LinkObjectGeneralInfo("FeedEntry", "link")), 
	FRIENDS_FEEDS(new LinkObjectGeneralInfo("FeedEntry", "link")), 
	COURSE_FEEDS(new LinkObjectGeneralInfo("FeedEntry", "link")), 
	PERSONAL_TWEETS(new LinkObjectGeneralInfo("TwitterPostSocialActivity", "postUrl")), 
	COURSE_TWEETS(new LinkObjectGeneralInfo("TwitterPostSocialActivity", "postUrl"));
	
    private LinkObjectGeneralInfo objectInfo; 
    
    EmailContextToObjectMapper(LinkObjectGeneralInfo objectType) {
        this.objectInfo = objectType;
    }

	public LinkObjectGeneralInfo getObjectInfo() {
		return objectInfo;
	}
}
