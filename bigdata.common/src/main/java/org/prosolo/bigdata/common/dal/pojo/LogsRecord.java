package org.prosolo.bigdata.common.dal.pojo;/**
 * Created by zoran on 10/10/16.
 */

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.LearningContext;

import java.io.Serializable;

/**
 * zoran 10/10/16
 */
public class LogsRecord implements Serializable {
        private Long date;
        private Long timestamp;
        private Long actorId;
        private String sessionId;
        private EventType eventType;
        private LearningContext learningContext;

        public LogsRecord(Long date, Long time, Long actorId, String sessionId, EventType eventType, LearningContext learningContext){
               setDate(date);
               setTimestamp(time);
                setActorId(actorId);
                setSessionId(sessionId);
                setEventType(eventType);
                setLearningContext(learningContext);
        }
        public Long getDate() {
                return date;
        }
        public void setDate(Long date) {
                this.date = date;
        }
        public Long getTimestamp() {
                return timestamp;
        }
        public void setTimestamp(Long timestamp) {
                this.timestamp = timestamp;
        }
        public Long getActorId() {
                return actorId;
        }
        public void setActorId(Long actorId) {
                this.actorId = actorId;
        }
        public String getSessionId() {
                return sessionId;
        }
        public void setSessionId(String sessionId) {
                this.sessionId = sessionId;
        }
        public EventType getEventType() {
                return eventType;
        }
        public void setEventType(EventType eventType) {
                this.eventType = eventType;
        }
        public LearningContext getLearningContext() {
                return learningContext;
        }
        public void setLearningContext(LearningContext learningContext) {
                this.learningContext = learningContext;
        }
}
