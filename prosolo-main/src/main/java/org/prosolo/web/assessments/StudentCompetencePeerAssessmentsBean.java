package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@ManagedBean(name = "studentCompetencePeerAssessmentsBean")
@Component("studentCompetencePeerAssessmentsBean")
@Scope("view")
public class StudentCompetencePeerAssessmentsBean implements Paginable, Serializable {

	private static final long serialVersionUID = 9022737920789959225L;

	private static Logger logger = Logger.getLogger(StudentCompetencePeerAssessmentsBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private CredentialManager credentialManager;
	@Inject private Competence1Manager compManager;
	@Inject private AskForCompetenceAssessmentBean askForAssessmentBean;

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

	//needed when new assessment request is sent
	private long targetCompId;

	public void init() {
		try {
			decodedCredId = idEncoder.decodeId(credId);
			decodedCompId = idEncoder.decodeId(compId);

			if (decodedCompId > 0) {
				targetCompId = compManager.getTargetCompetenceId(decodedCompId, loggedUserBean.getUserId());

				//if user is not enrolled he is not allowed to access this page
				if (targetCompId <= 0) {
					PageUtil.accessDenied();
				} else {
					if (decodedCredId > 0) {
						credentialTitle = credentialManager.getCredentialTitle(decodedCredId);
					}
					competenceTitle = compManager.getCompetenceTitle(decodedCompId);
					if (competenceTitle != null) {
						if (page > 0) {
							paginationData.setPage(page);
						}
						getAssessmentsFromDB();
						assessmentTypesConfig = compManager.getCompetenceAssessmentTypesConfig(decodedCompId);
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

	private void getAssessmentsFromDB() {
		PaginatedResult<AssessmentData> res = assessmentManager.getPaginatedCompetencePeerAssessmentsForStudent(
				decodedCompId, loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"),
				(paginationData.getPage() - 1) * paginationData.getLimit(), paginationData.getLimit());
		paginationData.update((int) res.getHitsNumber());
		assessments = res.getFoundNodes();
	}

	public void initAskForAssessment() {
		askForAssessmentBean.init(decodedCompId, targetCompId, AssessmentType.PEER_ASSESSMENT);
	}

	public boolean isPeerAssessmentEnabled() {
		return AssessmentUtil.isPeerAssessmentEnabled(assessmentTypesConfig);
	}

	public boolean isSelfAssessmentEnabled() {
		return AssessmentUtil.isSelfAssessmentEnabled(assessmentTypesConfig);
	}

	private void getAssessmentsWithExceptionHandling() {
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

	public void submitAssessment() {
		try {
			boolean success = askForAssessmentBean.submitAssessmentRequestAndReturnStatus();
			if (success) {
				paginationData.setPage(1);
				getAssessmentsWithExceptionHandling();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error sending the assessment request");
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
}
