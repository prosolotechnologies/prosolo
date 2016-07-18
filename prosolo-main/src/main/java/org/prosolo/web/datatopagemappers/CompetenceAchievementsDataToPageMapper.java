package org.prosolo.web.datatopagemappers;

import java.util.List;

import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.web.achievements.data.CompetenceAchievementsData;
import org.prosolo.web.achievements.data.TargetCompetenceData;

/**
 * @author "Musa Paljos"
 * 
 */

public class CompetenceAchievementsDataToPageMapper
        implements IDataToPageMapper<CompetenceAchievementsData, List<TargetCompetence1>> {

    @Override
    public CompetenceAchievementsData mapDataToPageObject(List<TargetCompetence1> targetCompetence1List) {
        CompetenceAchievementsData competenceAchievementsData = new CompetenceAchievementsData();

        TargetCompetenceData targetCompetenceData;

        for (TargetCompetence1 targetCompetence1 : targetCompetence1List) {
            if (targetCompetence1 != null) {
                targetCompetenceData = new TargetCompetenceData(targetCompetence1.getId(),
                        targetCompetence1.getDescription(), targetCompetence1.getTitle(),
                        targetCompetence1.isHiddenFromProfile(), 
                        targetCompetence1.getDuration(), 
                        targetCompetence1.getType(),
                        targetCompetence1.getCompetence().getId(),
                        targetCompetence1.getTargetCredential().getCredential().getId());
                competenceAchievementsData.getTargetCompetenceDataList().add(targetCompetenceData);
            }
        }
        return competenceAchievementsData;
    }

}