package org.prosolo.web.courses.credential;

import java.io.Serializable;

import javax.inject.Inject;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("bookmarkBean")
@Scope("request")
public class BookmarkBean implements Serializable {

	private static final long serialVersionUID = -559017454498882337L;
	
	@Inject private CredentialManager credentialManager;
	@Inject private Competence1Manager competenceManager;
	@Inject private LoggedUserBean loggedUserBean;

	public void bookmarkCredential(CredentialData cred) {
		try {
			LearningContextData context = PageUtil.extractLearningContextData();
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
	
	public void bookmarkCompetence(CompetenceData1 comp) {
		try {
			LearningContextData context = PageUtil.extractLearningContextData();
			if(comp.isBookmarkedByCurrentUser()) {
				competenceManager.deleteCompetenceBookmark(comp.getCompetenceId(), 
						loggedUserBean.getUserId(), context);
			} else {
				competenceManager.bookmarkCompetence(comp.getCompetenceId(), loggedUserBean.getUserId(),
						context);
			}
			comp.setBookmarkedByCurrentUser(!comp.isBookmarkedByCurrentUser());
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
}
