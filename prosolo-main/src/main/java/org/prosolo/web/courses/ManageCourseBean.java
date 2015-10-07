/**
 * 
 */
package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.rest.courses.CourseParser;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseCompetenceData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.courses.util.CourseDataConverter;
import org.prosolo.web.dialogs.data.CompetenceFormData;
import org.prosolo.web.search.SearchCompetencesBean;
import org.prosolo.web.search.data.CompetenceData;
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
@ManagedBean(name = "manageCourseBean")
@Component("manageCourseBean")
@Scope("view")
public class ManageCourseBean implements Serializable {

	private static final long serialVersionUID = -4687357161654807601L;

	private static final long DEFAULT_COMPETENCE_DURATION = 5;
	
	private static Logger logger = Logger.getLogger(ManageCourseBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CourseManager courseManager;
	@Autowired private TagManager tagManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private CompetenceManager competenceManager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private CourseData courseData = new CourseData();
	private CourseData basedOnCourseData = new CourseData();
	
	private String competencesJson;
	private String changeHolder = "1";
	
	// prerequisites, corequisites
	private List<Competence> suggestedPrerequisites = new ArrayList<Competence>();
	private List<Competence> suggestedCorequisites = new ArrayList<Competence>();
	private CourseCompetence recentlyAddedObjCompetence;
	
	// competence suggestions
	private List<CompetenceData> suggestedCompetences;
	
	// create competence
	private CompetenceFormData competenceFormData = new CompetenceFormData();
	
	private boolean creatingNew;
	private CourseData courseToDelete;
	
	
	private long id;
	
	public void init() {
		if (id > 0) {
			try {
				Course course = courseManager.loadResource(Course.class, id);
				courseData = new CourseData(course);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			this.creatingNew = false;
		} else {
			this.creatingNew = true;
			this.courseData.setCreatorType(CreatorType.MANAGER);
			this.courseData.setCreator(loggedUser.getUser());
		}
	}
	
	/*
	 * ACTIONS
	 */
	
	public void autosave() {
		try {
			updateCourse();
			
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
							"manager.courses.autosave.success", 
							loggedUser.getLocale()));
		} catch (KeyNotFoundInBundleException | EventException e) {
			logger.error(e);
		}
	}
	
	public void saveCourse() {
		try {
			if (creatingNew) {
				Course course = courseManager.updateCompetencesAndSaveNewCourse(
						courseData.getTitle(), 
						courseData.getDescription(), 
						courseData.getBasedOnCourse(), 
						courseData.getOriginalCompetences(),
						new HashSet<Tag>(tagManager.parseCSVTagsAndSave(courseData.getTagsString())),
						new HashSet<Tag>(tagManager.parseCSVTagsAndSave(courseData.getHashtagsString())),
						loggedUser.getUser(), 
						CreatorType.MANAGER,
						courseData.isStudentsCanAddNewCompetences(),
						courseData.isPublished());
				
				try {
					PageUtil.fireSuccessfulInfoMessage("browseCourses:courseSearchForm:growl", 
							ResourceBundleUtil.getMessage(
									"manager.courses.createCourse.success", 
									loggedUser.getLocale(), 
									course.getTitle()));
				} catch (KeyNotFoundInBundleException e) {
					logger.error(e);
				}
			} else {
				updateCourse();

				try {
					PageUtil.fireSuccessfulInfoMessage("browseCourses:courseSearchForm:growl", 
							ResourceBundleUtil.getMessage(
									"manager.courses.updateCourse.success", 
									loggedUser.getLocale()));
				} catch (KeyNotFoundInBundleException e) {
					logger.error(e);
				}
			}
			
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.getFlash().setKeepMessages(true);
			context.redirect(context.getRequestContextPath() + "/manage/credentials");
		} catch (EventException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	private void updateCourse() throws EventException {
		@SuppressWarnings("unused")
		Course updatedCourse = courseManager.updateCourse(
				courseData.getCourse(), 
				courseData.getTitle(), 
				courseData.getDescription(), 
				courseData.getOriginalCompetences(),
				new HashSet<Tag>(tagManager.parseCSVTagsAndSave(courseData.getTagsString())),
				new HashSet<Tag>(tagManager.parseCSVTagsAndSave(courseData.getHashtagsString())),
				courseData.getBlogs(),
				loggedUser.getUser(),
				courseData.isStudentsCanAddNewCompetences(),
				courseData.isPublished());
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
			courseData.getOriginalCompetences().add(new CourseCompetenceData(courseCompetence));
			checkForPrerequisitesAndCorequisites(courseCompetence);
			
			serializeCompetencesToJson();
			
			try {
				updateCourse();
				PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
						ResourceBundleUtil.getMessage(
								"manager.courses.addCompetenceToCourse", 
								loggedUser.getLocale(), 
								competence.getTitle(),
								courseData.getTitle()));
			} catch (KeyNotFoundInBundleException | EventException e) {
				logger.error(e);
			}
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

			courseData.getOriginalCompetences().add(new CourseCompetenceData(new CourseCompetence(competence)));
			
			try {
				PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
						ResourceBundleUtil.getMessage(
								"manager.courses.createAndAddCompetenceToCourse", 
								loggedUser.getLocale(), 
								title));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		} catch (EventException e) {
			logger.error(e);
		}
	}

