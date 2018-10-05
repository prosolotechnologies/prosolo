package org.prosolo.web.notification.data;

public class NotificationFilter {

	private NotificationFilterType type;
	private boolean applied;
	
	public NotificationFilter(NotificationFilterType type, boolean applied) {
		this.type = type;
		this.applied = applied;
	}

	public NotificationFilterType getType() {
		return type;
	}

	public void setType(NotificationFilterType type) {
		this.type = type;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}
	
}
