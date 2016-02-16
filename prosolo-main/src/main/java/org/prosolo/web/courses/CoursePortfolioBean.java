/**
 * 
 */
package org.prosolo.web.courses;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interfaceSettings.InterfaceCacheObserver;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.courses.util.CourseDataConverter;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "coursePortfolioBean")
@Component("coursePortfolioBean")
@Scope("session")
public class CoursePortfolioBean implements Serializable {

	private static final long serialVersionUID = 1342193465369917836L;

	private static Logger logger = Logger.getLogger(CoursePortfolioBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CourseManager courseManager;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private PortfolioBean portfolioBean;
	@Autowired private EventFactory eventFactory;
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LearnBean learningGoalsBean;

	// course portfolio
	private long coursePortfolioId;
	private List<CourseData> activeCourses;
	private List<CourseData> futureCourses;
	private List<CourseData> completedCourses;
	private List<CourseData> withdrawnCourses;
	
	private boolean portfolioEmpty;
	
	// activating course from withdrawn
	private boolean restorePreviousLearning = true;
	private CourseData courseToActivate;
	
	/*
	 * ACTIONS
	 */
	@PostConstruct
	public void initializeCoursePortfolio() {
		if (coursePortfolioId == 0 && loggedUser != null && loggedUser.isLoggedIn()) {
			CoursePortfolio coursePortfolio = courseManager.getOrCreateCoursePortfolio(loggedUser.getUser());
			this.coursePortfolioId = coursePortfolio.getId();
			
			this.activeCourses = CourseDataConverter.convertToCoursesData(coursePortfolio.findEnrollment(Status.ACTIVE));
			Collections.sort(this.activeCourses);
			
			this.futureCourses = CourseDataConverter.convertToCoursesData(coursePortfolio.findEnrollment(Status.NOT_STARTED));
			Collections.sort(this.futureCourses);
			
			this.completedCourses = CourseDataConverter.convertToCoursesData(coursePortfolio.findEnrollment(Status.COMPLETED));
			Collections.sort(this.completedCourses);
			
			this.withdrawnCourses = CourseDataConverter.convertToCoursesData(coursePortfolio.findEnrollment(Status.WITHDRAWN));
			Collections.sort(this.withdrawnCourses);
			
			checkIfPortfolioIsEmpty();
		}
	}
	
	private void checkIfPortfolioIsEmpty() {
		if (activeCourses.isEmpty() && futureCourses.isEmpty() && 
				completedCourses.isEmpty() && withdrawnCourses.isEmpty()) {
			portfolioEmpty = true;
		} else {
			portfolioEmpty = false;
		}
	}

	public void addActiveCourse(final CourseEnrollment enrollment) {
		CourseData newCourseData = new CourseData(enrollment);
		newCourseData.setActive(true);
		newCourseData.setInFutureCourses(false);
		newCourseData.setInWithdrawnCourses(false);
		
		activeCourses.add(newCourseData);
		
		GoalDataCache goalData = learningGoalsBean.getData().getDataForTargetGoal(newCourseData.getTargetGoalId());
		
		if (goalData != null) {
			newCourseData.setProgress(goalData.getData().getProgress());
		}
		
		checkIfPortfolioIsEmpty();
	}
	
	public void activateCourse(final CourseData courseData, String context) {
		activateCourse(courseData, restorePreviousLearning, context, null, null, null);
	}
	
	public CourseEnrollment activateCourse(final CourseData courseData, boolean restorePreviousLearning, 
			String context) {
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		return activateCourse(courseData, restorePreviousLearning, context, page, lContext, service);
	}
	
	public CourseEnrollment activateCourse(final CourseData courseData, boolean restorePreviousLearning, 
			String context, String page, String learningContext, String service) {
		// remove from future courses
		removeFromFutureCourses(courseData.getId());
		
		// remove from withdrawn courses
		removeFromWithdrawnCourses(courseData.getId());
		
		
		CourseEnrollment enrollment = null;
		
		try {
			Course course = courseManager.loadResource(Course.class, courseData.getId());
		
			// check if maybe this course is in withdrawn courses
			if (restorePreviousLearning && courseData.getTargetGoalId() != 0) {
				
				try {
					enrollment = courseManager.loadResource(CourseEnrollment.class, courseData.getEnrollmentId());
					
					// if enrollment has not been active before, then add course's competences to it. 
					// If it was withdrawn, then competences are already there
					if (enrollment.getStatus().equals(Status.NOT_STARTED)) {
						enrollment = courseManager.addCourseCompetencesToEnrollment(course, enrollment);
					}
					
					enrollment = courseManager.activateCourseEnrollment(loggedUser.getUser(), enrollment, context,
							page, learningContext, service);
					
					GoalDataCache goalData = learningGoalsBean.getData().getDataForTargetGoal(courseData.getTargetGoalId());
					
					if (goalData != null) {
						goalData.getData().setCourse(courseData);
						goalData.getData().setConnectedWithCourse(true);
					} else {
						learningGoalsBean.getData().addGoal(loggedUser.getUser(), enrollment.getTargetGoal());
					}
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			} else {
				TargetLearningGoal newTargetGoal = goalManager.createNewCourseBasedLearningGoal(
						loggedUser.getUser(), 
						course,
						null,
						"");
				
				enrollment = courseManager.enrollInCourse(loggedUser.getUser(), course, newTargetGoal, 
						context, page, learningContext, service);
				
				newTargetGoal.setCourseEnrollment(enrollment);
				newTargetGoal = courseManager.saveEntity(newTargetGoal);
				
				eventFactory.generateChangeProgressEvent(loggedUser.getUser(), newTargetGoal, 0);
				
				if (learningGoalsBean != null) {
					GoalDataCache goalData = learningGoalsBean.getData().addGoal(loggedUser.getUser(), newTargetGoal);
					courseData.setTargetGoalId(newTargetGoal.getId());
					goalData.getData().setCourse(courseData);
					goalData.getData().setConnectedWithCourse(true);
				}
			}
		
			courseData.setEnrollment(enrollment);
			courseData.setActive(true);
			courseData.setInCompletedCourses(false);
			courseData.setInFutureCourses(false);
			courseData.setInWithdrawnCourses(false);
		
			portfolioBean.initGoalStatistics();
			addActiveCourse(enrollment);
			
			logger.debug("User " + loggedUser.getUser() + " is now enrollened in a course "+course.getId());
			
			PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
					ResourceBundleUtil.getMessage(
							"courses.coursePortfolio.courseActivated.growl", 
							loggedUser.getLocale(), 
							course.getTitle()));
		} catch (EventException e) {
			logger.error(e);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return enrollment;
	}

	private void removeFromFutureCourses(long courseId) {
		Iterator<CourseData> iterator = futureCourses.iterator();
		
		while (iterator.hasNext()) {
			CourseData courseData1 = (CourseData) iterator.next();
			
			if (courseData1.getId() == courseId) {
				iterator.remove();
				break;
			}
		}
	}

	public void removeFromWithdrawnCourses(long courseId) {
		Iterator<CourseData> iterator1 = withdrawnCourses.iterator();
		
		while (iterator1.hasNext()) {
			CourseData courseData1 = (CourseData) iterator1.next();
			
			if (courseData1.getId() == courseId) {
				iterator1.remove();
				break;
			}
		}
	}
	
	public void addToFutureCourses(final CourseData courseData) {
//		courseData.setActive(false);
		courseData.setInFutureCourses(true);
		this.futureCourses.add(courseData);
		checkIfPortfolioIsEmpty();
		Collections.sort(this.futureCourses);
		
		try {
			CourseEnrollment enrollment = courseManager.addToFutureCourses(coursePortfolioId, courseData.getId());
			courseData.setEnrollment(enrollment);
			
			PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
					ResourceBundleUtil.getMessage(
							"courses.coursePortfolio.moveToFutureCourses.growl", 
							loggedUser.getLocale(), 
							courseData.getTitle()));
		} catch (ResourceCouldNotBeLoadedException e) {
			try {
				PageUtil.fireErrorMessage("coursesFormGrowl", 
						ResourceBundleUtil.getMessage(
								"courses.coursePortfolio.errorMoveToFutureCourses.growl", 
								loggedUser.getLocale(), 
								courseData.getTitle()));
			} catch (KeyNotFoundInBundleException e1) {
				logger.error(e1);
			}
			logger.error(e);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}
	
	public void deleteCourse(CourseData course, boolean deleteLearningHistory, String context,
			String page, String lContext, String service) {
		removeCourse(course, deleteLearningHistory, page, lContext, service);
		
		GoalDataCache goalData = learningGoalsBean.getData().getDataForTargetGoal(course.getTargetGoalId());
		
		if (goalData != null) {
			learningGoalsBean.deleteGoal(goalData, context);

			if (deleteLearningHistory) {
				course.setEnrollment(null);
				course.setTargetGoalId(0);
			}
			
			// reset collaborators' caches
			InterfaceCacheObserver cacheUpdater = ServiceLocator.getInstance().getService(InterfaceCacheObserver.class);
	    	
	    	if (cacheUpdater != null) {
	    		cacheUpdater.asyncResetGoalCollaborators(goalData.getData().getGoalId(), loggedUser.getUser());
	    	}
		} else {
			logger.error("This should not happen, there should be goal data gor target learnign goal: "+course.getTargetGoalId() );
		}
		
		try {
			PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
					ResourceBundleUtil.getMessage(
							"courses.course.withdrawn.growl", 
							loggedUser.getLocale(), 
							course.getTitle()));
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}

	public void removeCourse(final CourseData courseData, final boolean deleteLearningHistory, 
			String page, String learningContext, String service) {
		boolean deletingActiveCourse = false;
		
		// if inside active courses, remove it from there
		Iterator<CourseData> activeCoursesIterator = activeCourses.iterator();
		
		while (activeCoursesIterator.hasNext()) {
			CourseData courseData1 = (CourseData) activeCoursesIterator.next();
			
			if (courseData1.equals(courseData)) {
				activeCoursesIterator.remove();
				deletingActiveCourse = true;
				break;
			}
		}
		
		if (!deletingActiveCourse && futureCourses != null) {
			// remove from future courses
			Iterator<CourseData> iterator = futureCourses.iterator();
			
			while (iterator.hasNext()) {
				CourseData courseData1 = (CourseData) iterator.next();
				
				if (courseData1.equals(courseData)) {
					iterator.remove();
					break;
				}
			}
		}
		
		withdrawnCourses.add(courseData);
		courseData.setActive(false);
		courseData.setEnrolled(false);
		courseData.setInWithdrawnCourses(true);
		
		checkIfPortfolioIsEmpty();
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) courseManager.getPersistence().openSession();
				
				try {
					courseManager.withdrawFromCourse(loggedUser.getUser(), courseData.getEnrollmentId(), 
							deleteLearningHistory, session, page, learningContext, service);
				
					Course course = courseManager.loadResource(Course.class, courseData.getId(), session);
					eventFactory.generateEvent(EventType.COURSE_WITHDRAWN, loggedUser.getUser(), course);
					session.flush();
				} catch (EventException e) {
					logger.error(e);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				} catch(Exception e){
     				logger.error("Exception in handling message",e);
				} finally {
					if(session != null && session.isOpen()) {
						HibernateUtil.close(session);
					}
				}
			}
		});
	}
	
	public void completeCourse(final CourseData courseToComplete) {
		final long enrollmentId = courseToComplete.getEnrollmentId();
		
		boolean deletingActiveCourse = false;
		
		// if inside active courses, remove it from there
		Iterator<CourseData> activeCoursesIterator = activeCourses.iterator();
		
		while (activeCoursesIterator.hasNext()) {
			CourseData courseData = (CourseData) activeCoursesIterator.next();
			
			if (courseData.equals(courseToComplete)) {
				activeCoursesIterator.remove();
				deletingActiveCourse = true;
				break;
			}
		}
		
		if (!deletingActiveCourse && futureCourses != null) {
			removeFromFutureCourses(courseToComplete.getId());
		}
		
		courseToComplete.setActive(false);
		courseToComplete.setInFutureCourses(false);
		courseToComplete.setInCompletedCourses(true);
		courseToComplete.setDateFinished(new Date());
		this.completedCourses.add(courseToComplete);
		Collections.sort(this.completedCourses);
		
		try {
			final CourseEnrollment enrollment = courseManager.loadResource(CourseEnrollment.class, enrollmentId);
			eventFactory.generateEvent(EventType.COURSE_COMPLETED, loggedUser.getUser(), enrollment);
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Session session = (Session) courseManager.getPersistence().openSession();
					try{
					courseManager.completeCourseEnrollment(coursePortfolioId, enrollment, session);
					
					session.flush();
					}catch(Exception e){
	     				logger.error("Exception in handling message",e);
	     			
					} finally{
	     				HibernateUtil.close(session);
	     			} 
				}
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	public void replaceFutureCourse(CourseData updatedObjData) {
		for (int i = 0; i < futureCourses.size(); i++) {
			if (futureCourses.get(i).getId() == updatedObjData.getId()){
				futureCourses.set(i, updatedObjData);
				break;
			}
		}
	}
	
	public CourseData getActiveCourse(long targetGoalId) {
		if (activeCourses != null && !activeCourses.isEmpty()) {
			for (CourseData courseData : activeCourses) {
				if (courseData.getTargetGoalId() == targetGoalId) {
					return courseData;
				}
			}
		}
		return null;
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<CourseData> getFutureCourses() {
		initializeCoursePortfolio();
		return futureCourses;
	}

	public List<CourseData> getActiveCourses() {
		initializeCoursePortfolio();
		return activeCourses;
	}
	
	public List<CourseData> getCompletedCourses() {
		initializeCoursePortfolio();
		return completedCourses;
	}
	
	public List<CourseData> getWithdrawnCourses() {
		initializeCoursePortfolio();
		return withdrawnCourses;
	}

	public boolean isPortfolioEmpty() {
		return portfolioEmpty;
	}
 
	public void setCourseToActivate(CourseData courseToActivate) {
		this.courseToActivate = courseToActivate;
	}

	public CourseData getCourseToActivate() {
		return courseToActivate;
	}

	public boolean isRestorePreviousLearning() {
		return restorePreviousLearning;
	}

	public void setRestorePreviousLearning(boolean restorePreviousLearning) {
		this.restorePreviousLearning = restorePreviousLearning;
	}
	
	
}
