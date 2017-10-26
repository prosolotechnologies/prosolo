package org.prosolo.services.nodes.impl;

import java.util.*;

import javax.inject.Inject;

import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.SeenAnnouncement;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.courses.credential.announcements.AnnouncementPublishMode;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.AnnouncementManager")
public class AnnouncementManagerImpl extends AbstractManagerImpl implements AnnouncementManager {
	
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private EventFactory eventFactory;
	@Inject
	private AnnouncementManager self;

	private static final long serialVersionUID = 1L;
	private static final String GET_ANNOUNCEMENTS_FOR_CREDENTIAL = 
			"FROM Announcement AS announcement " 
		  + "LEFT JOIN FETCH announcement.createdBy " // load user and his data
		  + "WHERE announcement.credential.id = :credentialId "
		  + "AND announcement.deleted = :deleted "			
		  + "ORDER BY announcement.dateCreated DESC";
	private static final String COUNT_ANNOUNCEMENTS_FOR_CREDENTIAL = 
			"SELECT COUNT(*) FROM Announcement AS announcement " 
		  + "WHERE announcement.credential.id = :credentialId "
		  + "AND announcement.deleted = :deleted";
//	private static final String GET_LAST_SEEN_ANNOUNCEMENT_NATIVE = 
//			"SELECT id FROM seen_announcement WHERE announcement = (SELECT id FROM announcement WHERE credential = ? ORDER BY created DESC LIMIT 1) and user = ?";
	private static final String GET_SEEN_ANNOUNCEMENT = 
			"FROM SeenAnnouncement sa WHERE sa.announcement.id = :announcementId AND sa.user.id = :userId";
	
	
	@Override
	public AnnouncementData createAnnouncement(Long credentialId, String title, String text, Long creatorId, AnnouncementPublishMode mode,
											   UserContextData context)
			throws ResourceCouldNotBeLoadedException, EventException {

		Result<AnnouncementData> result = self.createAnnouncementAndGetEvents(credentialId, title, text, creatorId, mode, context);
		for (EventData ev : result.getEvents()) {
			eventFactory.generateEvent(ev);
		}
		return result.getResult();
	}

