package org.prosolo.services.notifications.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.*;
import org.prosolo.services.email.EmailSender;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.emailgenerators.NotificationEmailContentGenerator1;
import org.prosolo.services.notifications.emailgenerators.NotificationEmailGenerator;
import org.prosolo.services.notifications.emailgenerators.NotificationEmailGeneratorFactory;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.services.notifications.factory.NotificationDataFactory;
import org.prosolo.services.notifications.factory.NotificationSectionDataFactory;
import org.prosolo.web.util.page.PageSection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@Service("org.prosolo.services.notifications.NotificationManager")
public class NotificationManagerImpl extends AbstractManagerImpl implements NotificationManager {
	
	private static final long serialVersionUID = -1373529937043699141L;
	
	private static Logger logger = Logger.getLogger(NotificationManager.class);
	
	@Inject
	private NotificationDataFactory notificationDataFactory;
	@Inject
	private NotificationEmailGeneratorFactory notificationEmailGeneratorFactory;
	@Inject
	private EmailSender emailSender;
	@Inject
	private RoleManager roleManager;
	@Inject
	private NotificationSectionDataFactory notificationSectionDataFactory;
 	
	@Override
	@Transactional (readOnly = true)
	public Integer getNumberOfUnreadNotifications(long userId, NotificationSection section) {
		  String query=
		   "SELECT COUNT(notification1) " +
		   "FROM Notification1 notification1 " +
		   "WHERE notification1.receiver.id = :userId " +
	       "AND notification1.read = false " +
	       "AND notification1.section =:section" ;
		  
		  long resNumber = (long) persistence.currentManager().createQuery(query)
		     .setLong("userId", userId)
			 .setString("section",section.toString())
		     .uniqueResult();
		  
		    return (int) resNumber;
		 }
	
