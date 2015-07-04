package org.prosolo.services.activityWall.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.SocialStreamSubView;
import org.prosolo.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.ActivityWallFactory;
import org.prosolo.services.activityWall.impl.data.HashtagInterest;
import org.prosolo.services.nodes.LearningGoalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.activitystream.impl.ActivityWallFactory")
public class ActivityWallFactoryImpl implements ActivityWallFactory, Serializable {
	
	private static final long serialVersionUID = 5127669660466023337L;
	
	@Autowired private LearningGoalManager goalManager;
	
	@Override
	@Transactional(readOnly = false)
	public Collection<SocialStreamSubView> createGoalWallSubViews(User user, SocialActivity socialActivity, List<HashtagInterest> goalHashtags, Session session) {
		List<SocialStreamSubView> subViews = new ArrayList<SocialStreamSubView>();
		if (goalHashtags != null) {
			for (HashtagInterest goalHashtag : goalHashtags) {
				SocialStreamSubView createGoalWallSubView = createGoalWallSubView(user, socialActivity, goalHashtag, session);
				subViews.add(createGoalWallSubView);
			}
		} else {
			SocialStreamSubView createGoalWallSubView = createGoalWallSubView(user, socialActivity, null, session);
			subViews.add(createGoalWallSubView);
		}
		return subViews;
	}

	@Override
	@Transactional(readOnly = false)
	public SocialStreamSubView createGoalWallSubView(User user, SocialActivity socialActivity, HashtagInterest goalHashtags, Session session) {
		user = (User) session.load(User.class, user.getId());
		BaseEntity object = socialActivity.getObject();
		BaseEntity target =  socialActivity.getTarget();
		
		Collection<TargetLearningGoal> userGoals = goalManager.getUserTargetGoals(user, session);
		
		if (userGoals != null && !userGoals.isEmpty()) {
			Set<Node> relatedResources = getRelevantResources(goalHashtags,	object, userGoals);
			relatedResources.addAll(getRelevantResources(goalHashtags, target, userGoals));
			
			return createSubView(SocialStreamSubViewType.GOAL_WALL, 
								relatedResources, 
								goalHashtags != null ? goalHashtags.getHashtags() : null,
								session);
		}
		return null;
	}

	private Set<Node> getRelevantResources(HashtagInterest goalHashtags,
			BaseEntity object,
			Collection<TargetLearningGoal> userGoals) {
		
		if (object instanceof LearningGoal || object instanceof TargetLearningGoal) {
			return getGoalRelevantForResource(object, userGoals);
		} else if (object instanceof TargetCompetence) {
			return getGoalRelevantForResource(((TargetCompetence) object).getParentGoal(), userGoals);
		} else if (object instanceof TargetActivity) {
			return getGoalRelevantForResource(((TargetActivity) object).getParentCompetence().getParentGoal(), userGoals);
		}
		
		return new HashSet<Node>();
	}

	private Set<Node> getGoalRelevantForResource(BaseEntity object,
			Collection<TargetLearningGoal> userGoals) {
		Set<Node> relatedResources = new HashSet<Node>();
		
		goalLoop: for (TargetLearningGoal targetGoal : userGoals) {
			if ((object != null && object instanceof TargetLearningGoal && 
					((TargetLearningGoal) object).getLearningGoal().getId() == targetGoal.getLearningGoal().getId()) 
				|| 
				(object != null && object instanceof LearningGoal && 
					((LearningGoal) object).getId() == targetGoal.getLearningGoal().getId())) {
				
				relatedResources.add(targetGoal);
				break goalLoop;
			}
		}
		return relatedResources;
	}
	
	@Override
	@Transactional(readOnly = false)
	public SocialStreamSubView createStatusWallSubView(Collection<Tag> hashtags, Session session) {
		return createSubView(SocialStreamSubViewType.STATUS_WALL, new HashSet<Node>(), hashtags, session);
	}

	@Override
	@Transactional(readOnly = false)
	public SocialStreamSubView createSubView(SocialStreamSubViewType type, Set<Node> relatedResources, 
			Collection<Tag> hashtags, Session session) {
		
		SocialStreamSubView subView = new SocialStreamSubView();
		subView.setType(type);
		
		if (hashtags != null) {
			subView.getHashtags().addAll(hashtags);
		}
		
		if (relatedResources != null) {
			subView.setRelatedResources(relatedResources);
		} else {
			subView.setRelatedResources(new HashSet<Node>());
		}
		session.save(subView);
		
		return subView;
	}

}
