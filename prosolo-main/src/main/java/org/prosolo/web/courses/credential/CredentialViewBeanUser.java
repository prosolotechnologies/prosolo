package org.prosolo.web.courses.credential;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentRequestData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.AskForCredentialAssessmentBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "credentialViewBean")
@Component("credentialViewBean")
@Scope("view")
public class CredentialViewBeanUser implements Serializable {

	private static final long serialVersionUID = 2225577288550403383L;

	private static Logger logger = Logger.getLogger(CredentialViewBeanUser.class);

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private Activity1Manager activityManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private Competence1Manager compManager;
	@Inject
	private AnnouncementManager announcementManager;
	@Inject
	private AskForCredentialAssessmentBean askForAssessmentBean;
	@Inject
	private AssignStudentToInstructorDialogBean assignStudentToInstructorDialogBean;

	private String id;
	private long decodedId;
	private boolean justEnrolled;

	private long numberOfUsersLearningCred;

	private CredentialData credentialData;
	private ResourceAccessData access;
	private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();

	private int numberOfAnnouncements;
	
	private int numberOfTags;

	public void init() {
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				retrieveUserCredentialData();
				numberOfAnnouncements = announcementManager.numberOfAnnouncementsForCredential(decodedId);
				/*
				 * if user does not have at least access to resource in read only mode throw access denied exception.
				 */
				if (!access.isCanRead()) {
					PageUtil.accessDenied();
				} else {
					if (justEnrolled) {
						PageUtil.fireSuccessfulInfoMessage(	"You have enrolled the " + credentialData.getIdData().getTitle());
					}
	
					if (credentialData.isEnrolled()) {
						numberOfUsersLearningCred = credentialManager.getNumberOfUsersLearningCredential(decodedId);
						numberOfTags = credentialManager.getNumberOfTags(credentialData.getIdData().getId());
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				PageUtil.fireErrorMessage("Error while retrieving credential data");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void initAskForAssessment(AssessmentType aType) {
		/*
		passing credential level blind assessment mode is fine in this context because if
		peer assessment request is initiated here it will always be new assessment request.
		For tutor assessment it does not matter where are we getting blind assessment mode from
		because blind assessment mode is of importance only for peer assessments.
		 */
		askForAssessmentBean.init(decodedId, credentialData.getTargetCredId(), aType, credentialData.getAssessmentTypeConfig(aType).getBlindAssessmentMode());
	}

	private void retrieveUserCredentialData() {
		//note: if user is enrolled he does not need Learn privilege
		access = credentialManager.getResourceAccessData(decodedId, loggedUser.getUserId(),
				ResourceAccessRequirements.of(AccessMode.USER).addPrivilege(UserGroupPrivilege.Learn));
		credentialData = credentialManager
				.getFullTargetCredentialOrCredentialData(decodedId, loggedUser.getUserId());
	}

	/*
	 * ACTIONS
	 */
	
	public void enrollInCompetence(CompetenceData1 comp) {
		try {
			compManager.enrollInCompetence(comp.getCompetenceId(), loggedUser.getUserId(), loggedUser.getUserContext());

			PageUtil.redirect("/credentials/" + id + "/" + idEncoder.encodeId(comp.getCompetenceId()) + "?justEnrolled=true");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error while enrolling in a " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		}
	}

	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if (!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = new ArrayList<>();

			if (cd.isEnrolled()) {
				activities = activityManager.getTargetActivitiesData(cd.getTargetCompId());
			} else {
				activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
			}

			// determining whether user can access activities, i.e. whether to render activity titles as links
			// or as a label.
			boolean canAccessActivities = true;

			// if user is not enrolled in a credential, then he can not access activities unless having the Learn
			// privilege to the parent competency
			if (!credentialData.isEnrolled()) {
				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.USER)
						.addPrivilege(UserGroupPrivilege.Learn)
						.addPrivilege(UserGroupPrivilege.Edit);

				ResourceAccessData compAccess = compManager.getResourceAccessData(cd.getCompetenceId(), loggedUser.getUserId(), req);

				if (!compAccess.isCanAccess()) {
					canAccessActivities = false;
				}
			}
			for (ActivityData activity : activities) {
				activity.setCanAccess(canAccessActivities);
			}

			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}

	public void enrollInCredential() {
		try {
			credentialManager.enrollInCredential(decodedId, loggedUser.getUserContext());
			//reload user credential data after enroll
			retrieveUserCredentialData();
			numberOfUsersLearningCred = credentialManager.getNumberOfUsersLearningCredential(decodedId);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}

	public void setupAssessmentRequestRecepient() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("assessmentRecipient");
		if (StringUtils.isNotBlank(id)) {
			assessmentRequestData.setAssessorId(Long.valueOf(id));
		}
	}

	public boolean userHasAssessmentForCredential() {
		Long assessmentCount = assessmentManager.countAssessmentsForUserAndCredential(loggedUser.getUserId(),
				decodedId);
		if (assessmentCount > 0) {
			logger.debug("We found " + assessmentCount + " assessments for user " + loggedUser.getUserId()
					+ "for credential" + decodedId);
			return true;
		}
		return false;
	}

	public String getAssessmentIdForUser() {
		return idEncoder.encodeId(
				assessmentManager.getAssessmentIdForUser(loggedUser.getUserId(), credentialData.getTargetCredId()));
	}

	/**
	 * This method is called after student has chosen an instructor from the modal (it this option is enabled for
	 * the delivery)
	 */
	public void updateAfterInstructorIsAssigned() {
		InstructorData instructor = assignStudentToInstructorDialogBean.getInstructor();

		if (instructor != null) {
			credentialData.setInstructorId(instructor.getInstructorId());
			credentialData.setInstructorAvatarUrl(instructor.getUser().getAvatarUrl());
			credentialData.setInstructorFullName(instructor.getUser().getFullName());
		} else {
			credentialData.setInstructorId(-1);
			credentialData.setInstructorAvatarUrl(null);
			credentialData.setInstructorFullName(null);
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public ResourceAccessData getAccess() {
		return access;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public boolean isJustEnrolled() {
		return justEnrolled;
	}

	public void setJustEnrolled(boolean justEnrolled) {
		this.justEnrolled = justEnrolled;
	}

	public AssessmentRequestData getAssessmentRequestData() {
		return assessmentRequestData;
	}

	public void setAssessmentRequestData(AssessmentRequestData assessmentRequestData) {
		this.assessmentRequestData = assessmentRequestData;
	}

	public long getNumberOfUsersLearningCred() {
		return numberOfUsersLearningCred;
	}

	public void setNumberOfUsersLearningCred(long numberOfUsersLearningCred) {
		this.numberOfUsersLearningCred = numberOfUsersLearningCred;
	}

	public int getNumberOfTags() {
		return numberOfTags;
	}

	public void setNumberOfTags(int numberOfTags) {
		this.numberOfTags = numberOfTags;
	}

	public int getNumberOfAnnouncements() {
		return numberOfAnnouncements;
	}

	public void setNumberOfAnnouncements(int numberOfAnnouncements) {
		this.numberOfAnnouncements = numberOfAnnouncements;
	}
}
