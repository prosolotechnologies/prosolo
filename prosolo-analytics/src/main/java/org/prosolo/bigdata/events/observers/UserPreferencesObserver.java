package org.prosolo.bigdata.events.observers;/**
 * Created by zoran on 25/07/16.
 */

import com.google.gson.Gson;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.scala.clustering.ProfileEventsChecker$;
import org.prosolo.bigdata.scala.clustering.StudentPreferenceChecker$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

import java.util.List;

/**
 * zoran 25/07/16
 */
public class UserPreferencesObserver  implements EventObserver{

    StudentPreferenceChecker$ eventsChecker=StudentPreferenceChecker$.MODULE$;
    EventType[] supportedTypes=null;
    @Override
    public Topic[] getSupportedTopics() {
        return new Topic[0];
    }

    @Override
    public EventType[] getSupportedTypes() {
        if(supportedTypes==null){
            List<EventType> supportedTypesList=eventsChecker.getSupportedEventTypes();
            supportedTypes=supportedTypesList.toArray(new EventType[supportedTypesList.size()]);
        }
        return supportedTypes;
    }

    @Override
    public void handleEvent(DefaultEvent event) {
        Gson gson=new Gson();
        LogEvent logEvent=(LogEvent) event;
        System.out.println("USER PREFERENCE HANDLING EVENT:"+gson.toJson(event));
        if(eventsChecker.isEventObserved(logEvent)){
            Double weight=eventsChecker.getEventWeight(logEvent);
        }
    }
}
