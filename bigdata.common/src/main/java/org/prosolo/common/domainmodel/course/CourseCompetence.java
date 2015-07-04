/**
 * 
 */
package org.prosolo.common.domainmodel.course;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.course.CourseCompetence;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class CourseCompetence extends BaseEntity {

	private static final long serialVersionUID = -7174401109898801518L;
	
	private long order;
	private long daysOffset;
	private long duration;
	private Competence competence;
	
	public CourseCompetence() {}
	
	public CourseCompetence(Competence competence) {
		this.competence = competence;
		this.duration = competence.getDuration();
	}
	
	public CourseCompetence(CourseCompetence courseCompetence) {
		this.order = courseCompetence.getOrder();
		this.daysOffset = courseCompetence.getDaysOffset();
		this.duration = courseCompetence.getDuration();
		this.competence = courseCompetence.getCompetence();
	}

	@Column(name = "rank")
	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}
	
	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDaysOffset() {
		return daysOffset;
	}

	public void setDaysOffset(long daysOffset) {
		this.daysOffset = daysOffset;
	}

	@OneToOne
	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}

}
