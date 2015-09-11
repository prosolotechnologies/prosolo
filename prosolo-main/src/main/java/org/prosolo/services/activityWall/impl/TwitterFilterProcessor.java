package org.prosolo.services.activityWall.impl;

import java.util.Set;

import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.TwitterFilter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
 * @author Zoran Jeremic Jan 27, 2015
 *		
 */
@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.TWITTER })
public class TwitterFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity socialActivity, User user, Filter filter) {
		if (!(socialActivity instanceof TwitterPostSocialActivity)) {
			return false;
		}
		
		TwitterPostSocialActivity twitterPostSA = (TwitterPostSocialActivity) socialActivity;
		Set<Tag> hashtags = twitterPostSA.getHashtags();
		Set<Tag> filterHashtags = (Set<Tag>) ((TwitterFilter) filter).getHashtags();
		
		for (Tag tag : hashtags) {
			if (filterHashtags.contains(tag)) {
				return true;
			}
		}
		
		return false;
	}
	
}
