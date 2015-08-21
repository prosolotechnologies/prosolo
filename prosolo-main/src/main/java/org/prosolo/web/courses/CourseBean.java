/**
 * 
 */
package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.rest.courses.CourseParser;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseCompetenceData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.courses.util.CourseDataConverter;
import org.prosolo.web.dialogs.data.CompetenceFormData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.competences.CompetencesBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.SearchCompetencesBean;
import org.prosolo.web.search.data.CompetenceData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "courseBean")
@Component("courseBean")
@Scope("view")
public class CourseBean implements Serializable {

	private static final long serialVersionUID = -7267423572558023050L;

	private static final long DEFAULT_COMPETENCE_DURATION = 5;
	
	private static Logger logger = Logger.getLogger(CourseBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private CourseManager courseManager;
	@Autowired private CoursePortfolioBean coursePortfolioBean;
	@Autowired private TagManager tagManager;
	@Autowired private CompetenceManager competenceManager;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private EventFactory eventFactory;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private LearningGoalsBean learningGoalsBean;
	@Autowired private CompetencesBean competencesBean;

	private CourseData courseData = new CourseData();
	private CourseData basedOnCourseData = new CourseData();
	
	private String competencesJson;
	private String changeHolder = "1";
	
	// prerequisites, corequisites
	private List<Competence> suggestedPrerequisites = new ArrayList<Competence>();
	private List<Competence> suggestedCorequisites = new ArrayList<Competence>();
	private CourseCompetence recentlyAddedObjCompetence;
	
	// these are added competences before any editing was performed on a course
	private List<CourseCompetenceData> addedCompetencesBeforeSave;
	
	// competence suggestions
	private List<CompetenceData> suggestedCompetences;
	
	// create competence
	private CompetenceFormData competenceFormData = new CompetenceFormData();
	
	private boolean creatingNew;
	
	// used in a dialog
	private boolean hasLearningHistory = false;
	
	
	// PARAMETERS
	private long id;
	
	public void init() {
		if (id > 0) {
			try {
				Course course = courseManager.loadResource(Course.class, id);
				CourseEnrollment enrollment = courseManager.getCourseEnrollment(loggedUser.getUser(), course);

				courseData = new CourseData(course);
				
				if (enrollment != null) {
					courseData.setEnrollment(enrollment);
					hasLearningHistory = true;
					
					// set status
					if (enrollment.getStatus().equals(Status.ACTIVE)) {
						courseData.setActive(true);
					} else if (enrollment.getStatus().equals(Status.NOT_STARTED)) {
						courseData.setInFutureCourses(true);
					} else if (enrollment.getStatus().equals(Status.WITHDRAWN)) {
						courseData.setInWithdrawnCourses(true);;
					} else {
						courseData.setInCompletedCourses(true);
					}
					
					// move original course competences from added to original
					courseData.getOriginalCompetences().clear();
					
					courseData.setAddedCompetences(CourseDataConverter.convertToCompetenceCourseData(enrollment.getAddedCompetences(), true));
				
					List<CourseCompetence> courseCompetences = course.getCompetences();
					Iterator<CourseCompetenceData> iterator = courseData.getAddedCompetences().iterator();
					
					addedComsLoop: while (iterator.hasNext()) {
						CourseCompetenceData courseCompetenceData = (CourseCompetenceData) iterator.next();
						
						for (CourseCompetence courseCompetence : courseCompetences) {
							if (courseCompetenceData.getCompetenceId() == courseCompetence.getCompetence().getId()) {
								iterator.remove();
								
								courseCompetenceData.setOriginalDaysOffset(courseCompetence.getDaysOffset());
								courseCompetenceData.setOriginalDuration(courseCompetence.getDuration());
								courseData.getOriginalCompetences().add(courseCompetenceData);

								continue addedComsLoop;
							}
						}
					}
					
					this.addedCompetencesBeforeSave = new LinkedList<CourseCompetenceData>(courseData.getAddedCompetences());
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * ACTIONS
	 */
	public void enrollInCourse() {
		String context = PageUtil.getPostParameter("context");
		
		coursePortfolioBean.activateCourse(courseData, true, context);
	}
	
//	public void suspendCourse() {
//		coursePortfolioBean.deleteCourse(courseData);
//
//		this.courseData.setEnrolled(false);
//		this.courseData.setActive(false);
//		this.courseData.setInFutureCourses(true);
//	}
	
	public void addToFutureCourses() {
		if (coursePortfolioBean != null) {
			
			@SuppressWarnings("unused")
			String context = PageUtil.getPostParameter("context");
			
			coursePortfolioBean.addToFutureCourses(courseData);
			courseData.setInFutureCourses(true);
		}
	}
	
//	public void withdrawFromCourse(CourseData courseData) {
//		if (coursePortfolioBean != null) {
//			coursePortfolioBean.deleteCourse(courseData);
//		}
//	}
	
//	public void removeFromFutureCourses() {
//		if (coursePortfolioBean != null) {
//			coursePortfolioBean.deleteCourse(courseData);
//			courseData.setInFutureCourses(false);
//			
//			try {
//				PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
//						ResourceBundleUtil.getMessage(
//								"courses.course.removed.growl", 
//								loggedUser.getLocale(), 
//								courseData.getTitle()));
//			} catch (KeyNotFoundInBundleException e) {
//				logger.error(e);
//			}
//		}
//	}
	
	public void saveCourse() {
		List<CourseCompetenceData> allCompetenceData = new ArrayList<CourseCompetenceData>(courseData.getAddedCompetences());
		allCompetenceData.addAll(courseData.getOriginalCompetences());
		
		List<CourseCompetence> competences = new ArrayList<CourseCompetence>();
		List<CourseCompetenceData> competencesToDelete = this.addedCompetencesBeforeSave;
		GoalDataCache goalDataCache = learningGoalsBean.getData().getDataForTargetGoal(courseData.getTargetGoalId());
		
		for (CourseCompetenceData compsData : allCompetenceData) {
			try {
				CourseCompetence courseComp = null;
				
				if (compsData.isSaved()) {
					if (compsData.isDataChanged()) {
						courseComp = courseManager.updateCourseCompetence(
								compsData.getCourseCompetence(), 
								compsData.getModifiedDaysOffset(), 
								compsData.getModifiedDuration());
					} else {
						courseComp = compsData.getCourseCompetence();
					}
				} else {
					courseComp = courseManager.createCourseCompetence(
							compsData.getCompetenceId(), 
							compsData.getModifiedDaysOffset(), 
							compsData.getModifiedDuration());
					
					// creating target competence and adding it to the course-based goal
					
					competencesBean.connectCompetence(courseComp.getCompetence(), goalDataCache, "");
				}

				if (courseComp != null) {
					competences.add(courseComp);
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			
			// removing from competencesToDelete
			if (competencesToDelete != null)
				competencesToDelete.remove(compsData);
		}
		
		// deleting removed competences
		if (competencesToDelete != null && competencesToDelete.size() > 0) {
			for (CourseCompetenceData courseCompetenceData : competencesToDelete) {
				CompetenceDataCache compData = goalDataCache.getCompetenceDataCacheByCompId(courseCompetenceData.getCompetenceId());
				
				competencesBean.deleteCompetence(compData, goalDataCache);
			}
		}
		
		try {
			@SuppressWarnings("unused")
			CourseEnrollment updatedEnrollment = courseManager.updateEnrollment(
					courseData.getEnrollmentId(),
					competences);
			
			PageUtil.fireSuccessfulInfoMessage("browseCourses:courseSearchForm:growl", 
					ResourceBundleUtil.getMessage(
							"courses.course.detailsUpdated.growl", 
							loggedUser.getLocale()));
		} catch (ResourceCouldNotBeLoadedException e) {
			try {
				PageUtil.fireErrorMessage("browseCourses:courseSearchForm:growl", 
						ResourceBundleUtil.getMessage(
								"courses.course.errorDetaolsUpdated.growl", 
								loggedUser.getLocale()));
			} catch (KeyNotFoundInBundleException e1) {
				logger.error(e);
			}
			
			PageUtil.fireErrorMessage("browseCourses:courseSearchForm:growl", "");
			logger.error(e);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}

		try {
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.getFlash().setKeepMessages(true);
			context.redirect(context.getRequestContextPath() + "/plan.xhtml");
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	/*
	 * Invoked from the competence search 
	 */
	public void connectCompetence(Competence competence) {
		if (courseData != null) {
			CourseCompetence courseCompetence = new CourseCompetence(competence);
			
			if (courseCompetence.getDuration() == 0) {
				courseCompetence.setDuration(DEFAULT_COMPETENCE_DURATION);
			}
			courseData.getAddedCompetences().add(new CourseCompetenceData(courseCompetence));
			checkForPrerequisitesAndCorequisites(courseCompetence);
			
			serializeCompetencesToJson();
			
			// remove from suggested competences
			if (suggestedCompetences != null) {
				Iterator<CompetenceData> compIterator = suggestedCompetences.iterator();
				
				while (compIterator.hasNext()) {
					CompetenceData competenceData = (CompetenceData) compIterator.next();
					
					if (competenceData.getId() == competence.getId()) {
						compIterator.remove();
						break;
					}
				}
			}
			
			PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", "Added competence '"+competence.getTitle()+"'.");
		}
	}
	
	private void checkForPrerequisitesAndCorequisites(CourseCompetence courseCompetence) {
		this.recentlyAddedObjCompetence = courseCompetence;
		suggestedPrerequisites.clear();
		suggestedCorequisites.clear();
		
		Competence comp = courseManager.merge(courseCompetence.getCompetence());
		
		if (!comp.getPrerequisites().isEmpty()) {
			suggestedPrerequisites.addAll(comp.getPrerequisites());
		}
		if (!comp.getCorequisites().isEmpty()) {
			suggestedCorequisites.addAll(comp.getCorequisites());
		}
	}
	
	public void addSuggestedPrerequisitesCorequisites() {
//		int index = courseData.getCompetences().indexOf(recentlyAddedObjCompetence);
//		
//		// adding prerequisites
//		if (!suggestedPrerequisites.isEmpty()) {
//			// calculating longest duration in order to move recentlyAddedObjCompetence's offset to that number
//			int longestPrereqDuration = 0;
//			
//			for (Competence prereq : suggestedPrerequisites) {
//				if (prereq.getDuration() > longestPrereqDuration)
//					longestPrereqDuration = prereq.getDuration();
//			}
//			
//			for (Competence prereq : suggestedPrerequisites) {
//				CourseCompetence prereqObjCompetence = new CourseCompetence(prereq);
//				
//				if (prereqObjCompetence.getDuration() == 0) {
//					prereqObjCompetence.setDuration(DEFAULT_COMPETENCE_DURATION);
//				}
//				
//				if (longestPrereqDuration > recentlyAddedObjCompetence.getDaysOffset()) {
//					recentlyAddedObjCompetence.setDaysOffset(longestPrereqDuration);
//					prereqObjCompetence.setDaysOffset(0);
//				} else {
//					prereqObjCompetence.setDaysOffset(recentlyAddedObjCompetence.getDaysOffset() - prereqObjCompetence.getDuration());
//				}
//				
//				courseData.getCompetences().add(++index, prereqObjCompetence);
//			}
//		}
//		
//		// adding corequisites
//		if (!suggestedCorequisites.isEmpty()) {
//			for (Competence prereq : suggestedCorequisites) {
//				CourseCompetence coreqObjCompetence = new CourseCompetence(prereq);
//				
//				if (coreqObjCompetence.getDuration() == 0) {
//					coreqObjCompetence.setDuration(DEFAULT_COMPETENCE_DURATION);
//				}
//				coreqObjCompetence.setDaysOffset(recentlyAddedObjCompetence.getDaysOffset());
//				courseData.getCompetences().add(++index, coreqObjCompetence);
//			}
//		}
	}
	
	public void removeSuggestedPrerequisite(Competence comp) {
		if (suggestedPrerequisites.contains(comp)) {
			Iterator<Competence> iterator = suggestedPrerequisites.iterator();
			
			while (iterator.hasNext()) {
				Competence c = (Competence) iterator.next();
				
				if (comp.equals(c)) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	public void removeSuggestedCorequisite(Competence comp) {
		if (suggestedCorequisites.contains(comp)) {
			Iterator<Competence> iterator = suggestedCorequisites.iterator();
			
			while (iterator.hasNext()) {
				Competence c = (Competence) iterator.next();
				
				if (comp.equals(c)) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	public void clearSuggestedPrerequisitesCorequisites() {
		suggestedPrerequisites.clear();
		suggestedCorequisites.clear();
	}
	
	// invoked from new Competence form
	public void saveCompetence() {
		try {
			String title = StringUtil.cleanHtml(competenceFormData.getTitle());
			Competence competence = competenceManager.createCompetence(
					loggedUser.getUser(),
					title,
					StringUtil.cleanHtml(competenceFormData.getDescription()),
					competenceFormData.getValidity(),
					competenceFormData.getDuration(),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(competenceFormData.getTagsString())),
					competenceFormData.getPrerequisites(),
					competenceFormData.getCorequisites());

			courseData.getAddedCompetences().add(new CourseCompetenceData(new CourseCompetence(competence)));
			
			PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl",
					ResourceBundleUtil.getMessage(
							"courses.course.newCompetenceCreatedAndAdded.growl", 
							loggedUser.getLocale(), 
							title));
		} catch (EventException e) {
			logger.error(e);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}

	public void resetCompetenceFormData() {
		competenceFormData = new CompetenceFormData();
	}

	public void removeCompetence(CourseData course, long competenceId) {
		if (course != null && competenceId > 0) {
			course.removeAddedCourseCompetence(competenceId);
			serializeCompetencesToJson();
		}
	}
	
	public void initializeSuggestedCompetences() {
		if (suggestedCompetences == null) {
			
			if (courseData.getCourse() != null) {
				List<Long> idsOfcompetencesToExclude = getAllCompetencesIds();
				
				suggestedCompetences = SearchCompetencesBean.convertToCompetenceData(
						courseManager.getOtherUsersCompetences(
								courseData.getCourse(), 
								idsOfcompetencesToExclude, 
								loggedUser.getUser()));
			}
		}
	}
	
	public void addSuggestedCompetence(CompetenceData compData) {
		courseData.getAddedCompetences().add(new CourseCompetenceData(new CourseCompetence(compData.getCompetence())));
		
		PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", "Added competence '"+compData.getTitle()+"'.");
	}
	
	public void updateChanges() {
		String[] data = changeHolder.split(",");
		
		int order = Integer.parseInt(data[0]);
		int start = Integer.parseInt(data[1]);
		int duration = Integer.parseInt(data[2]);
		
		List<CourseCompetenceData> competences = new ArrayList<CourseCompetenceData>(courseData.getOriginalCompetences());
		competences.addAll(courseData.getAddedCompetences());
		
		CourseCompetenceData courseComp = competences.get(order-1);
		courseComp.setModifiedDaysOffset(start);
		courseComp.setModifiedDuration(duration+1);
		courseComp.setDataChanged(true);
	}
	
	/**
	 * Used for seach component to know which competences to exclude
	 */
	public List<Long> getAllCompetencesIds() {
		List<CourseCompetenceData> competences = new ArrayList<CourseCompetenceData>(courseData.getOriginalCompetences());
		competences.addAll(courseData.getAddedCompetences());
		
		return CourseDataConverter.getIdsOfCourseCompetences(competences);
	}
	
	/**
	 * Used for serializing current competences into a JSON
	 */
	public void serializeCompetencesToJson() {
		this.competencesJson = CourseParser.parseCourseCompetence(
				courseData.isEnrolled() ? courseData.getAddedCompetences() : null, 
				courseData.getOriginalCompetences(),
				courseData.isEnrolled()).toString();
	}
	
	/**
	 * Used in Show all competences dialog
	 */
	
	
	/*
	 * PARAMETERS
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	public void test() {

	}
	
	public long getId() {
		return id;
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public CourseData getCourse() {
		return courseData;
	}
	
	public CourseData getBasedOnCourseData() {
		return basedOnCourseData;
	}

	public String getCompetencesJson() {
		return competencesJson;
	}

	public String getChangeHolder() {
		return changeHolder;
	}

	public void setChangeHolder(String changeHolder) {
		this.changeHolder = changeHolder;
	}

	public List<Competence> getSuggestedPrerequisites() {
		return suggestedPrerequisites;
	}

	public List<Competence> getSuggestedCorequisites() {
		return suggestedCorequisites;
	}

	public CourseCompetence getRecentlyAddedObjCompetence() {
		return recentlyAddedObjCompetence;
	}

	public List<CompetenceData> getSuggestedCompetences() {
		return suggestedCompetences;
	}

	public CompetenceFormData getCompetenceFormData() {
		return competenceFormData;
	}

	public boolean isCreatingNew() {
		return creatingNew;
	}

	public void setCreatingNew(boolean creatingNew) {
		this.creatingNew = creatingNew;
	}

	public boolean isHasLearningHistory() {
		return hasLearningHistory;
	}
	
}
