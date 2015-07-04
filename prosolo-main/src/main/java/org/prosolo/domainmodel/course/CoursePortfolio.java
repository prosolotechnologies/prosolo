/**
 * 
 */
package org.prosolo.domainmodel.course;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.prosolo.domainmodel.course.CourseEnrollment;
import org.prosolo.domainmodel.course.Status;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
//@Table(name = "portfolio_CoursePortfolio")
public class CoursePortfolio extends BaseEntity {

	private static final long serialVersionUID = -1349795508576060440L;

	private User user;
	private Set<CourseEnrollment> enrollments;
	
	public CoursePortfolio() {
		enrollments = new HashSet<CourseEnrollment>();
	}

	@OneToOne
	@Cascade(CascadeType.MERGE)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@OneToMany
	@Cascade(org.hibernate.annotations.CascadeType.MERGE)
	@JoinTable(name = "course_portfolio_enrollments_course_enrollment")
	public Set<CourseEnrollment> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(Set<CourseEnrollment> enrollments) {
		this.enrollments = enrollments;
	}
	
	public boolean addEnrollment(CourseEnrollment enrollment) {
		if (enrollments != null && !enrollments.contains(enrollment)) {
			return enrollments.add(enrollment);
		}
		return false;
	}
	
	public List<CourseEnrollment> findEnrollment(Status status) {
		List<CourseEnrollment> en = new ArrayList<CourseEnrollment>();
		
		for (CourseEnrollment courseEnrollment : enrollments) {
			if (courseEnrollment.getStatus().equals(status)) {
				en.add(courseEnrollment);
			}
		}
		
		return en;
	}

	public CourseEnrollment removeEnrollment(long enrollmentId) {
		if (enrollmentId > 0 && enrollments != null) {
			Iterator<CourseEnrollment> iterator = enrollments.iterator();
			
			while (iterator.hasNext()) {
				CourseEnrollment courseEnrollment = (CourseEnrollment) iterator.next();
				
				if (courseEnrollment.getId() == enrollmentId) {
					iterator.remove();
					return courseEnrollment;
				}
			}
		}
		return null;
	}
}
