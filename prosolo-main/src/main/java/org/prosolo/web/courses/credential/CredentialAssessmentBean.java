package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.CompetenceAssessmentData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialAssessmentBean")
@Component("credentialAssessmentBean")
@Scope("view")
public class CredentialAssessmentBean implements Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(CredentialAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;

	// PARAMETERS
	private String id;
	private long decodedId;

	// used when managing single assessment
	private String assessmentId;
	private long decodedAssessmentId;
	private FullAssessmentData fullAssessmentData;
	private String reviewText;

	// used for managing multiple assessments
	private String credentialTitle;
	private String context;
	private List<AssessmentData> assessmentData;
	private boolean searchForPending = true;
	private boolean searchForApproved = true;

	public void init() {

		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitleForCredentialWithType(decodedId,
						LearningResourceType.UNIVERSITY_CREATED);
				if (title != null) {
					credentialTitle = title;
					assessmentData = assessmentManager.getAllAssessmentsForCredential(decodedId,
							loggedUserBean.getUser().getId(), searchForPending, searchForApproved, idEncoder,
							new SimpleDateFormat("MMMM dd, yyyy"));

				} else {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				}
			} catch (Exception e) {
				logger.error("Error while loading assessment data", e);
				PageUtil.fireErrorMessage("Error while loading assessment data");
			}
		}
	}

	public void initAssessment() {
		decodedAssessmentId = idEncoder.decodeId(assessmentId);
		if (decodedAssessmentId > 0) {
			
			try {
					fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, 
							idEncoder, new SimpleDateFormat("MMMM dd, yyyy"));
					credentialTitle = fullAssessmentData.getTitle();

			} catch (Exception e) {
				logger.error("Error while loading assessment data", e);
				PageUtil.fireErrorMessage("Error while loading assessment data");
			}
		}

	}
	
	public void approveCredential() {
		try {
			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getTargetCredentialId(),reviewText);
			markCredentialApproved();
			PageUtil.fireSuccessfulInfoMessage("assessCredentialFormGrowl","You have sucessfully approved credential for "+fullAssessmentData.getStudentFullName());
		}
		catch (Exception e) {
			logger.error("Error aproving assessment data", e);
			PageUtil.fireErrorMessage("Error while approving assessment data");
		}
	}

	private void markCredentialApproved() {
		fullAssessmentData.setApproved(true);
		for(CompetenceAssessmentData compAssessmentData : fullAssessmentData.getCompetenceAssesmentData()) {
			compAssessmentData.setApproved(true);
		}
		
	}

	public void searchForAll() {
		searchForApproved = true;
		searchForPending = true;
		init();
	}

	public void searchForNone() {
		searchForApproved = false;
		searchForPending = false;
		init();
	}

	public void searchForPendingFlip() {
		setSearchForPending(!searchForPending);
		init();
	}

	public void searchForApprovedFlip() {
		setSearchForApproved(!searchForApproved);
		init();
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public CredentialManager getCredManager() {
		return credManager;
	}

	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
	}

	public LoggedUserBean getLoggedUserBean() {
		return loggedUserBean;
	}

	public void setLoggedUserBean(LoggedUserBean loggedUserBean) {
		this.loggedUserBean = loggedUserBean;
	}

	public List<AssessmentData> getAssessmentData() {
		return assessmentData;
	}

	public void setAssessmentData(List<AssessmentData> assessmentData) {
		this.assessmentData = assessmentData;
	}

	public boolean isSearchForPending() {
		return searchForPending;
	}

	public void setSearchForPending(boolean searchForPending) {
		this.searchForPending = searchForPending;
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(String assessmentId) {
		this.assessmentId = assessmentId;
	}

	public FullAssessmentData getFullAssessmentData() {
		return fullAssessmentData;
	}

	public void setFullAssessmentData(FullAssessmentData fullAssessmentData) {
		this.fullAssessmentData = fullAssessmentData;
	}

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	
}
