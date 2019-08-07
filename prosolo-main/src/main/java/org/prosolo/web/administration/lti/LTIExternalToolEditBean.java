/**
 * 
 */
package org.prosolo.web.administration.lti;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.users.UserScopeFilter;
import org.prosolo.search.util.users.UserSearchConfig;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.ToolSetManager;
import org.prosolo.services.lti.data.ExternalToolFormData;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.persistence.Basic;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * JSF bean responsible for a page for editing LTI external tool for organization unit
 *
 * @author stefanvuckovic
 * @date 2019-07-12
 * @since 1.3.3
 * 
 */
@ManagedBean(name = "ltiExternalToolEditBean")
@Component("ltiExternalToolEditBean")
@Scope("view")
public class LTIExternalToolEditBean implements Serializable {

	private static Logger logger = Logger.getLogger(LTIExternalToolEditBean.class);
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Inject private LtiToolManager toolManager;
	@Inject private ToolSetManager tsManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;
	@Inject private OrganizationManager organizationManager;
	@Inject private UserGroupTextSearch userGroupTextSearch;

	@Getter @Setter
	private String organizationId;
	@Getter @Setter
	private String unitId;
	@Getter @Setter
	private String id;

	@Getter
	private long decodedOrganizationId;
	@Getter
	private long decodedUnitId;
	private long decodedId;

	@Getter
	private ExternalToolFormData toolData;
	@Getter
	private String organizationTitle;
	@Getter
	private String unitTitle;

	@Getter @Setter
	private String searchTerm;

	@Getter
	private List<BasicObjectInfo> userGroups = new ArrayList<>();
	private static final int SEARCH_GROUPS_LIMIT = 5;

	public void init() {
		decodedOrganizationId = idEncoder.decodeId(organizationId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedId = idEncoder.decodeId(id);
		if (decodedOrganizationId > 0 && decodedUnitId > 0) {
			unitTitle = unitManager.getUnitTitle(decodedOrganizationId, decodedUnitId);
			if (unitTitle != null) {
				organizationTitle = organizationManager.getOrganizationTitle(decodedOrganizationId);

				if (decodedId > 0) {
					toolData = toolManager.getExternalToolData(decodedId);
					if (toolData.getOrganizationId() != decodedOrganizationId || toolData.getUnitId() != decodedUnitId) {
						PageUtil.notFound();
					} else {
						logger.debug("Editing external tool with id " + id);
					}
				} else {
					toolData = new ExternalToolFormData();
					toolData.setToolType(ResourceType.Global);
					toolData.setOrganizationId(decodedOrganizationId);
					toolData.setUnitId(decodedUnitId);
					logger.debug("Creating new external tool");
				}
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void searchGroups() {
		if (StringUtils.isBlank(searchTerm)) {
			userGroups.clear();
			return;
		}

		PaginatedResult<BasicObjectInfo> userGroupsRes = userGroupTextSearch.searchUserGroupsAndReturnBasicInfo(
				decodedOrganizationId, decodedUnitId, searchTerm, 0, SEARCH_GROUPS_LIMIT);
		userGroups = userGroupsRes.getFoundNodes();
	}

	public void selectUserGroup(BasicObjectInfo group) {
		toolData.setUserGroupData(group);
		resetSearch();
	}

	private void resetSearch() {
		userGroups.clear();
		searchTerm = "";
	}

	public void removeUserGroup() {
		toolData.setUserGroupData(null);
	}
	
	public void save() {
		if (toolData.getToolId() > 0) {
			try {
				toolManager.updateLtiTool(toolData);
				logger.info("LTI tool updated");
				PageUtil.fireSuccessfulInfoMessage("The external tool has been updated");
			} catch (Exception e) {
				PageUtil.fireErrorMessage("Error updating the external tool");
			}
		} else {
			try {
				toolData = tsManager.saveToolSet(toolData, loggedUserBean.getUserId());
				logger.info("LTI tool saved");
				PageUtil.fireSuccessfulInfoMessageAcrossPages("The external tool has been saved");
				PageUtil.redirect("/admin/organizations/" + organizationId + "/units/" + unitId + "/auto-enrollment/" + idEncoder.encodeId(toolData.getToolId()) + "/edit");
			} catch (Exception e) {
				PageUtil.fireErrorMessage("Error saving the external tool");
			}
		}
	}
	
}
