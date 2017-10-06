package org.prosolo.web.datatopagemappers;

import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.achievements.data.CredentialAchievementsData;
import org.prosolo.web.achievements.data.TargetCredentialData;

import javax.inject.Inject;
import java.util.List;

public class CredentialAchievementsDataToPageMapper
		implements IDataToPageMapper<CredentialAchievementsData, List<TargetCredentialData>> {

	public CredentialAchievementsDataToPageMapper() {}

	@Inject
	private CredentialManager credentialManager;

	@Override
	public CredentialAchievementsData mapDataToPageObject(List<TargetCredentialData> targetCredential1List) {
		CredentialAchievementsData credentialAchievementsData = new CredentialAchievementsData();

		for (TargetCredentialData targetCredential1 : targetCredential1List) {
			if (targetCredential1 != null) {
				TargetCredentialData targetCredentialData = new TargetCredentialData(
						targetCredential1.getId(),
						targetCredential1.getCredential().getTitle(),
						targetCredential1.getCredential().getDescription(),
						targetCredential1.isHiddenFromProfile(),
						targetCredential1.getCredential().getDuration(),
						targetCredential1.getCredential().getId(),
						targetCredential1.getProgress(),
						targetCredential1.getNextCompetenceToLearnId());

				credentialAchievementsData.getTargetCredentialDataList().add(targetCredentialData);
			}
		}
		return credentialAchievementsData;
	}

}
