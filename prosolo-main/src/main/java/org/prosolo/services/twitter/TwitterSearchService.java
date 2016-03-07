package org.prosolo.services.twitter;

import org.prosolo.common.domainmodel.user.User;

import twitter4j.Twitter;
@Deprecated
public interface TwitterSearchService {

	Twitter initializeTwitter(User user);

}