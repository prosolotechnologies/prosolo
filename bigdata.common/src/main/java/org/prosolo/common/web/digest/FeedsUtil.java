package org.prosolo.common.web.digest;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.feeds.CourseRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.CredentialTwitterHashtagsFeedsDigest;
import org.prosolo.common.domainmodel.feeds.FriendsRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.SubscribedRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.SubscribedTwitterHashtagsFeedsDigest;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class FeedsUtil {
	
	public static List<String> convertToDigestClassNames(List<FilterOption> filters) {
		List<String> digestTypes = new ArrayList<String>();
		
		for (FilterOption filter : filters) {
			digestTypes.add(convertToDigestClassName(filter));
		}
		return digestTypes;
	}

	public static String convertToDigestClassName(FilterOption filter) {
		switch (filter) {
			case myfeeds:
				return SubscribedRSSFeedsDigest.class.getSimpleName();
			case friendsfeeds:
				return FriendsRSSFeedsDigest.class.getSimpleName();
			case coursefeeds:
				return CourseRSSFeedsDigest.class.getSimpleName();
			case mytweets:
				return SubscribedTwitterHashtagsFeedsDigest.class.getSimpleName();
			case coursetweets:
				return CredentialTwitterHashtagsFeedsDigest.class.getSimpleName();
		}
		return SubscribedRSSFeedsDigest.class.getSimpleName();
	}

	public static FilterOption convertToFilterOption(String className) {
		if (className.equals(SubscribedRSSFeedsDigest.class.getSimpleName())) {
			return FilterOption.myfeeds;
		} else if (className.equals(FriendsRSSFeedsDigest.class.getSimpleName())) {
			return FilterOption.friendsfeeds;
		} else if (className.equals(CourseRSSFeedsDigest.class.getSimpleName())) {
			return FilterOption.coursefeeds;
		} else if (className.equals(SubscribedTwitterHashtagsFeedsDigest.class.getSimpleName())) {
			return FilterOption.mytweets;
		} else if (className.equals(CredentialTwitterHashtagsFeedsDigest.class.getSimpleName())) {
			return FilterOption.coursetweets;
		}
		return FilterOption.myfeeds;
	}
}
