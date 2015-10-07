package org.prosolo.web.communications.evaluation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.evaluation.Badge;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.exceptions.EvaluationNotSupportedException;
import org.prosolo.services.nodes.exceptions.InvalidParameterException;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.ResourceSettingsBean;
import org.prosolo.web.communications.evaluation.data.EvaluatedResourceData;
import org.prosolo.web.communications.evaluation.data.EvaluatedResourceType;
import org.prosolo.web.communications.evaluation.data.EvaluationFormData;
import org.prosolo.web.communications.evaluation.data.SelectedBadge;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "newevaluation")
@Component("newevaluation")
@Scope("view")
public class NewEvaluationBean implements Serializable {

	private static final long serialVersionUID = 4690471817093010853L;

	protected static Logger logger = Logger.getLogger(NewEvaluationBean.class);

	@Autowired private EvaluationDataFactory evaluationDataFactory;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private BadgeManager badgeManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceSettingsBean adminSettings;

	private EvaluationFormData formData;
	private EvaluationSubmission evaluationSubmission;
	private Collection<Competence> compSearchResults;
	
	private long evaluationSubmissionId;
	private boolean editMode = false;
	
	private List<SocialActivityData> allActivities;
	private int refreshRate = Integer.MAX_VALUE;
	
	private boolean showActivityDetails = true;
	
	private List<Badge> allBadges;
	
	private boolean goalAcceptanceBasedOnCompetence;
	private boolean evaluator;
	
	private String resubmitContext;

	public NewEvaluationBean(){
		logger.debug("initializing");
		compSearchResults = new ArrayList<Competence>();
	}
	
	public void initialize() throws IOException {
		boolean hasAccess = false;
		
		if (evaluationSubmissionId > 0) {
			hasAccess = initializeEvaluationSubmission();
		}
		
		if (!hasAccess) {
			PageUtil.sendToAccessDeniedPage();
			return;
		}
		
		this.goalAcceptanceBasedOnCompetence = 
				(adminSettings.getSettings().isGoalAcceptanceDependendOnCompetence() && !formData.getEvaluatedCompetences().isEmpty())
				|| 
				!formData.isDraft();
	}

