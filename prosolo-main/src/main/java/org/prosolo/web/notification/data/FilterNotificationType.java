package org.prosolo.web.notification.data;

public class FilterNotificationType {

	private NotificationTypeFilter filter;
	private boolean applied;
	
	public FilterNotificationType() {
		
	}
	
	public FilterNotificationType(NotificationTypeFilter filter, boolean applied) {
		this.filter = filter;
		this.applied = applied;
	}

	public NotificationTypeFilter getFilter() {
		return filter;
	}

	public void setFilter(NotificationTypeFilter filter) {
		this.filter = filter;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}
	
}