	@Override
	@Transactional
	public Result<AnnouncementData> createAnnouncementAndGetEvents(Long credentialId, String title, String text, Long creatorId,
																   AnnouncementPublishMode publishMode,
																   UserContextData context)
			throws ResourceCouldNotBeLoadedException, EventException, DbConnectionException {

		Announcement announcement = new Announcement();
		announcement.setTitle(title);
		announcement.setText(text);
		announcement.setDateCreated(new Date());
		//create and add creator
		try {
			User user = loadResource(User.class, creatorId);
			announcement.setCreatedBy(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		//create and add credential
		Credential1 credential = new Credential1();
		credential.setId(credentialId);
		announcement.setCredential(credential);
		Announcement newAnnouncement = saveEntity(announcement);

		Result<AnnouncementData> result = new Result<>();
		Announcement announcement1 = new Announcement();
		announcement1.setId(announcement.getId());
		Map<String, String> parameters = new HashMap<>();
		parameters.put("credentialId", credentialId + "");
		parameters.put("publishMode", publishMode.getText());

		result.addEvent(eventFactory.generateEventData(EventType.AnnouncementPublished, context,
				announcement1, credential, null, parameters));

		result.setResult(mapToData(newAnnouncement));

		return result;
	}


	@Override
	@Transactional(readOnly=true)
	public int numberOfAnnouncementsForCredential(Long credentialId) {
		return ((Long)getCountAnnouncementsForCredentialQuery(credentialId, false).uniqueResult()).intValue();
	}
	

	@Override
	@Transactional(readOnly=true)
	public AnnouncementData getAnnouncement(Long announcementId) throws ResourceCouldNotBeLoadedException {
		return mapToData(get(Announcement.class, announcementId));
	}

	@Override
	@Transactional
	public void readAnnouncement(Long announcementId, Long userId) {
		SeenAnnouncement seenAnnouncement = (SeenAnnouncement) getSeenAnnouncementQuery(userId, announcementId).uniqueResult();
		if(seenAnnouncement == null) {
			persistSeenAnnouncement(announcementId, userId);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<AnnouncementData> getAllAnnouncementsForCredential(Long credentialId, int page, int limit) throws ResourceCouldNotBeLoadedException {
		Query query = getAnnouncementsForCredentialQuery(credentialId, page, limit, false);
		List<Announcement> announcements = query.list();
		List<AnnouncementData> announcementData = new ArrayList<>(announcements.size());
		for(Announcement original : announcements) {
			announcementData.add(mapToData(original));
		}
		return announcementData;
	}
	


	@Override
	@Transactional(readOnly=true)
	public Long getLastAnnouncementIdIfNotSeen(Long credentialId, Long userId) {
		Announcement lastAnnouncement = (Announcement) getLastAnnouncementForCredentialQuery(credentialId, false).uniqueResult();
		if(lastAnnouncement != null) {
			SeenAnnouncement seenAnnouncement = (SeenAnnouncement) getSeenAnnouncementQuery(userId, lastAnnouncement.getId()).uniqueResult();
			if(seenAnnouncement != null) {
				return seenAnnouncement.getId();
			}
		}
		return null;
	}
	


	@Override
	@Transactional(readOnly=true)
	public AnnouncementData getLastAnnouncementForCredential(Long credentialId) {
		Announcement lastAnnouncement = (Announcement) getLastAnnouncementForCredentialQuery(credentialId, false).uniqueResult();
		if(lastAnnouncement != null) {
			return mapToData(lastAnnouncement);
		}
		else return null;
	}

	private Query getAnnouncementsForCredentialQuery(Long credentialId, int page, int limit, boolean deleted) {
		Query query = persistence.currentManager().createQuery(GET_ANNOUNCEMENTS_FOR_CREDENTIAL)
				.setLong("credentialId", credentialId)
				.setBoolean("deleted", deleted);
		query.setFirstResult(limit * page).setMaxResults(limit);
		return query;
	}
	
	private Query getLastAnnouncementForCredentialQuery(Long credentialId, boolean deleted) {
		Query query = persistence.currentManager().createQuery(GET_ANNOUNCEMENTS_FOR_CREDENTIAL)
				.setLong("credentialId", credentialId)
				.setBoolean("deleted", deleted);
		query.setMaxResults(1);
		return query;
	}
	
	private Query getCountAnnouncementsForCredentialQuery(Long credentialId, boolean deleted) {
		Query query = persistence.currentManager().createQuery(COUNT_ANNOUNCEMENTS_FOR_CREDENTIAL)
				.setLong("credentialId", credentialId)
				.setBoolean("deleted", deleted);
		return query;
	}
	
//	private Query getLastReadAnnouncementQuery(Long userId, Long credentialId) {
//		Query query = persistence.currentManager().createSQLQuery(GET_LAST_SEEN_ANNOUNCEMENT_NATIVE)
//				.addEntity(SeenAnnouncement.class);
//		query.setParameter(0, credentialId);
//		query.setParameter(1, userId);
//		return query;
//	}
	
	private Query getSeenAnnouncementQuery(Long userId, Long announcementId) {
		Query query = persistence.currentManager().createQuery(GET_SEEN_ANNOUNCEMENT);
		query.setParameter("announcementId", announcementId);
		query.setParameter("userId", userId);
		return query;
	}

	private AnnouncementData mapToData(Announcement original) {
		AnnouncementData data = new AnnouncementData();
		data.setId(original.getId());
		data.setTitle(original.getTitle());
		data.setText(original.getText());
		data.setCreatorFullName(original.getCreatedBy().getFullName());
		data.setCreatorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(original.getCreatedBy(), ImageFormat.size120x120));
		data.setCreationTime(DateUtil.getMillisFromDate(original.getDateCreated()));
		data.setEncodedId(idEncoder.encodeId(original.getId()));
		return data;
	}

	private void persistSeenAnnouncement(Long announcementId, Long userId) {
		SeenAnnouncement seen = new SeenAnnouncement();
		Announcement announcement = new Announcement();
		announcement.setId(announcementId);
		seen.setAnnouncement(announcement);
		User user= new User();
		user.setId(userId);
		seen.setUser(user);
		seen.setDateCreated(new Date());
		saveEntity(seen);
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}


}
