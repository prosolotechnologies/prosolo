package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UnitRoleMembershipData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;


/**
 * @author Stefan Vuckovic
 * @date 2017-07-13
 * @since 0.7
 */
@ManagedBean(name = "unitUsersBean")
@Component("unitUsersBean")
@Scope("view")
public class UnitUsersBean implements Serializable {

	private static final long serialVersionUID = 3627974280316984122L;

	protected static Logger logger = Logger.getLogger(UnitUsersBean.class);

	@Inject private UnitManager unitManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private RoleManager roleManager;
	@Inject private UserTextSearch textSearch;

	private String orgId;
	private long decodedOrgId;
	private String id;
	private long decodedId;

	private long roleId;
	//users currently added to unit with role
	private List<UnitRoleMembershipData> existingUsers;
	//users not added to unit that are being searched
	private List<UserData> users;
	private String searchTerm = "";

	private PaginationData paginationData = new PaginationData();

	public void initTeachers() {
		init(RoleNames.MANAGER);
	}

	public void initStudents() {
		init(RoleNames.USER);
	}

	public void initInstructors() {
		init(RoleNames.INSTRUCTOR);
	}

	public void init(String role) {
		try {
			decodedOrgId = idEncoder.decodeId(orgId);
			decodedId = idEncoder.decodeId(id);

			if (decodedOrgId > 0 && decodedId > 0) {
				roleId = roleManager.getRoleIdsForName(role).get(0);

			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getDecodedOrgId() {
		return decodedOrgId;
	}

	public long getDecodedId() {
		return decodedId;
	}
}

