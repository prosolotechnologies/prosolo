package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.CourseSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CourseInstructor;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.rest.courses.data.CompetenceJsonData;
import org.prosolo.services.rest.courses.data.SerieJsonData;
import org.prosolo.services.rest.courses.data.SerieType;
import org.prosolo.web.courses.data.CourseCompetenceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.CourseManager")
public class CourseManagerImpl extends AbstractManagerImpl implements CourseManager {
	
	private static final long serialVersionUID = 2483832929849611510L;

	private static Logger logger = Logger.getLogger(CourseManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceFactory resourceFactory;
	@Inject
	private LearningGoalManager goalManager;

	@Override
	@Transactional
	public Long findCourseIdForTargetCompetence(Long targetCompetenceId) {
		System.out.println("FIND COURSE ID FOR TARGET COMPETENCE:"+targetCompetenceId);
		String query =
				"SELECT DISTINCT course.id " +
						"FROM CourseEnrollment enrollment " +
						"LEFT JOIN enrollment.course course " +
						"LEFT JOIN enrollment.targetGoal tGoal "+
						"LEFT JOIN tGoal.targetCompetences tCompetence "+
						"WHERE tCompetence.id=:targetCompetenceId";

		Long courseId=  (Long) persistence.currentManager().createQuery(query).
				setLong("targetCompetenceId", targetCompetenceId).
				uniqueResult();
		if (courseId != null) {
			return courseId;
		}else return 0l;
	}
	@Override
	@Transactional
	public Long findCourseIdForTargetLearningGoal(Long targetGoalId) {
		System.out.println("FIND COURSE ID FOR TARGET GOAL:"+targetGoalId);
		String query =
				"SELECT DISTINCT course.id " +
						"FROM CourseEnrollment enrollment " +
						"LEFT JOIN enrollment.course course " +
						"LEFT JOIN enrollment.targetGoal tGoal "+
					 	"WHERE tGoal.id=:targetGoalId";
		Long courseId=  (Long) persistence.currentManager().createQuery(query).
				setLong("targetGoalId", targetGoalId).
				uniqueResult();
		if (courseId != null) {
			return courseId;
		}else return 0l;
	}

	@Override
	@Transactional
	public Long findCourseIdForTargetActivity(Long targetActivityId) {
		System.out.println("FIND COURSE ID FOR TARGET ACTIVITY:"+targetActivityId);

		String query =
				"SELECT DISTINCT course.id " +
						"FROM CourseEnrollment enrollment " +
						"LEFT JOIN enrollment.course course " +
						"LEFT JOIN enrollment.targetGoal tGoal "+
						"LEFT JOIN tGoal.targetCompetences tCompetence "+
						"LEFT JOIN tCompetence.targetActivities tActivity "+
						"WHERE tActivity.id=:targetActivityId";

		Long courseId=  (Long) persistence.currentManager().createQuery(query).
				setLong("targetActivityId", targetActivityId).
				uniqueResult();
		if (courseId != null) {
			return courseId;
		}else return 0l;
	}

	@Override
	@Transactional (readOnly = false)
	public Course updateCompetencesAndSaveNewCourse(String title, String description,
			Course basedOn, List<CourseCompetenceData> competences, 
			Collection<Tag> tags, Collection<Tag> hashtags, User maker,
			CreatorType creatorType,
			boolean studentsCanAddNewCompetences, boolean pubilshed) throws EventException {
		
		List<CourseCompetence> updatedCompetences = saveUnsavedCompetences(competences);
		
		Course newCourse = resourceFactory.createCourse(
				title, 
				description, 
				basedOn, 
				updatedCompetences, 
				tags,
				hashtags,
				maker, 
				creatorType, 
				studentsCanAddNewCompetences,
				pubilshed);
		
		eventFactory.generateEvent(EventType.Create, maker, newCourse);
		
		return newCourse;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Course saveNewCourse(String title, String description, 
			Course basedOn, List<CourseCompetence> courseCompetences, 
			Set<Tag> tags, Set<Tag> hashtags, User maker, 
			CreatorType creatorType, boolean studentsCanAddNewCompetences, 
			boolean published) throws EventException {
		
		Course newCourse = resourceFactory.createCourse(
				title, 
				description, 
				basedOn, 
				courseCompetences, 
				tags,
				hashtags,
				maker, 
				creatorType, 
				studentsCanAddNewCompetences,
				published);
		
		eventFactory.generateEvent(EventType.Create, maker, newCourse);
		
		return newCourse;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Course updateCourse(Course course, String title, String description,
			List<CourseCompetenceData> competences,
			Collection<Tag> tags, Collection<Tag> hashtags, List<String> blogs, User user,
			boolean studentsCanAddNewCompetences, boolean pubilshed) throws EventException {
		
		List<CourseCompetence> updatedCompetences = saveUnsavedCompetences(competences);
		
		if (course != null) {
			course = merge(course);
			Course updatedCourse = resourceFactory.updateCourse(
					course, 
					title, 
					description, 
					updatedCompetences, 
					tags, 
					hashtags,
					blogs,
					studentsCanAddNewCompetences,
					pubilshed);
			
			updatedCourse = merge(updatedCourse);
			
			eventFactory.generateEvent(EventType.Edit, user, updatedCourse);
			
			return updatedCourse;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Course updateCourseFeeds(Course course, List<String> blogs, User user) throws EventException {
		if (course != null) {
			course = merge(course);
			
			Course updatedCourse = resourceFactory.updateCourse(
					course, 
					course.getTitle(), 
					course.getDescription(), 
					course.getCompetences(), 
					course.getTags(), 
					course.getHashtags(),
					blogs,
					course.isStudentsCanAddNewCompetences(),
					course.isPublished());
			
			updatedCourse = merge(updatedCourse);
			
			eventFactory.generateEvent(EventType.Edit, user, updatedCourse);
			
			return updatedCourse;
		}
		return null;
	}
	
	private List<CourseCompetence> saveUnsavedCompetences(List<CourseCompetenceData> competences) {
		List<CourseCompetence> updatedCompetences = new ArrayList<CourseCompetence>();

		for (CourseCompetenceData compsData : competences) {
			try {
				CourseCompetence courseComp = null;
				
				if (compsData.isSaved()) {
					if (compsData.isDataChanged()) {
						courseComp = updateCourseCompetence(
								compsData.getCourseCompetence(), 
								compsData.getModifiedDaysOffset(), 
								compsData.getModifiedDuration());
					} else {
						courseComp = merge(compsData.getCourseCompetence());
					}
				} else {
					courseComp = createCourseCompetence(
							compsData.getCompetenceId(), 
							compsData.getModifiedDaysOffset(), 
							compsData.getModifiedDuration());
				}

				if (courseComp != null) {
					updatedCompetences.add(courseComp);
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
		return updatedCompetences;
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseCompetence createCourseCompetence(long competenceId,
			long daysOffset, long duration) throws ResourceCouldNotBeLoadedException {
		Competence comp = loadResource(Competence.class, competenceId);
		
		CourseCompetence courseComp = new CourseCompetence();
		courseComp.setCompetence(comp);
		courseComp.setDaysOffset(daysOffset);
		courseComp.setDuration(duration);
		return saveEntity(courseComp);
	}
	
	@Override
	@Transactional(readOnly = false)
	public CourseCompetence updateCourseCompetence(
			CourseCompetence courseCompetence, long modifiedDaysOffset,
			long modifiedDuration) {
		
		if (courseCompetence != null) {
			courseCompetence.setDuration(modifiedDuration);
			courseCompetence.setDaysOffset(modifiedDaysOffset);
			return saveEntity(courseCompetence);
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public CoursePortfolio getCoursePortfolio(User user) {
		return getCoursePortfolio(user, persistence.currentManager());
	}
	
	public CoursePortfolio getCoursePortfolio(User user, Session session) {
		String query = 
			"SELECT DISTINCT coursePortfolio " +
			"FROM CoursePortfolio coursePortfolio " +
			"WHERE coursePortfolio.user = :user";

		return (CoursePortfolio) session.createQuery(query).
				setEntity("user", user).
				uniqueResult();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Node> getCourseCompetencesFromActiveCourse(User user) {
		String query = 
			"SELECT DISTINCT competence " +
			"FROM CoursePortfolio coursePortfolio " +
			"LEFT JOIN coursePortfolio.enrollments enrollment "+
			"LEFT JOIN enrollment.course course "+
			"LEFT JOIN course.competences courseComp "+
			"LEFT JOIN courseComp.competence competence "+
			"LEFT JOIN enrollment.addedCompetences addedCourseComp "+
			"LEFT JOIN addedCourseComp.competence competence "+
			"WHERE coursePortfolio.user = :user " +
				"AND enrollment.status = :activeStatus";

		@SuppressWarnings("unchecked")
		List<Node> competences = persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setString("activeStatus", Status.ACTIVE.name()).
				list();
		return competences;
	}
	
	@Override
	public CoursePortfolio getOrCreateCoursePortfolio(User user) {
		CoursePortfolio coursePortfolio = getCoursePortfolio(user);
		
		if (coursePortfolio == null) {
			coursePortfolio = new CoursePortfolio();
			coursePortfolio.setUser(user);
			coursePortfolio = saveEntity(coursePortfolio);
		}
		return coursePortfolio;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<CompetenceJsonData> getCourseComeptences(long id) {
		String query = 
			"SELECT comp.title, tComp.dateCreated, tComp.completedDay " +
			"FROM Course course " +
			"LEFT JOIN course.competences courseComps " +
			"LEFT JOIN course.maker user " +
			"LEFT JOIN user.learningGoals goal " +
			"LEFT JOIN goal.targetCompetences tComp " +
			"LEFT JOIN tComp.competence comp " +
			"WHERE course.id = :id " +
				"AND courseComps = comp " +
			"ORDER BY tComp.dateCreated ASC";

		@SuppressWarnings("unchecked")
		List<Object> result = persistence.currentManager().createQuery(query).
				setLong("id", id).
				list();
		
		List<CompetenceJsonData> compList = new ArrayList<CompetenceJsonData>();  
		
		if (result != null && !result.isEmpty()) {
			
			Date floorStart = null;
			
			for (int i = 0; i < result.size(); i++) {
				Object[] res = (Object[]) result.get(i);
				
				String title = (String) res[0];
				Date start = (Date) res[1];
				Date completion = (Date) res[2];
				
				if (i == 0) {
					floorStart = start;
				}
				
				long startDay = DateUtil.getDayDifference(floorStart, start) + 1;
				long dayDiff = DateUtil.getDayDifference(start, completion);
				
				SerieJsonData s1 = new SerieJsonData();
			    s1.setName(SerieType.Current);
			    s1.setStart(startDay);
			    s1.setDuration(dayDiff);

			    CompetenceJsonData compJson = new CompetenceJsonData();
			    compJson.setName(title);
			    compJson.setId(i+1);
			    compJson.addSerie(s1);
			    
			    compList.add(compJson);
			}
		}
		
		return compList;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Competence> getOtherUsersCompetences(Course course,
			List<Long> idsOfcompetencesToExclude, User user) {
		
		String query = 
			"SELECT DISTINCT comp " +
			"FROM CourseEnrollment enrollment " +
			"LEFT JOIN enrollment.course course " +
			"LEFT JOIN enrollment.addedCompetences addedCompetences " +
			"LEFT JOIN addedCompetences.competence comp " +
			"WHERE course = :course " +
				"AND comp.id NOT IN (:excludedCompIds)" +
			"ORDER BY comp.title ";

		@SuppressWarnings("unchecked")
		List<Competence> result = persistence.currentManager().createQuery(query).
				setEntity("course", course).
				setParameterList("excludedCompIds", idsOfcompetencesToExclude).
				list();
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public CourseEnrollment getCourseEnrollment(User user, Course course) {
		return getCourseEnrollment(user, course, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public CourseEnrollment getCourseEnrollment(User user, Course course, Session session) {
		String query = 
			"SELECT DISTINCT enrollment " +
			"FROM CoursePortfolio coursePortfolio " +
			"LEFT JOIN coursePortfolio.enrollments enrollment "+
			"WHERE coursePortfolio.user = :user " +
				"AND enrollment.course = :course";
		
		CourseEnrollment result = (CourseEnrollment) session.createQuery(query).
				setEntity("user", user).
				setEntity("course", course).
				uniqueResult();
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public TargetLearningGoal getTargetLearningGoalForCourse(User user, Course course) {
		String query = 
				"SELECT DISTINCT targetGoal " +
				"FROM CoursePortfolio coursePortfolio " +
				"LEFT JOIN coursePortfolio.enrollments enrollment "+
				"LEFT JOIN enrollment.targetGoal targetGoal "+
				"WHERE coursePortfolio.user = :user " +
					"AND enrollment.course = :course";
		
		TargetLearningGoal result = (TargetLearningGoal) persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setEntity("course", course).
				uniqueResult();
		return result;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Course deleteCourse(long courseId) throws ResourceCouldNotBeLoadedException {
		Course course = loadResource(Course.class, courseId);
		course.setDeleted(true);
		return saveEntity(course);
	}
	
	@Override
	@Transactional(readOnly = false)
	public CourseEnrollment enrollInCourse(User user, Course course, TargetLearningGoal targetGoal, String context) {
		if (course != null) {
			// if user has previously been enrolled into this course, remove that enrollment
			CourseEnrollment oldCourseEnrollment = getCourseEnrollment(user, course);
			
			if (oldCourseEnrollment != null) {
				removeEnrollmentFromCoursePortfolio(user, oldCourseEnrollment.getId());
			}
			
			
			Date date = new Date();
			List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
			
			for (CourseCompetence courseCompetence : course.getCompetences()) {
				CourseCompetence cc = new CourseCompetence();
				cc.setDateCreated(date);
				cc.setCompetence(courseCompetence.getCompetence());
				cc.setDaysOffset(courseCompetence.getDaysOffset());
				cc.setDuration(courseCompetence.getDuration());
				cc = saveEntity(cc);
				
				courseCompetences.add(cc);
			}
			
			CourseEnrollment enrollment = new CourseEnrollment();
			enrollment.setCourse(course);
			enrollment.setUser(user);
			enrollment.setDateStarted(date);
			enrollment.setStatus(Status.ACTIVE);
			enrollment.setAddedCompetences(courseCompetences);
			enrollment.setTargetGoal(targetGoal);
			enrollment = saveEntity(enrollment);
			
			CoursePortfolio portfolio = getOrCreateCoursePortfolio(user);
			portfolio.addEnrollment(enrollment);
			saveEntity(portfolio);
			
			try {
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("context", context);
			
				eventFactory.generateEvent(EventType.ENROLL_COURSE, user, enrollment, course, parameters);
			} catch (EventException e) {
				logger.error(e);
			}
			
			return enrollment;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment addCourseCompetencesToEnrollment(Course course,
			CourseEnrollment enrollment) {
		List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
		
		for (CourseCompetence courseCompetence : course.getCompetences()) {
			CourseCompetence cc = new CourseCompetence();
			cc.setDateCreated(new Date());
			cc.setCompetence(courseCompetence.getCompetence());
			cc.setDaysOffset(courseCompetence.getDaysOffset());
			cc.setDuration(courseCompetence.getDuration());
			cc = saveEntity(cc);
			
			courseCompetences.add(cc);
		}
		
		enrollment.setAddedCompetences(courseCompetences);
		return saveEntity(enrollment);
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment updateEnrollment(long enrollmentId,	List<CourseCompetence> competences) throws ResourceCouldNotBeLoadedException {
		CourseEnrollment enrollment = loadResource(CourseEnrollment.class, enrollmentId);
		enrollment.setAddedCompetences(competences);
		return saveEntity(enrollment);
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment addCompetenceToEnrollment(long enrollmentId, CourseCompetence courseComp) throws ResourceCouldNotBeLoadedException {
		CourseEnrollment enrollment = loadResource(CourseEnrollment.class, enrollmentId);
		boolean added = enrollment.addCompetence(courseComp);
		
		if (added) {
			enrollment = saveEntity(enrollment);
		}
		return enrollment;
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment removeCompetenceFromEnrollment(long enrollmentId, CourseCompetence courseComp) throws ResourceCouldNotBeLoadedException {
		CourseEnrollment enrollment = loadResource(CourseEnrollment.class, enrollmentId);
		boolean added = enrollment.removeCompetence(courseComp);
		
		if (added) {
			enrollment = saveEntity(enrollment);
		}
		return enrollment;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void removeCompetenceFromEnrollment(long enrollmentId,
			long competenceId) throws ResourceCouldNotBeLoadedException {
		CourseEnrollment enrollment = loadResource(CourseEnrollment.class, enrollmentId);
		
		Iterator<CourseCompetence> courseCompIterator = enrollment.getAddedCompetences().iterator();
		
		while (courseCompIterator.hasNext()) {
			CourseCompetence courseCompetence = (CourseCompetence) courseCompIterator.next();
			
			if (courseCompetence.getCompetence().getId() == competenceId) {
				courseCompIterator.remove();
				break;
			}
		}
		saveEntity(enrollment);
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment addToFutureCourses(long coursePortfolioId, Course course) throws ResourceCouldNotBeLoadedException {
		CoursePortfolio coursePortfolio = loadResource(CoursePortfolio.class, coursePortfolioId);
		
		CourseEnrollment enrollment = new CourseEnrollment();
		enrollment.setCourse(course);
		enrollment.setUser(coursePortfolio.getUser());
		enrollment.setStatus(Status.NOT_STARTED);
		enrollment = saveEntity(enrollment);
		
		coursePortfolio.addEnrollment(enrollment);
		saveEntity(coursePortfolio);
		
		return enrollment;
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment activateCourseEnrollment(User user, CourseEnrollment enrollment, String context) {
		enrollment.setDateStarted(new Date());
		enrollment.setStatus(Status.ACTIVE);
		
		TargetLearningGoal targetGoal = enrollment.getTargetGoal();
		
		if (targetGoal != null) {
			targetGoal.setCourseEnrollment(enrollment);
			targetGoal = saveEntity(targetGoal);
		}
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		try {
			System.out.println("ACTIVATE COURSE EVENT FIRED");
			eventFactory.generateEvent(EventType.ACTIVATE_COURSE, user, enrollment, parameters);
		} catch (EventException e) {
			logger.error(e);
		}
		
		return saveEntity(enrollment);
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment withdrawFromCourse(User user, long enrollmentId, boolean deleteLearningHistory, 
			Session session) throws ResourceCouldNotBeLoadedException {
		logger.info("withdrawFromCourse");
		CourseEnrollment enrollment = (CourseEnrollment) session.get(CourseEnrollment.class, enrollmentId);
		logger.info("withdrawFromCourse");
		boolean userHasWithdrawnFromCourse = hasUserAlreadyWithdrawnFromCourse(user, enrollment.getCourse(), session);
		logger.info("withdrawFromCourse");
		if (userHasWithdrawnFromCourse) {
			removeEnrollmentFromCoursePortfolio(user, enrollmentId, session);
//			session.delete(enrollment);
		} else {
			enrollment.setStatus(Status.WITHDRAWN);
			saveEntity(enrollment, session);
		}
		logger.info("withdrawFromCourse");
		if (deleteLearningHistory) {
			enrollment.setTargetGoal(null);
			saveEntity(enrollment, session);
		}
		logger.info("withdrawFromCourse");
		return enrollment;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void deleteEnrollmentForCourse(User user, Course course) {
		CourseEnrollment enrollment = getCourseEnrollment(user, course);
		
		if (enrollment != null) {
			enrollment.setStatus(Status.WITHDRAWN);
			enrollment.setTargetGoal(null);
			saveEntity(enrollment);
			
			removeEnrollmentFromCoursePortfolio(user, enrollment.getId());
		}
	}
	
	private void removeEnrollmentFromCoursePortfolio(User user,	long enrollmentId) {
		removeEnrollmentFromCoursePortfolio(user, enrollmentId, persistence.currentManager());
	}

	private void removeEnrollmentFromCoursePortfolio(User user,
			long enrollmentId, Session session) {
		CoursePortfolio portfolio = getCoursePortfolio(user);
		
		if (portfolio != null) {
			portfolio.removeEnrollment(enrollmentId);
			saveEntity(portfolio, session);
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public CoursePortfolio addEnrollment(long portfolioId, CourseEnrollment enrollment) throws ResourceCouldNotBeLoadedException {
		CoursePortfolio portfolio = loadResource(CoursePortfolio.class, portfolioId, true);
		return addEnrollment(portfolio, enrollment);
	}
	
	@Override
	@Transactional (readOnly = false)
	public CoursePortfolio addEnrollment(CoursePortfolio portfolio, CourseEnrollment enrollment) throws ResourceCouldNotBeLoadedException {
		portfolio.addEnrollment(enrollment);
		return saveEntity(portfolio);
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment completeCourseEnrollment(long coursePortfolioId, CourseEnrollment enrollment, Session session) {
		enrollment = (CourseEnrollment) session.get(CourseEnrollment.class, enrollment.getId());
		
		if (enrollment != null) {
			enrollment.setActive(false);
			enrollment.setDateFinished(new Date());
			enrollment.setStatus(Status.COMPLETED);
			session.saveOrUpdate(enrollment);
			
			return enrollment;
		}
		return enrollment;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Map<Long, List<Long>> getCoursesParticipants(List<Course> courses) {
		String query = 
			"SELECT DISTINCT course.id, enrollment.user.id " +
			"FROM CourseEnrollment enrollment " +
			"LEFT JOIN enrollment.course course " +
			"WHERE course IN (:courses)";

		@SuppressWarnings("unchecked")
		List<Object[]> result = persistence.currentManager().createQuery(query).
				setParameterList("courses", courses).
				list();
		Map<Long, List<Long>> counts = new HashMap<Long, List<Long>>();
		if (result != null && !result.isEmpty()) {
			
			
			for (Object[] res : result) {
				Long courseId = (Long) res[0];
				Long userId = (Long) res[1];
				
				List<Long> courseMemberIds = counts.get(courseId);
				
				if (courseMemberIds == null) {
					courseMemberIds = new ArrayList<Long>();
				}
				courseMemberIds.add(userId);
				
				counts.put(courseId, courseMemberIds);
			}
			
		}
		return counts;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getCourseParticipants(long courseId) {
		String query = 
			"SELECT DISTINCT enrollment.user " +
			"FROM CourseEnrollment enrollment " +
			"LEFT JOIN enrollment.course course " +
			"WHERE course.id = :course";
		
		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query).
				setLong("course", courseId).
				list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Map<String, Object>> getCourseParticipantsWithCourseInfo(long courseId) throws DbConnectionException {
		try {
		String query = 
			"SELECT DISTINCT enrollment.user, instructorUser, tGoal.progress " +
			"FROM CourseEnrollment enrollment " +
				"LEFT JOIN enrollment.course course " +
				"LEFT JOIN enrollment.targetGoal tGoal " +
				"LEFT JOIN enrollment.instructor instructor " +
				"LEFT JOIN instructor.user instructorUser " +
				"WHERE course.id = :course " + 
				"AND enrollment.status = :status";
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager().createQuery(query).
					setLong("course", courseId).
					setParameter("status", Status.ACTIVE).
					list();
			
			List<Map<String, Object>> resultList = new LinkedList<>();
			if (result != null && !result.isEmpty()) {
				for (Object[] res : result) {
					Map<String, Object> resMap = new LinkedHashMap<>();
					User user = (User) res[0];
					User instructor = (User) res[1];
					Integer courseProgress = (int) res[2];
	
					resMap.put("user", user);
					resMap.put("instructor", instructor);
					resMap.put("courseProgress", courseProgress);
					
					resultList.add(resMap);
				}
			}
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while loading course participants");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void addCompetenceToEnrollment(long enrollmentId, long competenceId) throws ResourceCouldNotBeLoadedException {
		Competence competence = loadResource(Competence.class, competenceId);
		CourseEnrollment enrollment = loadResource(CourseEnrollment.class, enrollmentId);
		
		CourseCompetence courseComp = createCourseCompetence(competenceId, 0, competence.getDuration());
		enrollment.addCompetence(courseComp);
		saveEntity(enrollment);
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserEnrolledInCourse(User user, Course course) {
		String query = 
			"SELECT COUNT(enrollment.id) " +
			"FROM CoursePortfolio coursePortfolio " +
			"LEFT JOIN coursePortfolio.enrollments enrollment "+
			"WHERE coursePortfolio.user = :user " +
				"AND enrollment.course = :course";
		
		Long result = (Long) persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setEntity("course", course).
				uniqueResult();
		
		return result > 0;
	}
	
	@Transactional (readOnly = true)
	private boolean hasUserAlreadyWithdrawnFromCourse(User user, Course course) {
		return hasUserAlreadyWithdrawnFromCourse(user, course, persistence.currentManager());
	}
	
	private boolean hasUserAlreadyWithdrawnFromCourse(User user, Course course, Session session) {
		String query = 
			"SELECT COUNT(enrollment) " +
			"FROM CoursePortfolio coursePortfolio " +
			"LEFT JOIN coursePortfolio.enrollments enrollment "+
			"WHERE coursePortfolio.user = :user " +
				"AND enrollment.course = :course " +
				"AND enrollment.status = :withdrawnStatus";
		
		Long result = (Long) session.createQuery(query).
				setEntity("user", user).
				setEntity("course", course).
				setString("withdrawnStatus", Status.WITHDRAWN.name()).
				uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = false)
	@Deprecated
	public void fixCourseReferences() {
		String query = 
			"SELECT socialActivity " +
			"FROM SocialActivity socialActivity " +
			"WHERE socialActivity.action = :action " +
				"AND socialActivity.courseEnrollmentObject IS NULL " +
				"AND socialActivity.courseObject IS NOT NULL ";
		
		@SuppressWarnings("unchecked")
		List<SocialActivity> result = persistence.currentManager().createQuery(query).
				setString("action", EventType.ENROLL_COURSE.name()).
				list();
		
		int index = 0;
			
		if (result != null) {
			for (SocialActivity socialActivity : result) {
				if (socialActivity instanceof CourseSocialActivity) {
//					CourseSocialActivity courseSocialActivity = (CourseSocialActivity) socialActivity;
//
//					User user = courseSocialActivity.getMaker();
//					Course course = courseSocialActivity.getCourseObject();
//					
//					if (course != null) {
//						CourseEnrollment enrollment = getCourseEnrollment(user, course);
//						
//						if (enrollment != null) {
//							courseSocialActivity.setCourseEnrollmentObject(enrollment);
//							saveEntity(courseSocialActivity);
//							
//							index++;
//						}
//					}
				}
			}
		}
		logger.info("fixCourseReferences method affected "+index+" rows");
	}
	
	@Override
	@Transactional (readOnly = true)
	public Long getTargetLearningGoalIdForCourse(long userId, long courseId) {
		String query = 
				"SELECT DISTINCT targetGoal.id " +
				"FROM CoursePortfolio coursePortfolio " +
				"LEFT JOIN coursePortfolio.enrollments enrollment "+
				"LEFT JOIN enrollment.targetGoal targetGoal "+
				"WHERE coursePortfolio.user.id = :user " +
					"AND enrollment.course.id = :course";
		
		Long targetGoalId = (Long) persistence.currentManager().createQuery(query).
				setLong("user", userId).
				setLong("course", courseId).
				uniqueResult();
		return targetGoalId;
	}
	@Override
	@Transactional (readOnly = true)
	public Map<String,Set<Long>> getTargetLearningGoalIdsForCourse(Course course) {
		Set<Long> targetGoals = new TreeSet<Long>();
		Set<Long> lGoals=new TreeSet<Long>();
		Map<String,Set<Long>> resultSets=new HashMap<String,Set<Long>>();
		String query = 
				"SELECT DISTINCT targetGoal.id, lGoal.id " +
				"FROM TargetLearningGoal targetGoal " +
			//	"LEFT JOIN coursePortfolio.enrollments enrollment "+
				"LEFT JOIN targetGoal.courseEnrollment enrollment "+
				//"LEFT JOIN enrollment.targetGoal targetGoal "+
				"LEFT JOIN targetGoal.learningGoal lGoal "+
				"WHERE enrollment.course = :course";
		
		@SuppressWarnings("unchecked")
		List<Object> results =  persistence.currentManager().createQuery(query).
				setEntity("course", course).
				list();
		if (results != null) {
			Iterator<?> iterator = results.iterator();
			while (iterator.hasNext()) {
				Object[] ids = (Object[]) iterator.next();
				Long targetLearningGoalId=(Long) ids[0];
				targetGoals.add(targetLearningGoalId);
				Long learningGoalId=(Long) ids[1];
				lGoals.add(learningGoalId);
			}			
		}
		resultSets.put("goals", lGoals);
		resultSets.put("targetGoals",targetGoals);
		return resultSets;
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getTargetCompetencesForCourse(Course course) {
		Set<Long> targetCompetences = new TreeSet<Long>();
		
		String query = 
			"SELECT tComp.id " +
			"FROM CourseEnrollment courseEnrollment " +
			"LEFT JOIN courseEnrollment.targetGoal targetGoal "+
			"LEFT JOIN targetGoal.targetCompetences tComp "+			
			"WHERE courseEnrollment.course = :course and tComp.id>0 ";

		
		@SuppressWarnings("unchecked")
		List<Long> tComps =  persistence.currentManager().createQuery(query).
				setEntity("course", course).
				list();
		System.out.println("FOUND TG COMPETENCES SIZE:"+tComps.size());
		if (tComps != null) {
			 targetCompetences.addAll(tComps);
		}

		return targetCompetences;
	}
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getTargetActivitiesForCourse(Course course) {
		Set<Long> targetActivities = new TreeSet<Long>();
		
		String query = 
			"SELECT tActivity.id " +
			"FROM CourseEnrollment courseEnrollment " +
			"LEFT JOIN courseEnrollment.targetGoal targetGoal "+
			"LEFT JOIN targetGoal.targetCompetences tComp "+	
			"LEFT JOIN tComp.targetActivities tActivity "+
			"WHERE courseEnrollment.course = :course and tActivity.id>0 ";

		
		@SuppressWarnings("unchecked")
		List<Long> tActivities =  persistence.currentManager().createQuery(query).
				setEntity("course", course).
				list();
		
		System.out.println("FOUND TG Activities SIZE:"+tActivities.size());
		
		if (tActivities != null) {
			 targetActivities.addAll(tActivities);
		}

		return targetActivities;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Collection<Course> getAllActiveCourses() {
		String query = 
			"SELECT DISTINCT course " +
			"FROM Course course " +
			"WHERE course.published = :published ";
		
		@SuppressWarnings("unchecked")
		List<Course> result = persistence.currentManager().createQuery(query).
				setBoolean("published", true).
				list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateExcludedFeedSources(Course course, List<FeedSource> disabledFeedSources) {
		course = merge(course);
		
		course.getExcludedFeedSources().clear();
		
		for (FeedSource feedSource : disabledFeedSources) {
			course.addExcludedFeedSource(merge(feedSource));
		}
		
		saveEntity(course);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Object[] getTargetGoalAndCompetenceIds(long userId, long courseId, long competenceId){
		String query = 
				"SELECT DISTINCT targetGoal.id, tc.id " +
				"FROM CoursePortfolio coursePortfolio " +
				"INNER JOIN coursePortfolio.enrollments enrollment "+
				"INNER JOIN enrollment.targetGoal targetGoal "+
				"INNER JOIN targetGoal.targetCompetences tc "+
				"WHERE coursePortfolio.user = :user " +
					"AND enrollment.course = :course "+
				    "AND tc.competence = :competence";
		
				return (Object[]) persistence.currentManager().createQuery(query).
						setLong("user", userId).
						setLong("course", courseId).
						setLong("competence", competenceId).
						uniqueResult();
	}
	
	@Override
	@Transactional
	public void enrollUserIfNotEnrolled(User user, long courseId) throws RuntimeException{
		try{
			Course course = (Course) persistence.currentManager().load(Course.class, courseId);
			boolean enrolled = isUserEnrolledInCourse(user, course);
			if(!enrolled){
				TargetLearningGoal targetLGoal = goalManager.createNewCourseBasedLearningGoal(user, course, null, "");
				CourseEnrollment enrollment = enrollInCourse(user, course, targetLGoal, null);
				targetLGoal.setCourseEnrollment(enrollment);
				targetLGoal = saveEntity(targetLGoal);
				logger.info("User with email "+user.getEmail().getAddress() + " enrolled in course with id "+course.getId());
			}
		}catch(Exception e){
			throw new RuntimeException("Error while enrolling user");
		}
	}
}
