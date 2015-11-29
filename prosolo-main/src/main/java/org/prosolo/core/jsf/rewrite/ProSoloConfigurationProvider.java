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
				.addRule(Join.path("/passwordReset").to("/passwordReset.xhtml"))
				.addRule(Join.path("/recovery").to("/recovery.xhtml"))
				.addRule(Join.path("/register").to("/register.xhtml"))
				.addRule(Join.path("/verify").to("/verify.xhtml"))
				.addRule(Join.path("/terms").to("/terms.xhtml"))
				.addRule(Join.path("/maintenance").to("/maintenance.xhtml"))
				.addRule(Join.path("/notfound").to("/notfound.xhtml"))
				
				.addRule(Join.path("/").to("/index.xhtml"))
				.addRule(Join.path("/learn/{id}/{comp}").to("/learn.xhtml"))
				.addRule(Join.path("/learn/{id}").to("/learn.xhtml"))
				.addRule(Join.path("/learn").to("/learn.xhtml"))
				.addRule(Join.path("/plan").to("/plan.xhtml"))
				.addRule(Join.path("/profile/{id}").to("/publicprofile.xhtml"))
				.addRule(Join.path("/profile").to("/profile.xhtml"))
				.addRule(Join.path("/credentials/{id}").to("/course.xhtml"))
				.addRule(Join.path("/digest").to("/digest.xhtml"))
				.addRule(Join.path("/communications/{tab}").to("/communications.xhtml"))
				.addRule(Join.path("/search/{tab}").to("/search.xhtml"))
				.addRule(Join.path("/settings/{tab}").to("/settings.xhtml"))
				.addRule(Join.path("/settings").to("/settings.xhtml"))
				.addRule(Join.path("/evaluation/{id}").to("/evaluation.xhtml"))
				.addRule(Join.path("/posts/{id}/{comment}").to("/post.xhtml"))
				.addRule(Join.path("/posts/{id}").to("/post.xhtml"))
				.addRule(Join.path("/messages").to("/messages.xhtml"))
				
				.addRule(Join.path("/manage/competences/{id}").to("/manage/competence.xhtml"))
				.addRule(Join.path("/manage/competences").to("/manage/competences.xhtml"))
				.addRule(Join.path("/manage/credentials/create/").to("/manage/newCourse.xhtml"))
				.addRule(Join.path("/manage/competences/{comp}/tools").to("/manage/tools.xhtml"))
				.addRule(Join.path("/manage/credentials/{cred}/tools").to("/manage/tools.xhtml"))
				.addRule(Join.path("/manage/tools/{cred}/{comp}/{act}/create").to("/manage/externalTools/toolDetails.xhtml"))
				.addRule(Join.path("/manage/tools/{id}").to("/manage/externalTools/toolDetails.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}/students").to("/manage/courseMembers.xhtml"))
				.addRule(Join.path("/manage/credentials/{id}").to("/manage/course.xhtml"))
				.addRule(Join.path("/manage/credentials").to("/manage/courses.xhtml"))
				.addRule(Join.path("/manage/reports").to("/manage/reports.xhtml"))
				.addRule(Join.path("/manage/students/{id}").to("/manage/studentProfile.xhtml"))
				.addRule(Join.path("/admin/users").to("/admin/users.xhtml"))
				.addRule(Join.path("/admin/roles").to("/admin/roles.xhtml"))
				.addRule(Join.path("/admin/dashboard").to("/admin/dashboard.xhtml"))
				.addRule(Join.path("/admin/settings").to("/admin/settings.xhtml"))
				.addRule(Join.path("/admin/analyticsSettings").to("/admin/analyticsSettings.xhtml"));
	}
}