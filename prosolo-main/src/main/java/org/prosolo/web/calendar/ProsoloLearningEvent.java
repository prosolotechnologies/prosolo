package org.prosolo.web.calendar;

import java.util.Date;

import org.prosolo.domainmodel.general.Node;

public class ProsoloLearningEvent extends ProsoloDefaultScheduleEvent {
	
	private static final long serialVersionUID = -3171126957733710893L;

	private Node resource;

	public ProsoloLearningEvent() { }

	public ProsoloLearningEvent(String title, Date start, Date end,	String styleClass) {
		super(title, start, end, styleClass);
	}
	
	public ProsoloLearningEvent(Node resource, String title, Date start, Date end) {
		super(title, start, end);
		this.resource = resource;
	}

	public Node getResource() {
		return resource;
	}

	public void setResource(Node resource) {
		this.resource = resource;
	}

	public void setStyleClass(String styleClass) {
		super.setStyleClass(styleClass);
	}

}
