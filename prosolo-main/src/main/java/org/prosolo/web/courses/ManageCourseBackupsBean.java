package org.prosolo.web.courses;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.services.importing.CourseBackupManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseBackupData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.search.SearchCoursesBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Zoran Jeremic Apr 20, 2014
 *
 */
@ManagedBean(name = "manageCourseBackupsBean")
@Component("manageCourseBackupsBean")
@Scope("view")
public class ManageCourseBackupsBean implements Serializable{
	
	private static final long serialVersionUID = 3655032199601605733L;

	private static Logger logger = Logger.getLogger(ManageCourseBackupsBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CourseBackupManager courseBackupManager;
	@Autowired private SearchCoursesBean searchCoursesBean;
	
	private Collection<CourseBackupData> courseBackups;
	private CourseData courseToBackup;
	private boolean includeCompetences=true;
	private boolean includeActivities=true;
	private boolean includeKeywords=true;
	private boolean includeFiles=true;
	private UploadedFile file;
	
	@PostConstruct
	public void initBackups(){
		setCourseBackups(courseBackupManager.readAllCourseBackupsForUser(loggedUser.getUser().getId()));
	}

	public void backupCourse() {
		courseBackupManager.createCourseBackup(
				this.courseToBackup.getId(), 
				loggedUser.getUser().getId(), 
				includeCompetences, 
				includeActivities,
				includeKeywords, 
				includeFiles);
		
		try {
			PageUtil.fireSuccessfulInfoMessage("backupCourseDialogGrowl",
					ResourceBundleUtil.getMessage("manager.courses.createBackup.success", loggedUser.getLocale()));
			
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		this.initBackups();
	}
			
	public void deleteCourseBackup(CourseBackupData courseBackup) {
		boolean deleted = courseBackupManager.deleteCourseBackup(loggedUser.getUser().getId(), courseBackup);
		
		if (deleted) {
			this.courseBackups.remove(courseBackup);
			
			try {
				PageUtil.fireSuccessfulInfoMessage("restoreCourseDialogGrowl",
						ResourceBundleUtil.getMessage("manager.courses.deleteCourse.success", loggedUser.getLocale()));
				
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		} else {
			PageUtil.fireErrorMessage("File was not found.");
		}
	}
	
	public void restoreCourseBackup(CourseBackupData backupData) {
		boolean restored = courseBackupManager.restoreCourseBackup(loggedUser.getUser().getId(), backupData);
		if (restored) {
			// this.courseBackups.remove(courseBackup);
			searchCoursesBean.searchAllCourses();
			
			try {
				PageUtil.fireSuccessfulInfoMessage("restoreCourseDialogGrowl",
						ResourceBundleUtil.getMessage("manager.courses.restoreCourse.success", loggedUser.getLocale()));
				
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		} else {
			try {
				PageUtil.fireErrorMessage("restoreCourseDialogGrowl",
						ResourceBundleUtil.getMessage("manager.courses.restoreCourse.error", loggedUser.getLocale()));
				
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}

	public void handleBackupFileUpload(FileUploadEvent event) {
		FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		UploadedFile uploadedFile = event.getFile();
		courseBackupManager.storeUploadedBackup(uploadedFile, loggedUser.getUser().getId());
		this.initBackups();
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public Collection<CourseBackupData> getCourseBackups() {
		return courseBackups;
	}
	
	public void setCourseBackups(Collection<CourseBackupData> courseBackups) {
		this.courseBackups = courseBackups;
	}
	
	public CourseData getCourseToBackup() {
		return courseToBackup;
	}
	
	public void setCourseToBackup(CourseData courseToBackup) {
		this.courseToBackup = courseToBackup;
	}
	
	public UploadedFile getFile() {
		return file;
	}
	
	public void setFile(UploadedFile file) {
		this.file = file;
	}
	
	public boolean isIncludeCompetences() {
		return includeCompetences;
	}
	
	public void setIncludeCompetences(boolean includeCompetences) {
		this.includeCompetences = includeCompetences;
	}
	
	public boolean isIncludeActivities() {
		return includeActivities;
	}
	
	public void setIncludeActivities(boolean includeActivities) {
		this.includeActivities = includeActivities;
	}
	
	public boolean isIncludeKeywords() {
		return includeKeywords;
	}
	
	public void setIncludeKeywords(boolean includeKeywords) {
		this.includeKeywords = includeKeywords;
	}
	
	public boolean isIncludeFiles() {
		return includeFiles;
	}
	
	public void setIncludeFiles(boolean includeFiles) {
		this.includeFiles = includeFiles;
	}
}
