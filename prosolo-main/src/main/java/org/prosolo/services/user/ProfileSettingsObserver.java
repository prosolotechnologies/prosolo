package org.prosolo.services.user;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.profile.ProfileSettingsData;
import org.prosolo.util.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Random;

/**
 * @author Nikola Milikic
 * @date 2019-04-09
 * @since 1.3.2
 */
@Service("org.prosolo.services.user.ProfileSettingsObserver")
public class ProfileSettingsObserver extends EventObserver {

    private static Logger logger = Logger.getLogger(ProfileSettingsObserver.class.getName());

    @Inject private StudentProfileManager studentProfileManager;
    @Inject private UrlIdEncoder idEncoder;

    @Override
    public EventType[] getSupportedEvents() {
        return new EventType[]{
                EventType.Registered
        };
    }

    @Override
    public Class<? extends BaseEntity>[] getResourceClasses() {
        return null;
    }

    @Override
    public void handleEvent(Event event) {
        long userId = event.getActorId();

        Session session = (Session) studentProfileManager.getPersistence().openSession();

        logger.info("Activating ProfileSettingsObserver - creating new ProfileSettings instance for the user " + userId);

        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            studentProfileManager.generateProfileSettings(userId, true, session);

            transaction.commit();
        } catch (Exception e) {
            logger.error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session);
        }
    }
}