	public void resetCompetenceFormData() {
		competenceFormData = new CompetenceFormData();
	}

	public void removeCompetence(CourseData course, long competenceId) {
		if (course != null && competenceId > 0) {
			
			course.removeOriginalCourseCompetence(competenceId);
			serializeCompetencesToJson();
			
			
			try {
				updateCourse();
				PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", 
						ResourceBundleUtil.getMessage(
								"manager.courses.removeCompetenceFromCourse", 
								loggedUser.getLocale()));
			} catch (KeyNotFoundInBundleException | EventException e) {
				logger.error(e);
			}
		}
	}
	
	public void initializeSuggestedCompetences() {
		if (suggestedCompetences == null) {
			
			if (courseData.getCourse() != null) {
				List<Long> idsOfcompetencesToExclude = CourseDataConverter.getIdsOfCourseCompetences(courseData.getAddedCompetences());
				
				suggestedCompetences = SearchCompetencesBean.convertToCompetenceData(
						courseManager.getOtherUsersCompetences(
								courseData.getCourse(), 
								idsOfcompetencesToExclude, 
								loggedUser.getUser()));
			}
		}
	}
	
	public void addSuggestedCompetence(CompetenceData compData) {
		courseData.getOriginalCompetences().add(new CourseCompetenceData(new CourseCompetence(compData.getCompetence())));
		
		PageUtil.fireSuccessfulInfoMessage("coursesFormGrowl", "Added competence '"+compData.getTitle()+"'.");
	}
	
	public void updateChanges() {
		String[] data = changeHolder.split(",");
		
		int order = Integer.parseInt(data[0]);
		int start = Integer.parseInt(data[1]);
		int duration = Integer.parseInt(data[2]);
		
		CourseCompetenceData courseComp = courseData.getOriginalCompetences().get(order-1);
		courseComp.setModifiedDaysOffset(start);
		courseComp.setModifiedDuration(duration+1);
		courseComp.setDataChanged(true);
	}
	
	/**
	 * Used for serializing current competences into a JSON
	 */
	public void serializeCompetencesToJson() {
		this.competencesJson = CourseParser.parseCourseCompetence(courseData.getOriginalCompetences(), null, false).toString();
	}
	
	public void deleteCourse() {
		if (courseToDelete == null) {
			return;
		}
		
		try {
			Course course = courseManager.deleteCourse(courseToDelete.getId());
			
			eventFactory.generateEvent(EventType.Delete, loggedUser.getUser(), course);
			
			// TODO: Zoran, somehow synchronously delete course from indexes before courses form is refreshed
			
			PageUtil.fireSuccessfulInfoMessage("browseCourses:courseSearchForm:growl", 
					ResourceBundleUtil.getMessage(
							"manager.courses.deleteCourse.success", 
							loggedUser.getLocale()));
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			
			try {
				PageUtil.fireErrorMessage("browseCourses:courseSearchForm:growl", 
						ResourceBundleUtil.getMessage(
								"manager.courses.deleteCourse.error", 
								loggedUser.getLocale()));
			} catch (KeyNotFoundInBundleException e1) {
				logger.error(e1);
			}
		} catch (EventException e) {
			logger.error(e);
			try {
				PageUtil.fireErrorMessage("browseCourses:courseSearchForm:growl", 
						ResourceBundleUtil.getMessage(
								"manager.courses.deleteCourse.error", 
								loggedUser.getLocale()));
			} catch (KeyNotFoundInBundleException e1) {
				logger.error(e1);
			}
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Used for serializing current competences into a JSON
	 */
	public List<Long> getAllCompetencesIds() {
		List<CourseCompetenceData> competences = new ArrayList<CourseCompetenceData>(courseData.getOriginalCompetences());
		competences.addAll(courseData.getAddedCompetences());
		
		return CourseDataConverter.getIdsOfCourseCompetences(competences);
	}
	
	
	/*
	 * PARAMETERS
	 */
	public void setId(long id) {
		this.id = id;
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

	public CreatorType getCreatorType() {
		return courseData.getCreatorType();
	}

	public void setCreatorType(CreatorType creatorType) {
		this.courseData.setCreatorType(creatorType);
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

	public CourseData getCourseToDelete() {
		return courseToDelete;
	}

	public void setCourseToDelete(CourseData courseToDelete, String context) {
		this.courseToDelete = courseToDelete;
	}

}
