package org.prosolo.web.activitywall.util;

import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
 

/**
@author Zoran Jeremic Jan 16, 2015
 *
 */

public class ActivityWallUtils {
	private static Logger logger = Logger.getLogger(ActivityWallUtils.class);
	
	public static int getIndexOfSocialActivity(List<SocialActivityData> allActivities, long lastActivityToDisplayId) {
		int index = 0;
		
		if (allActivities != null) {
			for (SocialActivityData goalWallData : allActivities) {
				if (goalWallData.getSocialActivity().getId() == lastActivityToDisplayId) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
	public static VisibilityType getResourceVisibility(BaseEntity resource) {
		VisibilityType visibility = null;
		
		if (resource instanceof TargetActivity) {
			visibility = getTargetActivityVisibility((TargetActivity) resource);
		} else if (resource instanceof TargetCompetence) {
			visibility = getTargetCompetenceVisibility((TargetCompetence) resource);
		} else if (resource instanceof TargetLearningGoal) {
			visibility = getTargetGoalVisibility((TargetLearningGoal) resource);
		}
		
		if (visibility == null) {
			visibility = ((Node) resource).getVisibility();
		}
		
		if (visibility == null) {
			visibility = VisibilityType.PUBLIC;
			logger.error("Instance of "+resource.getClass().getSimpleName()+" with id "+resource.getId()+" has null visibility");
		}
		return visibility;
	}
	
	private static VisibilityType getTargetActivityVisibility(TargetActivity targetActivity) {
		return getTargetCompetenceVisibility(targetActivity.getParentCompetence());
	}
	
	private static VisibilityType getTargetCompetenceVisibility(TargetCompetence targetCompetence) {
		return getTargetGoalVisibility(targetCompetence.getParentGoal());
	}
	
	private static VisibilityType getTargetGoalVisibility(TargetLearningGoal targetGoal) {
		return targetGoal.getVisibility();
	}

}

