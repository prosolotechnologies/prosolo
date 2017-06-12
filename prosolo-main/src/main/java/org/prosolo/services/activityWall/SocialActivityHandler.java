package org.prosolo.services.activityWall;

import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;

/**
@author Zoran Jeremic Jan 16, 2015
 *
 */

public interface SocialActivityHandler {

	SocialActivity1 addSociaActivitySyncAndPropagateToStatusAndGoalWall(Event event) throws EventException;

	void propagateSocialActivity(Event event) throws EventException;

}

