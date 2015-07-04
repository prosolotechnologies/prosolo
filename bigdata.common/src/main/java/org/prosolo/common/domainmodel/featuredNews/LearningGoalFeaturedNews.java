package org.prosolo.common.domainmodel.featuredNews;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.featuredNews.FeaturedNews;

@Entity
public class LearningGoalFeaturedNews extends FeaturedNews {

	private static final long serialVersionUID = -598616576508805354L;

	private LearningGoal resource;
	
	private EventType action;
//	private Node object;
	
//	private Event event;

	@OneToOne(fetch = FetchType.LAZY)
	public LearningGoal getResource() {
		return resource;
	}

	public void setResource(LearningGoal resource) {
		this.resource = resource;
	}
	
	@Enumerated(EnumType.STRING)
	public EventType getAction() {
		return action;
	}

	public void setAction(EventType action) {
		this.action = action;
	}
	
//	@OneToOne(fetch = FetchType.LAZY)
//	public Node getObject() {
//		return object;
//	}
//
//	public void setObject(Node object) {
//		this.object = object;
//	}

//	@OneToOne(fetch = FetchType.LAZY)
//	public Event getEvent() {
//		return event;
//	}
//
//	public void setEvent(Event event) {
//		this.event = event;
//	}

}
