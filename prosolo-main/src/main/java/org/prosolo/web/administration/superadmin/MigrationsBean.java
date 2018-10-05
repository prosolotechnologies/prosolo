package org.prosolo.web.administration.superadmin;

import org.apache.log4j.Logger;
import org.prosolo.services.migration.CommonCustomMigrationService;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name="migrationsBean")
@Component("migrationsBean")
@Scope("view")
public class MigrationsBean implements Serializable {

	private static final long serialVersionUID = -747140356305370777L;
	
	protected static Logger logger = Logger.getLogger(MigrationsBean.class);
	
	@Inject
	private CommonCustomMigrationService commonCustomMigrationService;
	@Inject
	private LoggedUserBean loggedUser;

	public void migrateAssessments() {
		try {
			commonCustomMigrationService.migrateAssessments();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessments migrated");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error migrating assessments");
		}
	}

	public void migrateAssessmentDiscussions() {
		try {
			commonCustomMigrationService.migrateAssessmentDiscussions();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessment discussions migrated");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error migrating assessment discussions");
		}
	}

	public void migrateCompetenceAssessmentPoints() {
		try {
			commonCustomMigrationService.migrateCompetenceAssessmentPoints();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessments migration successful");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error migrating assessments");
		}
	}

	public void migrateSelfAssessments() {
		try {
			commonCustomMigrationService.createSelfAssessments(loggedUser.getUserContext());
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Created self-assessments");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error creating self-assessments");
		}
	}

	public void migrateCredentialAssessmentsAssessedFlag() {
		try {
			commonCustomMigrationService.migrateCredentialAssessmentsAssessedFlag();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessments migrated");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error migrating assessments");
		}
	}

}
