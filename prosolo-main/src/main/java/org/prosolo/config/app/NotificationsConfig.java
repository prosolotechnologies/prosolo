package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class NotificationsConfig {
	
	@Element(name = "top-notifications-to-show")
	public int topNotificationsToShow;

}
