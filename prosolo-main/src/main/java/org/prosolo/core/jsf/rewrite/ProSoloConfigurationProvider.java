package org.prosolo.core.jsf.rewrite;

import javax.servlet.ServletContext;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
@RewriteConfiguration
public class ProSoloConfigurationProvider extends HttpConfigurationProvider {
	
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
				.addRule(Join.path("/recovery").to("/recovery.xhtml"))
				.addRule(Join.path("/register").to("/register.xhtml"))
				.addRule(Join.path("/verify").to("/verify.xhtml"))
				.addRule(Join.path("/terms").to("/terms.xhtml"))
				.addRule(Join.path("/maintenance").to("/maintenance.xhtml"))
				.addRule(Join.path("/notfound").to("/notfound.xhtml"))
				
				.addRule(Join.path("/").to("/credentialLibrary.xhtml"))
				.addRule(Join.path("/learn/{id}/{comp}").to("/learn.xhtml"))
				.addRule(Join.path("/learn/{id}").to("/learn.xhtml"))
				.addRule(Join.path("/learn").to("/learn.xhtml"))
				.addRule(Join.path("/plan").to("/plan.xhtml"))
				.addRule(Join.path("/profile/{id}").to("/profile.xhtml"))
				.addRule(Join.path("/profile").to("/profile.xhtml"))
				.addRule(Join.path("/credentials/{id}/edit").to("/create-credential.xhtml"))
				.addRule(Join.path("/credentials/new").to("/create-credential.xhtml"))
				//.addRule(Join.path("/credentials/{id}").to("/course.xhtml"))
				.addRule(Join.path("/credentials/{id}").to("/credential.xhtml"))
				//TODO validate these competence patterns 
				.addRule(Join.path("/competences/{id}/edit").to("/create-competence.xhtml"))
				.addRule(Join.path("/competences/new").to("/create-competence.xhtml"))
				.addRule(Join.path("/credentials/{credId}/competences/new").to("/create-competence.xhtml"))
				.addRule(Join.path("/credentials/{credId}/{compId}").to("/competence.xhtml"))
				.addRule(Join.path("/competences/{compId}").to("/competence.xhtml"))
				.addRule(Join.path("/competences/{compId}/{id}/edit").to("/create-activity.xhtml"))
				.addRule(Join.path("/competences/{compId}/newActivity").to("/create-activity.xhtml"))
				.addRule(Join.path("/credentials/{credId}/{compId}/{actId}").to("/activity.xhtml"))
				.addRule(Join.path("/competences/{compId}/{actId}").to("/activity.xhtml"))
				.addRule(Join.path("/digest").to("/digest.xhtml"))
				.addRule(Join.path("/communications/{tab}").to("/communications.xhtml"))
				.addRule(Join.path("/search/{tab}").to("/search.xhtml"))
				.addRule(Join.path("/settings_old/{tab}").to("/settings_old.xhtml"))
				.addRule(Join.path("/settings_old").to("/settings_old.xhtml"))
				.addRule(Join.path("/settings/password").to("/settings/password.xhtml"))
				.addRule(Join.path("/settings/email").to("/settings/email.xhtml"))
				.addRule(Join.path("/settings").to("/settings.xhtml"))
				.addRule(Join.path("/evaluation/{id}").to("/evaluation.xhtml"))
				.addRule(Join.path("/posts/{id}/{comment}").to("/post.xhtml"))
				.addRule(Join.path("/posts/{id}").to("/post.xhtml"))
				.addRule(Join.path("/messages/{id}").to("/messages.xhtml"))
				.addRule(Join.path("/messages").to("/messages.xhtml"))
				.addRule(Join.path("/library").to("/credentialLibrary.xhtml"))
				.addRule(Join.path("/library/credentials").to("/credentialLibrary.xhtml"))
				.addRule(Join.path("/library/competences").to("/competenceLibrary.xhtml"))
				.addRule(Join.path("/notifications").to("/notifications.xhtml"))
				.addRule(Join.path("/achievements/credentials").to("/achievements/credentials.xhtml"))
				.addRule(Join.path("/achievements/competences").to("/achievements/competences.xhtml"))
				.addRule(Join.path("/achievements/inprogress").to("/achievements/inprogress.xhtml"))
				.addRule(Join.path("/achievements/externalcompetences").to("/achievements/externalcompetences.xhtml"))
				.addRule(Join.path("/achievements").to("/achievements/credentials"))
				.addRule(Join.path("/people").to("/people.xhtml"))
				
