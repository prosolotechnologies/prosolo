package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.BasicCourseData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "courseOverallBean")
@Component("courseOverallBean")
@Scope("view")
public class CourseOverallBean implements Serializable {

	private static final long serialVersionUID = -5307089486810665269L;
	
	private static Logger logger = Logger.getLogger(CourseOverallBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CourseManager courseManager;
	@Autowired private TagManager tagManager;
	@Inject private UrlIdEncoder idEncoder;

	private BasicCourseData courseData;
	private BasicCourseData backupCourseData;

	private String id;
	
	private PublishedStatus[] courseStatusArray;
	
	public void init() {
		if(id == null) {
			PageUtil.fireErrorMessage("You need to pass course id");
		} else {
			long decodedId = idEncoder.decodeId(id);
			
			if (decodedId > 0) {
				try {
					Course course = courseManager.loadResource(Course.class, decodedId);
					courseData = new BasicCourseData(course);
					backupCourseData = BasicCourseData.copyBasicCourseData(courseData);
					courseStatusArray = PublishedStatus.values();
				} catch(ObjectNotFoundException onf) {
					try {
						logger.error(onf);
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound");
					} catch (IOException e) {
						logger.error(e);
					}
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			} else {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
				
			}
		}
	}
	
	/*
	 * ACTIONS
	 */
	
	public void updateCourse() throws EventException {
		try {
			courseData.setPublished();
			courseManager.updateCourse(courseData.getId(), courseData.getTitle(), 
					courseData.getDescription(), 
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(courseData.getTagsString())), 
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(courseData.getHashtagsString())), 
					courseData.isPublished(), loggedUser.getUserId());
			backupCourseData = BasicCourseData.copyBasicCourseData(courseData);
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
					"manager.courses.updateCourse.success", 
					loggedUser.getLocale()));
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		} catch(DbConnectionException e) {
			courseData = BasicCourseData.copyBasicCourseData(backupCourseData);
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	
	/*
	 * PARAMETERS
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public CreatorType getCreatorType() {
		return courseData.getCreatorType();
	}

	public void setCreatorType(CreatorType creatorType) {
		this.courseData.setCreatorType(creatorType);
	}

	public PublishedStatus[] getCourseStatusArray() {
		return courseStatusArray;
	}

	public void setCourseStatusArray(PublishedStatus[] courseStatusArray) {
		this.courseStatusArray = courseStatusArray;
	}

	public BasicCourseData getCourseData() {
		return courseData;
	}

	public void setCourseData(BasicCourseData courseData) {
		this.courseData = courseData;
	}

}
