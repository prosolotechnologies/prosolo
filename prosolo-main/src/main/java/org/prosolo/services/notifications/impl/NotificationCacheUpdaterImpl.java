package org.prosolo.services.notifications.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.services.notifications.NotificationCacheUpdater;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.notification.ManagerNotificationsBean;
import org.prosolo.web.notification.StudentNotificationsBean;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Nikola Milikic
 * @version 0.5
 */
@Service("org.prosolo.services.notifications.NotificationCacheUpdater")
public class NotificationCacheUpdaterImpl implements NotificationCacheUpdater, Serializable {

    private Logger logger = Logger.getLogger(NotificationCacheUpdaterImpl.class);

    private static final long serialVersionUID = -2234740595717219169L;

    @Inject
    private NotificationManager notificationManager;
    @Inject
    private AuthenticatedUserService authenticatedUserService;

    @Override
    public void updateNotificationData(long notificationId, HttpSession userSession) throws ResourceCouldNotBeLoadedException {
        if (userSession != null) {
            StudentNotificationsBean studentNotificationsBean = (StudentNotificationsBean) userSession.getAttribute("studentNotificationsBean");
            ManagerNotificationsBean managerNotificationsBean = (ManagerNotificationsBean) userSession.getAttribute("managerNotificationsBean");
            Optional<ProsoloUserDetails> prosoloUserDetails = authenticatedUserService.getUserDetailsFromSession(userSession);
            Locale locale = prosoloUserDetails.isPresent() ? prosoloUserDetails.get().getLocale() : new Locale("en", "US");

            if (studentNotificationsBean != null || managerNotificationsBean != null) {
                NotificationData notificationData = notificationManager.getNotificationData(notificationId, false, locale);

                switch (notificationData.getSection()) {
                    case STUDENT:
                        if (studentNotificationsBean != null) {
                            try {
                                studentNotificationsBean.addNotification(notificationData);
                            } catch (DbConnectionException e) {
                                logger.error(e);
                            }
                        }
                        break;
                    case MANAGE:
                        if (managerNotificationsBean != null) {
                            try {
                                managerNotificationsBean.addNotification(notificationData);
                            } catch (DbConnectionException e) {
                                logger.error(e);
                            }
                        }
                        break;
                }
            }

        }
    }

}
