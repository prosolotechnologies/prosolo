/**
 * 
 */
package org.prosolo.web.courses.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.web.courses.data.CourseCompetenceData;
import org.prosolo.web.courses.data.CourseData;

/**
 * @author "Nikola Milikic"
 *
 */
public class CourseDataConverter {

	public static List<CourseData> convertToCoursesData(Collection<Course> courses) {
		List<CourseData> coursesData = new ArrayList<CourseData>();
		
		if (courses != null && !courses.isEmpty()) {
			for (Course obj : courses) {
				CourseData objData = new CourseData(obj);
				coursesData.add(objData);
			}
		}
		return coursesData;
	}
	
	public static List<CourseCompetenceData> convertToCompetenceCourseData(List<CourseCompetence> competences, boolean saved) {
		List<CourseCompetenceData> courseCompetencesData = new ArrayList<CourseCompetenceData>();

		if (competences != null) {
			for (CourseCompetence courseCompetence : competences) {
				CourseCompetenceData ccData = new CourseCompetenceData(courseCompetence);
				ccData.setSaved(saved);
				courseCompetencesData.add(ccData);
			}
		}
		
		return courseCompetencesData;
	}
	
	public static List<Long> getIdsOfCourseCompetences(List<CourseCompetenceData> courseCompetencesData) {
		if (courseCompetencesData != null) {
			ArrayList<Long> ids = new ArrayList<Long>();
			
			for (int i = 0; i < courseCompetencesData.size(); i++) {
				ids.add(courseCompetencesData.get(i).getCompetenceId());
			}
			return ids;
		}
		return null;
	}

	public static List<CourseData> convertToCoursesData(List<CourseEnrollment> enrollments) {
		List<CourseData> coursesData = new ArrayList<CourseData>();

		if (enrollments != null) {
			for (CourseEnrollment en : enrollments) {
				CourseData ccData = new CourseData(en);
				coursesData.add(ccData);
			}
		}
		
		return coursesData;
	}
}
