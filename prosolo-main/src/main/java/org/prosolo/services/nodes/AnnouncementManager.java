package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.web.courses.credential.announcements.AnnouncementPublishMode;

public interface AnnouncementManager {
	
	AnnouncementData createAnnouncement(Long credentialId, String title, String text, Long creatorId, AnnouncementPublishMode publishMode);
	
	AnnouncementData getAnnouncement(Long announcementId) throws ResourceCouldNotBeLoadedException;
	
	void readAnnouncement(Long announcementId, Long userId);
	
	List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId, int page, int limit) throws ResourceCouldNotBeLoadedException;

	int numberOfAnnouncementsForCredential(Long credentialId);
	
	Long getLastAnnouncementIdIfNotSeen(Long credentialId, Long userId);
	
	AnnouncementData getLastAnnouncementForCredential(Long credentialId);

	void generateAnnouncementPublishedEvent(Credential1 credential1, Announcement announcement,Map<String,String> parameters,
											UserContextData contextData) throws EventException;

}
