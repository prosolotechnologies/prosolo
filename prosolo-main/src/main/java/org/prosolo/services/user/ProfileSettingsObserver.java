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
import org.prosolo.util.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Random;

/**
 * @author Nikola Milikic
 * @date 2019-04-09
 * @since 1.3.1
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

            User user = (User) session.get(User.class, userId);

            // remove all non alphabetic characters from the name
            String name = StringUtils.stripNonAlphanumericCharacters(user.getName());

            // remove all non alphabetic characters from the last name
            String lastname = StringUtils.stripNonAlphanumericCharacters(user.getLastname());

            String nameLastName = name + "-" + lastname;

            // limit {name}-{lastName} combination to 40 characters
            if (nameLastName.length() > 40) {
                nameLastName = nameLastName.substring(0, 40);
            }

            // if profile URL is already occupied, try 10 more time with a different profile URL
            int retryTimes = 0;
            long suffix = userId;

            while (retryTimes < 10) {
                String newProfileUrl = null;
                try {
                    newProfileUrl = nameLastName + "-" + idEncoder.encodeId(suffix);
                    studentProfileManager.createProfileSettings(user, newProfileUrl, true, session);
                    break;
                } catch (DataIntegrityViolationException e) {
                    logger.debug("Error saving ProfileSettings for the user " + userId + ", the profile URL is already taken: " + newProfileUrl, e);
                }

                // as a new suffix use a generated long between 10000 and 100000
                suffix = 10000 + (int) (new Random().nextFloat() * (100000 - 1));
                retryTimes++;
            }

            if (retryTimes == 10) {
                logger.error("Error saving ProfileSettings for the user " + userId + ", the original profile URL and 10 variations of the same are already occupied.");
            }

            transaction.commit();
        } catch (Exception e) {
            logger.error("Error", e);
            transaction.rollback();
        } finally {
            HibernateUtil.close(session);
        }
    }
}
