package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;

@Component("bookmarkBean")
@Scope("request")
public class BookmarkBean implements Serializable {

	private static final long serialVersionUID = -559017454498882337L;
	private static Logger logger = Logger.getLogger(BookmarkBean.class);

	@Inject private CredentialManager credentialManager;
	@Inject private Competence1Manager competenceManager;
	@Inject private LoggedUserBean loggedUserBean;

	public void bookmarkCredential(CredentialData cred) {
		try {
			if(cred.isBookmarkedByCurrentUser()) {
				credentialManager.deleteCredentialBookmark(cred.getIdData().getId(), loggedUserBean.getUserContext());
			} else {
				credentialManager.bookmarkCredential(cred.getIdData().getId(), loggedUserBean.getUserContext());
			}
			cred.setBookmarkedByCurrentUser(!cred.isBookmarkedByCurrentUser());
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void bookmarkCompetence(CompetenceData1 comp) {
		try {
			if(comp.isBookmarkedByCurrentUser()) {
				competenceManager.deleteCompetenceBookmark(comp.getCompetenceId(),
						loggedUserBean.getUserContext());
			} else {
				competenceManager.bookmarkCompetence(comp.getCompetenceId(), loggedUserBean.getUserContext());
			}
			comp.setBookmarkedByCurrentUser(!comp.isBookmarkedByCurrentUser());
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
}
