/**
 * 
 */
package org.prosolo.web.calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.primefaces.model.DefaultScheduleEvent;
import org.prosolo.web.activitywall.data.UserData;

/**
 * @author "Nikola Milikic"
 *
 */
public abstract class ProsoloDefaultScheduleEvent extends DefaultScheduleEvent {

	private static final long serialVersionUID = 9087147861907566380L;

	private CalendarEventType type;
	private List<UserData> guestsList;

	public ProsoloDefaultScheduleEvent() {
		super();
	}
	
	/**
	 * @param title
	 * @param start
	 * @param end
	 * @param styleClass
	 */
	public ProsoloDefaultScheduleEvent(String title, Date start, Date end,
			String styleClass) {
		super(title, start, end, styleClass);
	}

	/**
	 * @param title
	 * @param start
	 * @param end
	 */
	public ProsoloDefaultScheduleEvent(String title, Date start, Date end) {
		super(title, start, end);
	}
	
	public List<UserData> getGuestsList() {
		return guestsList;
	}

	public void setGuestsList(List<UserData> guestsList) {
		this.guestsList = guestsList;
	}
	
	public void addGuest (UserData userData) {
		if (userData != null)
			guestsList.add(userData);
	}
	
	public void removeGuest(UserData userData) {
		if (userData != null) {
			Iterator<UserData> iterator = this.guestsList.iterator();
			
			while (iterator.hasNext()) {
				UserData u = (UserData) iterator.next();
				
				if (u.equals(userData)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	public CalendarEventType getType() {
		return type;
	}

	public void setType(CalendarEventType type) {
		this.type = type;
	}
	
}
