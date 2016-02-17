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
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.CompetenceData;
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
	@Inject private FeedSourceManager feedSourceManager;

	@Override
	@Transactional
	public Long findCourseIdForTargetCompetence(Long targetCompetenceId) {
		//System.out.println("FIND COURSE ID FOR TARGET COMPETENCE:"+targetCompetenceId);
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
	//System.out.println("FIND COURSE ID FOR TARGET GOAL:"+targetGoalId);
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
		//System.out.println("FIND COURSE ID FOR TARGET ACTIVITY:"+targetActivityId);

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
			long basedOnCourseId, List<CourseCompetenceData> competences, 
			Collection<Tag> tags, Collection<Tag> hashtags, User maker,
			CreatorType creatorType,
			boolean studentsCanAddNewCompetences, boolean pubilshed) throws EventException, ResourceCouldNotBeLoadedException {
		
		List<CourseCompetence> updatedCompetences = saveUnsavedCompetences(competences);
		
		Course basedOn = loadResource(Course.class, basedOnCourseId);
		
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
	public Course updateCourse(long courseId, String title, String description,
			List<CourseCompetenceData> competences,
			Collection<Tag> tags, Collection<Tag> hashtags, List<String> blogs, User user,
			boolean studentsCanAddNewCompetences, boolean pubilshed) throws EventException, ResourceCouldNotBeLoadedException {
		
		List<CourseCompetence> updatedCompetences = saveUnsavedCompetences(competences);
		
		if (courseId > 0) {
			Course course = loadResource(Course.class, courseId);
			
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
	public List<Competence> getOtherUsersCompetences(long courseId,
			List<Long> idsOfcompetencesToExclude, User user) {
		
		String query = 
			"SELECT DISTINCT comp " +
			"FROM CourseEnrollment enrollment " +
			"LEFT JOIN enrollment.course course " +
			"LEFT JOIN enrollment.addedCompetences addedCompetences " +
			"LEFT JOIN addedCompetences.competence comp " +
			"WHERE course.id = :courseId " +
				"AND comp.id NOT IN (:excludedCompIds)" +
			"ORDER BY comp.title ";

		@SuppressWarnings("unchecked")
		List<Competence> result = persistence.currentManager().createQuery(query).
				setLong("courseId", courseId).
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
	public CourseEnrollment enrollInCourse(User user, long courseId, TargetLearningGoal targetGoal, String context,
			String page, String lContext, String service) throws ResourceCouldNotBeLoadedException {
		
		Course course = loadResource(Course.class, courseId);
		
		Map<String, Object> res = resourceFactory.enrollUserInCourse(user, course, targetGoal, context);
		CourseEnrollment enrollment = null;
		
		if (res != null) {
			enrollment = (CourseEnrollment) res.get("enrollment");
		}
		
		if (enrollment != null) {
			try {
				
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("context", context);
			
				eventFactory.generateEvent(EventType.ENROLL_COURSE, user, enrollment, course, parameters);
				
				fireInstructorAssignEvent(user, enrollment, course.getId(), res, page, lContext, service);
			} catch (EventException e) {
				logger.error(e);
			}	
		}
		return enrollment;
	}
	
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment addCourseCompetencesToEnrollment(long courseId,
			CourseEnrollment enrollment) throws ResourceCouldNotBeLoadedException {
		List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
		
		Course course = loadResource(Course.class, courseId);
		
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
	public CourseEnrollment addToFutureCourses(long coursePortfolioId, long courseId) throws ResourceCouldNotBeLoadedException {
		CoursePortfolio coursePortfolio = loadResource(CoursePortfolio.class, coursePortfolioId);
		Course course = loadResource(Course.class, coursePortfolioId);
		
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
	public CourseEnrollment activateCourseEnrollment(User user, CourseEnrollment enrollment, String context,
			String page, String learningContext, String service) {
		enrollment.setDateStarted(new Date());
		enrollment.setStatus(Status.ACTIVE);
		
		TargetLearningGoal targetGoal = enrollment.getTargetGoal();
		
		if (targetGoal != null) {
			targetGoal.setCourseEnrollment(enrollment);
			targetGoal = saveEntity(targetGoal);
		}
		
		Map<String, Object> courseData = getCourseIdAndInstructorAssignMethod(enrollment.getId());
		if(courseData != null) {
			Long courseId = (Long) courseData.get("id");
			Boolean automaticAssign = !((Boolean) courseData.get("assignManually"));
			if(automaticAssign) {
				List<Long> ids = new ArrayList<>();
				ids.add(enrollment.getId());
				Map<String, Object> res = resourceFactory.assignStudentsToInstructorAutomatically(courseId, ids, 0);
				fireInstructorAssignEvent(user, enrollment, courseId, res, page, learningContext, service);
			}
		}
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		try {
			System.out.println("ACTIVATE COURSE EVENT FIRED");
			eventFactory.generateEvent(EventType.ACTIVATE_COURSE, user, enrollment, parameters);
		} catch (EventException e) {
			logger.error(e);
		}
		
		enrollment = saveEntity(enrollment);
		
		return enrollment;
		
	}
	
	private Map<String, Object> getCourseIdAndInstructorAssignMethod(long enrollmentId) {
		try {
			String query = 
					"SELECT course.id, course.manuallyAssignStudentsToInstructors " +
					"FROM CourseEnrollment enrollment " +
					"INNER JOIN enrollment.course course " +
					"WHERE enrollment.id = :enrollmentId";
			
			Object[] res = (Object[]) persistence.currentManager().createQuery(query).
					setLong("enrollmentId", enrollmentId).
					uniqueResult();
			if(res == null) {
				return null;
			}
			Long id = (Long) res[0];
			Boolean assignManually = (Boolean) res[1];
			Map<String, Object> result = null;
			if(id != null) {
				result = new HashMap<>();
				result.put("id", id);
				result.put("assignManually", assignManually);
			}
			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course data");
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	@Transactional (readOnly = false)
	public CourseEnrollment withdrawFromCourse(User user, long enrollmentId, boolean deleteLearningHistory, 
			Session session, String page, String lContext, String service) throws ResourceCouldNotBeLoadedException {
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
		enrollment.setInstructor(null);
		enrollment.setAssignedToInstructor(false);
		logger.info("withdrawFromCourse");
		
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			Map<String, Object> courseData = getCourseIdAndInstructorAssignMethod(enrollment.getId());
			if(courseData != null) {
				Long courseId = (Long) courseData.get("id");
				parameters.put("courseId", courseId + "");
				long instructorUserId = getInstructorUserIdForEnrollment(enrollment.getId());
				User target = new User();
				target.setId(instructorUserId);
				eventFactory.generateEvent(EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR, user, user, target, 
						null, page, lContext, service, new Class[] {NodeChangeObserver.class}, parameters);
			}
		} catch (EventException e) {
			logger.error(e);
		}
		
		return enrollment;
	}
	
	private long getInstructorUserIdForEnrollment(long enrollmentId) {
		try {
			String query = 
					"SELECT user.id " +
					"FROM CourseEnrollment enrollment " +
					"INNER JOIN enrollment.instructor instructor " +
					"INNER JOIN instructor.user user " +
					"WHERE enrollment.id = :enrollmentId";
			
			Long res = (Long) persistence.currentManager().createQuery(query).
					setLong("enrollmentId", enrollmentId).
					uniqueResult();
			if(res == null) {
				return 0;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
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
	
	@Override
	public void removeEnrollmentFromCoursePortfolio(User user,	long enrollmentId) {
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
			"WHERE course IN (:courses) " +
			"AND (enrollment.status = :statusActive " +
			  "OR enrollment.status = :statusCompleted)";

		@SuppressWarnings("unchecked")
		List<Object[]> result = persistence.currentManager().createQuery(query).
				setParameterList("courses", courses).
				setParameter("statusActive", Status.ACTIVE).
				setParameter("statusCompleted", Status.COMPLETED).
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
	@Transactional(readOnly = false)
	public void enrollUserIfNotEnrolled(User user, long courseId, String page, 
			String learningContext, String service) throws RuntimeException {
		try {
			Course course = (Course) persistence.currentManager().load(Course.class, courseId);
			boolean enrolled = isUserEnrolledInCourse(user, course);
			
			if (!enrolled) {
				Map<String, Object> res = resourceFactory.enrollUserInCourse(user, course);
				CourseEnrollment enrollment = null;
				
				if (res != null) {
					enrollment = (CourseEnrollment) res.get("enrollment");
				}
				
				logger.info("User with email "+user.getEmail().getAddress() + " enrolled in course with id "+course.getId());
				
				if (enrollment != null) {
					try {
						Map<String, String> parameters = null;
						eventFactory.generateEvent(EventType.ENROLL_COURSE, user, enrollment, course, parameters);
					} catch (EventException e) {
						logger.error(e);
					}

					fireInstructorAssignEvent(user, enrollment, courseId, res, page, learningContext, service);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new RuntimeException("Error while enrolling user");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void fireInstructorAssignEvent(User user, CourseEnrollment enrollment, long courseId, 
			Map<String, Object> res, String page, String lContext, String service) {
		Map<Long, Long> assigned = (Map<Long, Long>) res.get("assigned");
		
		if (assigned != null) {
			Long instructorId = assigned.get(enrollment.getId());
			
			if (instructorId != null) {
				long insUserId = getUserIdForInstructor(instructorId);
				
				try {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("courseId", courseId + "");
					User target = new User();
					target.setId(insUserId);
					eventFactory.generateEvent(EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR, user, user, target,
							null, page, lContext, service, new Class[] { NodeChangeObserver.class }, null);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getUserCoursesWithProgressAndInstructorInfo(long userId) throws DbConnectionException {
		return getUserCoursesWithProgressAndInstructorInfo(userId, persistence.currentManager());
	}
	
	//returns only active courses
	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getUserCoursesWithProgressAndInstructorInfo(long userId, Session session) throws DbConnectionException {
		try {
			String query = 
					"SELECT  course.id, tGoal.progress, userInstructor.id " +
					"FROM CoursePortfolio coursePortfolio " +
					"INNER JOIN coursePortfolio.enrollments enrollment "+
					"INNER JOIN enrollment.targetGoal tGoal " +
					"INNER JOIN enrollment.course course " +
					"LEFT JOIN enrollment.instructor instructor " +
					"LEFT JOIN instructor.user userInstructor " +
					"WHERE coursePortfolio.user.id = :user " +
					"AND (enrollment.status = :statusActive " +
						  "OR enrollment.status = :statusCompleted)";
				
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager().createQuery(query).
					setLong("user", userId).
					setParameter("statusActive", Status.ACTIVE).
					setParameter("statusCompleted", Status.COMPLETED).
					list();
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			if (result != null) {
				for (Object[] res : result) {
					Map<String, Object> resMap = new LinkedHashMap<>();
					
					Long courseId = (Long) res[0];
					Integer courseProgress = (int) res[1];
					Long instructorId = (Long) res[2];
					
					resMap.put("course", courseId);
					resMap.put("instructorId", instructorId);
					resMap.put("courseProgress", courseProgress);
					
					resultList.add(resMap);
				}
			}
			return resultList;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while loading course info");
		}	
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<User> getUsersAssignedToInstructor(long instructorId) throws DbConnectionException {
		try {
			String query = 
					"SELECT DISTINCT student " +
					"FROM CourseInstructor courseInstructor " +
					"LEFT JOIN courseInstructor.assignedStudents courseEnrollment "+
					"INNER JOIN courseEnrollment.user student "+
					"WHERE courseInstructor.id = :instructorId";
			
					@SuppressWarnings("unchecked")
					List<User> result = persistence.currentManager().createQuery(query).
							setLong("instructorId", instructorId).
							list();
					
					if(result == null) {
						return new ArrayList<>();
					} 
					
					return result;
		} catch(Exception e) {
			throw new DbConnectionException("Error while loading students assigned to instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getCourseInstructors(long courseId) throws DbConnectionException {
		try {
			String query = 
					"SELECT courseInstructor.id, instructor.avatarUrl, instructor.name, instructor.lastname, " +
					"instructor.position, courseInstructor.maxNumberOfStudents, size(courseEnrollment) " +
					"FROM CourseInstructor courseInstructor " +
					"INNER JOIN courseInstructor.user instructor "+
					"LEFT JOIN courseInstructor.assignedStudents courseEnrollment "+
					"INNER JOIN courseInstructor.course course "+
					"WHERE course.id = :courseId " +
					"GROUP BY courseInstructor.id";
			
					@SuppressWarnings("unchecked")
					List<Object[]> result = persistence.currentManager().createQuery(query).
							setLong("courseId", courseId).
							list();
					
					List<Map<String, Object>> resultList = new ArrayList<>();
					if (result != null) {
						for (Object[] res : result) {
							
							Long instructorId = (Long) res[0];
							if(instructorId != null) {
								Map<String, Object> resMap = new LinkedHashMap<>();
								String avatarUrl = (String) res[1];
								String firstName = (String) res[2];
								String lastName = (String) res[3];
								String position = (String) res[4];
								int maxNumberOfStudents = (int) res[5];
								int numberOfAssignedStudents = (int) res[6];
								
								resMap.put("instructorId", instructorId);
								resMap.put("avatarUrl", avatarUrl);
								resMap.put("firstName", firstName);
								resMap.put("lastName", lastName);
								resMap.put("position", position);
								resMap.put("maxNumberOfStudents", maxNumberOfStudents);
								resMap.put("numberOfAssignedStudents", numberOfAssignedStudents);
								
								resultList.add(resMap);
							}

						}
				   }
					
				return resultList;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course instructors");
		}
	}
	
	@Override
	@Transactional(readOnly = false) 
	public void assignInstructorToStudent(long studentId, long instructorId, long courseId) throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager().load(User.class, studentId);
			Course course = (Course) persistence.currentManager().load(Course.class, courseId);
			CourseInstructor instructor = (CourseInstructor) persistence.currentManager().load(CourseInstructor.class, instructorId);
			
			CourseEnrollment enrollment = getCourseEnrollment(user, course);
			if(enrollment != null) {
				enrollment.setAssignedToInstructor(true);
				enrollment.setInstructor(instructor);
			}
		} catch(Exception e) {
			throw new DbConnectionException("Error while assigning student to an instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getCourseIdsForInstructor(long instructorId) throws DbConnectionException {
		try {
			String query = 
					"SELECT course.id " +
					"FROM CourseInstructor courseInstructor " +
					"INNER JOIN courseInstructor.user instructor "+
					"INNER JOIN courseInstructor.course course "+
					"WHERE instructor.id = :instructorId";
			
					@SuppressWarnings("unchecked")
					List<Long> result = persistence.currentManager().createQuery(query).
							setLong("instructorId", instructorId).
							list();
					
				return result == null ? new ArrayList<>() : result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course instructors");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getCourseInstructor(long userId, long courseId) throws DbConnectionException {
		try {
			String query = 
					"SELECT courseInstructor.id, instructor.avatarUrl, instructor.id, instructor.name, instructor.lastname, " +
					"instructor.position, courseInstructor.maxNumberOfStudents, size(courseEnrollment) " +
					"FROM CourseInstructor courseInstructor " +
					"INNER JOIN courseInstructor.user instructor " +
					"INNER JOIN courseInstructor.course course " +
					"LEFT JOIN courseInstructor.assignedStudents courseEnrollment "+
					"WHERE instructor.id = :userId " +
					"AND course.id = :courseId";
			
					Object[] result = (Object[]) persistence.currentManager().createQuery(query)
							.setLong("userId", userId)
							.setLong("courseId", courseId)
							.uniqueResult();
					
					Map<String, Object> resMap = null;
					if (result != null) {
						Long instructorId = (Long) result[0];
						if(instructorId != null) {
							resMap = new LinkedHashMap<>();
							String avatarUrl = (String) result[1];
							long instructorUserId = (Long) result[2];
							String firstName = (String) result[3];
							String lastName = (String) result[4];
							String position = (String) result[5];
							int maxNumberOfStudents = (int) result[6];
							int numberOfAssignedStudents = (int) result[7];
							
							resMap.put("instructorId", instructorId);
							resMap.put("avatarUrl", avatarUrl);
							resMap.put("userId", instructorUserId);
							resMap.put("firstName", firstName);
							resMap.put("lastName", lastName);
							resMap.put("position", position);
							resMap.put("maxNumberOfStudents", maxNumberOfStudents);
							resMap.put("numberOfAssignedStudents", numberOfAssignedStudents);
						}

				   }
					
				return resMap;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course instructor data");
		}
	}
	
	/*remove instructor from course and based on boolean parameter, sets all their students
	  as unassigned and returns list of unassigned enrollment ids as a map entry with key 'manual'
	  or automatically assigns students to instructors and returns map with enrollment id - instructor id
	  key value pairs as a map entry with key 'automatic'. With automatic assign, there is a possibility to
	  have unassigned students if maximum capacity is reached for all instructors and in that case, list of 
	  unassigned enrollment ids is also returned
	*/
	@Override
	@Transactional(readOnly = false)
	public Map<String, Object> removeInstructorFromCourse(long courseInstructorId, long courseId, 
			boolean reassignAutomatically) throws DbConnectionException {
		try {
			CourseInstructor instructor = (CourseInstructor) persistence.currentManager().
					load(CourseInstructor.class, courseInstructorId);
			List<Long> enrollmentIds = getCourseEnrollmentsForInstructor(courseInstructorId);
			Map<String, Object> result = null;
			if(reassignAutomatically) {
				result = assignStudentsAutomatically(courseId, enrollmentIds, 
						courseInstructorId);
				return result;
			} else {
				result = new HashMap<>();
				updateStudentsAssigned(instructor, null, enrollmentIds);
				result.put("manual", enrollmentIds);
			}
			persistence.currentManager().delete(instructor);
			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing instructor from course");
		}
	}
	
	private Map<String, Object> assignStudentsAutomatically(long courseId, List<Long> enrollmentIds, long courseInstructorId) {
		Map<String, Object> result = resourceFactory
				.assignStudentsToInstructorAutomatically(courseId, enrollmentIds, courseInstructorId);
		@SuppressWarnings("unchecked")
		List<Long> unassigned = (List<Long>) result.get("unassigned");
		if(unassigned != null && !unassigned.isEmpty()) {
			CourseInstructor instructor = (CourseInstructor) persistence.currentManager().
					load(CourseInstructor.class, courseInstructorId);
			updateStudentsAssigned(instructor, null, unassigned);
		}
		Map<String, Object> res = new HashMap<>();
		@SuppressWarnings("unchecked")
		Map<Long, Long> ids = (Map<Long, Long>) result.get("assigned");
		res.put("automatic", ids);
		res.put("manual", unassigned);
		return res;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Map<String, Object> reassignStudentsAutomatically(long instructorId, long courseId) throws DbConnectionException {
		try {
			List<Long> enrollmentIds = getCourseEnrollmentsForInstructor(instructorId);
			return assignStudentsAutomatically(courseId, enrollmentIds, instructorId);
		} catch(Exception e) {
			throw new DbConnectionException("Error while reassigning students");
		}
	}
	
	private List<Long> getCourseEnrollmentsForInstructor(long instructorId) throws DbConnectionException {
		try {
			String query = 
					"SELECT enrollment.id " +
					"FROM CourseEnrollment enrollment " +
					"INNER JOIN enrollment.instructor courseInstructor "+
					"WHERE courseInstructor.id = :instructorId";
			
				@SuppressWarnings("unchecked")
				List<Long> result = persistence.currentManager().createQuery(query).
						setLong("instructorId", instructorId).
						list();
					
					
				return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course enrollments for instructor");
		}

	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getBasicInstructorInfo(long instructorId) throws DbConnectionException {
		try {
			String query = 
					"SELECT instructor.name, instructor.lastname, courseInstructor.maxNumberOfStudents, " +
					"student.id, student.name, student.lastname, student.avatarUrl, instructor.id " +
					"FROM CourseInstructor courseInstructor " +
					"INNER JOIN courseInstructor.user instructor " +
					"LEFT JOIN courseInstructor.assignedStudents enrollment " +
					"LEFT JOIN enrollment.user student " +
					"WHERE courseInstructor.id = :instructorId";
			
					@SuppressWarnings("unchecked")
					List<Object[]> result = persistence.currentManager().createQuery(query).
							setLong("instructorId", instructorId).
							list();
					
					Map<String, Object> resMap = null;
					if (result != null) {
						resMap = new HashMap<>();
					    boolean first = true;
					    List<Map<String, Object>> assignedStudents = new ArrayList<>();
					    for(Object[] res : result) {
					    	if(first) {
					    		String firstName = (String) res[0];
								String lastName = (String) res[1];
								int maxNumberOfStudents = (int) res[2];
								long userId = (long) res[7];
								resMap.put("firstName", firstName);
								resMap.put("lastName", lastName);
								resMap.put("maxNumberOfStudents", maxNumberOfStudents);
								resMap.put("userId", userId);
								first = false;
							}
					    	Long id = (Long) res[3];
					    	if(id != null) {
					    		Map<String, Object> studentMap = new HashMap<>();
					    		studentMap.put("id", id);
					    		String firstName = (String) res[4];
					    		studentMap.put("firstName", firstName);
					    		String lastName = (String) res[5];
					    		studentMap.put("lastName", lastName);
					    		String avatarUrl = (String) res[6];
					    		studentMap.put("avatarUrl", avatarUrl);
					    		assignedStudents.add(studentMap);
					    	}
					    }
					    if(!assignedStudents.isEmpty()) {
					    	resMap.put("students", assignedStudents);
					    }
				   }
					
				return resMap;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving instructor data");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = false)
	public void updateStudentsAssignedToInstructor(long instructorId, long courseId, List<Long> studentsToAssign, List<Long> studentsToUnassign) throws DbConnectionException {
		try {
			CourseInstructor instructor = (CourseInstructor) persistence.currentManager()
					.load(CourseInstructor.class, instructorId);
			String query1 = "SELECT enrollment.id " +
					"FROM CourseEnrollment enrollment "+
					"INNER JOIN enrollment.course course " + 
					"INNER JOIN enrollment.user user " + 
					"WHERE course.id = :courseId " +
					"AND user.id IN (:ids)";
			
			List<Long> idsAssign = null;
			if(studentsToAssign != null && !studentsToAssign.isEmpty()) {
				idsAssign = persistence.currentManager().createQuery(query1)
						.setLong("courseId", courseId)
						.setParameterList("ids", studentsToAssign)
						.list();
			}
			List<Long> idsUnAssign = null;
			if(studentsToUnassign != null && !studentsToUnassign.isEmpty()) {		
				idsUnAssign = persistence.currentManager().createQuery(query1)
						.setLong("courseId", courseId)
						.setParameterList("ids", studentsToUnassign)
						.list();
			}
			
			updateStudentsAssigned(instructor, idsAssign, idsUnAssign);
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}

	private void updateStudentsAssigned(CourseInstructor instructor, List<Long> enrollmentsToAssign, List<Long> enrollmentsToUnassign) throws DbConnectionException {
		try {
			String query = 
					"UPDATE " +
					"CourseEnrollment enrollment " +
				    "set enrollment.instructor = :instructor, " +
					"enrollment.assignedToInstructor = :assigned " +
				    "WHERE enrollment.id IN " +
						"(:ids)";
			
			if(enrollmentsToAssign != null && !enrollmentsToAssign.isEmpty()) {
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", instructor)
								.setBoolean("assigned", true)
								.setParameterList("ids", enrollmentsToAssign)
								.executeUpdate();
			}
			if(enrollmentsToUnassign != null && !enrollmentsToUnassign.isEmpty()) {						
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", null)
								.setBoolean("assigned", false)
								.setParameterList("ids", enrollmentsToUnassign)
								.executeUpdate();
			}
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public CourseInstructor saveCourseInstructor(long instructorId, long userId, long courseId, 
			int maxNumberOfAssignedStudents) throws DbConnectionException {
		int defaultNumberOfStudentsPerInstructor = 0;
		if(maxNumberOfAssignedStudents == 0) {
			defaultNumberOfStudentsPerInstructor = getDefaultNumberOfStudentsPerInstructor(courseId);
		} else {
			defaultNumberOfStudentsPerInstructor = maxNumberOfAssignedStudents;
		}
		try {
			if(instructorId != 0) {
				CourseInstructor courseInstructor = (CourseInstructor) persistence.currentManager().load(CourseInstructor.class,
						instructorId);
				courseInstructor.setMaxNumberOfStudents(defaultNumberOfStudentsPerInstructor);
				return courseInstructor;
			} else {
				User user = (User) persistence.currentManager().load(User.class, userId);
				Course course = (Course) persistence.currentManager().load(Course.class, courseId);
				
				CourseInstructor instructor = new CourseInstructor();
				instructor.setUser(user);
				instructor.setCourse(course);
				instructor.setMaxNumberOfStudents(defaultNumberOfStudentsPerInstructor);
				
				return saveEntity(instructor);
			}
		} catch(Exception e) {
			throw new DbConnectionException("Error while assigning instructor to a course");
		}
	}
	
	private int getDefaultNumberOfStudentsPerInstructor(long courseId) {
		String query = "SELECT course.defaultNumberOfStudentsPerInstructor " +
					   "FROM Course course " +
					   "WHERE course.id = :courseId";
		int result = (int) persistence.currentManager().createQuery(query)
						.setLong("courseId", courseId)
						.uniqueResult();
		
		return result;
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateStudentsAssignedToInstructors(List<Map<String, Object>> data) throws DbConnectionException {
		try {
			if(data != null) {
				for(Map<String, Object> map : data) {
					long instructorId = (long) map.get("id");
					long courseId = (long) map.get("courseId");
					@SuppressWarnings("unchecked")
					List<Long> usersToAssign = (List<Long>) map.get("assign");
					updateStudentsAssignedToInstructor(instructorId, courseId, usersToAssign, null);
				}
			}
		} catch(Exception e) {
			throw new DbConnectionException("Error while reassigning students");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean areStudentsManuallyAssignedToInstructor(long courseId) throws DbConnectionException {
		try {
			String query = 
					"SELECT course.manuallyAssignStudentsToInstructors " +
					"FROM Course course " +
					"WHERE course.id = :courseId";
			
				Boolean res = (Boolean) persistence.currentManager().createQuery(query).
						setLong("courseId", courseId).
						uniqueResult();
				if(res == null) {
					throw new Exception();
				} 
				return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course data");
		}

	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getUserIdsForEnrollments(List<Long> enrollmentIds) throws DbConnectionException {
		try {
			if(enrollmentIds == null || enrollmentIds.isEmpty()) {
				return null;
			}
			String query = 
					"SELECT user.id " +
					"FROM CourseEnrollment enrollment " +
					"INNER JOIN enrollment.user user " +
					"WHERE enrollment.id IN (:enrollmentIds)";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query).
					setParameterList("enrollmentIds", enrollmentIds).
					list();
			if(res == null) {
				return new ArrayList<>();
			} 
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user ids");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCourseTitle(long courseId) throws DbConnectionException {
		try {
			String query = 
					"SELECT course.title " +
					"FROM Course course " +
					"WHERE course.id = :courseId";
			
			String res = (String) persistence.currentManager().createQuery(query).
					setLong("courseId", courseId).
					uniqueResult();
				return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course title");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getUserIdForEnrollment(long enrollmentId) throws DbConnectionException {
		try {
			String query = 
					"SELECT user.id " +
					"FROM CourseEnrollment enrollment " +
					"INNER JOIN enrollment.user user " +
					"WHERE enrollment.id = :enrollmentId";
			
			Long res = (Long) persistence.currentManager().createQuery(query).
					setLong("enrollmentId", enrollmentId).
					uniqueResult();
			if(res == null) {
				return 0;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getUserIdForInstructor(long instructorId) throws DbConnectionException {
		try {
			String query = 
					"SELECT user.id " +
					"FROM CourseInstructor instructor " +
					"INNER JOIN instructor.user user " +
					"WHERE instructor.id = :instructorId";
			
			Long res = (Long) persistence.currentManager().createQuery(query).
					setLong("instructorId", instructorId).
					uniqueResult();
			if(res == null) {
				return 0;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CourseCompetence> getCourseCompetences(long courseId) throws DbConnectionException {
		try {
			String query = 
					"SELECT competence " +
					"FROM Course course " +
					"LEFT JOIN course.competences competence " +
					"WHERE course.id = :courseId " +
					"ORDER BY competence.order";
			
			@SuppressWarnings("unchecked")
			List<CourseCompetence> competences = persistence.currentManager().createQuery(query).
					setLong("courseId", courseId).
					list();
			if(competences == null) {
				return new ArrayList<>();
			}
			return competences;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course competences");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Course createNewUntitledCourse(User maker, CreatorType creatorType) throws DbConnectionException {
		try {
			return saveNewCourse("Untitled", null, null, null, null, null, maker, creatorType, false, false);
		} catch(Exception e) {
			throw new DbConnectionException("Error while creating new course");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public Course updateCourse(long courseId, String title, String description, Collection<Tag> tags, 
			Collection<Tag> hashtags, boolean published, User user) throws DbConnectionException {
		try {
			Course updatedCourse = resourceFactory.updateCourse(
					courseId, 
					title, 
					description, 
					tags, 
					hashtags,
					published);
			
			eventFactory.generateEvent(EventType.Edit, user, updatedCourse);
			
			return updatedCourse;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating course");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void removeFeed(long courseId, long feedSourceId) throws DbConnectionException {
		try {
			Course course = (Course) persistence.currentManager().load(Course.class, courseId);
			FeedSource feedSource = (FeedSource) persistence.currentManager().load(FeedSource.class, feedSourceId);
			course.getBlogs().remove(feedSource);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing blog from the course");
		}
	}
	
	//returns true if new blog is added to the course, false if it already exists
	@Override
	@Transactional(readOnly = false)
	public boolean saveNewCourseFeed(long courseId, String feedLink) throws DbConnectionException {
		try {
			Course course = (Course) persistence.currentManager().load(Course.class, courseId);
			FeedSource feedSource = feedSourceManager.getOrCreateFeedSource(null, feedLink);
			List<FeedSource> blogs = course.getBlogs();
			if(!blogs.contains(feedSource)) {
				blogs.add(feedSource);
				return true;
			}
			return false;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding new course blog");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isMandatoryStructure(long courseId) throws DbConnectionException {
		try {
			String query = 
					"SELECT course.competenceOrderMandatory " +
					"FROM Course course " +
					"WHERE course.id = :courseId";

			Boolean mandatory = (Boolean) persistence.currentManager().createQuery(query).
					setLong("courseId", courseId).
					uniqueResult();
			if(mandatory == null) {
				return false;
			}
			return mandatory;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading course data");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateCourseCompetences(long courseId, boolean mandatoryStructure, 
			List<CompetenceData> competences) throws DbConnectionException {
		try {
			Course course = (Course) persistence.currentManager().load(Course.class, courseId);
			course.setCompetenceOrderMandatory(mandatoryStructure);
			saveCompetences(course, competences);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving course competences");
		}
	}
	
	private void saveCompetences(Course course, List<CompetenceData> competences) {
		try {
			for (CompetenceData c : competences) {
				switch(c.getStatus()) {
					case CREATED:
						createNewCourseCompetence(course, c.getCompetenceId(), c.getOrder());
						break;
					case CHANGED:
						updateCourseCompetence(c.getCourseCompetenceId(), c.getOrder());
						break;
					case UP_TO_DATE:
						break;
					case REMOVED:
						deleteById(CourseCompetence.class, c.getCourseCompetenceId(), persistence.currentManager());
						break;
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competences");
		}
	}
	private CourseCompetence updateCourseCompetence(long courseCompetenceId, long order) {
		try {
			CourseCompetence cc = (CourseCompetence) persistence.currentManager().
					load(CourseCompetence.class, courseCompetenceId);
			cc.setOrder(order);
			return cc;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving course competence");
		}
	}
	private CourseCompetence createNewCourseCompetence(Course course, long competenceId, long order) {
		try {
			CourseCompetence cc = new CourseCompetence();
			Competence c = (Competence) persistence.currentManager().load(Competence.class, competenceId);
			cc.setCompetence(c);
			cc.setOrder(order);
			cc.setCourse(course);
			return saveEntity(cc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving course competence");
		}
	}
}

