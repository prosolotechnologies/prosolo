package org.prosolo.web.activitywall.displayers;

import java.util.List;

import org.prosolo.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.services.activityWall.filters.LearningGoalFilter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service
@Scope("prototype")
public class GoalWallSocialActivitiesDisplayer extends DefaultWallSocialActivitiesDisplayer {
	
	@Override
	protected List<SocialActivityData> fetchActivities(int offset, int limit) {
		return activityWallManager.getLearningGoalSocialActivities(
				loggedUser.getId(), 
				offset, 
				limit,
				((LearningGoalFilter) filter).getGoalId(),
				((LearningGoalFilter) filter).getSelectedParticipant());
	}
	
	@Override
	protected SocialStreamSubViewType getSubViewType() {
		return SocialStreamSubViewType.GOAL_WALL;
	}

}
