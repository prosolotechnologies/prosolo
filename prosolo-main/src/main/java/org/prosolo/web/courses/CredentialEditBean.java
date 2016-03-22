package org.prosolo.web.courses;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.data.BasicCredentialData;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialEditBean")
@Component("credentialEditBean")
@Scope("view")
public class CredentialEditBean implements Serializable {

	private static final long serialVersionUID = 3430513767875001534L;

	private static Logger logger = Logger.getLogger(CredentialEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CourseManager courseManager;

	private BasicCredentialData credentialData;
	
	private PublishedStatus[] courseStatusArray;
	
	public void init() {	
		credentialData = new BasicCredentialData();
		courseStatusArray = PublishedStatus.values();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void saveCredential() {
		try {
			credentialData.setAdditionalValues();
			
			courseManager.saveNewCredential(credentialData, loggedUser.getUser());
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void deleteCredential() {
		try {
			//when can credential be deleted
			//courseManager.deleteCredential(0);
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	 public void listener(AjaxBehaviorEvent event) {
	        System.out.println("listener");
	        System.out.println(credentialData.isMandatoryFlow());
	 }
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public PublishedStatus[] getCourseStatusArray() {
		return courseStatusArray;
	}

	public void setCourseStatusArray(PublishedStatus[] courseStatusArray) {
		this.courseStatusArray = courseStatusArray;
	}

	public BasicCredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(BasicCredentialData credentialData) {
		this.credentialData = credentialData;
	}

}
