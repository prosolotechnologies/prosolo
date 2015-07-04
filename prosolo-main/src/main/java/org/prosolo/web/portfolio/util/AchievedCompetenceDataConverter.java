/**
 * 
 */
package org.prosolo.web.portfolio.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.util.date.DateUtil;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.portfolio.data.AchievedCompetenceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.web.portfolio.util.AchievedCompetenceDataConverter")
public class AchievedCompetenceDataConverter {
	
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private BadgeManager badgeManager;
	
	public static List<AchievedCompetenceData> convertCompetences(List<CompetenceDataCache> competences) {
		List<AchievedCompetenceData> compData = new ArrayList<AchievedCompetenceData>();
		
		if (competences != null && !competences.isEmpty()) {
			for (CompetenceDataCache c : competences) {
				compData.add(convertCompetence(c));
			}
		}
		
		return compData;
	}

	public static AchievedCompetenceData convertCompetence(CompetenceDataCache competenceData) {
		AchievedCompetenceData achievedCompData = new AchievedCompetenceData();
		achievedCompData.setTitle(competenceData.getData().getTitle());
		achievedCompData.setVisibility(competenceData.getData().getVisibility());
		
		achievedCompData.setCompetenceId(competenceData.getData().getCompetenceId());
		achievedCompData.setTargetCompetenceId(competenceData.getData().getId());
		
		achievedCompData.setEvaluationCount(competenceData.getEvaluationCount());
		achievedCompData.setBadgeCount(competenceData.getBadgeCount());
		
		return achievedCompData;
	}

	public List<AchievedCompetenceData> convertAchievedComps(List<AchievedCompetence> achievedCompetences) {
		List<AchievedCompetenceData> compData = new ArrayList<AchievedCompetenceData>();
		
		if (achievedCompetences != null && !achievedCompetences.isEmpty()) {
			Collections.sort(achievedCompetences, new AchievedCompCompletedDateComparator());
			
			for (AchievedCompetence c : achievedCompetences) {
				compData.add(convertAchievedCompetence(c));
			}
		}
		
		return compData;
	}

	public AchievedCompetenceData convertAchievedCompetence(AchievedCompetence achievedComp) {
		AchievedCompetenceData achievedCompData = new AchievedCompetenceData();
		achievedCompData.setId(achievedComp.getId());
		achievedCompData.setTitle(achievedComp.getCompetence().getTitle());
		achievedCompData.setDateCompleted(DateUtil.getPrettyDate(achievedComp.getCompletedDate()));
		achievedCompData.setVisibility(achievedComp.getVisibility());
		achievedCompData.setCompetence(achievedComp);
		achievedCompData.setCompetenceId(achievedComp.getCompetence().getId());
		
		achievedCompData.setEvaluationCount(evaluationManager.getApprovedEvaluationCountForResource(AchievedCompetence.class, achievedComp.getId()));
		achievedCompData.setRejectedEvaluationCount(evaluationManager.getRejectedEvaluationCountForResource(AchievedCompetence.class, achievedComp.getId()));
		achievedCompData.setBadgeCount(badgeManager.getBadgeCountForResource(AchievedCompetence.class, achievedComp.getId()));
		
		if (achievedComp.getTargetCompetence() != null) {
			achievedCompData.setTargetCompetenceId(achievedComp.getTargetCompetence().getId());
		}
		
		return achievedCompData;
	}
	

	public List<AchievedCompetenceData> convertTargetCompetences(List<TargetCompetence> competences) {
		List<AchievedCompetenceData> compData = new ArrayList<AchievedCompetenceData>();
		
		if (competences != null && !competences.isEmpty()) {
			for (TargetCompetence tc : competences) {
				compData.add(convertTargetCompetence(tc));
			}
		}
		
		return compData;
	}

	public AchievedCompetenceData convertTargetCompetence(TargetCompetence targetComp) {
		AchievedCompetenceData achievedCompData = new AchievedCompetenceData();
		achievedCompData.setTitle(targetComp.getTitle());
		achievedCompData.setVisibility(targetComp.getVisibility());
		
		achievedCompData.setCompetenceId(targetComp.getCompetence().getId());
		achievedCompData.setTargetCompetenceId(targetComp.getId());
		
		achievedCompData.setEvaluationCount(evaluationManager.getApprovedEvaluationCountForResource(TargetCompetence.class, targetComp.getId()));
		achievedCompData.setRejectedEvaluationCount(evaluationManager.getRejectedEvaluationCountForResource(TargetCompetence.class, targetComp.getId()));
		achievedCompData.setBadgeCount(badgeManager.getBadgeCountForResource(TargetCompetence.class, targetComp.getId()));
		
		return achievedCompData;
	}

}

