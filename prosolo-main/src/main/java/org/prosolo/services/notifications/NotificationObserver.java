package org.prosolo.services.notifications;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.AppConfig;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.event.CentralEventDispatcher;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.eventprocessing.NotificationEventProcessor;
import org.prosolo.services.notifications.eventprocessing.NotificationEventProcessorFactory;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.services.user.ActiveUsersSessionRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class is an observer to the {@link CentralEventDispatcher} that is invoked whenever an event that is related to a notification occurs.  
 */
@Service("org.prosolo.services.notifications.NotificationObserver")
public class NotificationObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(NotificationObserver.class.getName());

	@Inject private ActiveUsersSessionRegistry activeUsersSessionRegistry;
	@Inject private DefaultManager defaultManager;
	@Inject private NotificationCacheUpdater notificationCacheUpdater;
	@Inject private SessionMessageDistributer messageDistributer;
	@Inject private AnalyticalServiceCollector analyticalServiceCollector;
	@Inject private NotificationEventProcessorFactory notificationEventProcessorFactory;
	@Inject private InterfaceSettingsManager interfaceSettingsManager;
	@Inject private NotificationManager notificationManager;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.Follow,
				EventType.ACTIVITY_REPORT_AVAILABLE,
				EventType.Comment,
				EventType.Comment_Reply,
				EventType.Like,
				EventType.Dislike,
				EventType.Post,
				EventType.AssessmentRequested,
				EventType.AssessmentApproved,
				EventType.AssessmentComment,
				EventType.AnnouncementPublished,
				EventType.GRADE_ADDED,
				EventType.ASSESSMENT_REQUEST_ACCEPTED,
				EventType.ASSESSMENT_REQUEST_DECLINED,
				EventType.ASSESSOR_WITHDREW_FROM_ASSESSMENT,
				EventType.ASSESSOR_ASSIGNED_TO_ASSESSMENT,
				EventType.ASSESSMENT_REQUEST_EXPIRED,
				EventType.ASSESSMENT_TOKENS_NUMBER_UPDATED
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	public void handleEvent(Event event) {
		try {
			NotificationEventProcessor processor = notificationEventProcessorFactory
					.getNotificationEventProcessor(event);
			if (processor != null) {
				List<Notification1> notifications = processor.getNotificationList();

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
							Set<HttpSession> userSessions = activeUsersSessionRegistry.getAllUserSessions(notification.getReceiver().getId());
							for (HttpSession httpSession : userSessions) {
								notificationCacheUpdater.updateNotificationData(notification.getId(), httpSession);
							}
						}
					 				
						if (notification.isNotifyByEmail() && CommonSettings.getInstance().config.emailNotifier.activated) {
							try {
								UserSettings userSettings = interfaceSettingsManager.
										getOrCreateUserSettings(notification.getReceiver().getId());
								Locale locale = getLocale(userSettings);
								/*
								 * get all notification data in one query instead of issuing session.update
								 * for sender and receiver - all in order to avoid lazy initialization exception
								 */
								NotificationData notificationData = notificationManager
										.getNotificationData(notification.getId(), true, locale);
								String domain = CommonSettings.getInstance().config.appConfig.domain;
								
								if (domain.endsWith("/")) {
									domain = domain.substring(0, domain.length() - 1);
								}
								
								final String urlPrefix = domain;
								taskExecutor.execute(() -> {
									Session session1 = (Session) defaultManager.getPersistence().openSession();
									try {
										String email = CommonSettings.getInstance().config.appConfig.projectMode.equals(AppConfig.ProjectMode.DEV) ? CommonSettings.getInstance().config.appConfig.developerEmail : notificationData.getReceiver().getEmail();
										logger.info("Sending notification via email to " + email);
										analyticalServiceCollector.storeNotificationData(email, notificationData);
										/*
										boolean sent = notificationManager.sendNotificationByEmail(
												email,
												notificationData.getReceiver().getFullName(),
												notificationData.getActor().getFullName(),
												notificationData.getPredicate(),
												notificationData.getObjectId(),
												notificationData.getObjectType(),
												notificationData.getObjectTitle(),
												urlPrefix + notificationData.getLink(),
												DateUtil.getTimeAgoFromNow(notificationData.getDate()),
												notificationData.getNotificationType(),
												session1);

										if (sent) {
											logger.info("Email notification to " + email + " is sent." + (CommonSettings.getInstance().config.appConfig.projectMode ? " Development mode is on" : ""));
										} else {
											logger.error("Error sending email notification to " + email);
										}*/
									} finally {
										HibernateUtil.close(session1);
									}
								});
							} catch (Exception e) {
								logger.error("error", e);
							}
						}
					}
				}
			} else {
				logger.debug("This notification is not supported by any notification processor." + event);
			}
		} catch (Exception e) {
			logger.error("Error", e);
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
