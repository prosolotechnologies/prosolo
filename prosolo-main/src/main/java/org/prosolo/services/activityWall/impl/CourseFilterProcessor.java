package org.prosolo.services.activityWall.impl;

import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.CourseFilter;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
 * @author Zoran Jeremic Feb 4, 2015
 *
 */
@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.COURSE })
public class CourseFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity socialActivity, User user, Filter filter) {
		VisibilityType visibility = socialActivity.getVisibility();
		
		if (visibility.equals(VisibilityType.PRIVATE) && socialActivity.getMaker().getId() != user.getId()) {
			return false;
		}
		CourseFilter courseFilter = (CourseFilter) filter;
		if (socialActivity.getHashtags().size() > 0) {
			for (Tag tag : socialActivity.getHashtags()) {
				if (courseFilter.contains(tag.getTitle())) {
					return true;
				}
			}
		}
		if (socialActivity.getTarget() != null) {
			if (checkObject(socialActivity.getTarget(), courseFilter)) {
				return true;
			}
		}
		
		if (socialActivity.getObject() != null) {
			if (checkObject(socialActivity.getObject(), courseFilter)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean checkObject(BaseEntity object, CourseFilter courseFilter) {
		long objectId = object.getId();
		object = HibernateUtil.initializeAndUnproxy(object);
		
		if (object instanceof TargetLearningGoal) {
			if (courseFilter.containsTargetLearningGoal(objectId)) {
				return true;
			}
		} else if (object instanceof LearningGoal) {
			if (courseFilter.containsLearningGoal(objectId)) {
				return true;
			}
		} else if (object instanceof TargetCompetence) {
			if (courseFilter.containsTargetCompetence(objectId)) {
				return true;
			}
		} else if (object instanceof TargetActivity) {
			if (courseFilter.containsTargetActivity(objectId)) {
				return true;
			}
		}
		return false;
	}
	
}
