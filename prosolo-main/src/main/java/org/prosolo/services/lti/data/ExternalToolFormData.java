package org.prosolo.services.lti.data;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.services.nodes.data.BasicObjectInfo;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
@Getter @Setter
public class ExternalToolFormData implements Serializable {
	
	private static final long serialVersionUID = 8543622282202374274L;

	private ResourceType toolType;
	private long toolId;
	private String title;
	private String description;
	private long organizationId;
	private long unitId;
	private BasicObjectInfo userGroupData;
	private String launchUrl;
	private String consumerKey;
	private String consumerSecret;
	private String regUrl;
	
	private boolean initialized;

	public ExternalToolFormData () {
		
	}
	
	public ExternalToolFormData(LtiTool tool) {
		toolType = tool.getToolType();
		toolId = tool.getId();
		title = tool.getName();
		description = tool.getDescription();
		if (tool.getToolType() == ResourceType.Global) {
			organizationId = tool.getOrganization().getId();
			unitId = tool.getUnit().getId();
			if (tool.getUserGroup() != null) {
				userGroupData = new BasicObjectInfo(tool.getUserGroup().getId(), tool.getUserGroup().getName());
			}
		}
		launchUrl = tool.getFullLaunchURL();
		consumerKey = tool.getToolSet().getConsumer().getKeyLtiOne();
		consumerSecret = tool.getToolSet().getConsumer().getSecretLtiOne();
		regUrl = tool.getToolSet().getFullRegistrationURL();
		initialized = true;
	}
	
}
