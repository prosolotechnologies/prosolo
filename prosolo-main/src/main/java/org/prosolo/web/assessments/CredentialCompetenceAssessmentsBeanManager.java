package org.prosolo.web.assessments;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.assessment.data.CompetenceAssessmentsSummaryData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ManagedBean(name = "credentialCompetenceAssessmentsBeanManager")
@Component("credentialCompetenceAssessmentsBeanManager")
@Scope("view")
public class CredentialCompetenceAssessmentsBeanManager implements AssessmentCommentsAware, Serializable, Paginable {

	private static final long serialVersionUID = 3547469761666767275L;

	private static Logger logger = Logger.getLogger(CredentialCompetenceAssessmentsBeanManager.class);
	
	@Inject private UrlIdEncoder idEncoder;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private AssessmentManager assessmentManager;
	@Inject private ActivityAssessmentBean activityAssessmentBean;
	@Inject private StudentCompetenceAssessmentBean competenceAssessmentBean;
	@Inject private CredentialManager credManager;

	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private int page;

	private CompetenceAssessmentsSummaryData assessmentsSummary;
	private CredentialIdData credentialIdData;

	private PaginationData paginationData = new PaginationData();
	
	private ResourceAccessData access;

	private LearningResourceType currentResType;

	private SelectableAssessmentFilter[] filters;

	public void init() {
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedCompId > 0 && decodedCredId > 0) {
			try {
				access = credManager.getResourceAccessData(decodedCredId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					if (page > 0) {
						paginationData.setPage(page);
					}

					int counter = 0;
					AssessmentFilter[] filterValues = AssessmentFilter.values();
					filters = new SelectableAssessmentFilter[filterValues.length];
					for (AssessmentFilter filter : AssessmentFilter.values()) {
						SelectableAssessmentFilter f = new SelectableAssessmentFilter(filter, true);
						filters[counter++] = f;
					}
					/*
					if user is credential editor he can see assessments for all students
					*/
					assessmentsSummary = assessmentManager
							.getCompetenceAssessmentsDataForInstructorCredentialAssessment(
									decodedCredId, decodedCompId, loggedUserBean.getUserId(), !access.isCanEdit(),
									getSelectedFilters(), paginationData.getLimit(),
									(paginationData.getPage() - 1) * paginationData.getLimit());

					this.paginationData.update((int) assessmentsSummary.getAssessments().getHitsNumber());

					if (assessmentsSummary == null) {
						PageUtil.notFound();
					} else {
						loadCredentialTitle();
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private List<org.prosolo.services.assessment.data.AssessmentFilter> getSelectedFilters() {
		return Arrays.stream(filters)
				.filter(f -> f.isSelected())
				.map(f -> f.getFilter().getFilter())
				.collect(Collectors.toList());
	}

	private void loadCredentialTitle() {
		credentialIdData = credManager.getCredentialIdData(decodedCredId, null);
	}

	private boolean isCurrentUserAssessor(CompetenceAssessmentDataFull compAssessment) {
		if (compAssessment == null) {
			return false;
		} else {
			return loggedUserBean.getUserId() == compAssessment.getAssessorId();
		}
	}

	/**
	 * User is assessor in current context if he accesses assessment from manage section and this is
	 * Instructor assessment or he accesses it from student section and this is self or peer assessment
	 *
	 * @return
	 */
	public boolean isUserAssessorInCurrentContext(CompetenceAssessmentDataFull compAssessment) {
		boolean manageSection = PageUtil.isInManageSection();
		return isCurrentUserAssessor(compAssessment)
				&& ((manageSection && compAssessment.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT)
				|| (!manageSection && (compAssessment.getType() == AssessmentType.SELF_ASSESSMENT || compAssessment.getType() == AssessmentType.PEER_ASSESSMENT)));
	}

	private boolean isCurrentUserAssessedStudent(CompetenceAssessmentDataFull competenceAssessment) {
		return competenceAssessment != null && loggedUserBean.getUserId() == competenceAssessment.getStudentId();
	}

	public boolean isUserAssessedStudentInCurrentContext(CompetenceAssessmentDataFull competenceAssessment) {
		return isCurrentUserAssessedStudent(competenceAssessment) && !PageUtil.isInManageSection();
	}

	public boolean canUserEditDelivery() {
		return access.isCanEdit();
	}

	//GET DATA DEPENDING ON WHICH ASSESSMENT IS CURRENTLY SELECTED (COMPETENCE OR ACTIVITY)

	public long getCurrentCompetenceAssessmentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompAssessmentId();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getCompetenceAssessmentId();
		}
		return 0;
	}

	@Override
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

	@Override
	public BlindAssessmentMode getCurrentBlindAssessmentMode() {
		if (currentResType == null) {
			return null;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getCompAssessment().getBlindAssessmentMode();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getBlindAssessmentMode();
		}
		return null;
	}

	public long getCurrentStudentId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getUserId();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getStudentId();
		}
		return 0;
	}

