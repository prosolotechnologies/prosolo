package org.prosolo.web.datatopagemappers;

import java.util.List;

import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.web.achievements.data.CompetenceAchievementsData;
import org.prosolo.web.achievements.data.TargetCompetenceData;

import javax.inject.Inject;

/**
 * @author "Musa Paljos"
 * 
 */

public class CompetenceAchievementsDataToPageMapper
        implements IDataToPageMapper<CompetenceAchievementsData, List<TargetCompetenceData>> {

    @Inject
    private Competence1Manager competence1Manager;

    @Override
    public CompetenceAchievementsData mapDataToPageObject(List<TargetCompetenceData> targetCompetence1List) {
        CompetenceAchievementsData competenceAchievementsData = new CompetenceAchievementsData();

        TargetCompetenceData targetCompetenceData;

        for (TargetCompetenceData targetCompetence1 : targetCompetence1List) {
            if (targetCompetence1 != null) {
                CompetenceData1 competenceData1 = competence1Manager.getCompetenceData(targetCompetence1.getCompetenceId());
                targetCompetenceData = new TargetCompetenceData(
                        targetCompetence1.getId(),
                        competenceData1.getDescription(),
                        competenceData1.getTitle(),
                        targetCompetence1.isHiddenFromProfile(),
                        competenceData1.getDuration(),
                        competenceData1.getType(),
                        competenceData1.getCompetenceId());
                competenceAchievementsData.getTargetCompetenceDataList().add(targetCompetenceData);
            }
        }
        return competenceAchievementsData;
    }

}