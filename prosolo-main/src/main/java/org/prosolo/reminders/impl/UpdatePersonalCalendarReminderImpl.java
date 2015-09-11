package org.prosolo.reminders.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activities.requests.RequestStatus;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.domainmodel.user.reminders.ReminderType;
import org.prosolo.common.domainmodel.user.reminders.RequestReminder;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.reminders.UpdatePersonalCalendarReminder;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.home.RemindersBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.reminders.UpdatePersonalCalendarReminder")
public class UpdatePersonalCalendarReminderImpl implements UpdatePersonalCalendarReminder {
	
	private Logger logger = Logger.getLogger(UpdatePersonalCalendarReminderImpl.class);
	
	@Autowired private DefaultManager defaultManager;
	@Autowired private PersonalCalendarManager personalCalendarQueries;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private PersonalCalendarManager calendarManager;
	
	private static Collection<EventType> eventsToBeProcessed = new ArrayList<EventType>();
 
	public UpdatePersonalCalendarReminderImpl(){
		eventsToBeProcessed.add(EventType.Create);
		eventsToBeProcessed.add(EventType.Edit);
		eventsToBeProcessed.add(EventType.Delete);
		eventsToBeProcessed.add(EventType.Completion);
		eventsToBeProcessed.add(EventType.EVALUATION_REQUEST);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Reminder getDeadlineReminderForResource(Event event) {
		Reminder reminder = null;
		if (event.getAction().equals(EventType.Edit) || event.getAction().equals(EventType.Completion) ||(event.getAction().equals(EventType.Delete))) {
			// load existing reminder
			BaseEntity resource = event.getObject();
			
			if (resource instanceof TargetLearningGoal) {
				resource = ((TargetLearningGoal) resource).getLearningGoal();
			}
			
			reminder = personalCalendarQueries.loadExistingReminder(resource, event.getActor(), ReminderType.DEADLINE);
		}
		if ((reminder == null) || (event.getAction().equals(EventType.Create))) {
			reminder = new Reminder();
			reminder.setReminderType(ReminderType.DEADLINE);
		}
		return reminder;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateReminder(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		 
		//Session session=persistence.openSession();
		try{
		if (checkEventTypesToProcess(event)) {
			User user = event.getActor();
			PersonalCalendar personalCalendar = null;
			Reminder reminder = null;
			HttpSession httpSession = applicationBean.getUserSession(user.getId());
			BaseEntity resource = event.getObject();
			
			RemindersBean remindersBean = null;
			if (httpSession != null) {
				remindersBean = (RemindersBean) httpSession.getAttribute("remindersBean");
			}

			if ((resource instanceof LearningGoal) && (((LearningGoal) resource).getDeadline() != null) || 
					(resource instanceof TargetLearningGoal) && (((TargetLearningGoal) resource).getLearningGoal().getDeadline() != null)) {
				
				Date deadline = null;
				
				if (resource instanceof LearningGoal) {
					deadline = ((LearningGoal) resource).getDeadline();
				} else {
					deadline = ((TargetLearningGoal) resource).getLearningGoal().getDeadline();
				}
				
				reminder = getDeadlineReminderForResource(event);
				
				personalCalendar = calendarManager.getOrCreateCalendar(user, session);
				
				if (event.getAction().equals(EventType.Delete)) {
					personalCalendar = (PersonalCalendar) session.merge(personalCalendar);

					personalCalendar.removeReminder(reminder);
					reminder = (Reminder) session.merge(reminder);
					session.delete(reminder);

					if (httpSession != null) {
						remindersBean.removeReminder((Node) resource);
					}
				} else if (event.getAction().equals(EventType.Completion)) {
					reminder.setReminderStatus(ReminderStatus.COMPLETED);
					
					if (httpSession != null) {
						remindersBean.resourceCompleted((Node) resource);
					}
				} else if (event.getAction().equals(EventType.Create)) {
					reminder.setDeadline(deadline);
					reminder.setResource((Node) resource);
					reminder.setReminderStatus(ReminderStatus.ACTIVE);
					session.save(reminder);
					personalCalendar.addReminder(reminder);
					
					if (httpSession != null) {
						remindersBean.addReminder(reminder);
					}
				} else if (event.getAction().equals(EventType.Edit)) {
					reminder.setDeadline(deadline);
					reminder.setResource((Node) resource);
					reminder.setReminderStatus(ReminderStatus.ACTIVE);
					session.saveOrUpdate(reminder);
					
					if (httpSession != null) {
						remindersBean.updateReminder(reminder);
					}
				}
			} else if (resource instanceof Request) {
				if (event.getAction().equals(EventType.EVALUATION_REQUEST)) {
					
					Request request = (Request) event.getObject();
					RequestStatus status = request.getStatus();
					
					User receiver = null;
					
					if (status.equals(RequestStatus.SENT)) {
						receiver = request.getSentTo();
						
						personalCalendar = calendarManager.getOrCreateCalendar(receiver, session);
							
						reminder = new RequestReminder();
						reminder.setReminderType(ReminderType.REQUEST);
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, 7);
						
						Date deadline = calendar.getTime();
						 
						reminder.setDeadline(deadline);
						((RequestReminder) reminder).setRequestResource(request);
				
						reminder.setReminderStatus(ReminderStatus.ACTIVE);
						session.save(reminder);
						//reminder = defaultManager.saveEntity(reminder);
						
						if (httpSession != null) {
							remindersBean.updateReminder(reminder);
						}
						personalCalendar.addReminder(reminder);
					} else {
						receiver = request.getMaker();
					}
				}
			}
			if (personalCalendar != null) {
				session.save(personalCalendar);
			}
		}
		 session.flush();
		}catch(Exception e){
			logger.error("Exception in handling message",e);
		}finally{
			HibernateUtil.close(session);
		}
	}
	
	private boolean checkEventTypesToProcess(Event event) {
		return eventsToBeProcessed.contains(event.getAction());
	}

}
