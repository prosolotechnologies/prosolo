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

	@PostConstruct
	public void init() {
		credentials = credManager.getNRecentlyLearnedInProgressCredentials(
				loggedUserBean.getUser().getId(), LIMIT);
	}
	
	public List<CredentialData> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<CredentialData> credentials) {
		this.credentials = credentials;
	}

	
}
