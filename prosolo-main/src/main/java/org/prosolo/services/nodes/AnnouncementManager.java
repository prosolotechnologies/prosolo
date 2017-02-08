package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.web.courses.credential.announcements.AnnouncementPublishMode;

public interface AnnouncementManager {
	
	public AnnouncementData createAnnouncement(Long credentialId, String title, String text, Long creatorId, AnnouncementPublishMode publishMode);
	
	public AnnouncementData getAnnouncement(Long announcementId) throws ResourceCouldNotBeLoadedException;
	
	public void readAnnouncement(Long announcementId, Long userId);
	
	public List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId, int page, int limit) throws ResourceCouldNotBeLoadedException;

	public int numberOfAnnouncementsForCredential(Long credentialId);
	
	public Long getLastAnnouncementIdIfNotSeen(Long credentialId, Long userId);
	
	public AnnouncementData getLastAnnouncementForCredential(Long credentialId);

}
