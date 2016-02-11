package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.CourseCompetence;

public class CompetenceData {

	private long courseCompetenceId;
	private long competenceId;
	private String title;
	private long order;
	private ObjectStatus status;
	
	public CompetenceData() {
		
	}

	public CompetenceData(long courseCompetenceId, long competenceId, String title, long order, 
			ObjectStatus status) {
		this.courseCompetenceId = courseCompetenceId;
		this.competenceId = competenceId;
		this.title = title;
		this.order = order;
		this.status = status;
	}


	public CompetenceData(Competence competence) {
		this.competenceId = competence.getId();
		this.title = competence.getTitle();
		this.status = ObjectStatus.CREATED;
	}
	
	public CompetenceData(CourseCompetence competence) {
		this.courseCompetenceId = competence.getId();
		this.competenceId = competence.getCompetence().getId();
		this.title = competence.getCompetence().getTitle();
		this.order = competence.getOrder();
		this.status = ObjectStatus.UP_TO_DATE;
	}

	public long getCourseCompetenceId() {
		return courseCompetenceId;
	}

	public void setCourseCompetenceId(long courseCompetenceId) {
		this.courseCompetenceId = courseCompetenceId;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}

	public ObjectStatus getStatus() {
		return status;
	}

	public void setStatus(ObjectStatus status) {
		this.status = status;
	}
	
}
