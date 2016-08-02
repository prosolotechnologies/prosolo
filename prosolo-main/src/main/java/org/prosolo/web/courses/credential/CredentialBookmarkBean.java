package org.prosolo.web.courses.credential;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialBookmarkBean")
@Component("credentialBookmarkBean")
@Scope("request")
public class CredentialBookmarkBean implements Serializable {

	private static final long serialVersionUID = -559017454498882337L;
	
	@Inject private CredentialManager credentialManager;
	@Inject private LoggedUserBean loggedUserBean;

	public void bookmarkCredential(CredentialData cred) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData context = new LearningContextData(page, lContext, service);
			if(cred.isBookmarkedByCurrentUser()) {
				credentialManager.deleteCredentialBookmark(cred.getId(), 
						loggedUserBean.getUserId(), context);
			} else {
				credentialManager.bookmarkCredential(cred.getId(), loggedUserBean.getUserId(),
						context);
			}
			cred.setBookmarkedByCurrentUser(!cred.isBookmarkedByCurrentUser());
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
}
