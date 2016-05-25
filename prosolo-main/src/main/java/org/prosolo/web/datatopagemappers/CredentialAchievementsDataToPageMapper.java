package org.prosolo.web.datatopagemappers;

import java.util.List;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.achievements.data.CredentialAchievementsData;
import org.prosolo.web.achievements.data.TargetCredentialData;

public class CredentialAchievementsDataToPageMapper implements IDataToPageMapper<CredentialAchievementsData, List<TargetCredential1>> {
	
	private UrlIdEncoder idEncoder;
	
	public CredentialAchievementsDataToPageMapper(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}
	
	@Override
	public CredentialAchievementsData mapDataToPageObject(List<TargetCredential1> targetCredential1List) {
		CredentialAchievementsData credentialAchievementsData = new CredentialAchievementsData();
		
		TargetCredentialData targetCredentialData;
		
		for(TargetCredential1 targetCredential1 : targetCredential1List) {
			if(targetCredential1 != null) { 
				targetCredentialData = new TargetCredentialData(targetCredential1.getId(), targetCredential1.getTitle(), targetCredential1.getDescription(), idEncoder);
				credentialAchievementsData.getTargetCredentialDataList().add(targetCredentialData);
				
			}
			
		}
		
		return credentialAchievementsData;
	}

	

	
}
