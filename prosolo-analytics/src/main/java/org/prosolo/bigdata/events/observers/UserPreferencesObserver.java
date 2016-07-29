package org.prosolo.bigdata.events.observers;/**
 * Created by zoran on 25/07/16.
 */

import com.google.gson.Gson;
import org.prosolo.bigdata.dal.cassandra.impl.UserRecommendationsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.scala.clustering.ProfileEventsChecker$;
import org.prosolo.bigdata.scala.clustering.StudentPreferenceChecker$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.util.date.DateUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

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
            System.out.println("EVENT IS OBSERVED");
            Stack<Context> contextsStack=new Stack();
            Double weight=eventsChecker.getEventWeight(logEvent);
            LearningContext learningContext=logEvent.getLearningContext();
            if(learningContext!=null){
                Context context=learningContext.getContext();
                contextsStack.push(context);
                System.out.println("ADDING FIRST CONTEXT:"+gson.toJson(context));
                while(context.getContext()!=null){
                    context=context.getContext();
                    contextsStack.push(context);
                    System.out.println("ADDING CONTEXT:"+gson.toJson(context));

                }
                System.out.println("STACK:"+contextsStack.size());

              // Iterator<Context> iter=contextsStack.iterator();
                Double increment=1.0;
                while(!contextsStack.empty()){
                    Context currContext=contextsStack.pop();
                    if(eventsChecker.isObjectTypeObserved(currContext.getObjectType())){
                        System.out.println("Should store preference here for:"+currContext.getName()+" id:"+currContext.getId()+" increment:"+increment+" weight:"+weight+" Actor:"+logEvent.getActorId());
                      if(currContext.getId()!=null){
                          long dateEpoch= DateUtil.getDaysSinceEpoch();
                          Double prevPreferences=UserRecommendationsDBManagerImpl.getInstance().getStudentPreferenceForDate(logEvent.getActorId(),currContext.getObjectType(),currContext.getId(),dateEpoch);
                          System.out.println("PREVIOUS PREFERENCE:"+prevPreferences+" SHOULD BE NOW:"+(increment*weight+prevPreferences));
                          UserRecommendationsDBManagerImpl.getInstance()
                                  .insertStudentPreferenceForDate(logEvent.getActorId(),currContext.getObjectType(),currContext.getId(),increment*weight+prevPreferences,dateEpoch);
                      }

                    }
                    increment=increment/2;
                }
            }
        }
    }
}
