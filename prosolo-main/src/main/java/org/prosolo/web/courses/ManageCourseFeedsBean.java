/**
 * 
 */
package org.prosolo.web.courses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.feeds.data.UserFeedSourceAggregate;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "manageCourseFeedsBean")
@Component("manageCourseFeedsBean")
@Scope("view")
public class ManageCourseFeedsBean implements Serializable {

	private static final long serialVersionUID = 8115222785928157348L;

	private static Logger logger = Logger.getLogger(ManageCourseFeedsBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private FeedsManager feedsManager;
	@Autowired private CourseManager courseManager;
	
	private List<UserFeedSourceAggregate> userFeedSources;
	private CourseData course;

	private long id;
	private String blogToAdd;
	
	public void init() {
		if (id > 0) {
			try {
				userFeedSources = feedsManager.getFeedSourcesForCourse(id);
				course = new CourseData(courseManager.loadResource(Course.class, id));
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * ACTIONS
	 */
	public void autosaveCourse() {
		try {
			courseManager.updateCourseFeeds(course.getCourse(), course.getBlogs(), loggedUser.getUser());
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch (EventException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error saving changes");
		}
	}

	public void autosaveFeeds() {
		List<FeedSource> disabledFeedSources = new ArrayList<FeedSource>();
		
		for (UserFeedSourceAggregate userFeedSource : userFeedSources) {
			if (!userFeedSource.isIncluded()) {
				disabledFeedSources.add(userFeedSource.getFeedSource());
			}
		}
		
		courseManager.updateExcludedFeedSources(course.getCourse(), disabledFeedSources);
		
		PageUtil.fireSuccessfulInfoMessage("Changes are saved");
	}
	
	public void addBlogLink() {
		boolean success = this.course.addBlog(blogToAdd);
		
		if (success) {
			autosaveCourse();
		} else {
			PageUtil.fireErrorMessage("This link is already exists.");
		}
		this.blogToAdd = null;
	}
	
	public void removeBlogLink(String blogToRemove) {
		this.course.removeBlog(blogToRemove);

		autosaveCourse();
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
	public List<UserFeedSourceAggregate> getUserFeedSources() {
		return userFeedSources;
	}
	
	public void setUserFeedSources(List<UserFeedSourceAggregate> userFeedSources) {
		this.userFeedSources = userFeedSources;
	}

	public CourseData getCourse() {
		return course;
	}

	public void setCourse(CourseData course) {
		this.course = course;
	}
	
	public String getBlogToAdd() {
		return blogToAdd;
	}

	public void setBlogToAdd(String blogToAdd) {
		this.blogToAdd = blogToAdd;
	}
	
}
