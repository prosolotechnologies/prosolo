package org.prosolo.services.activityWall.impl;

import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.interfacesettings.FilterType;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
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
			System.out.println("VISIBILITY PRIVATE...");
			return false;
		}		
		CourseFilter courseFilter = (CourseFilter) filter;
		if(socialActivity.getHashtags().size()>0){
			for(Tag tag:socialActivity.getHashtags()){
				if(courseFilter.contains(tag.getTitle())){  
					return true;
				}
			}
		}
		if(socialActivity.getTarget()!=null){
			System.out.println("target:"+socialActivity.getTarget().getClass().getName()+" ID:"+socialActivity.getTarget().getId());
			if(checkObject(socialActivity.getTarget(),courseFilter)){
				return true;
			}
		}
		
		if(socialActivity.getObject()!=null){
			System.out.println("object:"+socialActivity.getObject().getClass().getName());
			if(checkObject(socialActivity.getObject(),courseFilter)){
				return true;
			}
		}
		
		return false;
	}
	private boolean checkObject(BaseEntity object, CourseFilter courseFilter){
		long objectId=object.getId();
		object=HibernateUtil.initializeAndUnproxy(object);
		System.out.println("CHECKING OBJECT:"+object.getClass().getSimpleName()+" ID:"+objectId);
		if(object instanceof TargetLearningGoal){
			if(courseFilter.containsTargetLearningGoal(objectId)){
				return true;
			}
		}else if(object instanceof LearningGoal){
			if(courseFilter.containsLearningGoal(objectId)){				
				return true;
			}
		}else if(object instanceof TargetCompetence){
			System.out.println("TARGET COMPETENCES:"+courseFilter.getTargetCompetences().size());
			if(courseFilter.containsTargetCompetence(objectId)){
				return true;
			}
		}else if(object instanceof TargetActivity){
			System.out.println("TARGET ACTIVITIES:"+courseFilter.getTargetActivities().size());
			if(courseFilter.containsTargetActivity(objectId)){
				return true;
			}
		}
		return false;
	}
	
}