	public long getCurrentAssessorId() {
		if (currentResType == null) {
			return 0;
		}
		switch (currentResType) {
			case ACTIVITY:
				return activityAssessmentBean.getActivityAssessmentData().getAssessorId();
			case COMPETENCE:
				return competenceAssessmentBean.getCompetenceAssessmentData().getAssessorId();
		}
		return 0;
	}

	@Override
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

	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentDataFull assessment) {
		competenceAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	public void prepareLearningResourceAssessmentForGrading(ActivityAssessmentData assessment) {
		activityAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
		currentResType = LearningResourceType.ACTIVITY;
	}

	//prepare grading end

	public void prepareLearningResourceAssessmentForApproving(CompetenceAssessmentDataFull assessment) {
		competenceAssessmentBean.prepareLearningResourceAssessmentForApproving(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	//GET DATA DEPENDING ON WHICH ASSESSMENT IS CURRENTLY SELECTED (COMPETENCE OR ACTIVITY) END


	/*
	ACTIONS
	 */

	//prepare for commenting
	public void prepareLearningResourceAssessmentForCommenting(ActivityAssessmentData assessment) {
		activityAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.ACTIVITY;
	}

	public void prepareLearningResourceAssessmentForCommenting(CompetenceAssessmentDataFull assessment) {
		competenceAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
		currentResType = LearningResourceType.COMPETENCE;
	}

	//prepare for commenting end

	private void loadAssessments() {
		try {
			List<org.prosolo.services.assessment.data.AssessmentFilter> selectedFilters = getSelectedFilters();
			//if none of the filters is selected, there should be no assessments displayed
			if (selectedFilters.isEmpty()) {
				PaginatedResult<CompetenceAssessmentDataFull> emptyRes = new PaginatedResult<>();
				assessmentsSummary.setAssessments(emptyRes);
				this.paginationData.update(0);
			} else {
				/*
				if user is credential editor he can see assessments for all students
				*/
				assessmentsSummary.setAssessments(
						assessmentManager.getPaginatedStudentsCompetenceAssessments(
								decodedCredId, decodedCompId, loggedUserBean.getUserId(), !access.isCanEdit(),
								getSelectedFilters(), paginationData.getLimit(), (paginationData.getPage() - 1) * paginationData.getLimit()));
				this.paginationData.update((int) assessmentsSummary.getAssessments().getHitsNumber());
			}
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

	public void filterChanged() {
		paginationData.setPage(1);
		loadAssessments();
	}

	public void checkAllFilters() {
		markAllFilters(true);
	}

	public void uncheckAllFilters() {
		markAllFilters(false);
	}

	private void markAllFilters(boolean selected) {
		for(SelectableAssessmentFilter filter : filters) {
			filter.setSelected(selected);
		}
		filterChanged();
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
		List<CompetenceAssessmentDataFull> competenceAssessmentData = assessmentsSummary.getAssessments().getFoundNodes();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentDataFull comp : competenceAssessmentData) {
				if (comp.getActivityAssessmentData() != null) {
					for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
						if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
							return Optional.of(act);
						}
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
			Optional<CompetenceAssessmentDataFull> compAssessment = getCompetenceAssessmentById(
					assessmentId);
			compAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<CompetenceAssessmentDataFull> getCompetenceAssessmentById(long assessmentId) {
		List<CompetenceAssessmentDataFull> competenceAssessmentData = assessmentsSummary.getAssessments().getFoundNodes();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentDataFull ca : competenceAssessmentData) {
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
		return credentialIdData.getTitle();
	}

    public CredentialIdData getCredentialIdData() {
        return credentialIdData;
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

	public SelectableAssessmentFilter[] getFilters() {
		return filters;
	}
}