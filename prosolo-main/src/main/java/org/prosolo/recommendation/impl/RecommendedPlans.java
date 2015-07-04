package org.prosolo.recommendation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prosolo.domainmodel.workflow.LearningPlan;

/**
 * zoran
 */

public class RecommendedPlans implements Serializable {

	private static final long serialVersionUID = 5801421769461908832L;

	private List<LearningPlan> learningPlans;
	private Map<Long, List<LearningPlan>> appendedPlans;

	public RecommendedPlans() {
		learningPlans = new ArrayList<LearningPlan>();
		appendedPlans = new HashMap<Long, List<LearningPlan>>();
	}

	public List<LearningPlan> getLearningPlans() {
		return learningPlans;
	}

	public void setLearningPlans(List<LearningPlan> learningPlans) {
		this.learningPlans = learningPlans;
	}

	public void addLearningPlan(LearningPlan lPlan) {
		if (!learningPlans.contains(lPlan)) {
			learningPlans.add(lPlan);
		}
	}

	public Map<Long, List<LearningPlan>> getAppendedPlans() {
		return appendedPlans;
	}

	public void setAppendedPlans(Map<Long, List<LearningPlan>> appendedPlans) {
		this.appendedPlans = appendedPlans;
	}

	public void addAppendedPlan(LearningPlan rootPlan, LearningPlan childPlan) {
		List<LearningPlan> plans = null;
		
		if (this.appendedPlans.containsKey(rootPlan.getId())) {
			plans = this.appendedPlans.get(rootPlan.getId());

		} else {
			plans = new ArrayList<LearningPlan>();
		}
		
		if (!plans.contains(childPlan)) {
			plans.add(childPlan);

			this.appendedPlans.remove(rootPlan.getId());

			this.appendedPlans.put(rootPlan.getId(), plans);
		}
	}

	public void addAppendedPlans(LearningPlan parentPlan, List<LearningPlan> plans) {
		for (LearningPlan plan : plans) {
			addAppendedPlan(parentPlan, plan);
		}
	}

}
