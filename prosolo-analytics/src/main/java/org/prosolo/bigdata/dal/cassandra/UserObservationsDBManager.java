package org.prosolo.bigdata.dal.cassandra;

import java.util.Set;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.UserProfileFeatures;
import org.prosolo.bigdata.events.analyzers.ObservationType;

import com.datastax.driver.core.Row;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public interface UserObservationsDBManager {

	boolean updateUserObservationsCounter(Long date, Long userid, long login,
			long lmsuse, long resourceview, long discussionview);

	boolean updateUserProfileActionsObservationCounter(Long date, Long userid, Long courseid,
													   ObservationType observationType);

	List<Row> findAllUsersObservationsForDate(Long date);

	//boolean updateUserProfileActionsObservationCounter(Long date, Long userid, ObservationType observationType);

	List<Row> findAllUsersProfileObservationsForDate(Long date, Long courseId);

	void insertUserQuartileFeaturesByProfile(Long courseid, String profile, Long date, Long userid, String sequence);

	void insertUserQuartileFeaturesByDate(Long courseid, Long date, Long userid, String profile, String sequence);

	void updateUserCurrentProfile(Long courseid, Long userid, String profile,String profilefullname,  List<String> sequence);

	UserProfileFeatures findUserCurrentProfileInCourse(Long courseId, Long userId);

	List<Row> findAllUserQuartileFeaturesForCourse(Long courseId);

	//List<Row> findAllUserQuartileFeaturesForCourseAndWeek(Long courseId, Long date);

	List<Row> findAllUserQuartileFeaturesForCourseAndProfile(Long courseId, String profile);

	List<Row> findAllUserQuartileFeaturesForCourseProfileAndWeek(Long courseId, String profile, Long date);
	List<Row> findAllUserQuartileFeaturesForCourseDate(Long  courseId, Long  endDateSinceEpoch);

	Set<Long> findAllUserCourses(Long userId);

	void enrollUserToCourse(Long userId, Long courseId);

	void withdrawUserFromCourse(Long userId, Long courseId);
}
