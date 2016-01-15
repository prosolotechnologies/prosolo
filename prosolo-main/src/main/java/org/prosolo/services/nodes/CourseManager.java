package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.rest.courses.data.CompetenceJsonData;
import org.prosolo.web.courses.data.CourseCompetenceData;
import org.springframework.transaction.annotation.Transactional;

public interface CourseManager extends AbstractManager {

	 Long findCourseIdForTargetCompetence(Long targetCompetenceId);

	@Transactional
	Long findCourseIdForTargetLearningGoal(Long targetGoalId);

	Long findCourseIdForTargetActivity(Long targetCompetenceId);

	Course updateCompetencesAndSaveNewCourse(String title, String description,
											 Course basedOn, List<CourseCompetenceData> courseCompetences,
											 Collection<Tag> tags, Collection<Tag> hashtags, User maker,
											 CreatorType creatorType, boolean studentsCanAddNewCompetences,
											 boolean pubilshed) throws EventException;
	
	Course saveNewCourse(String title, String description, 
			Course basedOn, List<CourseCompetence> courseCompetences, 
			Set<Tag> tags, Set<Tag> hashtags, User maker, 
			CreatorType creatorType, boolean studentsCanAddNewCompetences, 
			boolean published) throws EventException;
	
	Course updateCourse(Course course, String title, String description,
			List<CourseCompetenceData> courseCompetences, 
			Collection<Tag> tags, Collection<Tag> hashtags, List<String> blogs, User user,
			boolean studentsCanAddNewCompetences, 
			boolean pubilshed) throws EventException;
	
	Course updateCourseFeeds(Course course, List<String> blogs, User user) throws EventException;
	
	CourseCompetence createCourseCompetence(long competenceId, long daysOffset, long duration) throws ResourceCouldNotBeLoadedException;

	CourseCompetence updateCourseCompetence(CourseCompetence courseCompetence,
			long modifiedDaysOffset, long modifiedDuration);

	CoursePortfolio getCoursePortfolio(User user);

	CoursePortfolio getOrCreateCoursePortfolio(User user);

	List<CompetenceJsonData> getCourseComeptences(long id);

	List<Competence> getOtherUsersCompetences(Course course,
			List<Long> idsOfcompetencesToExclude, User user);

	List<Node> getCourseCompetencesFromActiveCourse(User user);

	CourseEnrollment getCourseEnrollment(User user, Course course);
	
	CourseEnrollment getCourseEnrollment(User user, Course course, Session session);
	
	TargetLearningGoal getTargetLearningGoalForCourse(User user, Course course);

	Course deleteCourse(long courseId) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment enrollInCourse(User user, Course course, TargetLearningGoal targetGoal, String context);

	CourseEnrollment updateEnrollment(long enrollmentId, List<CourseCompetence> competences) throws ResourceCouldNotBeLoadedException;
	
	CourseEnrollment addCompetenceToEnrollment(long enrollmentId, CourseCompetence courseComp) throws ResourceCouldNotBeLoadedException;
	
	CourseEnrollment removeCompetenceFromEnrollment(long enrollmentId, CourseCompetence courseComp) throws ResourceCouldNotBeLoadedException;

	void removeCompetenceFromEnrollment(long enrollmentId, long competenceId) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment addToFutureCourses(long coursePortfolioId, Course course) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment addCourseCompetencesToEnrollment(Course course, CourseEnrollment enrollment);

	CourseEnrollment activateCourseEnrollment(User user, CourseEnrollment enrollment, String context);
	
	CourseEnrollment withdrawFromCourse(User user, long enrollmentId, boolean deleteLearningHistory, Session session) throws ResourceCouldNotBeLoadedException;

	CoursePortfolio addEnrollment(long portfolioId, CourseEnrollment enrollment) throws ResourceCouldNotBeLoadedException;

	CoursePortfolio addEnrollment(CoursePortfolio portfolio, CourseEnrollment enrollment) throws ResourceCouldNotBeLoadedException;

	Map<Long, List<Long>> getCoursesParticipants(List<Course> courses);
	
	List<User> getCourseParticipants(long courseId);

	CourseEnrollment completeCourseEnrollment(long coursePortfolioId, CourseEnrollment enrollment, Session session);

	void addCompetenceToEnrollment(long enrollmentId, long competenceId) throws ResourceCouldNotBeLoadedException;

	boolean isUserEnrolledInCourse(User user, Course course);

	void deleteEnrollmentForCourse(User user, Course course);

	// USED ONLY BY AN ADMIN USER
	void fixCourseReferences();

	Long getTargetLearningGoalIdForCourse(long userId, long courseId);

	Map<String, Set<Long>> getTargetLearningGoalIdsForCourse(Course course);

	Set<Long> getTargetCompetencesForCourse(Course course);

	Set<Long> getTargetActivitiesForCourse(Course course);

	Collection<Course> getAllActiveCourses();

	void updateExcludedFeedSources(Course course, List<FeedSource> disabledFeedSources);
	
	public Object[] getTargetGoalAndCompetenceIds(long userId, long courseId, long competenceId);
	
	public void enrollUserIfNotEnrolled(User user, long courseId) throws RuntimeException;

	List<Map<String, Object>> getCourseParticipantsWithCourseInfo(long courseId) throws DbConnectionException;
	
	List<Map<String, Object>> getUserCoursesWithProgressAndInstructorInfo(long userId) throws DbConnectionException;
	
	List<Map<String, Object>> getUserCoursesWithProgressAndInstructorInfo(long userId, Session session) throws DbConnectionException;

	List<User> getUsersAssignedToInstructor(long instructorId) throws DbConnectionException;
	
	void removeEnrollmentFromCoursePortfolio(User user,	long enrollmentId);
	
	List<Map<String, Object>> getCourseInstructors(long courseId) throws DbConnectionException;

	void assignInstructorToStudent(long studentId, long instructorId, long courseId) throws DbConnectionException;
	
	List<Long> getCourseIdsForInstructor(long instructorId) throws DbConnectionException;
	
	Map<String, Object> getCourseInstructor(long userId) throws DbConnectionException;
	
	void removeInstructorFromCourse(long courseInstructorId) throws DbConnectionException;
}
