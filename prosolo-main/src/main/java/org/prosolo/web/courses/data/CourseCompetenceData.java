/**
 * 
 */
package org.prosolo.web.courses.data;

import java.io.Serializable;

import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.course.CourseCompetence;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CourseCompetenceData implements Serializable {

	private static final long serialVersionUID = -3418809515385816296L;

	private long id;
	private long competenceId;
	private String title;
	private int validity;

	private long originalDaysOffset;
	private long originalDuration;

	private long averageDaysOffset;
	private long averageDuration;

	private long modifiedDaysOffset;
	private long modifiedDuration;
	
	private boolean saved;
	private boolean dataChanged;
	private CourseCompetence courseCompetence;
	
	public CourseCompetenceData(CourseCompetence courseCompetence) {
		this.id = courseCompetence.getId();
		
		Competence competence = courseCompetence.getCompetence();
		this.competenceId = competence.getId();
		this.title = competence.getTitle();
		this.validity = competence.getValidityPeriod();
		this.originalDaysOffset = courseCompetence.getDaysOffset();
		this.originalDuration = courseCompetence.getDuration();
		this.modifiedDaysOffset = courseCompetence.getDaysOffset();
		this.modifiedDuration = courseCompetence.getDuration();
		
		this.courseCompetence = courseCompetence;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public int getValidity() {
		return validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

	public long getOriginalDaysOffset() {
		return originalDaysOffset;
	}

	public void setOriginalDaysOffset(long originalDaysOffset) {
		this.originalDaysOffset = originalDaysOffset;
	}

	public long getOriginalDuration() {
		return originalDuration;
	}

	public void setOriginalDuration(long originalDuration) {
		this.originalDuration = originalDuration;
	}

	public long getAverageDaysOffset() {
		return averageDaysOffset;
	}

	public void setAverageDaysOffset(long averageDaysOffset) {
		this.averageDaysOffset = averageDaysOffset;
	}

	public long getAverageDuration() {
		return averageDuration;
	}

	public void setAverageDuration(long averageDuration) {
		this.averageDuration = averageDuration;
	}

	public long getModifiedDaysOffset() {
		return modifiedDaysOffset;
	}

	public void setModifiedDaysOffset(long modifiedDaysOffset) {
		this.modifiedDaysOffset = modifiedDaysOffset;
	}

	public long getModifiedDuration() {
		return modifiedDuration;
	}

	public void setModifiedDuration(long modifiedDuration) {
		this.modifiedDuration = modifiedDuration;
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	
	public boolean isDataChanged() {
		return dataChanged;
	}

	public void setDataChanged(boolean dataChanged) {
		this.dataChanged = dataChanged;
	}

	public CourseCompetence getCourseCompetence() {
		return courseCompetence;
	}

	public void setCourseCompetence(CourseCompetence courseCompetence) {
		this.courseCompetence = courseCompetence;
	}
	
}
