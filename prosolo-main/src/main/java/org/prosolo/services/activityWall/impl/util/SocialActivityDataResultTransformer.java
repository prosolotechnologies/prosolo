package org.prosolo.services.activityWall.impl.util;

import java.math.BigInteger;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class SocialActivityDataResultTransformer {
	
	private static Logger logger = Logger.getLogger(SocialActivityDataResultTransformer.class);
	
	public static AliasToBeanConstructorResultTransformer getConstructor() {
		try {
			return new AliasToBeanConstructorResultTransformer(
					SocialActivityData.class.getConstructor(
						BigInteger.class, 	// socialActivityId
						String.class,		// socialActivityDType
						Date.class,			// dateCreated
						Date.class,			// lastAction
						Character.class,	// updated
						BigInteger.class,	// actorId
						String.class,		// actorName
						String.class,		// actorLastname
						String.class,		// actorAvatarUrl
						String.class,		// actorPosition
						String.class,		// actorProfileUrl
						BigInteger.class,	// postObjectId
						String.class,		// postObjectDType
						BigInteger.class,	// nodeObjectId
						String.class,		// nodeObjectDType
						String.class,		// nodeObjectTitle
						String.class,		// nodeObjectDescription
						BigInteger.class,	// courseEnrollmentObjectId
						BigInteger.class,	// courseId
						String.class,		// courseTitle
						String.class,		// courseDescription
						BigInteger.class,	// userObjectId
						String.class,		// actionName
						BigInteger.class,	// nodeTargetId
						String.class,		// nodeTargetDType
						String.class,		// nodeTargetTitle
						BigInteger.class,	// goalTargetId
						String.class,		// goalTargetTitle
						BigInteger.class,	// userTargetId
						BigInteger.class,	// socialActivityConfigId
						Character.class,	// commentsDisabled
						String.class,		// text
						Integer.class,		// likeCount
						Integer.class,		// dislikeCount
						Integer.class,		// shareCount
						String.class,		// visibility
						String.class,		// twitterPosterName
						String.class,		// twitterPosterNickname
						String.class,		// twitterPosterProfileUrl
						String.class,		// twitterPosterAvatarUrl
						String.class,		// twitterPostUrl
						Integer.class,		// twitterUserType
						String.class,		// postRichContentTitle
						String.class,		// postRichContentDescription
						String.class,		// postRichContentContentType
						String.class,		// postRichContentImageUrl
						String.class,		// postRichContentLink
						Integer.class,		// liked
						Integer.class		// disliked
					)
				);
		} catch (NoSuchMethodException e) {
			logger.error(e);
		} catch (SecurityException e) {
			logger.error(e);
		}
		return null;
	}
	
}
