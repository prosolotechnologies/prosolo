package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.data.AnnouncementData;

public interface AnnouncementManager {
	
	public long createAnnouncement(Long credentialId, String title, String text, User creator);
	
	public void readAnnouncement(Long announcementId, Long userId);
	
	public List<AnnouncementData> getUnseenAnnouncements(Long credentialId, Long userId);
	
	public List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId,int page, int numberPerPage) throws ResourceCouldNotBeLoadedException;
	
	public AnnouncementData get(Long id) throws ResourceCouldNotBeLoadedException;

}
