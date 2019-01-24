package org.prosolo.bigdata.events.observers;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.LogEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl;
import org.prosolo.bigdata.events.analyzers.ObservationType;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.scala.clustering.SNAEventsChecker$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Zoran Jeremic Apr 6, 2015
 */

public class LogEventsPersisterObserver implements EventObserver {
    private static Logger logger = Logger.getLogger(LogEventsPersisterObserver.class.getName());

    SNAEventsChecker$ eventsChecker = SNAEventsChecker$.MODULE$;

    @Override
    public Topic[] getSupportedTopics() {
        // TODO Auto-generated method stub
        return new Topic[]{Topic.LOGS};
    }

    @Override
    public EventType[] getSupportedTypes() {
        return null;
    }

    @Override
    public void handleEvent(DefaultEvent event) {
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;
            LogEventDBManagerImpl.getInstance().insertLogEvent(logEvent);
            Gson g = new Gson();
            logger.debug("HANDLING LOG EVENT:" + g.toJson(logEvent));
            if (logEvent.getTargetUserId() > 0) {
                Set<Long> courses = new HashSet<Long>();

                if (logEvent.getCourseId() == 0) {
                    LearningContext context = logEvent.getLearningContext();
                    Context credContext = context.getSubContextWithName(ContextName.CREDENTIAL);
                    if (credContext != null && credContext.getId() > 0) {
                        courses.add(credContext.getId());
                    } else {
                        Set<Long> actorCourses = UserObservationsDBManagerImpl.getInstance().findAllUserCourses(logEvent.getActorId());
                        courses.addAll(actorCourses);
                        Set<Long> targetUserCourses = UserObservationsDBManagerImpl.getInstance().findAllUserCourses(logEvent.getTargetUserId());
                        courses.addAll(targetUserCourses);
                    }

                } else {
                    courses.add(logEvent.getCourseId());
                }
                if (logEvent.getActorId() != logEvent.getTargetUserId()) {
                    long actorId = logEvent.getActorId();
                    long targetUserId = logEvent.getTargetUserId();
                    for (Long courseId : courses) {
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("course", courseId);
                        data.put("source", actorId);
                        data.put("target", targetUserId);
                        AnalyticalEventDBManagerImpl.getInstance().updateGenericCounter(DataName.SOCIALINTERACTIONCOUNT, data);
                        logger.debug("OBSERVED LOG EVENT:" + event.getEventType()
                                + " actor:" + logEvent.getActorId()
                                + " with Target UserID:" + logEvent.getTargetUserId()
                                + " course:" + logEvent.getCourseId()
                                + " inserted course:" + courseId);
                        if (eventsChecker.isEventObserved(logEvent)) {
                            ObservationType observationType = eventsChecker.getObservationType(logEvent);

                            SocialInteractionStatisticsDBManagerImpl.getInstance().updateToFromInteraction(courseId, actorId, targetUserId, observationType);
                        }
                    }

                }
            }
        }

    }

}
