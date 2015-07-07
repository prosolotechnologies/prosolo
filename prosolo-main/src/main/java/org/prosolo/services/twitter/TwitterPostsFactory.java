package org.prosolo.services.twitter;

import java.util.List;

import org.prosolo.common.domainmodel.user.User;

import twitter4j.Status;

/**
 *
 * @author Zoran Jeremic, Sep 1, 2014
 *
 */
public interface TwitterPostsFactory {

	void postStatusFromHashtagListener(Status tweet, List<String> twitterHashtags);

	void postStatusFromTwitter(User user, Status tweet);

}
