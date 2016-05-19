/**
 * 
 */
package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.common.email.generators.EmailContentGenerator;

public class NotificationEmailContentGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "notification";
	}
	
	private String name;
	private String actor;
	private String predicate;
	private String objectTitle;
	private String date;
	private String link;
	
	public NotificationEmailContentGenerator(String name, String actor, String predicate,
			String objectTitle, String date, String link) {
		this.name = name;
		this.actor = actor;
		this.predicate = predicate;
		this.objectTitle = objectTitle;
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

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getObjectTitle() {
		return objectTitle;
	}

	public void setObjectTitle(String objectTitle) {
		this.objectTitle = objectTitle;
	}

}