package org.prosolo.web.lti.urlbuilder;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.CourseManager;

public class CompetenceUrlBuilder extends ToolLaunchUrlBuilder{

	@Override
	protected String getUrlParameters(LtiTool tool, long userId) {
		Object [] ids = ServiceLocator.getInstance().getService(CourseManager.class).getTargetGoalAndCompetenceIds(userId, tool.getLearningGoalId(), tool.getCompetenceId());
		return "id="+ids[0]+"&comp="+ids[1];
	}

}
