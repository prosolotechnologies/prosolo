/**
 * 
 */
package org.prosolo.services.activityWall;

import java.util.Set;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.services.activityWall.impl.data.UserInterests;

/**
 * @author "Nikola Milikic"
 *
 */
public interface SocialActivityResolver {

	Set<UserInterests> getStatusWallTargetGroup(SocialActivity socialActivity, Session session);
	
	Set<UserInterests> getGoalWallTargetGroup(SocialActivity socialActivity, Session session);

}