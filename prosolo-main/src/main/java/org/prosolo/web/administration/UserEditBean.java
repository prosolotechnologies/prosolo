package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bojan
 *
 * May 25, 2017
 */


@ManagedBean(name = "userEditBean")
@Component("userEditBean")
@Scope("view")
public class UserEditBean implements Serializable {

	private static final long serialVersionUID = 1787939548656065892L;

	protected static Logger logger = Logger.getLogger(UserEditBean.class);

	@Inject
	private UserManager userManager;
	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private RoleManager roleManager;
	@Inject
	private PasswordResetManager passwordResetManager;
	@Inject
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private PageAccessRightsResolver pageAccessRightsResolver;
	@Autowired
	private UserTextSearch textSearch;
	@Inject private OrganizationManager organizationManager;

	private String orgId;
	private long decodedOrgId;
	private String id;
	private long decodedId;
	private UserData accountData;
	private UserData userToDelete;
	private UserData user;
	private UserData newOwner = new UserData();
	private List<UserData> users;
	private String searchTerm;
	private List<Role> allRoles;
	private List<RoleCheckboxData> allRolesCheckBoxData;
	private List<UserData> usersToExclude = new ArrayList<>();

	private String organizationTitle;

	private static final String passwordFieldId = "editUserPassword:formMain:inputPassword";

	public void initPassword() {
		logger.debug("initializing");
		try {
			if (orgId != null) {
				decodedOrgId = idEncoder.decodeId(orgId);
				if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
					initDataForPasswordEdit();
					initOrgTitle();
				} else {
					PageUtil.accessDenied();
				}
			} else {
				initDataForPasswordEdit();
			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading page");
		}
	}


	private void initDataForPasswordEdit() {
		decodedId = idEncoder.decodeId(id);
		user = userManager.getUserData(decodedId);
		accountData = new UserData();
		usersToExclude.add(user);
	}

