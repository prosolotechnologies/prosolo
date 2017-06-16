package org.prosolo.web.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.LtiTool;

/**
 * 
 * @author Stefan Vuckovic
 * @deprecated since 0.7
 */
@Deprecated
public class CompetenceUrlBuilder extends ToolLaunchUrlBuilder{

	@Override
	protected String getUrlParameters(LtiTool tool, long userId) {
//		Object [] ids = ServiceLocator.getInstance().getService(CourseManager.class).
//				getTargetGoalAndCompetenceIds(userId, tool.getLearningGoalId(), tool.getCompetenceId());
//		long courseId = (long) ids[0];
//		long compId = (long) ids[1];
//		return "id="+ServiceLocator.getInstance().getService(UrlIdEncoder.class).encodeId(courseId) +
//				"&comp=" + ServiceLocator.getInstance().getService(UrlIdEncoder.class).encodeId(compId);
		return null;
	}

}
