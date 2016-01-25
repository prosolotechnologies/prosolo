package org.prosolo.common.domainmodel.course;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class CourseInstructor {

	private long id;
	private int maxNumberOfStudents;
	private User user;
	private Course course;
	private Set<CourseEnrollment> assignedStudents;
	
	@Id
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getMaxNumberOfStudents() {
		return maxNumberOfStudents;
	}
	public void setMaxNumberOfStudents(int maxNumberOfStudents) {
		this.maxNumberOfStudents = maxNumberOfStudents;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="instructor")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public Set<CourseEnrollment> getAssignedStudents() {
		return assignedStudents;
	}
	public void setAssignedStudents(Set<CourseEnrollment> assignedStudents) {
		this.assignedStudents = assignedStudents;
	}
	
}
