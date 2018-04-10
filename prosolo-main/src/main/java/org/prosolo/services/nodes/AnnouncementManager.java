package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.web.courses.credential.announcements.AnnouncementPublishMode;

public interface AnnouncementManager {

	AnnouncementData createAnnouncement(Long credentialId, String title, String text, Long creatorId, AnnouncementPublishMode publishMode,
										UserContextData context)
			throws ResourceCouldNotBeLoadedException;

	Result<AnnouncementData> createAnnouncementAndGetEvents(Long credentialId, String title, String text, Long creatorId,
															AnnouncementPublishMode publishMode,
															UserContextData context)
			throws ResourceCouldNotBeLoadedException, DbConnectionException;
	
	AnnouncementData getAnnouncement(Long announcementId) throws ResourceCouldNotBeLoadedException;
	
	void readAnnouncement(Long announcementId, Long userId);
	
	List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId, int page, int limit) throws ResourceCouldNotBeLoadedException;

	int numberOfAnnouncementsForCredential(Long credentialId);
	
	Long getLastAnnouncementIdIfNotSeen(Long credentialId, Long userId);
	
	AnnouncementData getLastAnnouncementForCredential(Long credentialId);

}
