package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.TextSearch;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.administration.data.ResourceSettingsData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 * Aug 27, 2014
 */
@ManagedBean(name = "adminSettings")
@Component("adminSettings")
@Scope("application")
public class ResourceSettingsBean implements Serializable {
	
	private static final long serialVersionUID = 3452898959032269681L;

	private static Logger logger = Logger.getLogger(ResourceSettingsBean.class.getName());
	
	@Autowired private TextSearch textSearch;
	@Autowired private RoleManager roleManager;
	@Autowired private ResourceSettingsManager resourceSettingsManager;
	
	
	@Autowired private CourseManager courseManager;
	
	private ResourceSettingsData settings;
	
	@PostConstruct
	public void init() {
		settings = new ResourceSettingsData(resourceSettingsManager.getResourceSettings());

		logger.debug("Initializing admin resource settings" +
				", selectedUsersCanDoEvaluation: " + settings.isSelectedUsersCanDoEvaluation() +
				", userCanCreateCompetence: " + settings.isUserCanCreateCompetence() +
				", goalAcceptanceDependendOnCompetence: " + settings.isGoalAcceptanceDependendOnCompetence());
	}
	
	public void updateSettings() {
		logger.debug("Updating admin resource settings" +
				", selectedUsersCanDoEvaluation: " + settings.isSelectedUsersCanDoEvaluation() +
				", userCanCreateCompetence: " + settings.isUserCanCreateCompetence() +
				", goalAcceptanceDependendOnCompetence: " + settings.isGoalAcceptanceDependendOnCompetence());
		
		EvaluationSettingsBean evaluationSettingsBean = PageUtil.getViewScopedBean("evaluationSettings", EvaluationSettingsBean.class);
		
		if (evaluationSettingsBean.isUsersChanged()) {
			setSelectedEvaluators(evaluationSettingsBean.getSelectedEvaluators());
			removeSelectedEvaluators(evaluationSettingsBean.getRemovedEvaluators());
		}
		
		settings.updateSettings();
		settings.setSettings(resourceSettingsManager.updateResourceSettigns(settings.getSettings()));
		
		PageUtil.fireSuccessfulInfoMessage("Settings are successfully saved.");
	}
	
	/* GETTERS / SETTERS */

	private void setSelectedEvaluators(List<UserData> selectedEvaluators) {
		if (selectedEvaluators != null && !selectedEvaluators.isEmpty()) {
			// create Evaluator role if does not exist
			
			try {
				Role role = roleManager.getOrCreateNewRole(
						ResourceBundleUtil.getMessage("admin.roles.predefinedRole.evaluator.name", new Locale("en", "US")), 
						ResourceBundleUtil.getMessage("admin.roles.predefinedRole.evaluator.description", new Locale("en", "US")), 
						false);
				
				for (UserData userData : selectedEvaluators) {
					roleManager.assignRoleToUser(
							role, 
							userData.getId());
				}
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}

	private void removeSelectedEvaluators(List<UserData> removedEvaluators) {
		if (removedEvaluators != null && !removedEvaluators.isEmpty()) {
			try {
				Role role = roleManager.getOrCreateNewRole(
					ResourceBundleUtil.getMessage("admin.roles.predefinedRole.evaluator.name", new Locale("en", "US")), 
					ResourceBundleUtil.getMessage("admin.roles.predefinedRole.evaluator.description", new Locale("en", "US")), 
					false);
			
				for (UserData userData : removedEvaluators) {
					roleManager.removeRoleFromUser(
							role, 
							userData.getId());
				}
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}

	public ResourceSettingsData getSettings() {
		return settings;
	}
	
	/*
	 * USED ONLY BY A SYSTEM USER ONCE
	 */
	public void fixCourses() {
		logger.info("Executing course reference fix by a system user");
		courseManager.fixCourseReferences();
		logger.info("Finished reference fix");
	}
}
