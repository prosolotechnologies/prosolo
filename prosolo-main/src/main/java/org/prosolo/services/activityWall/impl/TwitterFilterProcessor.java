package org.prosolo.services.activityWall.impl;

import java.util.Set;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.interfacesettings.FilterType;
import org.prosolo.domainmodel.user.User;
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
		System.out.println("CHECKING TWITTER");
		if (!(socialActivity instanceof TwitterPostSocialActivity)) {
			return false;
		}
		
		TwitterPostSocialActivity twitterPostSA = (TwitterPostSocialActivity) socialActivity;
		Set<Tag> hashtags = twitterPostSA.getHashtags();
		Set<Tag> filterHashtags = (Set<Tag>) ((TwitterFilter) filter).getHashtags();
		System.out.println("Hashtags:"+hashtags.toString());
		System.out.println("FILTER HASHTAGS:"+filterHashtags.toString());
		for (Tag tag : hashtags) {
			if (filterHashtags.contains(tag)) {
				return true;
			}
		}
		
		return false;
	}
	
}
