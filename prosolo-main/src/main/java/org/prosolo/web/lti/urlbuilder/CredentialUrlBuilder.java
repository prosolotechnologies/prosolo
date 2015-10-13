package org.prosolo.web.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.CourseManager;

public class CredentialUrlBuilder extends ToolLaunchUrlBuilder{
	
	@Override
	protected String getUrlParameters(LtiTool tool, long userId) {
		long id = ServiceLocator.getInstance().getService(CourseManager.class).getTargetLearningGoalIdForCourse(userId, tool.getLearningGoalId());
		return "id="+id;
	}

}
