package org.prosolo.services.notifications;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.CentralEventDispatcher;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.eventprocessing.NotificationEventProcessor;
import org.prosolo.services.notifications.eventprocessing.NotificationEventProcessorFactory;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This class is an observer to the {@link CentralEventDispatcher} that is invoked whenever an event that is related to a notification occurs.  
 */
@Service("org.prosolo.services.notifications.NotificationObserver")
public class NotificationObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(NotificationObserver.class.getName());

	@Inject private ApplicationBean applicationBean;
	@Inject private DefaultManager defaultManager;
	@Inject private NotificationCacheUpdater notificationCacheUpdater;
	@Inject private SessionMessageDistributer messageDistributer;
	@Inject private NotificationEventProcessorFactory notificationEventProcessorFactory;
	@Inject private InterfaceSettingsManager interfaceSettingsManager;
	@Inject private NotificationManager notificationManager;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.JOIN_GOAL_REQUEST,
				EventType.JOIN_GOAL_REQUEST_APPROVED,
				EventType.JOIN_GOAL_REQUEST_DENIED,
				EventType.JOIN_GOAL_INVITATION,
				EventType.JOIN_GOAL_INVITATION_ACCEPTED,
				//TODO is it safe do delete these? Now we use assessments
				EventType.EVALUATION_REQUEST, 
				EventType.EVALUATION_ACCEPTED,
				EventType.EVALUATION_GIVEN, 
//				EventType.EVALUATION_EDITED, 
				EventType.Follow,
				EventType.ACTIVITY_REPORT_AVAILABLE,
				EventType.Comment,
				EventType.Comment_Reply,
				EventType.Like,
				EventType.Dislike,
				EventType.Post,
				EventType.AssessmentRequested,
				EventType.AssessmentApproved
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		
		try {
			NotificationEventProcessor processor = notificationEventProcessorFactory
					.getNotificationEventProcessor(event, session);
			if(processor != null) {
				List<Notification1> notifications = processor.getNotificationList();
				// make sure all data is persisted to the database
				session.flush();
				
				
				/*
				 * After all notifications have been generated, send them to their
				 * receivers. If those users are logged in, their notification cache
				 * will be updated with these new notifications.
				 */
				if (!notifications.isEmpty()) {
					
					for (Notification1 notification : notifications) {					
						if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
							messageDistributer.distributeMessage(
									ServiceType.ADD_NOTIFICATION, 
									notification.getReceiver().getId(),
									notification.getId(), 
									null, 
									null);
						} else {
							HttpSession httpSession = applicationBean.getUserSession(notification.getReceiver().getId());
							
							notificationCacheUpdater.updateNotificationData(
									notification.getId(), 
									httpSession, 
									session);
						}
					 				
						if (notification.isNotifyByEmail() && CommonSettings.getInstance().config.emailNotifier.activated) {
							try {
								UserSettings userSettings = interfaceSettingsManager.
										getOrCreateUserSettings(notification.getReceiver(), session);
								Locale locale = getLocale(userSettings);
								/*
								 * get all notification data in one query insted of issuing session.update
								 * for sender and receiver - all in order to avoid lazy initialization exception
								 */
								NotificationData notificationData = notificationManager
										.getNotificationData(notification.getId(), true, 
												session, locale);
								//session.update(notification.getActor());
								//session.update(receiver);						 
								String domain = Settings.getInstance().config.application.domain;
								if(domain.endsWith("/")) {
									domain = domain.substring(0, domain.length() - 1);
								}
								final String urlPrefix = domain;
								taskExecutor.execute(new Runnable() {
									@Override
									public void run() {
										
										notificationManager.sendNotificationByEmail(
												notificationData.getReceiver().getEmail(), 
												notificationData.getReceiver().getFullName(), 
												notificationData.getActor().getFullName(), 
												notificationData.getPredicate(),
												notificationData.getObjectTitle(),
												urlPrefix + notificationData.getLink(),
												DateUtil.getTimeAgoFromNow(notificationData.getDate()));
									}
								});
							} catch (Exception e) {
								logger.error(e);
								e.printStackTrace();
							}
						}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			if(session != null && session.isOpen()) {
				HibernateUtil.close(session);
			}
		}
	}
	
	public Locale getLocale(UserSettings userSettings) {
		if (userSettings!= null && userSettings.getLocaleSettings() != null) {
			return userSettings.getLocaleSettings().createLocale();
		} else {
			return new Locale("en", "US");
		}
	}

}
