package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceViewBean")
@Component("competenceViewBean")
@Scope("view")
public class CompetenceViewBeanUser implements Serializable {

	private static final long serialVersionUID = 9208762722353804216L;

	private static Logger logger = Logger.getLogger(CompetenceViewBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private CredentialManager credManager;

	private String credId;
	private long decodedCredId;
	private String compId;
	private long decodedCompId;
	private String mode;
	
	private CompetenceData1 competenceData;

	public void init() {	
		decodedCompId = idEncoder.decodeId(compId);
		if (decodedCompId > 0) {
			try {
				decodedCredId = idEncoder.decodeId(credId);
				if(decodedCredId > 0) {
					competenceData = competenceManager.getFullTargetCompetenceOrCompetenceData(
							decodedCredId, decodedCompId, loggedUser.getUser().getId());
				} else {
					if("preview".equals(mode)) {
						competenceData = competenceManager.getCompetenceDataForEdit(decodedCompId, 
								loggedUser.getUser().getId(), true);
						ResourceCreator rc = new ResourceCreator();
						User user = loggedUser.getUser();
						rc.setFullName(user.getName(), user.getLastname());
						rc.setAvatarUrl(user.getAvatarUrl());
						competenceData.setCreator(rc);
					} else {
						competenceData = competenceManager.getCompetenceDataForUser(decodedCompId, true, 
								true, true, loggedUser.getUser().getId(), true);
					}
				}
				if(competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					commentBean.init(CommentedResourceType.Competence, competenceData.getCompetenceId(),
							false);
					
					if(decodedCredId > 0) {
						String credTitle = null;
						if(competenceData.isEnrolled()) {
							credTitle = credManager.getTargetCredentialTitle(decodedCredId,
									loggedUser.getUser().getId());
						} else {
							credTitle = credManager.getCredentialTitle(decodedCredId);
						}
						competenceData.setCredentialId(decodedCredId);
						competenceData.setCredentialTitle(credTitle);
					}
				}
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
		return competenceData == null || competenceData.getCreator() == null ? false : 
			competenceData.getCreator().getId() == loggedUser.getUser().getId();
	}
	
	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	public String getLabelForCompetence() {
 		if(isPreview()) {
 			return "(Preview)";
 		} else if(isCurrentUserCreator() && !competenceData.isEnrolled() && !competenceData.isPublished()) {
 			return "(Draft)";
 		} else {
 			return "";
 		}
 	}
	
	public boolean isPreview() {
		return "preview".equals(mode);
	}

	/*
	 * ACTIONS
	 */
	
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceData1 getCompetenceData() {
		return competenceData;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public long getDecodedCredId() {
		return decodedCredId;
	}

	public void setDecodedCredId(long decodedCredId) {
		this.decodedCredId = decodedCredId;
	}

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public long getDecodedCompId() {
		return decodedCompId;
	}

	public void setDecodedCompId(long decodedCompId) {
		this.decodedCompId = decodedCompId;
	}

	public void setCompetenceData(CompetenceData1 competenceData) {
		this.competenceData = competenceData;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
