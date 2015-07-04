package org.prosolo.domainmodel.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.CourseEnrollment;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;

@Entity
public class TargetLearningGoal extends Node { 

	private static final long serialVersionUID = 5954250326819273338L;

	private int progress;
	private boolean progressActivityDependent;
 	private Collection<TargetCompetence> targetCompetences;
	private Date completedDate;
	private boolean archived;
	
	private LearningGoal learningGoal;
 
	private CourseEnrollment courseEnrollment;

	public TargetLearningGoal() {
	 	this.targetCompetences = new ArrayList<TargetCompetence>();
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isProgressActivityDependent() {
		return progressActivityDependent;
	}

	public void setProgressActivityDependent(boolean progressActivityDependent) {
		this.progressActivityDependent = progressActivityDependent;
	}
	
	@OneToMany
	@Cascade({org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.REFRESH})
	@JoinTable(name="user_LearningGoal_TargetCompetence")
	public Collection<TargetCompetence> getTargetCompetences() {
		return targetCompetences;
	}

	public void setTargetCompetences(Collection<TargetCompetence> targetCompetences) {
		this.targetCompetences = targetCompetences;
	}
	public boolean addTargetCompetence(TargetCompetence targetCompetence) {
		if (null != targetCompetence) {
			if (!getTargetCompetences().contains(targetCompetence)) {
				return getTargetCompetences().add(targetCompetence);
			}
		}
		return false;
	}
	
	public void removeCompetence(TargetCompetence tComp) {
		if (tComp != null) {
			Iterator<TargetCompetence> compIterator = getTargetCompetences().iterator();
			
			while (compIterator.hasNext()) {
				TargetCompetence targetCompetence = (TargetCompetence) compIterator.next();
				
				if (targetCompetence.getId() == tComp.getId()) {
					compIterator.remove();
					break;
				}
			}
		}
	}

	@OneToOne(fetch=FetchType.LAZY)
	public CourseEnrollment getCourseEnrollment() {
		return courseEnrollment;
	}

	public void setCourseEnrollment(CourseEnrollment courseEnrollment) {
		this.courseEnrollment = courseEnrollment;
	}

	@OneToOne
	public LearningGoal getLearningGoal() {
		return learningGoal;
	}

	public void setLearningGoal(LearningGoal learningGoal) {
		this.learningGoal = learningGoal;
	}

	@Override
	@Transient
	public String getTitle() {
		if(learningGoal!=null){
		return learningGoal.getTitle();
		}else{
			return null;
		}
	}
	
	@Override
	@Transient
	public String getDescription() {
		if(learningGoal!=null){
			return learningGoal.getDescription();
		}else{
			return null;
		}
		
	}

	public Date getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (archived ? 1231 : 1237);
		result = prime * result
				+ ((completedDate == null) ? 0 : completedDate.hashCode());
		result = prime
				* result
				+ ((courseEnrollment == null) ? 0 : courseEnrollment.hashCode());
		result = prime * result
				+ ((learningGoal == null) ? 0 : learningGoal.hashCode());
		result = prime * result + progress;
		result = prime * result + (progressActivityDependent ? 1231 : 1237);
		result = prime
				* result
				+ ((targetCompetences == null) ? 0 : targetCompetences
						.hashCode());
		return result;
	}

}