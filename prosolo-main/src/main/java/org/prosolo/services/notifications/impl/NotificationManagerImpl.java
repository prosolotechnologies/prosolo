package org.prosolo.services.notifications.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activities.requests.RequestStatus;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.email.EmailSender;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.emailgenerators.NotificationEmailContentGenerator;
import org.prosolo.services.notifications.emailgenerators.NotificationEmailContentGenerator1;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.services.notifications.factory.NotificationDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.notifications.NotificationManager")
public class NotificationManagerImpl extends AbstractManagerImpl implements NotificationManager {
	
	private static final long serialVersionUID = -1373529937043699141L;
	
	private static Logger logger = Logger.getLogger(NotificationManager.class);
	
	@Inject
	private NotificationDataFactory notificationDataFactory;
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
//	private Notification createNotificationForResource(BaseEntity resource){
//		Notification notification = null;
//		
//		if (resource instanceof Request) {
//			notification = new RequestNotification();
//			((RequestNotification) notification).setRequest((Request) resource);
//		} else if (resource instanceof EvaluationSubmission) {
//			notification = new EvaluationSubmissionNotification();
//			((EvaluationSubmissionNotification) notification).setEvaluationSubmission((EvaluationSubmission) resource);
//		} else if (resource instanceof Evaluation) {
//			notification = new EvaluationNotification();
//			((EvaluationNotification) notification).setEvaluation((Evaluation) resource);
//		} else if (resource instanceof Comment1) {
//			notification = new CommentNotification();
//			((CommentNotification) notification).setComment((Comment1) resource);
//		} else if (resource instanceof SocialActivity) {
//			notification = new SActivityNotification();
//			((SActivityNotification) notification).setSocialActivity((SocialActivity) resource);
//		} else if (resource instanceof Post) {
//			notification = new PostNotification();
//			((PostNotification) notification).setPost((Post) resource);
//		} else {
//			notification = new Notification();
//		}
//		
//		return notification;
//	}
	
//	@Override
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
//	public Notification createNotification(BaseEntity resource, User creator, 
//			User receiver, EventType type, String message, Date date, boolean notifyByUI, 
//			boolean notifyByEmail, Session session) {
//
//		Notification notification = createNotificationForResource(resource);
//		notification.setNotifyByUI(notifyByUI);
//		notification.setNotifyByEmail(notifyByEmail);
//		notification.setDateCreated(date);
//		notification.setUpdated(date);
//		notification.setMessage(message);
//		notification.setActor(creator);
//		notification.setReceiver(receiver);
//		notification.setRead(false);
//		notification.setType(type);
//		notification.setActions(RequestTypeToNotificationActionMapping.getNotificationActions(type));
//		notification.setActinable(!notification.getActions().isEmpty());;
//		session.save(notification);
//		
//		receiver = (User) session.merge(receiver);
//		
//		PersonalCalendar pCalendar = calendarManager.getOrCreateCalendar(receiver, session);
//		pCalendar=(PersonalCalendar) session.merge(pCalendar);
//		pCalendar.addNotification(notification);
//		session.saveOrUpdate(pCalendar);
//
//		session.flush();
//		return notification;
//	}
	
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
			
