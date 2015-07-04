package org.prosolo.domainmodel.feeds;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.feeds.FeedsDigest;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
public class CourseTwitterHashtagsFeedsDigest extends FeedsDigest {
	
	private static final long serialVersionUID = 4202843002556600808L;
	
	private Course course;

	@OneToOne
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}
	
}
