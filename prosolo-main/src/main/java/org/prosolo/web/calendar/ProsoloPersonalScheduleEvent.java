package org.prosolo.web.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;
import org.prosolo.common.web.activitywall.data.UserData;

public class ProsoloPersonalScheduleEvent extends ProsoloDefaultScheduleEvent {

	private static final long serialVersionUID = -705251141124940783L;
	
	private String description;
	
	private boolean guestsCanModify = false;
	
	public ProsoloPersonalScheduleEvent(String title, Date start, Date end, String styleClass) {
		super(title, start, end, styleClass);
		setGuestsList(new ArrayList<UserData>());
	}
	
	public ProsoloPersonalScheduleEvent(){
		setGuestsList(new ArrayList<UserData>());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isGuestsCanModify() {
		return guestsCanModify;
	}
	
	public void setGuestsCanModify(boolean guestsCanModify) {
		this.guestsCanModify = guestsCanModify;
	}
	
	public void setStartDate(Date date){
		super.setStartDate(date);
	}
	
	public void setEndDate(Date date){
		super.setEndDate(date);
	}
	
	public void setStartTime(Date date){
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int mins = calendar.get(Calendar.MINUTE);
		this.setStartDate(DateUtils.addHours(this.getStartDate(), hours));
		this.setStartDate(DateUtils.addMinutes(this.getStartDate(), mins));
	}
	
	public Date getStartTime(){
		return super.getStartDate();
	}
	
	public  void setEndTime(Date date){
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int mins = calendar.get(Calendar.MINUTE);
		this.setEndDate(DateUtils.addHours(this.getEndDate(), hours));
		this.setEndDate(DateUtils.addMinutes(this.getEndDate(), mins));
	}
	
	public Date getEndTime(){
		return super.getEndDate();
	}
	
}
