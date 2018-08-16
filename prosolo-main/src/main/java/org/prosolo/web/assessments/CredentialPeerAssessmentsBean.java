package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;

import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

public abstract class CredentialPeerAssessmentsBean implements Paginable, Serializable {

	private static final long serialVersionUID = -3611800729488406124L;

	private static Logger logger = Logger.getLogger(CredentialPeerAssessmentsBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private CredentialManager credentialManager;

	private String id;
	private long decodedId;
	private int page;

	private List<AssessmentData> assessments;
	private String credentialTitle;

	private PaginationData paginationData = new PaginationData();

	private List<AssessmentTypeConfig> assessmentTypesConfig;

	public void loadInitialAssessmentData() {
		credentialTitle = credentialManager.getCredentialTitle(decodedId, null);
		if (credentialTitle != null) {
			if (page > 0) {
				paginationData.setPage(page);
			}
			getAssessmentsFromDB();
			assessmentTypesConfig = credentialManager.getCredentialAssessmentTypesConfig(decodedId);
		}
	}

	protected void decodeCredentialId() {
		decodedId = idEncoder.decodeId(id);
	}

	private void getAssessmentsFromDB() {
		PaginatedResult<AssessmentData> res = assessmentManager.getPaginatedCredentialPeerAssessmentsForStudent(
				decodedId, getStudentId(), new SimpleDateFormat("MMMM dd, yyyy"),
				getAssessmentDisplayMode() == AssessmentDisplayMode.PUBLIC, (paginationData.getPage() - 1) * paginationData.getLimit(), paginationData.getLimit());
		paginationData.update((int) res.getHitsNumber());
		assessments = res.getFoundNodes();
	}

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
	}

	protected void getAssessmentsWithExceptionHandling() {
		try {
			getAssessmentsFromDB();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			getAssessmentsWithExceptionHandling();
		}
	}

	protected abstract AssessmentDisplayMode getAssessmentDisplayMode();
	protected abstract long getStudentId();

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

	public List<AssessmentData> getAssessments() {
		return assessments;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public CredentialManager getCredentialManager() {
		return credentialManager;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}
}