				.addRule(Join.path("/manage").to("/manage/credentialLibrary.xhtml"))
				.addRule(Join.path("/manage/").to("/manage/credentialLibrary.xhtml"))
				.addRule(Join.path("/manage/notifications").to("/manage/notifications.xhtml"))
				.addRule(Join.path("/manage/messages/{id}").to("/manage/messages.xhtml"))
				.addRule(Join.path("/manage/messages").to("/manage/messages.xhtml"))
				.addRule(Join.path("/manage/settings/password").to("/manage/settings/password.xhtml"))
				.addRule(Join.path("/manage/settings/email").to("/manage/settings/email.xhtml"))
				.addRule(Join.path("/manage/settings").to("/manage/settings.xhtml"))
				.addRule(Join.path("/manage/competences/{compId}/tools").to("/manage/tools.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/tools").to("/manage/tools.xhtml"))
				.addRule(Join.path("/manage/tools/{credId}/{compId}/{actId}/create").to("/manage/externalTools/toolDetails.xhtml"))
				.addRule(Join.path("/manage/tools/{id}").to("/manage/externalTools/toolDetails.xhtml"))
				//manage credential
				.addRule(Join.path("/manage/credentials/{id}/edit").to("/manage/create-credential.xhtml"))
				.addRule(Join.path("/manage/credentials/new").to("/manage/create-credential.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}").to("/manage/credential.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/students").to("/manage/credential-students.xhtml"))
				.addRule(Join.path("/manage/students/{id}").to("/manage/studentProfile.xhtml"))
				//.addRule(Join.path("/manage/credentials/{courseId}/competences").to("/manage/credential-competences.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/instructors").to("/manage/credential-instructors.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/feeds").to("/manage/courseFeeds.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/assessments").to("/manage/credential-assessments.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/assessments/{assId}").to("/manage/assessment-preview.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/rss").to("/manage/credential-rss.xhtml"))
				
				.addRule(Join.path("/manage/competences/{id}/edit").to("/manage/create-competence.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/competences/new").to("/manage/create-competence.xhtml"))
				.addRule(Join.path("/manage/competences/new").to("/manage/create-competence.xhtml"))
				//.addRule(Join.path("/manage/competences/{compId}/activities").to("/manage/competence-activities.xhtml"))
				//.addRule(Join.path("/manage/competences/{compId}").to("/manage/competence-overall.xhtml"))
				.addRule(Join.path("/manage/competences").to("/manage/competences.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/{compId}").to("/manage/competence.xhtml"))
				.addRule(Join.path("/manage/competences/{compId}").to("/manage/competence.xhtml"))
				
				//.addRule(Join.path("/manage/credentials/{courseId}/instructors/{id}/reassignStudents").to("/manage/credential-instructors-reassign.xhtml"))
				//.addRule(Join.path("/manage/credentials/{courseId}/instructors/{id}/edit").to("/manage/credential-instructors-add.xhtml"))
				//.addRule(Join.path("/manage/credentials/{courseId}/instructors/new").to("/manage/credential-instructors-add.xhtml"))
				//.addRule(Join.path("/manage/credentials/{courseId}/students/{id}").to("/manage/studentProfile.xhtml"))
				//.addRule(Join.path("/manage/credentials/{id}").to("/manage/credential.xhtml"))
				//.addRule(Join.path("/manage/credentials").to("/manage/courses.xhtml"))
				.addRule(Join.path("/manage/reports").to("/manage/reports.xhtml"))
				//manage activity
				.addRule(Join.path("/manage/competences/{compId}/{id}/edit").to("/manage/create-activity.xhtml"))
				.addRule(Join.path("/manage/competences/{compId}/newActivity").to("/manage/create-activity.xhtml"))
				.addRule(Join.path("/manage/credentials/{credId}/{compId}/{actId}").to("/manage/activity.xhtml"))
				.addRule(Join.path("/manage/competences/{compId}/{actId}").to("/manage/activity.xhtml"))
				//manage library
				.addRule(Join.path("/manage/library").to("/manage/credentialLibrary.xhtml"))
				.addRule(Join.path("/manage/library/credentials").to("/manage/credentialLibrary.xhtml"))
				.addRule(Join.path("/manage/library/competences").to("/manage/competenceLibrary.xhtml"))
				
				.addRule(Join.path("/admin/users").to("/admin/users.xhtml"))
				.addRule(Join.path("/admin/roles").to("/admin/roles.xhtml"))
				.addRule(Join.path("/admin/dashboard").to("/admin/dashboard.xhtml"))
				.addRule(Join.path("/admin/settings").to("/admin/settings.xhtml"))
				.addRule(Join.path("/admin/analyticsSettings").to("/admin/analyticsSettings.xhtml"));
	}
}