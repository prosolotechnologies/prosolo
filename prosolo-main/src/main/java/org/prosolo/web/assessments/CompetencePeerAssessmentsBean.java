package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.Competence1Manager;
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

public abstract class CompetencePeerAssessmentsBean implements Paginable, Serializable {

	private static final long serialVersionUID = -2986601936604939110L;

	private static Logger logger = Logger.getLogger(CompetencePeerAssessmentsBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private CredentialManager credentialManager;
	@Inject private Competence1Manager compManager;

	private String credId;
	private long decodedCredId;
	private String compId;
	private long decodedCompId;
	private int page;

	private List<AssessmentData> assessments;
	private String credentialTitle;
	private String competenceTitle;

	private PaginationData paginationData = new PaginationData();

	private List<AssessmentTypeConfig> assessmentTypesConfig;

	public void init() {
		try {
			decodedCredId = idEncoder.decodeId(credId);
			decodedCompId = idEncoder.decodeId(compId);

			if (decodedCompId > 0) {
				if (!isUserAllowedToAccess()) {
					PageUtil.accessDenied();
				} else {
					if (decodedCredId > 0) {
						credentialTitle = credentialManager.getCredentialTitle(decodedCredId, null);
					}
					competenceTitle = compManager.getCompetenceTitle(decodedCompId);
					if (competenceTitle != null) {
						if (page > 0) {
							paginationData.setPage(page);
						}
						getAssessmentsFromDB();
						assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId, true);
						loadAdditionalData();
					} else {
						PageUtil.notFound();
					}
				}
			} else {
				PageUtil.notFound();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	abstract void loadAdditionalData();
	abstract boolean isUserAllowedToAccess();
	abstract AssessmentDisplayMode getAssessmentDisplayMode();
	abstract long getStudentId();

	private void getAssessmentsFromDB() {
		PaginatedResult<AssessmentData> res = assessmentManager.getPaginatedCompetencePeerAssessmentsForStudent(
				decodedCompId, getStudentId(), shouldLoadOnlyApprovedAssessments(), new SimpleDateFormat("MMMM dd, yyyy"),
				(paginationData.getPage() - 1) * paginationData.getLimit(), paginationData.getLimit());
		paginationData.update((int) res.getHitsNumber());
		assessments = res.getFoundNodes();
	}

	private boolean shouldLoadOnlyApprovedAssessments() {
		return getAssessmentDisplayMode() == AssessmentDisplayMode.PUBLIC;
	}

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
	}

	void getAssessmentsWithExceptionHandling() {
		try {
			getAssessmentsFromDB();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

    public BlindAssessmentMode getBlindAssessmentMode() {
        return AssessmentUtil.getBlindAssessmentMode(assessmentTypesConfig, AssessmentType.PEER_ASSESSMENT);
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

	public String getCredentialTitle() {
		return credentialTitle;
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

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public Competence1Manager getCompManager() {
		return compManager;
	}

	public long getDecodedCompId() {
		return decodedCompId;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}
}
