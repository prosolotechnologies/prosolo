/**
 * 
 */
package org.prosolo.web.portfolio.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.web.dialogs.data.ExternalCreditData;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.web.portfolio.util.ExternalCreditsDataConverter")
public class ExternalCreditsDataConverter {
	
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private BadgeManager badgeManager;

	public List<ExternalCreditData> convertExternalCredits(List<ExternalCredit> externalCredits, User loggedUser, Locale locale) {
		List<ExternalCreditData> compData = new ArrayList<ExternalCreditData>();
		
		if (externalCredits != null && !externalCredits.isEmpty()) {
			for (ExternalCredit c : externalCredits) {
				compData.add(convertExternalCredit(c, loggedUser, locale));
			}
		}
		
		return compData;
	}

	public ExternalCreditData convertExternalCredit(ExternalCredit externalCredit, User loggedUser, Locale locale) {
		ExternalCreditData externalCreditData = new ExternalCreditData();
		externalCreditData.setId(externalCredit.getId());
		externalCreditData.setTitle(externalCredit.getTitle());
		externalCreditData.setDescription(externalCredit.getDescription());
		externalCreditData.setStart(externalCredit.getStart());
		externalCreditData.setEnd(externalCredit.getEnd());
		externalCreditData.setCertificateLink(externalCredit.getCertificateLink());
		externalCreditData.setExternalCredit(externalCredit);
		externalCreditData.setVisibility(externalCredit.getVisibility());
		
		List<Competence> competences = new ArrayList<Competence>();
		
		for (AchievedCompetence achComp : externalCredit.getCompetences()) {
			competences.add(achComp.getCompetence());
		}
		
		externalCreditData.setEvaluationCount(evaluationManager.getApprovedEvaluationCountForResource(ExternalCredit.class, externalCredit.getId()));
		externalCreditData.setRejectedEvaluationCount(evaluationManager.getRejectedEvaluationCountForResource(ExternalCredit.class, externalCredit.getId()));
		externalCreditData.setBadgeCount(badgeManager.getBadgeCountForResource(ExternalCredit.class, externalCredit.getId()));
		
		externalCreditData.setCompetences(competences);
		externalCreditData.setActivities(compWallActivityConverter.convertToActivityInstances(
				null,
				externalCredit.getTargetActivities(), 
				loggedUser.getId(), 
				true, 
				false, 
				locale));
		return externalCreditData;
	}

}

