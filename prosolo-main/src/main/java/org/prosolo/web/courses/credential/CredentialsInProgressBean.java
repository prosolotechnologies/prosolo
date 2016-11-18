package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialsInProgressBean")
@Component("credentialsInProgressBean")
@Scope("view")
public class CredentialsInProgressBean implements Serializable {

	private static final long serialVersionUID = -4046586835461640435L;
	
	@Inject private CredentialManager credManager;
	@Inject private LoggedUserBean loggedUserBean;
	
	private List<CredentialData> credentials;
	
	private static final int LIMIT = 2;
	private boolean moreToView;

	@PostConstruct
	public void init() {
		List<CredentialData> creds = credManager.getNRecentlyLearnedInProgressCredentials(
				loggedUserBean.getUserId(), LIMIT, true);
		if(creds.size() > LIMIT) {
			credentials = creds.subList(0, creds.size()-1);
			moreToView = true;
		} else {
			credentials = creds;
		}
	}
	
	public List<CredentialData> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<CredentialData> credentials) {
		this.credentials = credentials;
	}

	public boolean isMoreToView() {
		return moreToView;
	}

	public void setMoreToView(boolean moreToView) {
		this.moreToView = moreToView;
	}

	
}
