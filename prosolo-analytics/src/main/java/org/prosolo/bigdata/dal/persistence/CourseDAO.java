package org.prosolo.bigdata.dal.persistence;

import java.util.List;

public interface CourseDAO {

	List<Long> getAllCourseIds();
	
	String getCourseTitle(long courseId);

}