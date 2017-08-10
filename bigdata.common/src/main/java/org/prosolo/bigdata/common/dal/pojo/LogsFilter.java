package org.prosolo.bigdata.common.dal.pojo;/**
 * Created by zoran on 09/10/16.
 */

import org.prosolo.common.domainmodel.activities.events.EventType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * zoran 09/10/16
 */
public class LogsFilter implements Serializable{
    private List<Long> credentials=new ArrayList();
    private List<Long> competences=new ArrayList();
    private List<Long> activities=new ArrayList();
    private List<Long> students=new ArrayList();
    private List<EventType> eventTypes=new ArrayList();
    private Long afterDate=0l;
    private Long beforeDate=0l;
    private int from=0;
    private int size=10;

    public int getFrom() {     return from;    }
    public void setFrom(int from) {        this.from = from;    }
    public int getSize() {        return size;    }
    public void setSize(int size) {        this.size = size;    }
    public List<Long> getCredentials() {return credentials; }
    public void setCredentials(List<Long> credentials) {    this.credentials = credentials;    }
    public void addCredential(Long credential1){this.credentials.add(credential1);}
    public List<Long> getCompetences() {        return competences;    }
    public void setCompetences(List<Long> competences) {        this.competences = competences;    }
    public void addCompetence(Long competence1){this.competences.add(competence1);}
    public List<Long> getActivities() {        return activities;    }
    public void setActivities(List<Long> activities) {        this.activities = activities;    }
    public void addActivity(Long activity){this.activities.add(activity);}
    public List<Long> getStudents() {        return students;    }
    public void setStudents(List<Long> students) {        this.students = students;    }
    public void addStudent(Long student){this.students.add(student);}
    public List<EventType> getEventTypes() {        return eventTypes;    }
    public void setEventTypes(List<EventType> eventTypes) {        this.eventTypes = eventTypes;    }
    public void addEventType(EventType eventType){this.eventTypes.add(eventType);}
    public Long getAfterDate() {return afterDate;    }
    public void setAfterDate(Long afterDate) {        this.afterDate = afterDate;    }
    public Long getBeforeDate() {return beforeDate;    }
    public void setBeforeDate(Long beforeDate) { this.beforeDate = beforeDate;    }
}
