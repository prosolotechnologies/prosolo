package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceViewBeanManager")
@Component("competenceViewBeanManager")
@Scope("view")
public class CompetenceViewBeanManager implements Serializable {

	private static final long serialVersionUID = 1186517158327288554L;

	private static Logger logger = Logger.getLogger(CompetenceViewBeanManager.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;

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
				if("preview".equals(mode)) {
					competenceData = competenceManager
							.getCurrentVersionOfCompetenceForManager(decodedCompId, true, true);
				} else {
					competenceData = competenceManager
							.getCompetenceData(decodedCompId, true, true, true, false);
				}
				
				if(competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					/*
					 * check if user has instructor capability and if has, we should mark his comments as
					 * instructor comments
					 */
					boolean hasInstructorCapability = loggedUser.hasCapability("BASIC.INSTRUCTOR.ACCESS");
					commentBean.init(CommentedResourceType.Competence, competenceData.getCompetenceId(),
							hasInstructorCapability);
					decodedCredId = idEncoder.decodeId(credId);
					if(decodedCredId > 0) {
						String credTitle = credManager.getCredentialTitle(decodedCredId);
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
 		} else if(!competenceData.isPublished() && 
 				competenceData.getType() == LearningResourceType.UNIVERSITY_CREATED) {
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