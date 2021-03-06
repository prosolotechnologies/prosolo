package org.prosolo.core.jsf.rewrite;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

import javax.servlet.ServletContext;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
@RewriteConfiguration
public class RewriteConfigurationProvider extends HttpConfigurationProvider {
	
	@Override
	public int priority() {
		return 10;
	}
	
	@Override
	public Configuration getConfiguration(final ServletContext context) {
		return ConfigurationBuilder.begin()

				.addRule(Join.path("/login").to("/login.xhtml"))
				.addRule(Join.path("/loginAdmin").to("/loginAdmin.xhtml"))
				.addRule(Join.path("/about").to("/about.xhtml"))
				.addRule(Join.path("/accessDenied").to("/accessDenied.xhtml"))
				.addRule(Join.path("/reset/successful/{email}").to("/login-forgot-message.xhtml"))
				.addRule(Join.path("/reset").to("/login-forgot-password.xhtml"))
				.addRule(Join.path("/recovery/{key}").to("/login-new-password.xhtml"))
				.addRule(Join.path("/register").to("/register.xhtml"))
				.addRule(Join.path("/verify").to("/verify.xhtml"))
				.addRule(Join.path("/terms").to("/terms.xhtml"))
				.addRule(Join.path("/maintenance").to("/maintenance.xhtml"))
				.addRule(Join.path("/notfound").to("/404.xhtml"))

				.addRule(Join.path("/").to("/root.xhtml"))
				.addRule(Join.path("/home").to("/index.xhtml"))
				.addRule(Join.path("/learn/{id}/{comp}").to("/learn.xhtml"))
				.addRule(Join.path("/learn/{id}").to("/learn.xhtml"))
				.addRule(Join.path("/learn").to("/learn.xhtml"))
				.addRule(Join.path("/plan").to("/plan.xhtml"))
				.addRule(Join.path("/profile").to("/profile-legacy.xhtml"))
				.addRule(Join.path("/profile/{studentId}").to("/profile-legacy.xhtml"))
				.addRule(Join.path("/evidence/{competenceEvidenceId}/preview").to("/evidence-preview.xhtml"))
				.addRule(Join.path("/p/{customProfileUrl}").to("/profile.xhtml"))
				.addRule(Join.path("/credentials/{id}").to("/credential.xhtml"))
				.addRule(Join.path("/credentials/{id}/preview").to("/public-credential.xhtml"))
				.addRule(Join.path("/credentials/{id}/announcements/{announcementId}").to("/announcement.xhtml"))
				.addRule(Join.path("/credentials/{id}/announcements").to("/announcements.xhtml"))
				.addRule(Join.path("/credentials/{id}/students").to("/credential-students.xhtml"))
				.addRule(Join.path("/credentials/{id}/students/{studentId}").to("/credential-students-compare.xhtml"))
				.addRule(Join.path("/credentials/{id}/keywords").to("/credential-keywords.xhtml"))
				.addRule(Join.path("/credentials/{id}/assessments").to("/credential-assessments-root.xhtml"))
				.addRule(Join.path("/credentials/{id}/assessments/self").to("/credential-self-assessment.xhtml"))
				.addRule(Join.path("/credentials/{id}/assessments/instructor").to("/credential-instructor-assessment.xhtml"))
				.addRule(Join.path("/credentials/{id}/assessments/peer").to("/credential-peer-assessments.xhtml"))
				.addRule(Join.path("/credentials/{id}/assessments/peer/{assessmentId}").to("/credential-assessment.xhtml"))
				.addRule(Join.path("/credentials/{id}/assessments/instructor").to("/credential-instructor-assessment.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}").to("/competence.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/assessments").to("/competence-assessments-root.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/assessments/self").to("/competence-self-assessment.xhtml"))
                .addRule(Join.path("/credentials/{credId}/competences/{compId}/assessments/peer").to("/competence-peer-assessments.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/assessments/peer/{assessmentId}").to("/competence-peer-assessment.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/assessments/instructor").to("/competence-instructor-assessment.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/activities/{actId}").to("/activity.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/activities/{actId}/responses").to("/activity-responses.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/{compId}/activities/{actId}/responses/{targetActId}").to("/activity-response.xhtml"))
				.addRule(Join.path("/digest").to("/digest.xhtml"))
				.addRule(Join.path("/settings/password").to("/settings/password.xhtml"))
				.addRule(Join.path("/settings/email").to("/settings/email.xhtml"))
				.addRule(Join.path("/settings/twitterOAuth").to("/settings/twitterOAuth.xhtml"))
				.addRule(Join.path("/settings").to("/settings.xhtml"))
				.addRule(Join.path("/messages/{id}").to("/messages.xhtml"))
				.addRule(Join.path("/messages").to("/messages.xhtml"))
				.addRule(Join.path("/library").to("/credentialLibrary.xhtml"))
				.addRule(Join.path("/credentials").to("/credentialLibrary.xhtml"))
				.addRule(Join.path("/competencies").to("/competenceLibrary.xhtml"))
				.addRule(Join.path("/notifications").to("/notifications.xhtml"))
				.addRule(Join.path("/people").to("/people.xhtml"))
				.addRule(Join.path("/people/followers").to("/people-followers.xhtml"))
				.addRule(Join.path("/people/following").to("/people-following.xhtml"))
				.addRule(Join.path("/evidence").to("/evidence-repository.xhtml"))
				.addRule(Join.path("/evidence/{id}/edit").to("/evidence-edit.xhtml"))
				.addRule(Join.path("/evidence/new").to("/evidence-edit.xhtml"))
				.addRule(Join.path("/evidence/{id}").to("/evidence.xhtml"))
				/* student viewing all his assessments (credential-assessments.html)*/
				//.addRule(Join.path("/assessments").to("/assessments.xhtml"))

				.addRule(Join.path("/posts/{id}").to("/wall-post-view.xhtml"))
				.addRule(Join.path("/groups/{id}/join").to("/groups-join.xhtml"))
				.addRule(Join.path("/assessments/my/credentials").to("/my-assessments-credentials.xhtml"))
				.addRule(Join.path("/assessments/my/competences").to("/my-assessments-competences.xhtml"))
				.addRule(Join.path("/assessments/my/competences/{assessmentId}").to("/my-assessments-competences-assessment.xhtml"))

				.addRule(Join.path("/manage/notfound").to("/manage/404.xhtml"))
				.addRule(Join.path("/manage/accessDenied").to("/manage/accessDenied.xhtml"))
				.addRule(Join.path("/manage").to("/manage/library.xhtml"))
				.addRule(Join.path("/manage/rubrics").to("/manage/rubricLibrary.xhtml"))
				.addRule(Join.path("/manage/rubrics/{id}/settings").to("/manage/rubricEdit.xhtml"))
				.addRule(Join.path("/manage/rubrics/{id}/privacy").to("/manage/rubric-privacy.xhtml"))
				.addRule(Join.path("/manage/rubrics/{id}").to("/manage/rubric-criteria.xhtml"))
				.addRule(Join.path("/manage/notifications").to("/manage/notifications.xhtml"))
				.addRule(Join.path("/manage/settings/password").to("/manage/settings/password.xhtml"))
				.addRule(Join.path("/manage/settings/email").to("/manage/settings/email.xhtml"))
				.addRule(Join.path("/manage/settings").to("/manage/settings.xhtml"))
				.addRule(Join.path("/manage/competences/{compId}/tools").to("/manage/tools.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/tools").to("/manage/tools.xhtml"))
				.addRule(Join.path("/manage/tools/{credId}/{compId}/{actId}/create").to("/manage/externalTools/toolDetails.xhtml"))
				.addRule(Join.path("/manage/tools/{id}").to("/manage/externalTools/toolDetails.xhtml"))
				//manage credential
				.addRule(Join.path("/manage/credentials/new").to("/manage/credential-create.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}").to("/manage/credential.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/edit").to("/manage/credential-create.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/students").to("/manage/credential-students.xhtml"))
				.addRule(Join.path("/manage/students/{id}").to("/manage/studentProfile.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/editors").to("/manage/credential-editors.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/privacy").to("/manage/credential-privacy.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/who-can-learn").to("/manage/credential-who-can-learn.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/instructors").to("/manage/credential-instructors.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/assessments").to("/manage/credential-delivery-assessments.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/assessments/activities/{activityId}").to("/manage/credential-delivery-assessments-activity.xhtml"))
				.addRule(Join.path("/manage/credentials/{credentialId}/assessments/competencies/{competenceId}").to("/manage/credential-delivery-assessments-competence.xhtml"))
				/* instructor viewing single assessment where he is instructor (manage-assessment-preview.html) */
				.addRule(Join.path("/manage/credentials/{id}/assessments/{assessmentId}").to("/manage/credential-assessment.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/announcements").to("/manage/announcements.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/deliveries").to("/manage/credential-deliveries.xhtml"))


				.addRule(Join.path("/manage/credentials/{credId}/competences/new").to("/manage/create-competence.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{compId}").to("/manage/competence.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/edit").to("/manage/create-competence.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/who-can-learn").to("/manage/competence-who-can-learn.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/editors").to("/manage/competence-editors.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/students").to("/manage/competence-students.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/privacy").to("/manage/competence-privacy.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{compId}").to("/manage/competence.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/edit").to("/manage/create-competence.xhtml"))
				.addRule(Join.path("/manage/competences").to("/manage/competences.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/who-can-learn").to("/manage/competence-who-can-learn.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/editors").to("/manage/competence-editors.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/students").to("/manage/competence-students.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{id}/privacy").to("/manage/competence-privacy.xhtml"))

				.addRule(Join.path("/manage/reports").to("/manage/reports.xhtml"))
				//manage activity
				.addRule(Join.path("/manage/credentials/{credId}/competences/{compId}/activities/new").to("/manage/create-activity.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{compId}/activities/{actId}").to("/manage/activity.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/{compId}/activities/{id}/edit").to("/manage/create-activity.xhtml"))

				//manage library
				.addRule(Join.path("/manage/library").to("/manage/library.xhtml"))
				.addRule(Join.path("/manage/library/credentials").to("/manage/credentialLibrary.xhtml"))
				.addRule(Join.path("/manage/library/competencies").to("/manage/competenceLibrary.xhtml"))
				.addRule(Join.path("/manage/library/instructor/credentials").to("/manage/credential-library-instructor.xhtml"))
				.addRule(Join.path("/manage/evidence/{competenceEvidenceId}/preview").to("/manage/evidence-preview.xhtml"))

				//admin
				.addRule(Join.path("/admin/notfound").to("/admin/404.xhtml"))
				.addRule(Join.path("/admin/accessDenied").to("/admin/accessDenied.xhtml"))
				.addRule(Join.path("/admin/").to("/admin/root.xhtml"))
				.addRule(Join.path("/admin").to("/admin/root.xhtml"))
				.addRule(Join.path("/admin/roles").to("/admin/roles.xhtml"))
				.addRule(Join.path("/admin/admins").to("/admin/admins.xhtml"))
				.addRule(Join.path("/admin/admins/new").to("/admin/adminNew.xhtml"))
				.addRule(Join.path("/admin/admins/{id}/edit").to("/admin/adminNew.xhtml"))
				.addRule(Join.path("/admin/organizations").to("/admin/organizations.xhtml"))
				.addRule(Join.path("/admin/organizations/new").to("/admin/organization-settings.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/accounts/{id}/edit/password").to("/admin/userEditPassword.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/accounts/new").to("/admin/organizationUserEdit.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/accounts/{id}/edit").to("/admin/organizationUserEdit.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/accounts").to("/admin/organizationUsers.xhtml"))
				.addRule(Join.path("/admin/organizations/{id}/settings").to("/admin/organization-settings.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{id}/managers").to("/admin/unit-managers.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{id}/students").to("/admin/unit-students.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{id}/instructors").to("/admin/unit-instructors.xhtml"))
				.addRule(Join.path("/admin/organizations/{id}/units/{unitId}/credentials").to("/admin/unit-credentials.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/credentials/{id}").to("/admin/credential.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/credentials/{id}/who-can-learn").to("/admin/credential-who-can-learn.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/credentials/{credId}/{compId}").to("/admin/competence.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/credentials/{credId}/{compId}/{actId}").to("/admin/activity.xhtml"))
				.addRule(Join.path("/admin/organizations/{id}/units/{unitId}/settings").to("/admin/unit-settings.xhtml"))
				.addRule(Join.path("/admin/organizations/{id}/units").to("/admin/units.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{id}/groups").to("/admin/unit-groups.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/groups/{id}/users").to("/admin/unit-group-users.xhtml"))
				.addRule(Join.path("/admin/admins/{id}/edit/password").to("/admin/adminEditPassword.xhtml"))
				.addRule(Join.path("/admin/dashboard").to("/admin/dashboard.xhtml"))
				.addRule(Join.path("/admin/settings/password").to("/admin/settings/password.xhtml"))
				.addRule(Join.path("/admin/settings").to("/admin/settings.xhtml"))
				.addRule(Join.path("/admin/settings_old").to("/admin/settings_old.xhtml"))
				.addRule(Join.path("/admin/other").to("/admin/other.xhtml"))
				.addRule(Join.path("/admin/data-init").to("/admin/data-init.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/auto-enrollment").to("/admin/organizations-unit-auto-enrollment.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/auto-enrollment/new").to("/admin/organizations-unit-auto-enrollment-edit.xhtml"))
				.addRule(Join.path("/admin/organizations/{orgId}/units/{unitId}/auto-enrollment/{toolId}/edit").to("/admin/organizations-unit-auto-enrollment-edit.xhtml"));

	}
}