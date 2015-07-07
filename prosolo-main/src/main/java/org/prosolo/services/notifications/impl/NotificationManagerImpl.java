package org.prosolo.services.notifications.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.CommentNotification;
import org.prosolo.common.domainmodel.user.notifications.EvaluationNotification;
import org.prosolo.common.domainmodel.user.notifications.EvaluationSubmissionNotification;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.user.notifications.PostNotification;
import org.prosolo.common.domainmodel.user.notifications.RequestNotification;
import org.prosolo.common.domainmodel.user.notifications.SActivityNotification;
import org.prosolo.common.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.util.RequestTypeToNotificationActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.notifications.NotificationManager")
public class NotificationManagerImpl extends AbstractManagerImpl implements NotificationManager {
	
	private static final long serialVersionUID = -1373529937043699141L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(NotificationManager.class);
	
	private @Autowired UserManager userManager;
	private @Autowired DefaultManager defaultManager;
	private @Autowired PersonalCalendarManager calendarManager;
 	
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
			"ORDER BY notification.dateCreated DESC" ;
		
		Integer resNumber = (Integer) persistence.currentManager().createQuery(query)
		  	.setEntity("user", user)
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
		//	"LEFT JOIN FETCH notification.object "+
			"LEFT JOIN FETCH notification.actor "+
			"WHERE user = :user " +
			"ORDER BY notification.dateCreated DESC";
	  	
		@SuppressWarnings("unchecked")
		List<Notification> result = persistence.currentManager().createQuery(query)
		  	.setEntity("user", user)
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
				//Notification notification = loadResource(Notification.class, notificationIds[i]);
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
			User receiver, EventType type, String message, Date date, Session session) {

		Notification notification = createNotificationForResource(resource);
		notification.setDateCreated(date);
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
	
}
