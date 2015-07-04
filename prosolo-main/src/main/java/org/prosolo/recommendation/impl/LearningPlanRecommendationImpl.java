package org.prosolo.recommendation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.CompetenceActivity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.LearningPlan;
import org.prosolo.recommendation.LearningPlanRecommendation;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.recommendation.LearningPlanRecommendation")
public class LearningPlanRecommendationImpl implements LearningPlanRecommendation {
	
	private static Logger logger = Logger.getLogger(LearningPlanRecommendationImpl.class);

	@Autowired SuggestedLearningQueries suggLearningQueries;
	@Autowired DefaultManager defaultManager;

	private Map<Long, RecommendedPlans> plans = new HashMap<Long, RecommendedPlans>();

	private void updateTargetCompetenceStoredPlan(long targetCompId) {
		plans.remove(targetCompId);
		
		RecommendedPlans recPlans = readLearningPlans( targetCompId);
		plans.put(targetCompId, recPlans);
	}

	private LearningPlan getRootLearningPlan(LearningPlan lPlan) {
		//lPlan = defaultManager.merge(lPlan);
		
		if (lPlan.getBasedOn() == null) {
			return lPlan;
		} else {
			getRootLearningPlan(lPlan.getBasedOn());
		}
		return lPlan;
	}

	@Override
	public boolean hasAppendedPlans(LearningPlan lPlan, TargetCompetence selectedCompetence) {
		if (plans.containsKey(selectedCompetence.getId())) {
			RecommendedPlans recPlans = plans.get(selectedCompetence.getId());
			long rootLPId = getRootLearningPlan(lPlan).getId();
			
			if (recPlans.getAppendedPlans().containsKey(rootLPId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<LearningPlan> recommendLearningPlans(User user, long targetCompId, int limit) {
		List<LearningPlan> recommendedPlans = new ArrayList<LearningPlan>();
		if (plans.containsKey(targetCompId)) {
			updateTargetCompetenceStoredPlan(targetCompId);
		} else {
			 
			RecommendedPlans recPlans =readLearningPlans(targetCompId);
			plans.put(targetCompId, recPlans);
		}
		recommendedPlans = plans.get(targetCompId).getLearningPlans();
		if (recommendedPlans.size() < limit) {
			return recommendedPlans;
		} else {
			return recommendedPlans.subList(0, limit);
		}
	}

	@Override
	public List<LearningPlan> recommendLearningPlansForCompetence(User user, Competence selectedComp, int limit) {
 
		List<LearningPlan> recommendedPlans = new ArrayList<LearningPlan>();
		RecommendedPlans recPlans = new RecommendedPlans();
		readLearningPlansForCompetence(recPlans, selectedComp);
		plans.put(selectedComp.getId(), recPlans);
		recommendedPlans = plans.get(selectedComp.getId()).getLearningPlans();
		if (recommendedPlans.size() < limit) {
			return recommendedPlans;
		} else {
			return recommendedPlans.subList(0, limit);
		}
	}
	

	private void checkLearningPlanActivities(  LearningPlan lPlan, List<TargetActivity> targetActivities){
		Iterator<Activity> lPlanActivitiesIter = lPlan.getActivities().iterator();
		
		while (lPlanActivitiesIter.hasNext()) {
			Activity tAct = lPlanActivitiesIter.next();
			
			if (targetActivities != null) {
				for (TargetActivity ta : targetActivities) {
					
					if (ta.getActivity().equals(tAct)) {
						lPlanActivitiesIter.remove();
						break;
					}
				}
			}
		}
	}

	private RecommendedPlans  readLearningPlans( long targetCompId) {
		RecommendedPlans recPlans = new RecommendedPlans();
		try {
			TargetCompetence tCompetence = defaultManager.loadResource(TargetCompetence.class, targetCompId);
			
			Competence competence = tCompetence.getCompetence();
			List<LearningPlan> lPlans = suggLearningQueries.findAllLearningPlansForCompetence(competence);
			Iterator<LearningPlan> lPlansIterator=lPlans.iterator();
			List<LearningPlan> lPlansToAdd = new ArrayList<LearningPlan>();
			
			while (lPlansIterator.hasNext()) {
				LearningPlan lPlan=lPlansIterator.next();
				
				if(!lPlan.getActivities().isEmpty() 
						&& !checkIfLearningPlanIsDuplicate(lPlansToAdd, lPlan)){
					checkLearningPlanActivities( lPlan,tCompetence.getTargetActivities());
					if(lPlan.getActivities().size()>0){
						lPlansToAdd.add(lPlan);
					}
				}
				recPlans.setLearningPlans(lPlansToAdd);
			}
 
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return recPlans;
	}
	private boolean checkIfLearningPlanIsDuplicate(List<LearningPlan> lPlans,LearningPlan lPlan){
		boolean isDuplicate=false;
		for(LearningPlan checkLearningPlan:lPlans){
			if(checkLearningPlan.equals(lPlan)){
				break;
			}
			List<Activity> chActivities=checkLearningPlan.getActivities();
			if(chActivities.size()!=lPlan.getActivities().size()){
				break;
			}
			int counter=0;
			for(Activity act:chActivities){
				if(!lPlan.getActivities().contains(act)){
					break;
				}else{
					counter++;
				}
			}
			if(chActivities.size()==counter){
				isDuplicate=true;
				return isDuplicate;
			}
		}
		return isDuplicate;
	}
	private void readLearningPlansForCompetence(RecommendedPlans recPlans, Competence competence) {
		List<LearningPlan> lPlans=new ArrayList<LearningPlan>();
		 if(competence.getActivities().size()>0){
 				List<Activity> activities = new ArrayList<Activity>();
			for(CompetenceActivity compActivity:competence.getActivities()){
					activities.add(compActivity.getActivity());
			}
			LearningPlan lPlan = new LearningPlan();
			lPlan.setActivities(activities);
			lPlans.add(lPlan); 
			List<Activity> appendedActivities=suggLearningQueries.findAllAppendedActivitiesForCompetence(competence,lPlan.getActivities());
			Iterator<Activity> appendedActivitiesIt=appendedActivities.iterator();
			while(appendedActivitiesIt.hasNext()){
				Activity appActivity=appendedActivitiesIt.next();
				lPlan.addAppendedActivity(appActivity);
			}
			
		 }else{
			lPlans = suggLearningQueries.findAllLearningPlansForCompetence(competence);
			Iterator<LearningPlan> lPlansIterator = lPlans.iterator();
			
			while (lPlansIterator.hasNext()) {
				LearningPlan lPlan = lPlansIterator.next();
				if (lPlan.getActivities().isEmpty()) {
					lPlansIterator.remove();
				} else if (checkIfLearningPlanIsDuplicate(lPlans, lPlan)) {
					lPlansIterator.remove();
				}
			}
		 }
 
		recPlans.setLearningPlans(lPlans);
	}

	@Override
	public List<LearningPlan> getAppendedPlans(LearningPlan selectedPlan, TargetCompetence selectedComp) {
		if (plans.containsKey(selectedComp.getId())) {
			RecommendedPlans recPlans = plans.get(selectedComp.getId());
			Map<Long, List<LearningPlan>> appendedPlans = recPlans.getAppendedPlans();
			List<LearningPlan> lPlans = appendedPlans.get(selectedPlan.getId());

			return lPlans;
		} else
			return null;
	}

	@Override
	public List<LearningPlan> getAppendedPlansForCompetence(LearningPlan selectedPlan, Competence selectedComp) {
		if (plans.containsKey(selectedComp.getId())) {
			RecommendedPlans recPlans = plans.get(selectedComp.getId());
			
			Map<Long, List<LearningPlan>> appendedPlans = recPlans.getAppendedPlans();
			List<LearningPlan> lPlans = appendedPlans.get(selectedPlan.getId());
			return lPlans;
		} else
			return null;
	}

//	@Override
//	public boolean hasAppendedPlansForCompetence(LearningPlan plan, long competenceId) {
//		if (plans.containsKey(competenceId)) {
//			RecommendedPlans recPlans = plans.get(competenceId);
//			long rootLP = getRootLearningPlan(plan).getId();
//			
//			if (recPlans.getAppendedPlans().containsKey(rootLP)) {
//				return true;
//			}
//		}
//		return false;
//	}
	@Override
	public boolean hasAppendedPlansForCompetence(LearningPlan plan, long competenceId) {
		if(!plan.getAppendedActivities().isEmpty()){
			return true;
		}
		if (plans.containsKey(competenceId)) {
			RecommendedPlans recPlans = plans.get(competenceId);
			long rootLP = getRootLearningPlan(plan).getId();
			
			if (recPlans.getAppendedPlans().containsKey(rootLP)) {
				return true;
			}
		}
		return false;
	}
}
