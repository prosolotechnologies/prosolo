package org.prosolo.core.spring.security.authentication.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.LtiTool;

/**
 * 
 * @author Stefan Vuckovic
 * @deprecated since 0.7
 */
@Deprecated
public class CredentialUrlBuilder implements ToolLaunchUrlBuilder {

	@Override
	public String getLaunchUrl() {
		return null;
	}

	protected String getUrlParameters(LtiTool tool, long userId) {
//		long id = ServiceLocator.getInstance().getService(CourseManager.class).getTargetLearningGoalIdForCourse(userId, tool.getLearningGoalId());
//		return "id=" + ServiceLocator.getInstance().getService(UrlIdEncoder.class).encodeId(id);
		return null;
	}

}
