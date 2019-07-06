package org.prosolo.services.activityWall.observer;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.observer.factory.SocialActivityFactory;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("org.prosolo.services.activitywall.SocialStreamObserver")
public class SocialStreamObserver extends EventObserver {

    private static Logger logger = Logger.getLogger(SocialStreamObserver.class.getName());

    @Autowired
    private SocialActivityFactory socialActivityFactory;
    @Autowired
    private DefaultManager defaultManager;

    private List<ExcludedEventResource> excluded;

    public SocialStreamObserver() {
        excluded = new ArrayList<>();
    }

    public EventType[] getSupportedEvents() {
        return new EventType[]{
                //TODO for now we do not create social activities for competency and activity comments. This should be rethinked.
                //EventType.Comment,
                EventType.Post,
                EventType.Completion,
                EventType.Edit,
                EventType.Create
        };
    }

    @SuppressWarnings("unchecked")
    public Class<? extends BaseEntity>[] getResourceClasses() {
        return new Class[]{
                null, // when event does not have object, e.g. Edit_Profile
                Credential1.class,
                Activity1.class,
                //TargetActivity1.class,
                //TODO for now we do not create social activities for competency and activity comments. This should be rethinked.
                //Comment1.class
                Unit.class
        };
    }

    @Override
    public void handleEvent(Event event) {
        for (ExcludedEventResource ex : excluded) {
            if (event.getObject().getClass().equals(ex.getClazz()) &&
                    event.getAction().equals(ex.getEvent())) {
                return;
            }
        }

        Session session = (Session) defaultManager.getPersistence().openSession();
        session.setFlushMode(FlushMode.COMMIT);

        try {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                socialActivityFactory.createOrDeleteSocialActivity(event, session);
                transaction.commit();
            } catch (Exception e) {
                logger.error("Error", e);
                transaction.rollback();
            }
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    class ExcludedEventResource {
        private Class<? extends BaseEntity> clazz;
        private EventType event;

        public ExcludedEventResource(Class<? extends BaseEntity> clazz, EventType event) {
            this.clazz = clazz;
            this.event = event;
        }

        public Class<? extends BaseEntity> getClazz() {
            return clazz;
        }

        public EventType getEvent() {
            return event;
        }
    }

}
