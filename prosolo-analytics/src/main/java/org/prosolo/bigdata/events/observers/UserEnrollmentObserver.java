package org.prosolo.bigdata.events.observers;/**
 * Created by zoran on 31/07/16.
 */

import org.prosolo.bigdata.dal.cassandra.UserRecommendationsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UserRecommendationsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.scala.recommendations.SimilarUsersBasedOnPreferences$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

import java.util.Set;

/**
 * zoran 31/07/16
 */
public class UserEnrollmentObserver  implements EventObserver {
    SimilarUsersBasedOnPreferences$ recommender=SimilarUsersBasedOnPreferences$.MODULE$;
    @Override
    public Topic[] getSupportedTopics() {
        return new Topic[]{Topic.LOGS};
    }

    @Override
    public EventType[] getSupportedTypes() {
         return new EventType[]{EventType.Registered,EventType.ENROLL_COURSE};
    }

    @Override
    public void handleEvent(DefaultEvent event) {
        LogEvent logEvent = (LogEvent) event;
        long userid=logEvent.getActorId();
        if(logEvent.getEventType().equals(EventType.Registered)) {

            long time=logEvent.getTimestamp();
            UserRecommendationsDBManagerImpl.getInstance().insertNewUser(userid,time);

        }else if (logEvent.getEventType().equals(EventType.ENROLL_COURSE)) {
            if(UserRecommendationsDBManagerImpl.getInstance().isStudentNew(userid)){
                System.out.println("NEW STUDENT. GET SOME RECOMMENDATIONS");
                Set<Long> credentials= UserObservationsDBManagerImpl.getInstance().findAllUserCourses(userid);
                Long cId=logEvent.getCourseId();
                credentials.add(cId);
                recommender.recommendBasedOnCredentialsForColdStart(userid, credentials);
            }
        }

    }
}