	private boolean initializeEvaluationSubmission() {
		try {
			this.evaluationSubmission = evaluationManager.loadResource(EvaluationSubmission.class, evaluationSubmissionId);
			
			// check if user can view this page
			User evaluator = evaluationSubmission.getRequest().getSentTo();
			User evaluatee = evaluationSubmission.getRequest().getMaker();
			
			User loggedU = loggedUser.getUser();
			
			if (!(loggedU.getId() == evaluator.getId() || loggedU.getId() == evaluatee.getId())) {
				return false;
			}
			
			if (loggedU.equals(evaluator)) {
				this.evaluator = true;
			}
			
			this.allBadges = badgeManager.getAllResources(Badge.class);
			
			this.formData = evaluationDataFactory.create(this.evaluationSubmission, allBadges, loggedUser.getLocale(), loggedUser.getUser());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		setEditMode(true);
		return true;
	}

	public void submitEvaluation() {
		try {
			List<EvaluatedResourceData> allEvaluatedResources = new ArrayList<EvaluatedResourceData>();
			allEvaluatedResources.addAll(formData.getEvaluatedResources());
			allEvaluatedResources.addAll(formData.getAddedCompetences());
			
			evaluationSubmission = evaluationManager.finalizeEvaluationSubmission(
					evaluationSubmission, 
					allEvaluatedResources, 
					formData.getPrimeEvaluatedResource());
			
			eventFactory.generateEvent(EventType.EVALUATION_GIVEN, loggedUser.getUser(), evaluationSubmission);
			
			try {
				PageUtil.fireSuccessfulInfoMessage("evaluationForm:evaluationGrowl", 
						ResourceBundleUtil.getMessage(
								"evaluation.growl.sumbitted", 
								loggedUser.getLocale()));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
			
			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
			FacesContext.getCurrentInstance().getExternalContext().redirect("communications/evaluations");
		} catch (InvalidParameterException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (EvaluationNotSupportedException e) {
			logger.error(e);
		}
	}
	
	public void saveDraft() {
		try {
			List<EvaluatedResourceData> allEvaluatedResources = new ArrayList<EvaluatedResourceData>();
			allEvaluatedResources.addAll(formData.getEvaluatedResources());
			allEvaluatedResources.addAll(formData.getAddedCompetences());
			
			evaluationManager.editEvaluationSubmissionDraft(evaluationSubmission, allEvaluatedResources, formData.getPrimeEvaluatedResource().getComment());
			
			try {
				PageUtil.fireSuccessfulInfoMessage("evaluationForm:evaluationGrowl", 
						ResourceBundleUtil.getMessage(
								"evaluation.growl.savedDraft", 
								loggedUser.getLocale()));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
			
			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
			FacesContext.getCurrentInstance().getExternalContext().redirect("communications/evaluations");
		} catch (EventException e) {
			logger.error(e);
		} catch (InvalidParameterException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (EvaluationNotSupportedException e) {
			logger.error(e);
		}
	}
	
	public void initializeActivities() {
	}
	
	// invoked from search added competences search box
	public void connectCompetence(Competence comp){
		AchievedCompetence achievedComp = new AchievedCompetence();
		achievedComp.setCompetence(comp);
		achievedComp.setTitle(comp.getTitle());
		
		EvaluatedResourceData addedCompetence = new EvaluatedResourceData(achievedComp, EvaluatedResourceType.ACHIEVED_COMPETENCE, false);
		addedCompetence.addBadges(allBadges);
		formData.addCompetence(addedCompetence);
	}
	
	public void removeEvaluatedResource(EvaluatedResourceData resourceToRemoveData) {
		Iterator<EvaluatedResourceData> iterator = formData.getAddedCompetences().iterator();
		
		while (iterator.hasNext()) {
			EvaluatedResourceData resData = (EvaluatedResourceData) iterator.next();
			
			if (resData.equals(resourceToRemoveData)) {
				iterator.remove();
				break;
			}
		}
	}
	
	public void toggleBadge(EvaluatedResourceData evaluatedResData, SelectedBadge badge) {
		if (badge != null) {
			for (SelectedBadge b : evaluatedResData.getBadges()) {
				if (b.equals(badge)) {
					b.setSelected(!b.isSelected());
					break;
				}
			}
		}
	}
	
	// invoked from competence search to know which competences to exclude from search
	public ArrayList<Long> getAllCompetenceIds() {
		if (formData.getPrimeEvaluatedResource() != null) {
			ArrayList<Long> ids = new ArrayList<Long>();
			
			if (formData.getPrimeEvaluatedResource().getResource() instanceof TargetLearningGoal) {
				Collection<EvaluatedResourceData> addedCompetences = formData.getEvaluatedResourcesOfType(EvaluatedResourceType.TARGET_COMPETENCE);
				addedCompetences.addAll(formData.getAddedCompetences());
				
				for (EvaluatedResourceData evaluatedResourceData : addedCompetences) {
					if (evaluatedResourceData.getResource() instanceof Competence) {
						ids.add((evaluatedResourceData.getResource()).getId());
					} else if (evaluatedResourceData.getResource() instanceof TargetCompetence) {
						ids.add(((TargetCompetence) evaluatedResourceData.getResource()).getCompetence().getId());
					} else if (evaluatedResourceData.getResource() instanceof AchievedCompetence) {
						ids.add(((AchievedCompetence) evaluatedResourceData.getResource()).getCompetence().getId());
					}
				}
			} else if (formData.getPrimeEvaluatedResource().getResource() instanceof ExternalCredit) {
				Collection<AchievedCompetence> compsToExclude = ((ExternalCredit) formData.getPrimeEvaluatedResource().getResource()).getCompetences();
				
				if (compsToExclude != null) {
					for (AchievedCompetence comp : compsToExclude) {
						ids.add(comp.getCompetence().getId());
					}
				}

				Collection<EvaluatedResourceData> addedCompetences = formData.getAddedCompetences();
				
				if (addedCompetences != null) {
					for (EvaluatedResourceData evaluatedResourceData : addedCompetences) {
						ids.add(evaluatedResourceData.getId());
					}
				}
			}
				
			return ids;
		}
		return null;
	}
	
	public void resubmitEvaluationRequest() {
		try {
			Request request = evaluationManager.resubmitEvaluationRequest(loggedUser.refreshUser(), evaluationSubmission, "");
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", resubmitContext);
			parameters.put("resourceId", String.valueOf(formData.getPrimeEvaluatedResource().getId()));
			parameters.put("resourceType", formData.getPrimeEvaluatedResource().getResource().getClass().getSimpleName());
			parameters.put("user", String.valueOf(request.getSentTo().getId()));
			
			eventFactory.generateEvent(request.getRequestType(), request.getMaker(), request, parameters);
			
			formData.setWaitingForSubmission(true);
			formData.setCanBeResubmitted(false);
			
			PageUtil.fireSuccessfulInfoMessage("Evaluation request sent.");
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public EvaluationFormData getFormData() {
		return formData;
	}
	
	public void setFormData(EvaluationFormData formData) {
		this.formData = formData;
	}

	public Collection<Competence> getCompSearchResults() {
		return compSearchResults;
	}
	
	public long getEvaluationSubmissionId() {
		return evaluationSubmissionId;
	}

	public void setEvaluationSubmissionId(long evaluationSubmissionId) {
		this.evaluationSubmissionId = evaluationSubmissionId;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}
	
	public List<SocialActivityData> getAllActivities() {
		return allActivities;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public boolean isShowActivityDetails() {
		return showActivityDetails;
	}

	public void setShowActivityDetails(boolean showActivityDetails) {
		this.showActivityDetails = showActivityDetails;
	}

	public boolean isGoalAcceptanceBasedOnCompetence() {
		return goalAcceptanceBasedOnCompetence;
	}

	public boolean isEvaluator() {
		return evaluator;
	}

	public EvaluationSubmission getEvaluationSubmission() {
		return evaluationSubmission;
	}

	public void setResubmitContext(String resubmitContext) {
		this.resubmitContext = resubmitContext;
	}
	
}