			NotificationEmailContentGenerator1 generator = new NotificationEmailContentGenerator1(receiverName, actor, 
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
	
	@Override
	@Transactional(readOnly = false)
	public Notification1 createNotification(long actorId, 
			long receiverId, NotificationType type, Date date, 
			long objectId, ObjectType objectType, String link,
			boolean notifyByEmail, Session session) throws DbConnectionException {
		try {
			User actor = (User) persistence.currentManager().load(User.class, actorId);
			User receiver = (User) persistence.currentManager().load(User.class, receiverId);
			Notification1 notification = new Notification1();
			notification.setNotifyByEmail(notifyByEmail);
			notification.setDateCreated(date);
			notification.setActor(actor);
			notification.setReceiver(receiver);
			notification.setRead(false);
			notification.setType(type);
			notification.setObjectId(objectId);
			notification.setObjectType(objectType);
			notification.setLink(link);
			session.save(notification);
			
			return notification;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving notification");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<NotificationData> getNotificationsForUser(long userId, int page, int limit, 
			List<NotificationType> typesToInclude, Locale locale) throws DbConnectionException {
		try {
			boolean shouldFilterTypes = typesToInclude != null && !typesToInclude.isEmpty();
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT DISTINCT notification " +
								"FROM Notification1 notification " +
								"INNER JOIN FETCH notification.actor actor " +
								"WHERE notification.receiver.id = :userId ");
			if(shouldFilterTypes) {
				queryBuilder.append("AND notification.type IN (:types) ");
			}
			queryBuilder.append("ORDER BY notification.dateCreated DESC");
		  	
			Query q = persistence.currentManager().createQuery(queryBuilder.toString())
			  	.setLong("userId", userId);
			
			if(shouldFilterTypes) {
				q.setParameterList("types", typesToInclude);
			}
			if(limit != 0) {
				q.setFirstResult(page * limit)
				 .setMaxResults(limit);
			}
			
			@SuppressWarnings("unchecked")
			List<Notification1> result = q.list();
		  	
			List<NotificationData> notificationData = new ArrayList<>();
		  	if (result != null) {
		  		for(Notification1 notification : result) {
		  			String objectTitle = null;
		  			if(notification.getObjectId() > 0) {
			  			objectTitle = getObjectTitle(notification.getObjectId(), 
			  					notification.getObjectType(), persistence.currentManager());
		  			}
		  			NotificationData nd = notificationDataFactory.getNotificationData(notification, 
		  					objectTitle, locale);
		  			notificationData.add(nd);
		  		}
		  	}
		  	return notificationData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving notifications");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationData getNotificationData(long notificationId, Locale locale) 
			throws DbConnectionException {
		return getNotificationData(notificationId, persistence.currentManager(), locale);
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationData getNotificationData(long notificationId, Session session, Locale locale) 
			throws DbConnectionException {
		String query=
			"SELECT notification " +
			"FROM Notification1 notification " +
			"INNER JOIN FETCH notification.actor actor " +
			"WHERE notification.id = :id";
	  	
		Notification1 result = (Notification1) session
			.createQuery(query)
		  	.setLong("id", notificationId)
	  		.uniqueResult();
	  	
	  	return getNotificationData(result, session, locale);
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationData getNotificationData(Notification1 notification, Session session, Locale locale) 
			throws DbConnectionException {
		try {
		  	if (notification != null) {
		  		String objectTitle = null;
	  			if(notification.getObjectId() > 0) {
	  				objectTitle = getObjectTitle(notification.getObjectId(), 
	  					notification.getObjectType(), session);
	  			}
	  			return notificationDataFactory.getNotificationData(notification, 
	  					objectTitle, locale);
		  	}
		  	return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving notification data");
		}
	}

	private String getObjectTitle(long objectId, ObjectType objectType, Session session) {
		String query = "SELECT obj.title " +
					   "FROM " + objectType.getDbTableName() + " obj " +
					   "WHERE obj.id = :id";
		
		String title = (String) session
				.createQuery(query)
				.setLong("id", objectId)
				.uniqueResult();
		
		return title;
	}
	
	@Override
	public boolean sendNotificationByEmail(String email, String receiverName, String actor, 
			String predicate, String objectTitle, String link, String date) {
		email = email.toLowerCase();
		
		try {
			NotificationEmailContentGenerator generator = new NotificationEmailContentGenerator(
					receiverName, actor, predicate, objectTitle, date, link);
			
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
	
	@Override
	@Transactional (readOnly = true)
	public int getNumberOfNotificationsForUser(long userId, List<NotificationType> types) 
			throws DbConnectionException {
		try {
			StringBuilder queryBuilder = new StringBuilder();
		    queryBuilder.append("SELECT cast(COUNT(notification.id) as int) " +
							    "FROM Notification1 notification " +
								"WHERE notification.receiver.id = :userId ");
		    
		    if(types != null && !types.isEmpty()) {
		    	queryBuilder.append("AND notification.type IN (:types)");
		    }
			
			Query q = persistence.currentManager().createQuery(queryBuilder.toString())
			  	.setLong("userId", userId);
			if(types != null && !types.isEmpty()) {
				q.setParameterList("types", types);
			}
			
			Integer resNumber = (Integer) q.uniqueResult();
		  	return resNumber;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving notification data");
		}
	}
	
}
