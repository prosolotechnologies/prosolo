/**
 * 
 */
package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.common.email.generators.EmailContentGenerator;


public abstract class NotificationEmailGenerator extends EmailContentGenerator {
	
	private String name;
	private String actor;
	private String predicate;
	private String objectTitle;
	private String date;
	private String link;
	
	public NotificationEmailGenerator(String name, String actor, String predicate,
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

	public String getActor() {
		return actor;
	}

	public String getDate() {
		return date;
	}

	public String getLink() {
		return link;
	}

	public String getPredicate() {
		return predicate;
	}

	public String getObjectTitle() {
		return objectTitle;
	}

}