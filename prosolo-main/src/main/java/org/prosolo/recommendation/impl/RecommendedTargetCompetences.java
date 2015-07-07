package org.prosolo.recommendation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.competences.TargetCompetence;

/**
 * zoran
 */

public class RecommendedTargetCompetences implements Serializable {

	private static final long serialVersionUID = 5801421769461908832L;

	private List<TargetCompetence> targetCompetences;
	private Map<Long, List<TargetCompetence>> appendedTargetCompetences;

	public RecommendedTargetCompetences() {
		targetCompetences = new ArrayList<TargetCompetence>();
		appendedTargetCompetences = new HashMap<Long, List<TargetCompetence>>();
	}

	public List<TargetCompetence> getTargetCompetences() {
		return targetCompetences;
	}

	public void setTargetCompetences(List<TargetCompetence> TargetCompetences) {
		this.targetCompetences = TargetCompetences;
	}

	public void addTargetCompetence(TargetCompetence targetCompetence) {
		if (!targetCompetences.contains(targetCompetence)) {
			targetCompetences.add(targetCompetence);
		}
	}

	public Map<Long, List<TargetCompetence>> getAppendedTargetCompetence() {
		return appendedTargetCompetences;
	}

	public void setAppendedPlans(Map<Long, List<TargetCompetence>> appendedTargetCompetences) {
		this.appendedTargetCompetences = appendedTargetCompetences;
	}

	public void addTargetCompetence(TargetCompetence rootTargetCompetence, TargetCompetence childTargetCompetence) {
		List<TargetCompetence> targetCompetences = null;
		
		if (this.appendedTargetCompetences.containsKey(rootTargetCompetence.getId())) {
			targetCompetences = this.appendedTargetCompetences.get(rootTargetCompetence.getId());

		} else {
			targetCompetences = new ArrayList<TargetCompetence>();
		}
		if (!targetCompetences.contains(childTargetCompetence)) {
			targetCompetences.add(childTargetCompetence);

			this.appendedTargetCompetences.remove(rootTargetCompetence.getId());

			this.appendedTargetCompetences.put(rootTargetCompetence.getId(), targetCompetences);
		}
	}

	public void addAppendedTargetCompetences(TargetCompetence parentTargetCompetence, List<TargetCompetence> tComps) {
		for (TargetCompetence targetCompetence : tComps) {
			addTargetCompetence(parentTargetCompetence, targetCompetence);
		}
	}

}
