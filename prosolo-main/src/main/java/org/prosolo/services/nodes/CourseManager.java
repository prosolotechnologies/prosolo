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
import org.prosolo.common.domainmodel.course.CourseInstructor;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.BasicCredentialData;
import org.prosolo.services.nodes.data.CompetenceData;
import org.prosolo.services.rest.courses.data.CompetenceJsonData;
import org.prosolo.web.courses.data.CourseCompetenceData;
import org.springframework.transaction.annotation.Transactional;

public interface CourseManager extends AbstractManager {

	 Long findCourseIdForTargetCompetence(Long targetCompetenceId);

	@Transactional
	Long findCourseIdForTargetLearningGoal(Long targetGoalId);

	Long findCourseIdForTargetActivity(Long targetCompetenceId);

	Course updateCompetencesAndSaveNewCourse(String title, String description,
											 long basedOnCourseId, List<CourseCompetenceData> courseCompetences,
											 Collection<Tag> tags, Collection<Tag> hashtags, User maker,
											 CreatorType creatorType, boolean studentsCanAddNewCompetences,
											 boolean pubilshed) throws EventException, ResourceCouldNotBeLoadedException;
	
	Course saveNewCourse(String title, String description, 
			Course basedOn, List<CourseCompetence> courseCompetences, 
			Set<Tag> tags, Set<Tag> hashtags, User maker, 
			CreatorType creatorType, boolean studentsCanAddNewCompetences, 
			boolean published) throws EventException;
	
	Course updateCourse(long courseId, String title, String description,
			List<CourseCompetenceData> courseCompetences, 
			Collection<Tag> tags, Collection<Tag> hashtags, List<String> blogs, User user,
			boolean studentsCanAddNewCompetences, 
			boolean pubilshed) throws EventException, ResourceCouldNotBeLoadedException;
	
	Course updateCourseFeeds(Course course, List<String> blogs, User user) throws EventException;
	
	CourseCompetence createCourseCompetence(long competenceId, long daysOffset, long duration) throws ResourceCouldNotBeLoadedException;

	CourseCompetence updateCourseCompetence(CourseCompetence courseCompetence,
			long modifiedDaysOffset, long modifiedDuration);

	CoursePortfolio getCoursePortfolio(User user);

	CoursePortfolio getOrCreateCoursePortfolio(User user);

	List<CompetenceJsonData> getCourseComeptences(long id);

	List<Competence> getOtherUsersCompetences(long courseId,
			List<Long> idsOfcompetencesToExclude, User user);

	List<Node> getCourseCompetencesFromActiveCourse(User user);

	CourseEnrollment getCourseEnrollment(User user, Course course);
	
	CourseEnrollment getCourseEnrollment(User user, Course course, Session session);
	
	TargetLearningGoal getTargetLearningGoalForCourse(User user, Course course);

	Course deleteCourse(long courseId) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment enrollInCourse(User user, long courseId, TargetLearningGoal targetGoal, String context,
			String page, String lContext, String service) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment updateEnrollment(long enrollmentId, List<CourseCompetence> competences) throws ResourceCouldNotBeLoadedException;
	
	CourseEnrollment addCompetenceToEnrollment(long enrollmentId, CourseCompetence courseComp) throws ResourceCouldNotBeLoadedException;
	
	CourseEnrollment removeCompetenceFromEnrollment(long enrollmentId, CourseCompetence courseComp) throws ResourceCouldNotBeLoadedException;

	void removeCompetenceFromEnrollment(long enrollmentId, long competenceId) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment addToFutureCourses(long coursePortfolioId, long courseId) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment addCourseCompetencesToEnrollment(long courseId, CourseEnrollment enrollment) throws ResourceCouldNotBeLoadedException;

	CourseEnrollment activateCourseEnrollment(User user, CourseEnrollment enrollment, String context,
			String page, String learningContext, String service);
	
	CourseEnrollment withdrawFromCourse(User user, long enrollmentId, boolean deleteLearningHistory, 
			Session session, String page, String lContext, String service) throws ResourceCouldNotBeLoadedException;

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
	
	void enrollUserIfNotEnrolled(User user, long courseId, String page, 
			String learningContext, String service) throws RuntimeException;

	List<Map<String, Object>> getCourseParticipantsWithCourseInfo(long courseId) throws DbConnectionException;
	
	List<Map<String, Object>> getUserCoursesWithProgressAndInstructorInfo(long userId) throws DbConnectionException;
	
	List<Map<String, Object>> getUserCoursesWithProgressAndInstructorInfo(long userId, Session session) throws DbConnectionException;

	List<User> getUsersAssignedToInstructor(long instructorId) throws DbConnectionException;
	
	void removeEnrollmentFromCoursePortfolio(User user,	long enrollmentId);
	
	List<Map<String, Object>> getCourseInstructors(long courseId) throws DbConnectionException;

	void assignInstructorToStudent(long studentId, long instructorId, long courseId) throws DbConnectionException;
	
	List<Long> getCourseIdsForInstructor(long instructorId) throws DbConnectionException;
	
	Map<String, Object> getCourseInstructor(long userId, long courseId) throws DbConnectionException;
	
	Map<String, Object> removeInstructorFromCourse(long courseInstructorId, long courseId,
			boolean reassignAutomatically) throws DbConnectionException;
	
	Map<String, Object> reassignStudentsAutomatically(long instructorId, long courseId) throws DbConnectionException;
	
	Map<String, Object> getBasicInstructorInfo(long instructorId) throws DbConnectionException;
	
	void updateStudentsAssignedToInstructor(long instructorId, long courseId, List<Long> studentsToAssign, List<Long> studentsToUnassign) throws DbConnectionException;
	
	CourseInstructor saveCourseInstructor(long instructorId, long userId, long courseId, 
			int maxNumberOfAssignedStudents) throws DbConnectionException;
	
	void updateStudentsAssignedToInstructors(List<Map<String, Object>> data) throws DbConnectionException;
	
	boolean areStudentsManuallyAssignedToInstructor(long courseId) throws DbConnectionException;
	
	List<Long> getUserIdsForEnrollments(List<Long> enrollmentIds) throws DbConnectionException;
	
	String getCourseTitle(long courseId) throws DbConnectionException;
	
	long getUserIdForEnrollment(long enrollmentId) throws DbConnectionException;
	
	long getUserIdForInstructor(long instructorId) throws DbConnectionException;
	
	List<CourseCompetence> getCourseCompetences(long courseId) throws DbConnectionException;
	
	Course createNewUntitledCourse(User maker, CreatorType creatorType) throws DbConnectionException;
	
	Course updateCourse(long courseId, String title, String description, Collection<Tag> tags, 
			Collection<Tag> hashtags, boolean published, User user) throws DbConnectionException;
	
	void removeFeed(long courseId, long feedSourceId) throws DbConnectionException;
	
	boolean saveNewCourseFeed(long courseId, String feedLink) throws DbConnectionException;
	
	boolean isMandatoryStructure(long courseId) throws DbConnectionException;
	
	void updateCourseCompetences(long courseId, boolean mandatoryStructure, 
			List<CompetenceData> competences) throws DbConnectionException;
	
	Credential1 saveNewCredential(BasicCredentialData data, User createdBy) throws DbConnectionException;
	
	Credential1 deleteCredential(long credId) throws DbConnectionException;
}
