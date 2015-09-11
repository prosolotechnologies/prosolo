/**
 * 
 */
package org.prosolo.common.domainmodel.course;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.Status;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class CourseEnrollment extends BaseEntity {

	private static final long serialVersionUID = 8057373032718040137L;

	private boolean active;
	private Date dateStarted;
	private Date dateFinished;
	private Course course;
	private User user;
	private TargetLearningGoal targetGoal;
	private Status status;
	
	private List<CourseCompetence> addedCompetences;
	
	public CourseEnrollment() {
		addedCompetences = new ArrayList<CourseCompetence>();
	}
	
	@Type(type="true_false")
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Date dateFinished) {
		this.dateFinished = dateFinished;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public TargetLearningGoal getTargetGoal() {
		return targetGoal;
	}

	public void setTargetGoal(TargetLearningGoal targetGoal) {
		this.targetGoal = targetGoal;
	}

	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@ManyToMany
	@Cascade({org.hibernate.annotations.CascadeType.MERGE})
	@JoinTable(name = "course_enrollment_added_competences_course_competence")
	public List<CourseCompetence> getAddedCompetences() {
		return addedCompetences;
	}

	public void setAddedCompetences(List<CourseCompetence> addedCompetences) {
		this.addedCompetences = addedCompetences;
	}
	
	public boolean addCompetence(CourseCompetence courseCompetence) {
		if (courseCompetence != null) {
			if (!addedCompetences.contains(courseCompetence)) {
				return addedCompetences.add(courseCompetence);
			}
		}
		return false;
	}
	
	public boolean removeCompetence(CourseCompetence courseCompetenceToRemove) {
		Iterator<CourseCompetence> iterator = addedCompetences.iterator();
		
		while (iterator.hasNext()) {
			CourseCompetence courseComp = (CourseCompetence) iterator.next();
			
			if (courseComp.getId() == courseCompetenceToRemove.getId()) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
}
