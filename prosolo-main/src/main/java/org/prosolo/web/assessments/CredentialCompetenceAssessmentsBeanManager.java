package org.prosolo.web.assessments;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentsSummaryData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ManagedBean(name = "credentialCompetenceAssessmentsBeanManager")
@Component("credentialCompetenceAssessmentsBeanManager")
@Scope("view")
public class CredentialCompetenceAssessmentsBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = 3547469761666767275L;

	private static Logger logger = Logger.getLogger(CredentialCompetenceAssessmentsBeanManager.class);
	
	@Inject private UrlIdEncoder idEncoder;
	@Inject private Competence1Manager compManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private AssessmentManager assessmentManager;
	@Inject private ActivityAssessmentBean activityAssessmentBean;
	@Inject private CompetenceAssessmentBean competenceAssessmentBean;
	@Inject private CredentialManager credManager;
	@Inject private UnitManager unitManager;

	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private int page;

	private CompetenceAssessmentsSummaryData assessmentsSummary;
	private String credentialTitle;

	private PaginationData paginationData = new PaginationData();
	
	private ResourceAccessData access;

	private boolean countOnlyAssessmentsWhereCurrentUserIsAssessor;

	private DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

	private LearningResourceType currentResType;

	public void init() {
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedCompId > 0 && decodedCredId > 0) {
			try {
				/*
				 * check if user has instructor privilege for this resource and if has, we should mark his comments as
				 * instructor comments
				 */
				access = compManager.getResourceAccessData(decodedCompId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					if (page > 0) {
						paginationData.setPage(page);
					}
					/*
					if user has Manager role in one of the units where credential is used, he can see all assessments, otherwise
					he can only see assessments where he is instructor
					*/
					countOnlyAssessmentsWhereCurrentUserIsAssessor = !unitManager.checkIfUserHasRoleInUnitsConnectedToCompetence(
							loggedUserBean.getUserId(), decodedCompId, SystemRoleNames.MANAGER);
					assessmentsSummary = assessmentManager
							.getCompetenceAssessmentsDataForInstructorCredentialAssessment(
									decodedCredId, decodedCompId, loggedUserBean.getUserId(), countOnlyAssessmentsWhereCurrentUserIsAssessor,
									dateFormat, true,
									paginationData.getLimit(), (paginationData.getPage() - 1) * paginationData.getLimit());

					this.paginationData.update((int) assessmentsSummary.getAssessments().getHitsNumber());

					if (assessmentsSummary == null) {
						PageUtil.notFound();
					} else {
						loadCredentialTitle();
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				logger.error("Error", rnfe);
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void loadCredentialTitle() {
		credentialTitle = credManager.getCredentialTitle(decodedCredId);
	}

	public boolean isCurrentUserAssessor(CompetenceAssessmentData compAssessment) {
		if (compAssessment == null) {
			return false;
		} else {
			return loggedUserBean.getUserId() == compAssessment.getAssessorId();
		}
	}

	public boolean isCurrentUserAssessedStudent(CompetenceAssessmentData competenceAssessment) {
		return loggedUserBean.getUserId() == competenceAssessment.getStudentId();
	}

	public boolean canUserEditDelivery() {
		return access.isCanEdit();
	}

	//GET DATA DEPENDING ON WHICH ASSESSMENT IS CURRENTLY SELECTED (COMPETENCE OR ACTIVITY)

	public long getCurrentAssessmentCompetenceId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompetenceId();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getCompetenceId();
		}
		return 0;
	}

	public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getMessages();
		}
		return null;
	}

	public LearningResourceAssessmentBean getCurrentAssessmentBean() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean;
			case COMPETENCE:
				return competenceAssessmentBean;
		}
		return null;
	}

	public long getCurrentAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getCompetenceAssessmentId();
		}
		return 0;
	}

	public boolean hasStudentCompletedCurrentResource() {
		if (currentResType == null) {
			return false;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().isCompleted();
			case COMPETENCE:
				//for now
				return true;
		}
		return false;
	}

	public GradeData getCurrentGradeData() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getGrade();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getGradeData();
		}
		return null;
	}

	public String getCurrentResTitle() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getTitle();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getTitle();
		}
		return null;
	}

	//prepare grading

	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentData assessment) {
		competenceAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	public void prepareLearningResourceAssessmentForGrading(ActivityAssessmentData assessment) {
		activityAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
		currentResType = LearningResourceType.ACTIVITY;
	}

	//prepare grading end

	//GET DATA DEPENDING ON WHICH ASSESSMENT IS CURRENTLY SELECTED (COMPETENCE OR ACTIVITY) END


	/*
	ACTIONS
	 */
	public void removeAssessorNotification(CompetenceAssessmentData compAssessment) {
		try {
			assessmentManager.removeAssessorNotificationFromCompetenceAssessment(compAssessment.getCompetenceAssessmentId());
			compAssessment.setAssessorNotified(false);
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error removing the notification");
		}
	}

	public void approveCompetence(CompetenceAssessmentData compAssessment) {
		try {
			assessmentManager.approveCompetence(compAssessment.getCompetenceAssessmentId(), loggedUserBean.getUserContext());
			compAssessment.setApproved(true);
			compAssessment.setAssessorNotified(false);

			PageUtil.fireSuccessfulInfoMessage(
					"You have successfully approved the competence for " + compAssessment.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	//prepare for commenting
	public void prepareLearningResourceAssessmentForCommenting(ActivityAssessmentData assessment) {
		activityAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.ACTIVITY;
	}

	public void prepareLearningResourceAssessmentForCommenting(CompetenceAssessmentData assessment) {
		competenceAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	//prepare for commenting end

	private void loadAssessments() {
		try {
			assessmentsSummary.setAssessments(
					assessmentManager.getPaginatedStudentsCompetenceAssessments(
							decodedCredId, decodedCompId, loggedUserBean.getUserId(), countOnlyAssessmentsWhereCurrentUserIsAssessor,
							paginationData.getLimit(), (paginationData.getPage() - 1) * paginationData.getLimit(), dateFormat));
			this.paginationData.update((int) assessmentsSummary.getAssessments().getHitsNumber());
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading the assessments");
		}
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			loadAssessments();
		}
	}

	//MARK DISCUSSION READ

	public void markActivityAssessmentDiscussionRead() {
		String encodedActivityDiscussionId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			assessmentManager.markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					idEncoder.decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		List<CompetenceAssessmentData> competenceAssessmentData = assessmentsSummary.getAssessments().getFoundNodes();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentData comp : competenceAssessmentData) {
				for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
					if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
						return Optional.of(act);
					}
				}
			}
		}
		return Optional.empty();
	}

	public void markCompetenceAssessmentDiscussionRead() {
		String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedAssessmentId)) {
			long assessmentId = idEncoder.decodeId(encodedAssessmentId);
			assessmentManager.markCompetenceAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					assessmentId);
			Optional<CompetenceAssessmentData> compAssessment = getCompetenceAssessmentById(
					assessmentId);
			compAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<CompetenceAssessmentData> getCompetenceAssessmentById(long assessmentId) {
		List<CompetenceAssessmentData> competenceAssessmentData = assessmentsSummary.getAssessments().getFoundNodes();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentData ca : competenceAssessmentData) {
				if (assessmentId == ca.getCompetenceAssessmentId()) {
					return Optional.of(ca);
				}
			}
		}
		return Optional.empty();
	}

	private String getEncodedAssessmentIdFromRequest() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get("assessmentEncId");
	}
	//MARK DISCUSSION READ END

	/*
	ACTIONS END
	 */


	/*
	 * GETTERS / SETTERS
	 */

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

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public CompetenceAssessmentsSummaryData getAssessmentsSummary() {
		return assessmentsSummary;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public ResourceAccessData getAccess() {
		return access;
	}

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public LearningResourceType getCurrentResType() {
		return currentResType;
	}
}