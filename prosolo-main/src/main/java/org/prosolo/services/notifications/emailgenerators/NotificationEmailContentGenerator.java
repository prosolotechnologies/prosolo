/**
 * 
 */
package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.common.email.generators.EmailContentGenerator;
import org.prosolo.common.email.generators.MoustacheUtil;

/**
 * @author "Nikola Milikic"
 *
 */
public class NotificationEmailContentGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "notification";
	}
	
	private String name;
	private String actor;
	private String notificationType;
	private String notificationShortType;
	private String resourceTitle;
	private String message;
	private String date;
	private String link;
	
	public NotificationEmailContentGenerator(String name, String actor, String notificationType, 
			String notificationShortType, String resourceTitle,
			String message, String date, String link) {
		this.name = name;
		this.actor = actor;
		this.notificationType = notificationType;
		this.notificationShortType = notificationShortType;
		this.resourceTitle = resourceTitle;
		this.message = message;
		this.date = date;
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public String getNotificationShortType() {
		return notificationShortType;
	}

	public void setNotificationShortType(String notificationShortType) {
		this.notificationShortType = notificationShortType;
	}

	public String getResourceTitle() {
		return resourceTitle;
	}

	public void setResourceTitle(String resourceTitle) {
		this.resourceTitle = resourceTitle;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	
	
	

}