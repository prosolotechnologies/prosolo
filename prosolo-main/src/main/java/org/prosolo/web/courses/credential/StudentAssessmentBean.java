package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "studentAssessmentBean")
@Component("studentAssessmentBean")
@Scope("view")
public class StudentAssessmentBean implements Paginable,Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(StudentAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private CredentialManager credentialManager;

	private String context;
	private List<AssessmentData> assessmentData;
	private boolean searchForPending = true;
	private boolean searchForApproved = true;
	private String credentialTitle;
	private String id;
	private long decodedId;

	private PaginationData paginationData = new PaginationData(5);

	public void init() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			boolean userEnrolled = credentialManager.isUserEnrolled(decodedId, loggedUserBean.getUserId());

			if (!userEnrolled) {
				PageUtil.accessDenied();
			} else {
				try {
					credentialTitle = credentialManager.getCredentialTitle(decodedId);
					getAssessments();
				} catch (Exception e) {
					logger.error("Error loading assessments", e);
					PageUtil.fireErrorMessage("Error loading assessments");
				}
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void searchForAll() {
		// only reinitialize when at least one is already false
		if (!searchForApproved || !searchForPending) {
			searchForApproved = true;
			searchForPending = true;
			paginationData.setPage(1);
			getAssessmentsWithExceptionHandling();
		}
	}

	public void searchForNone() {
		// only reinitialize when at least one is true
		if (searchForApproved || searchForPending) {
			searchForApproved = false;
			searchForPending = false;
			paginationData.setPage(1);
			getAssessmentsWithExceptionHandling();
		}
	}

	private void getAssessments() {
		if (!searchForApproved && !searchForPending) {
			paginationData.update(0);
			assessmentData = new ArrayList<>();
		} else {
			paginationData.update(assessmentManager.countAssessmentsForUser(loggedUserBean.getUserId(),
					searchForPending, searchForApproved, decodedId));
			assessmentData = assessmentManager.getAllAssessmentsForStudent(loggedUserBean.getUserId(),
					searchForPending, searchForApproved, idEncoder, new SimpleDateFormat("MMMM dd, yyyy"),
					paginationData.getPage() - 1,
					paginationData.getLimit(), decodedId);
		}
	}

	private void getAssessmentsWithExceptionHandling() {
		try {
			getAssessments();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
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

	public boolean isSearchForPending() {
		return searchForPending;
	}

	public void setSearchForPending(boolean searchForPending) {
		this.searchForPending = searchForPending;
		paginationData.setPage(1);
		getAssessmentsWithExceptionHandling();
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
		paginationData.setPage(1);
		getAssessmentsWithExceptionHandling();
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

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getDecodedId() {
		return decodedId;
	}

}
