package org.prosolo.services.twitter;

import org.prosolo.domainmodel.user.User;

import twitter4j.Twitter;

public interface TwitterSearchService {

	Twitter initializeTwitter(User user);

}