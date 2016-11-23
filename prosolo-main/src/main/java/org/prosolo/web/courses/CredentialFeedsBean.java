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
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.feeds.data.CredentialFeedsData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "credentialFeedsBean")
@Component("credentialFeedsBean")
@Scope("view")
public class CredentialFeedsBean implements Serializable {

	private static final long serialVersionUID = 8115222785928157348L;

	private static Logger logger = Logger.getLogger(CredentialFeedsBean.class);
	
	@Autowired private FeedsManager feedsManager;
	@Autowired private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	
	private List<CredentialFeedsData> userFeedSources;
	private List<CredentialFeedsData> credentialFeeds;
	
	private CredentialFeedsData feedToEdit;
	private CredentialFeedsData backupFeed;
	
	private String id;
	private long decodedId;
	
	private String credentialTitle;
	
	public void init() {
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			try {
				if(credentialTitle == null) {
					credentialTitle = credentialManager.getCredentialTitle(decodedId);
				}
				userFeedSources = feedsManager.getUserFeedsForCredential(decodedId);
				credentialFeeds = feedsManager.getCredentialFeeds(decodedId);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
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
	
//	public void addBlogLink(String blogToAdd) {
//		boolean success = addBlog(blogToAdd);
//		if (!success) {
//			PageUtil.fireErrorMessage("This link already exists.");
//		}
//	}
	
//	public void removeBlogLink(String blogToRemove) {
//		removeBlog(blogToRemove);
//
//		//autosaveCourse();
//	}
	
//	public boolean addBlog(String blog) {
//		int indexOfSlash = blog.lastIndexOf("/");
//		
//		if (indexOfSlash >= 0 && indexOfSlash == blog.length()-1) {
//			blog = blog.substring(0, indexOfSlash);
//		}
//		
//		if (blog != null) {
//			if (!credentialFeeds.contains(blog)) {
//			//	courseFeeds.add(blog);
//				return true;
//			} else {
//				return false;
//			}
//		}
//		return false;
//	}
	
//	public boolean removeBlog(String blog) {
//		if (blog != null) {
//			//return feeds.remove(blog);
//		}
//		return false;
//	}
	
	public void createNewFeedForEdit() {
		feedToEdit = new CredentialFeedsData();
	}
	
	public void setFeedForEdit(CredentialFeedsData feed) {
		feedToEdit = feed;
		backupFeed = new CredentialFeedsData();
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
				boolean added = credentialManager.saveNewCredentialFeed(decodedId, 
						feedToEdit.getFeedLink());
				if(added) {
					credentialFeeds.add(feedToEdit);
				}
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(EntityAlreadyExistsException | DbConnectionException e) {
			if(edit) {
				boolean found = findFeedAndUpdateLink(credentialFeeds);
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
			credentialManager.removeFeed(decodedId, feedToEdit.getId());
			credentialFeeds.remove(feedToEdit);
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		feedToEdit = null;
		backupFeed = null;
	}
	
	private boolean findFeedAndUpdateLink(List<CredentialFeedsData> feeds) {
		for(CredentialFeedsData data : feeds) {
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
	
	public List<CredentialFeedsData> getUserFeedSources() {
		return userFeedSources;
	}

	public void setUserFeedSources(List<CredentialFeedsData> userFeedSources) {
		this.userFeedSources = userFeedSources;
	}

	public List<CredentialFeedsData> getCredentialFeeds() {
		return credentialFeeds;
	}

	public void setCredentialFeeds(List<CredentialFeedsData> credentialFeeds) {
		this.credentialFeeds = credentialFeeds;
	}

	public CredentialFeedsData getFeedToEdit() {
		return feedToEdit;
	}

	public void setFeedToEdit(CredentialFeedsData feedToEdit) {
		this.feedToEdit = feedToEdit;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

}
