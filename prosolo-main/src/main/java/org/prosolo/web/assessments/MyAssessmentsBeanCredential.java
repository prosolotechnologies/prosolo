package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "myAssessmentsBeanCredential")
@Component("myAssessmentsBeanCredential")
@Scope("view")
public class MyAssessmentsBeanCredential implements Paginable, Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(MyAssessmentsBeanCredential.class);

	@Inject private AssessmentManager assessmentManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credentialManager;
	@Inject private AssessmentAvailabilityBean assessmentAvailabilityBean;

	private int page;

	private String context;
	private List<AssessmentData> assessmentData;

	private PaginationData paginationData = new PaginationData(5);

	public void init() {
		try {
			if (page > 0) {
				paginationData.setPage(page);
			}
			getAssessments();
			assessmentAvailabilityBean.init();
		} catch (Exception e) {
			logger.error("Error loading assessments", e);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	private void getAssessments() {
//			paginationData.update(assessmentManager.countAssessmentsForUser(loggedUserBean.getUserId(),
//					searchForPending, searchForApproved, decodedId));
//			assessmentData = assessmentManager.getAllAssessmentsForStudent(loggedUserBean.getUserId(),
//					searchForPending, searchForApproved, idEncoder, new SimpleDateFormat("MMMM dd, yyyy"),
//					paginationData.getPage() - 1,
//					paginationData.getLimit(), decodedId);
	}

	private void getAssessmentsWithExceptionHandling() {
		try {
			getAssessments();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
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

	public PaginationData getPaginationData() {
		return paginationData;
	}

	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			getAssessmentsWithExceptionHandling();
		}
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
}