	@Override
	public boolean sendNotificationByEmail(String email, String receiverName, String actor, 
			String notificationType, String notificationShortType, String resourceTitle, String message, String date, boolean notifyByUI) {
		email = email.toLowerCase();
		
		try {
			String link = null; 
			if(notifyByUI) {
				link = CommonSettings.getInstance().config.appConfig.domain + "communications/notifications";
			}
			
			NotificationEmailContentGenerator1 generator = new NotificationEmailContentGenerator1(receiverName, actor, 
					notificationType, notificationShortType, resourceTitle, message, date, link);
			
			emailSender.sendEmail(generator,  email);
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
	public Notification1 createNotification(long actorId, NotificationActorRole actorRole,
			boolean anonymizedActor, long receiverId, NotificationType type, Date date,
			long objectId, ResourceType objectType, long targetId, ResourceType targetType, String link,
			boolean notifyByEmail, boolean isObjectOwner, PageSection section) throws DbConnectionException {
		try {
			User receiver = loadResource(User.class, receiverId);
			Notification1 notification = new Notification1();
			notification.setNotifyByEmail(notifyByEmail);
			notification.setDateCreated(date);
			if (actorId > 0) {
				notification.setActor((User) loadResource(User.class, actorId));
			}
			notification.setNotificationActorRole(actorRole);
			notification.setAnonymizedActor(anonymizedActor);
			notification.setReceiver(receiver);
			notification.setRead(false);
			notification.setType(type);
			notification.setObjectId(objectId);
			notification.setObjectType(objectType);
			notification.setTargetId(targetId);
			notification.setTargetType(targetType);
			notification.setLink(link);
			notification.setObjectOwner(isObjectOwner);
			notification.setSection(notificationSectionDataFactory.getSection(section));

			saveEntity(notification);

			return notification;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error saving notification");
		}
	}

	/**
	 *
	 *
	 * @param userId
	 * @param page
	 * @param limit
	 * @param filters	if empty, then no filtering will be applied
	 * @param locale
	 * @param section
	 * @return
	 * @throws DbConnectionException
	 */
	@Override
	@Transactional (readOnly = true)
	public List<NotificationData> getNotificationsForUser(long userId, int page, int limit,
														  List<NotificationType> filters, Locale locale,
														  NotificationSection section) throws DbConnectionException {
		try {
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT DISTINCT notification " +
								"FROM Notification1 notification " +
								"LEFT JOIN FETCH notification.actor actor " +
								"WHERE notification.receiver.id = :userId " +
								"AND notification.section =:section ");
			if (filters != null && !filters.isEmpty()) {
				queryBuilder.append("AND notification.type IN (:types) ");
			}
			queryBuilder.append("ORDER BY notification.dateCreated DESC");
		  	
			Query q = persistence.currentManager().createQuery(queryBuilder.toString())
			  	.setLong("userId", userId).setString("section",section.toString());

			if (filters != null && !filters.isEmpty()) {
				q.setParameterList("types", filters);
			}
			if (limit != 0) {
				q.setFirstResult(page * limit)
						.setMaxResults(limit);
			}
			
			@SuppressWarnings("unchecked")
			List<Notification1> result = q.list();
		  	
			List<NotificationData> notificationData = new LinkedList<>();
			if (result != null) {
				for (Notification1 notification : result) {
					String objectTitle = null;
					if (notification.getObjectId() > 0) {
						objectTitle = getObjectTitle(notification.getObjectId(),
								notification.getObjectType(), persistence.currentManager());
					}
					
					String targetTitle = null;
					if (notification.getTargetId() > 0) {
						targetTitle = getObjectTitle(notification.getTargetId(),
								notification.getTargetType(), persistence.currentManager());
					}
 					NotificationData nd = notificationDataFactory.getNotificationData(
							notification, 
							null,
							objectTitle, 
							targetTitle, 
							locale,
							notification.getSection());
					notificationData.add(nd);
				}
			}
		  	return notificationData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving notifications");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationData getNotificationData(long notificationId, boolean loadReceiver, Locale locale) 
			throws DbConnectionException {
		return getNotificationData(notificationId, loadReceiver, persistence.currentManager(), locale);
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationData getNotificationData(long notificationId, boolean loadReceiver, 
			Session session, Locale locale) throws DbConnectionException {
		StringBuilder query = new StringBuilder(
			"SELECT notification1 " +
			"FROM Notification1 notification1 " +
			"LEFT JOIN fetch notification1.actor actor ");
		
		if(loadReceiver) {
			query.append("INNER JOIN fetch notification1.receiver ");
		}
	  	
        query.append("WHERE notification1.id = :id");
        
		Notification1 result = (Notification1) session
			.createQuery(query.toString())
		  	.setLong("id", notificationId)
	  		.uniqueResult();
		
		//session.update(result.getActor());
		User receiver = loadReceiver ? result.getReceiver() : null;
	  	return getNotificationData(result, receiver, session, locale);
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationData getNotificationData(Notification1 notification, User receiver, 
			Session session, Locale locale) throws DbConnectionException {
		try {
		  	if (notification != null) {
		  		String objectTitle = null;
				if (notification.getObjectId() > 0) {
					objectTitle = getObjectTitle(notification.getObjectId(),
							notification.getObjectType(), session);
				}
				
				String targetTitle = null;
				if (notification.getTargetId() > 0) {
					targetTitle = getObjectTitle(notification.getTargetId(),
							notification.getTargetType(), persistence.currentManager());
				}
	  			return notificationDataFactory.getNotificationData(notification, receiver,
	  					objectTitle, targetTitle, locale, notification.getSection());
		  	}
		  	return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving notification data");
		}
	}

	private String getObjectTitle(long objectId, ResourceType objectType, Session session) {
		if (objectType == ResourceType.Student) {
			String query1 = "SELECT obj.name, obj.lastname " +
					"FROM " + objectType.getDbTableName() + " obj " +
					"WHERE obj.id = :id";
			Object[] res = (Object[]) session
					.createQuery(query1)
					.setLong("id", objectId)
					.uniqueResult();
			String firstName = (String) res[0];
			String lastName = (String) res[1];
			return firstName + " " +  lastName;
		}
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
			String predicate, long objectId, ResourceType objectType, String objectTitle, String link, String date, NotificationType type, Session session) {
		email = email.toLowerCase();
		
		try {
			NotificationEmailGenerator generator = notificationEmailGeneratorFactory.getNotificationEmailContentGenerator(
					receiverName, actor, predicate, objectId, objectType, objectTitle, date, link, type, session);
			
			return emailSender.sendEmail(generator,  email);
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
	public int getNumberOfNotificationsForUser(long userId, List<NotificationType> types, NotificationSection section)
			throws DbConnectionException {
		try {
			StringBuilder queryBuilder = new StringBuilder();
		    queryBuilder.append("SELECT cast(COUNT(notification.id) as int) " +
							    "FROM Notification1 notification " +
								"WHERE notification.receiver.id = :userId " +
								"AND notification.section = :section ");

			if (types != null && !types.isEmpty()) {
		    	queryBuilder.append("AND notification.type IN (:types)");
		    }
			
			Query q = persistence.currentManager().createQuery(queryBuilder.toString())
					.setLong("userId", userId)
					.setString("section", section.toString());

			if (types != null && !types.isEmpty()) {
				q.setParameterList("types", types);
			}
			
		  	return (Integer) q.uniqueResult();
		} catch(Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error retrieving notification data");
		}
	}

}
