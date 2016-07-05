package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "studentAssessmentBean")
@Component("studentAssessmentBean")
@Scope("view")
public class StudentAssessmentBean {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(StudentAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	
	private String context;
	private List<AssessmentData> assessmentData;
	private boolean searchForPending = true;
	private boolean searchForApproved = true;

	public void init() {

		try {
			assessmentData = assessmentManager.getAllAssessmentsForStudent(
					loggedUserBean.getUser().getId(), searchForPending, searchForApproved, idEncoder,
					new SimpleDateFormat("MMMM dd, yyyy"));

		} catch (Exception e) {
			logger.error("Error while loading assessment data", e);
			PageUtil.fireErrorMessage("Error while loading assessment data");
		}
	}
	
	public void searchForAll() {
		//only reinitialize when at least one is already false
		if(!searchForApproved || !searchForPending) {
			searchForApproved = true;
			searchForPending = true;
			init();
		}
	}

	public void searchForNone() {
		//only reinitialize when at least one is true
		if(searchForApproved || searchForPending) {
			searchForApproved = false;
			searchForPending = false;
			init();
		}
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
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

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
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
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}
	
	

}
