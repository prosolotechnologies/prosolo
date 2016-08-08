package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.AnnouncementManager")
public class AnnouncementManagerImpl extends AbstractManagerImpl implements AnnouncementManager {

	private static final long serialVersionUID = 1L;
	private static final String GET_ANNOUNCEMENTS_FOR_CREDENTIAL = 
			"FROM Announcement AS announcement " 
		  + "WHERE announcement.credential.id = :credentialId "
		  + "AND announcement.deleted = :deleted "			
		  + "ORDER BY announcement.dateCreated DESC";

	@Override
	@Transactional
	public long createAnnouncement(Long credentialId, String title, String text, User creator) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@Transactional
	public void readAnnouncement(Long announcementId, Long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional
	public List<AnnouncementData> getUnseenAnnouncements(Long credentialId, Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public AnnouncementData get(Long id) throws ResourceCouldNotBeLoadedException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId, int page, int numberPerPage) throws ResourceCouldNotBeLoadedException {
		Query query = getAnnouncementsForCredentialQuery(credentialId, page, numberPerPage, false);
		List<Announcement> announcements = query.list();
		List<AnnouncementData> announcementData = new ArrayList<>(announcements.size());
		for(Announcement original : announcements) {
			announcementData.add(mapToData(original));
		}
		return announcementData;
	}

	private Query getAnnouncementsForCredentialQuery(Long credentialId, int page, int numberPerPage, boolean deleted) {
		Query query = persistence.currentManager().createQuery(GET_ANNOUNCEMENTS_FOR_CREDENTIAL)
				.setLong("credentialId", credentialId)
				.setBoolean("deleted", deleted);
		query.setFirstResult(numberPerPage * page).setFetchSize(numberPerPage);
		return query;
	}

	private AnnouncementData mapToData(Announcement original) {
		AnnouncementData data = new AnnouncementData();
		data.setTitle(original.getTitle());
		data.setText(original.getText());
		data.setCreatorFullName(original.getCreatedBy().getFullName());
		data.setCreatorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(original.getCreatedBy(), ImageFormat.size34x34));
		data.setFormattedCreationDate(DateUtil.getPrettyDateEn(original.getDateCreated()));
		return data;
	}


	
}
