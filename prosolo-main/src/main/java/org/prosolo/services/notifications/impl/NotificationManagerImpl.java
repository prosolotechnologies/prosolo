package org.prosolo.services.notifications.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activities.requests.RequestStatus;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.CommentNotification;
import org.prosolo.common.domainmodel.user.notifications.EvaluationNotification;
import org.prosolo.common.domainmodel.user.notifications.EvaluationSubmissionNotification;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;
import org.prosolo.common.domainmodel.user.notifications.PostNotification;
import org.prosolo.common.domainmodel.user.notifications.RequestNotification;
import org.prosolo.common.domainmodel.user.notifications.SActivityNotification;
import org.prosolo.common.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.services.email.EmailSender;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.emailgenerators.NotificationEmailContentGenerator;
import org.prosolo.services.notifications.util.RequestTypeToNotificationActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.notifications.NotificationManager")
public class NotificationManagerImpl extends AbstractManagerImpl implements NotificationManager {
	
	private static final long serialVersionUID = -1373529937043699141L;
	
	private static Logger logger = Logger.getLogger(NotificationManager.class);
	
	private @Autowired PersonalCalendarManager calendarManager;
	@Inject
	private EmailSender emailSender;
 	
	@Override
	@Transactional (readOnly = true)
	public Integer getNumberOfUnreadNotifications(User user) {
		String query=
			"SELECT cast(COUNT(notification) as int) " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.notifications notification "+
			"WHERE user = :user " +
				"AND notification.read = false " +
				"AND notification.notifyByUI = :notifyByUI " +
			"ORDER BY notification.dateCreated DESC" ;
		
		Integer resNumber = (Integer) persistence.currentManager().createQuery(query)
		  	.setEntity("user", user)
		  	.setBoolean("notifyByUI", true)
		  	.uniqueResult();
		
	  	return resNumber;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Notification> getNotifications(User user, int limit, Collection<Notification> filterList) {
		String query=
			"SELECT DISTINCT notification " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.notifications notification "+
			"WHERE user = :user ";
		
		if (filterList != null && !filterList.isEmpty())
			query += "AND notification NOT IN (:filterList) ";
		
		query += "ORDER BY notification.dateCreated DESC" ;
		
		Query q = persistence.currentManager().createQuery(query)
			.setEntity("user", user);
		
		if (filterList != null && !filterList.isEmpty())
			q.setParameterList("filterList", filterList);
			
		@SuppressWarnings("unchecked")
		List<Notification> result =	q.setMaxResults(limit).list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<Notification>();
	}
	
	@Transactional (readOnly = true)
	public List<Notification> getNotifications(User user, int page, int limit) {
		String query=
			"SELECT DISTINCT notification " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.notifications notification "+
			"LEFT JOIN FETCH notification.actor "+
			"WHERE user = :user " +
			"AND notification.notifyByUI = :notifyByUI " +
			"ORDER BY notification.dateCreated DESC";
	  	
		@SuppressWarnings("unchecked")
		List<Notification> result = persistence.currentManager().createQuery(query)
		  	.setEntity("user", user)
		  	.setBoolean("notifyByUI", true)
		  	.setFirstResult(page * limit)
			.setMaxResults(limit)
	  		.list();
	  	
	  	if (result != null) {
	  		return result;
	  	}
	  	return new ArrayList<Notification>();
	}
	
	@Override
	@Transactional (readOnly = false)
	public Notification markAsRead(Notification notification, Session session) {
		if (notification != null && !notification.isRead()) {
			notification.setRead(true);
			session.saveOrUpdate(notification);
			return notification;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void markAsReadAllUnreadNotifications(User user, Session session) {
		String query =
			"SELECT DISTINCT notification.id " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.notifications notification "+
				"WHERE user = :user " +
				"AND notification.read = false ";

		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
		  	.setEntity("user", user)
	  		.list();
		
		if (result != null && !result.isEmpty()) {
			String query1 =
				"UPDATE Notification SET read = true " +
				"WHERE id IN (:ids)";
			
			@SuppressWarnings("unused")
			int result1 = session.createQuery(query1)
				.setParameterList("ids", result)
				.executeUpdate();
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean markAsRead(long[] notificationIds, Session session) throws ResourceCouldNotBeLoadedException {
		boolean successful = true;
		
		for (int i = 0; i < notificationIds.length; i++) {
			long notificationId = notificationIds[i];
			
			if (notificationId > 0) {
				Notification notification = (Notification) session.get(Notification.class, notificationId);
				notification = (Notification) session.merge(notification);
				
				Notification not = markAsRead(notification, session);
				
				successful = successful && not != null;
			}
		}
		return successful;
	}
	private Notification createNotificationForResource(BaseEntity resource){
		Notification notification = null;
		
		if (resource instanceof Request) {
			notification = new RequestNotification();
			((RequestNotification) notification).setRequest((Request) resource);
		} else if (resource instanceof EvaluationSubmission) {
			notification = new EvaluationSubmissionNotification();
			((EvaluationSubmissionNotification) notification).setEvaluationSubmission((EvaluationSubmission) resource);
		} else if (resource instanceof Evaluation) {
			notification = new EvaluationNotification();
			((EvaluationNotification) notification).setEvaluation((Evaluation) resource);
		} else if (resource instanceof Comment) {
			notification = new CommentNotification();
			((CommentNotification) notification).setComment((Comment) resource);
		} else if (resource instanceof SocialActivity) {
			notification = new SActivityNotification();
			((SActivityNotification) notification).setSocialActivity((SocialActivity) resource);
		} else if (resource instanceof Post) {
			notification = new PostNotification();
			((PostNotification) notification).setPost((Post) resource);
		} else {
			notification = new Notification();
		}
		
		return notification;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public Notification createNotification(BaseEntity resource, User creator, 
			User receiver, EventType type, String message, Date date, boolean notifyByUI, 
			boolean notifyByEmail, Session session) {

		Notification notification = createNotificationForResource(resource);
		notification.setNotifyByUI(notifyByUI);
		notification.setNotifyByEmail(notifyByEmail);
		notification.setDateCreated(date);
		notification.setUpdated(date);
		notification.setMessage(message);
		notification.setActor(creator);
		notification.setReceiver(receiver);
		notification.setRead(false);
		notification.setType(type);
		notification.setActions(RequestTypeToNotificationActionMapping.getNotificationActions(type));
		notification.setActinable(!notification.getActions().isEmpty());;
		session.save(notification);
		
		receiver = (User) session.merge(receiver);
		
		PersonalCalendar pCalendar = calendarManager.getOrCreateCalendar(receiver, session);
		pCalendar=(PersonalCalendar) session.merge(pCalendar);
		pCalendar.addNotification(notification);
		session.saveOrUpdate(pCalendar);

		session.flush();
		return notification;
	}
	
	@Override
	public Notification markNotificationStatus(long notificationId, NotificationAction status) throws ResourceCouldNotBeLoadedException {
		Notification notification = loadResource(Notification.class, notificationId);
		
		if (notification.getObject() instanceof Request) {
			Request request = (Request) notification.getObject();
			
			switch (status) {
				case ACCEPT:
					request.setStatus(RequestStatus.ACCEPTED);
					break;
				case IGNORE:
					request.setStatus(RequestStatus.IGNORED);
					break;
				case DENY:
					request.setStatus(RequestStatus.DENIED);
					break;
				default:
					break;
			}
			
			saveEntity(request);
		}
		
		notification.setUpdated(new Date());
		notification.setChosenAction(status);
		return saveEntity(notification);
	}
	
	@Override
	public boolean sendNotificationByEmail(String email, String receiverName, String actor, 
			String notificationType, String notificationShortType, String resourceTitle, String message, String date, boolean notifyByUI) {
		email = email.toLowerCase();
		
		try {
			String link = null; 
			if(notifyByUI) {
				link = Settings.getInstance().config.application.domain + "communications/notifications";
			}
			
			NotificationEmailContentGenerator generator = new NotificationEmailContentGenerator(receiverName, actor, 
					notificationType, notificationShortType, resourceTitle, message, date, link);
			
			emailSender.sendEmail(generator,  email, "ProSolo Notification");
			return true;
		} catch (AddressException e) {
			logger.error(e);
		} catch (MessagingException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch(Exception e) {
			logger.error(e);
		}
		return false;
	}
	
}
