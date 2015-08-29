package org.prosolo.web.dialogs;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.rest.courses.CourseParser;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="courseDialog")
@Component("courseDialog")
@Scope("view")
public class CourseDialogBean implements Serializable {
	
	private static final long serialVersionUID = -7268733340232310892L;

	protected static Logger logger = Logger.getLogger(CourseDialogBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CourseManager courseManager;
	
	private CourseData courseData;
	private Course course;
	
	private String competencesJson = "";
	private String changeHolder;
	private CourseCompetence selectedCourseCompetence;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public void openCourseDialog(Course course) {
		this.courseData = new CourseData(course);
	}
	
	public void openCourseDialogById(long courseId) {
		try {
			Course course = courseManager.loadResource(Course.class, courseId);
			
			this.courseData = new CourseData(course);
//			serializeCompetencesToJson();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void serializeCompetencesToJson() {
		if (courseData != null)
			this.competencesJson = CourseParser.parseCourseCompetence(
					courseData.getAddedCompetences(), 
					courseData.getOriginalCompetences(),
					true).toString();
	}

	/*
	 * GETTERS / SETTERS
	 */
	public CourseData getCourseData() {
		return courseData;
	}

	public void setCourseData(CourseData courseData) {
		this.courseData = courseData;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getCompetencesJson() {
		return competencesJson;
	}

	public void setCompetencesJson(String competencesJson) {
		this.competencesJson = competencesJson;
	}

	public String getChangeHolder() {
		return changeHolder;
	}

	public void setChangeHolder(String changeHolder) {
		this.changeHolder = changeHolder;
	}

	public CourseCompetence getSelectedCourseCompetence() {
		return selectedCourseCompetence;
	}

	public void setSelectedCourseCompetence(
			CourseCompetence selectedCourseCompetence) {
		this.selectedCourseCompetence = selectedCourseCompetence;
	}
	
}
