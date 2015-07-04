package org.prosolo.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.course.CourseEnrollment;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;

@Entity
public class CourseSocialActivity extends SocialActivity {

	private static final long serialVersionUID = -4862078826831751691L;

	private CourseEnrollment courseEnrollmentObject;
	
	private Node nodeTarget;
	
	@Override
	@Transient
	public BaseEntity getObject() {
		return courseEnrollmentObject;
	}

	@Override
	public void setObject(BaseEntity object) {
		this.courseEnrollmentObject = (CourseEnrollment) object;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public CourseEnrollment getCourseEnrollmentObject() {
		return courseEnrollmentObject;
	}

	public void setCourseEnrollmentObject(CourseEnrollment courseEnrollmentObject) {
		this.courseEnrollmentObject = courseEnrollmentObject;
	}

	@Override
	public void setTarget(BaseEntity object) {
		this.nodeTarget = (Node) object;
	}

	@Override
	@Transient
	public BaseEntity getTarget() {
		return nodeTarget;
	}

	@OneToOne
	public Node getNodeTarget() {
		return nodeTarget;
	}

	public void setNodeTarget(Node target) {
		this.nodeTarget = target;
	}

}
