package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialViewBean")
@Component("credentialViewBean")
@Scope("view")
public class CredentialViewBean implements Serializable {

	private static final long serialVersionUID = 2225577288550403383L;

	private static Logger logger = Logger.getLogger(CredentialViewBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;

	private String id;
	private long decodedId;
	
	private CredentialData credentialData;

	
	public void init() {	
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				credentialData = credentialManager.getAllCredentialDataForUser(decodedId, 
						loggedUser.getUser().getId());
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
		}
	}
	
	public boolean isCurrentUserCreator() {
		return credentialData.getCreator().getId() == loggedUser.getUser().getId();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void enrollInCredential() {
		try {
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(FacesContext.getCurrentInstance().getViewRoot().getViewId());
			lcd.setLearningContext(PageUtil.getPostParameter("context"));
			lcd.setService(PageUtil.getPostParameter("service"));
			CredentialData cd = credentialManager.enrollInCredential(decodedId, loggedUser.getUser(), lcd);
			cd.getId();
		} catch(DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

}
