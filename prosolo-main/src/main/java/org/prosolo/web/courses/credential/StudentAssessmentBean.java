package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

		try {
			decodedId = idEncoder.decodeId(id);
			if (!searchForApproved && !searchForPending) {
				paginationData.update(0);
				assessmentData = new ArrayList<>();
			} else {
				paginationData.update(assessmentManager.countAssessmentsForUser(loggedUserBean.getUserId(),
						searchForPending, searchForApproved));
				assessmentData = assessmentManager.getAllAssessmentsForStudent(loggedUserBean.getUserId(),
						searchForPending, searchForApproved, idEncoder, new SimpleDateFormat("MMMM dd, yyyy"),
						paginationData.getPage() - 1,
						paginationData.getLimit());
			}
		} catch (Exception e) {
			logger.error("Error while loading assessment data", e);
			PageUtil.fireErrorMessage("Error while loading assessment data");
		}
	}

	public void searchForAll() {
		// only reinitialize when at least one is already false
		if (!searchForApproved || !searchForPending) {
			searchForApproved = true;
			searchForPending = true;
			paginationData.setPage(1);
			init();
		}
	}

	public void searchForNone() {
		// only reinitialize when at least one is true
		if (searchForApproved || searchForPending) {
			searchForApproved = false;
			searchForPending = false;
			paginationData.setPage(1);
			init();
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
		init();
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
		paginationData.setPage(1);
		init();
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			init();
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

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}
}
