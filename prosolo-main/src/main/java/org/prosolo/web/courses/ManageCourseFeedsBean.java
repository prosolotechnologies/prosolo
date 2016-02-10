/**
 * 
 */
package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.feeds.data.CourseFeedsData;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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
	
	@Autowired private FeedsManager feedsManager;
	@Autowired private CourseManager courseManager;
	@Inject private UrlIdEncoder idEncoder;
	
	private List<CourseFeedsData> userFeedSources;
	private List<CourseFeedsData> courseFeeds;
	
	private CourseFeedsData feedToEdit;
	private CourseFeedsData backupFeed;
	
	private String id;
	private long decodedId;
	
	private String courseTitle;
	
	public void init() {
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			try {
				if(courseTitle == null) {
					courseTitle = courseManager.getCourseTitle(decodedId);
				}
				userFeedSources = feedsManager.getUserFeedsForCourse(decodedId);
				courseFeeds = feedsManager.getCourseFeeds(decodedId);
			} catch(DbConnectionException e) {
				try {
					logger.error(e);
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound");
				} catch (IOException ie) {
					logger.error(ie);
				}
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * ACTIONS
	 */
	
	public void addBlogLink(String blogToAdd) {
		boolean success = addBlog(blogToAdd);
		
		if (success) {
			//autosaveCourse();
		} else {
			PageUtil.fireErrorMessage("This link is already exists.");
		}
	}
	
	public void removeBlogLink(String blogToRemove) {
		removeBlog(blogToRemove);

		//autosaveCourse();
	}
	
	public boolean addBlog(String blog) {
		int indexOfSlash = blog.lastIndexOf("/");
		
		if (indexOfSlash >= 0 && indexOfSlash == blog.length()-1) {
			blog = blog.substring(0, indexOfSlash);
		}
		
		if (blog != null) {
			if (!courseFeeds.contains(blog)) {
			//	courseFeeds.add(blog);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean removeBlog(String blog) {
		if (blog != null) {
			//return feeds.remove(blog);
		}
		return false;
	}
	
	public void createNewFeedForEdit() {
		feedToEdit = new CourseFeedsData();
	}
	
	public void setFeedForEdit(CourseFeedsData feed) {
		feedToEdit = feed;
		backupFeed = new CourseFeedsData();
		backupFeed.setId(feed.getId());
		backupFeed.setFeedLink(feed.getFeedLink());
	}
	
	public void saveFeed() {
		removeSlashFromTheEnd();
		boolean edit = false;
		try {
			if(feedToEdit.getId() != 0) { 
				edit = true;
				feedsManager.updateFeedLink(feedToEdit);
			} else {
				boolean added = courseManager.saveNewCourseFeed(decodedId, feedToEdit.getFeedLink());
				if(added) {
					courseFeeds.add(feedToEdit);
				}
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(DbConnectionException e) {
			if(edit) {
				boolean found = findFeedAndUpdateLink(courseFeeds);
				if(!found) {
					findFeedAndUpdateLink(userFeedSources);
				}
			}
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		backupFeed = null;
	    feedToEdit = null;
	}
	
	private void removeSlashFromTheEnd() {
		String blog = feedToEdit.getFeedLink();
		int indexOfSlash = blog.lastIndexOf("/");
	
		if (indexOfSlash >= 0 && indexOfSlash == blog.length()-1) {
			blog = blog.substring(0, indexOfSlash);
			feedToEdit.setFeedLink(blog);
		}
	}

	public void deleteFeed() {
		try {
			courseManager.removeFeed(decodedId, feedToEdit.getId());
			courseFeeds.remove(feedToEdit);
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		feedToEdit = null;
		backupFeed = null;
	}
	
	private boolean findFeedAndUpdateLink(List<CourseFeedsData> feeds) {
		for(CourseFeedsData data : feeds) {
			if(data.getId() == feedToEdit.getId()) {
				data.setFeedLink(backupFeed.getFeedLink());
				return true;
			}
		}
		return false;
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
	
	public List<CourseFeedsData> getUserFeedSources() {
		return userFeedSources;
	}

	public void setUserFeedSources(List<CourseFeedsData> userFeedSources) {
		this.userFeedSources = userFeedSources;
	}

	public List<CourseFeedsData> getCourseFeeds() {
		return courseFeeds;
	}

	public void setCourseFeeds(List<CourseFeedsData> courseFeeds) {
		this.courseFeeds = courseFeeds;
	}

	public CourseFeedsData getFeedToEdit() {
		return feedToEdit;
	}

	public void setFeedToEdit(CourseFeedsData feedToEdit) {
		this.feedToEdit = feedToEdit;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

}