	private void initOrgTitle() {
		if (decodedOrgId > 0) {
			organizationTitle = organizationManager.getOrganizationTitle(decodedOrgId);
			if (organizationTitle == null) {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void initAdmin() {
		init(new String[] {SystemRoleNames.ADMIN, SystemRoleNames.SUPER_ADMIN});
	}

	public void initOrgUser() {
		decodedOrgId = idEncoder.decodeId(orgId);

		if(pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			initOrgTitle();
			if (organizationTitle != null) {
				init(new String[]{SystemRoleNames.USER, SystemRoleNames.INSTRUCTOR, SystemRoleNames.MANAGER, SystemRoleNames.ADMIN});
			}
		} else {
			PageUtil.accessDenied();
		}
	}

	public void init(String [] rolesArray) {
		try {
			decodedId = idEncoder.decodeId(id);
			// edit user
			if (decodedId > 0) {
				user = userManager.getUserWithRoles(decodedId, decodedOrgId);
				if (user != null) {
					accountData = new UserData();
				} else {
					user = new UserData();
					PageUtil.fireErrorMessage("Admin cannot be found");
				}
			}
			// new user
			else {
				user = new UserData();
			}
			allRoles = roleManager.getRolesByNames(rolesArray);
			usersToExclude.add(user);
			prepareRoles();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading page");
		}
	}

	private void prepareRoles() {
		try {
			allRolesCheckBoxData = new ArrayList<>();
			if (allRoles != null) {
				for (int i = 0; i < allRoles.size(); i++) {
					Role r = allRoles.get(i);
					RoleCheckboxData roleCheckboxData = new RoleCheckboxData(r.getTitle(), this.user.hasRole(r.getId()), r.getId());
					allRolesCheckBoxData.add(roleCheckboxData);
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}

	}

	public void saveUser() {
		if (this.user.getId() == 0) {
			createNewUser();
		} else {
			updateUser();
		}
	}

	private List<Long> getSelectedRoles(){
		return allRolesCheckBoxData.stream()
				.filter(RoleCheckboxData::isSelected)
				.map(RoleCheckboxData::getId)
				.collect(Collectors.toList());
	}

	private void createNewUser() {
		try {
			User adminUser = userManager.createNewUserAndSendEmail(decodedOrgId, this.user.getName(), this.user.getLastName(),
					this.user.getEmail(),true, this.user.getPassword(), this.user.getPosition(),
					null, null, getSelectedRoles(), false);

			logger.debug("New user (" + adminUser.getName() + " " + adminUser.getLastname() + ") for the user "
					+ loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessageAcrossPages("A new user has been created");
			if (decodedOrgId > 0) {
				PageUtil.redirect("/admin/organizations/" + orgId + "/users");
			} else {
				PageUtil.redirect("/admin/admins");
			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error trying to save user data");
		}
	}

	private void updateUser() {
		try {
			User updatedUser = userManager.updateUser(
					this.user.getId(),
					this.user.getName(),
					this.user.getLastName(),
					this.user.getEmail(),
					true,
					false,
					this.user.getPassword(),
					this.user.getPosition(),
					this.user.getNumberOfTokens(),
					getSelectedRoles(),
					allRoles.stream().map(Role::getId).collect(Collectors.toList()),
					loggedUser.getUserContext(decodedOrgId));

			logger.debug("Admin user (" + updatedUser.getId() + ") updated by the user " + loggedUser.getUserId());

			// refresh user roles
			this.user.setRoleIds(getSelectedRoles());

			PageUtil.fireSuccessfulInfoMessage("The user has been updated");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the user");
		}
	}

	public void setUserToDelete() {
		newOwner.setUserSet(false);
		searchTerm = "";
		users = null;
		this.userToDelete = user;
	}

	public void userReset() {
		searchTerm = "";
		users = null;
		newOwner.setUserSet(false);
	}

	public void setNewOwner(UserData userData) {
		newOwner.setId(userData.getId());
		newOwner.setAvatarUrl(userData.getAvatarUrl());
		newOwner.setFullName(userData.getFullName());
		newOwner.setPosition(userData.getPosition());
	}

	public void delete() {
		if (userToDelete != null) {
			try {
				userManager.deleteUser(this.userToDelete.getId(), newOwner.getId(), loggedUser.getUserContext(decodedOrgId));
				PageUtil.fireSuccessfulInfoMessage("The user has been deleted");
				userToDelete = null;
				String url = decodedOrgId > 0
						? "/admin/organizations/" + orgId + "/users"
						: "/admin/admins";
				PageUtil.redirect(url);
			} catch (Exception ex) {
				logger.error(ex);
				PageUtil.fireErrorMessage("Error deleting the user");
			}
		}
	}

	private void loadUsersForNewOwnerSearch() {
		this.users = null;
		if (searchTerm == null || searchTerm.isEmpty()) {
			users = null;
		} else {
			try {
				PaginatedResult<UserData> result = textSearch.searchUsers(decodedOrgId, searchTerm, 3, this.usersToExclude,null);
				users = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void resetAndSearch() {
		loadUsersForNewOwnerSearch();
	}

	public void savePassChangeForAnotherUser() {
		if (accountData.getNewPassword().length() < 6) {
			FacesContext context = FacesContext.getCurrentInstance();
			UIInput input = (UIInput) context.getViewRoot().findComponent(
					passwordFieldId);
			input.setValid(false);
			context.addMessage(passwordFieldId, new FacesMessage(
					"The password is too short. It has to contain at least 6 characters."));
			FacesContext.getCurrentInstance().validationFailed();
			return;
		}
		try {
			userManager.changePassword(user.getId(), accountData.getNewPassword());
			PageUtil.fireSuccessfulInfoMessage("The password has been updated");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the password");
		}
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<RoleCheckboxData> getAllRolesCheckBoxData() {
		return allRolesCheckBoxData;
	}

	public void setAllRolesCheckBoxData(List<RoleCheckboxData> allRolesCheckBoxData) {
		this.allRolesCheckBoxData = allRolesCheckBoxData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public UserData getAccountData() {
		return accountData;
	}

	public List<UserData> getUsers() {
		return users;
	}

	public void setUsers(List<UserData> users) {
		this.users = users;
	}

	public UserTextSearch getTextSearch() {
		return textSearch;
	}

	public void setTextSearch(UserTextSearch textSearch) {
		this.textSearch = textSearch;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public UserData getNewOwner() {
		return newOwner;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public long getDecodedOrgId() {
		return decodedOrgId;
	}

	public class RoleCheckboxData{

		private String label;
		private boolean selected;
		private long id;

		public RoleCheckboxData(String label, boolean selected, long id) {
			this.label = label;
			this.selected = selected;
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

	}
}

