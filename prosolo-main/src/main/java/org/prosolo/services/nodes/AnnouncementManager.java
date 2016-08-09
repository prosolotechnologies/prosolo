package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.web.courses.credential.AnnouncementPublishMode;

public interface AnnouncementManager {
	
	public AnnouncementData createAnnouncement(Long credentialId, String title, String text, Long creatorId, AnnouncementPublishMode publishMode);
	
	public void readAnnouncement(Long announcementId, Long userId);
	
	public List<AnnouncementData> getUnseenAnnouncements(Long credentialId, Long userId);
	
	public List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId,int page, int numberPerPage) throws ResourceCouldNotBeLoadedException;
	
	public AnnouncementData get(Long id) throws ResourceCouldNotBeLoadedException;

	public int numberOfAnnouncementsForCredential(long credentialId);

}
