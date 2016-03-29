package org.prosolo.web.courses;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;

	private String id;
	
	private CredentialData credentialData;
	
	private PublishedStatus[] courseStatusArray;
	
	public void init() {
		courseStatusArray = PublishedStatus.values();
		if(id == null) {
			credentialData = new CredentialData();
		} else {
			try {
				long decodedId = idEncoder.decodeId(id);
				logger.info("Editing credential with id " + decodedId);
				
				credentialData = credentialManager.getCredentialDataForCreator(decodedId, 
						loggedUser.getUser().getId());
				
				if(credentialData == null) {
					credentialData = new CredentialData();
					PageUtil.fireErrorMessage("Credential data can not be found");
				}
			} catch(Exception e) {
				logger.error(e);
				credentialData = new CredentialData();
				PageUtil.fireErrorMessage(e.getMessage());
			}
		}
		
		
	}
	
	/*
	 * ACTIONS
	 */
	
	public String previewCredential() {
		boolean saved = saveCredentialData();
		if(saved) {
			return "credential.xhtml?faces-redirect=true&id=" + 
					idEncoder.encodeId(credentialData.getId());
		}
		return null;
	}
	
	public void saveCredential() {
		saveCredentialData();
	}
	
	public boolean saveCredentialData() {
		try {
			credentialData.setAdditionalValues();
			if(credentialData.getId() > 0) {
				credentialManager.updateCredential(credentialData, 
						loggedUser.getUser());
			} else {
				Credential1 cred = credentialManager.saveNewCredential(credentialData, 
						loggedUser.getUser());
				credentialData.setId(cred.getId());
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	public void deleteCredential() {
		try {
			if(credentialData.getId() > 0) {
				credentialManager.deleteCredential(credentialData.getId());
				credentialData = new CredentialData();
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			}
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

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
