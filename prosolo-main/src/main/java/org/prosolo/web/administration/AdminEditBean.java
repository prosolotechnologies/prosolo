package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Bojan
 *
 * May 25, 2017
 */


@ManagedBean(name = "adminEditBean")
@Component("adminEditBean")
@Scope("view")
public class AdminEditBean implements Serializable {

	private static final long serialVersionUID = 1787939548656065892L;

	protected static Logger logger = Logger.getLogger(AdminEditBean.class);

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

	@Autowired
	private TextSearch textSearch;

	private String id;
	private long decodedId;
	private AccountData accountData;
	private UserData userToDelete;

	private UserData admin;
	private UserData newOwner = new UserData();
	private SelectItem[] allRoles;
	private List<UserData> admins;
	private String searchTerm;
	private RoleFilter filter;

	public void initPassword() {
		logger.debug("initializing");
		try {
			decodedId = idEncoder.decodeId(id);
			admin = new UserData();
			admin.setId(decodedId);
			accountData = new AccountData();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	public void init() {
		logger.debug("initializing");
		try {
			decodedId = idEncoder.decodeId(id);
			// edit user
			if (decodedId > 0) {
				User admin = userManager.getUserWithRoles(decodedId);
				if (admin != null) {
					this.admin = new UserData(admin);
					Set<Role> roles = admin.getRoles();
					if (roles != null) {
						for (Role r : roles) {
							this.admin.addRoleId(r.getId());
						}
					}
					accountData = new AccountData();
				} else {
					this.admin = new UserData();
					PageUtil.fireErrorMessage("Admin cannot be found");
				}
			}
			// new admin
			else {
				admin = new UserData();
			}
			prepareRoles();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	private void prepareRoles() {
		try {
			String[] rolesArray = new String[]{"Admin","Super Admin"};
			List<Role> adminRoles = roleManager.getAdminRoles(rolesArray);
			if (adminRoles != null) {
				allRoles = new SelectItem[adminRoles.size()];

				for (int i = 0; i < adminRoles.size(); i++) {
					Role r = adminRoles.get(i);
					SelectItem selectItem = new SelectItem(r.getId(), r.getTitle());
					allRoles[i] = selectItem;
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}

	}

	public void saveUser() {
		if (this.admin.getId() == 0) {
			createNewAdminUser();
		} else {
			updateAdminUser();
		}
	}

	private void createNewAdminUser() {
		try {
			User adminUser = userManager.createNewUser(this.admin.getName(), this.admin.getLastName(), this.admin.getEmail(),
					true, this.admin.getPassword(), this.admin.getPosition(), null, null, this.admin.getRoleIds());

			this.admin.setId(adminUser.getId());

			logger.debug("New Admin user (" + adminUser.getName() + " " + adminUser.getLastname() + ") for the user "
					+ loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessage("Admin user successfully saved");

			sendNewPassword();
		} catch (UserAlreadyRegisteredException e) {
			logger.debug(e);
			PageUtil.fireErrorMessage(e.getMessage());
		} catch (EventException e) {
			logger.debug(e);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to save user data");
		}
	}

	private void updateAdminUser() {
		try {
			boolean shouldChangePassword = this.admin.getPassword() != null && !this.admin.getPassword().isEmpty();
			User updatedUser = userManager.updateUser(this.admin.getId(), this.admin.getName(), this.admin.getLastName(),
					this.admin.getEmail(), true, shouldChangePassword, this.admin.getPassword(), this.admin.getPosition(),
					admin.getRoleIds(), loggedUser.getUserId());
			admin = new UserData(updatedUser);
			logger.debug("Admin user (" + updatedUser.getId() + ") updated by the user " + loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessage("Admin user successfully updated");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update admin data");
		} catch (EventException e) {
			logger.error(e);
		}
	}

	public UserData getUser() {
		return admin;
	}

	public void setUser(UserData user) {
		this.admin = user;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SelectItem[] getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(SelectItem[] allRoles) {
		this.allRoles = allRoles;
	}

	public void setUserToDelete() {
		newOwner.setUserSet(false);
		searchTerm = "";
		admins = null;
		this.userToDelete = admin;
	}

	public void userReset() {
		searchTerm = "";
		admins = null;
		newOwner.setUserSet(false);
	}

	public RoleFilter getFilter() {
		return filter;
	}

	public void setFilter(RoleFilter filter) {
		this.filter = filter;
	}

	public UserData getNewOwner() {
		return newOwner;
	}

	public void setNewOwner(UserData userData) {
		newOwner.setId(userData.getId());
		newOwner.setAvatarUrl(userData.getAvatarUrl());
		newOwner.setFullName(userData.getFullName());
	}

	public void savePassChangeForAnotherUser() {
		if (accountData.getNewPassword().length() < 6) {
			PageUtil.fireErrorMessage("Password is too short. It has to contain more than 6 characters.");
			return;
		}
		try {
			userManager.changePassword(admin.getId(), accountData.getNewPassword());
			PageUtil.fireSuccessfulInfoMessage("Password updated!");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the password");
		}
	}


	public AccountData getAccountData() {
		return accountData;
	}

	public List<UserData> getAdmins() {
		return admins;
	}

	public void setAdmins(List<UserData> admins) {
		this.admins = admins;
	}

	public TextSearch getTextSearch() {
		return textSearch;
	}

	public void setTextSearch(TextSearch textSearch) {
		this.textSearch = textSearch;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}


	public void sendNewPassword() {

		User userNewPass = userManager.getUser(admin.getEmail());
		if (userNewPass != null) {
			boolean resetLinkSent = passwordResetManager.initiatePasswordReset(userNewPass, userNewPass.getEmail(),
					CommonSettings.getInstance().config.appConfig.domain + "recovery");

			if (resetLinkSent) {
				PageUtil.fireSuccessfulInfoMessage("resetMessage",
						"Password instructions have been sent to given email ");
			} else {
				PageUtil.fireErrorMessage("resetMessage", "Error sending password instruction");
			}
		} else {
			PageUtil.fireErrorMessage("resetMessage", "User already registrated");
		}
	}

	public void delete() {
		if (userToDelete != null) {
			try {
				userManager.deleteUser(this.userToDelete.getId(), newOwner.getId());
				admins.remove(userToDelete);
				PageUtil.fireSuccessfulInfoMessage("User " + userToDelete.getFullName() + " is deleted.");
				userToDelete = null;
				ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
				extContext.redirect("/admin");
			} catch (Exception ex) {
				logger.error(ex);
				PageUtil.fireErrorMessage("Error while trying to delete user");
			}
		}
	}

	public void loadAdmins() {
		this.admins = null;
		if (searchTerm == null || searchTerm.isEmpty()) {
			admins = null;
		} else {
			try {
				TextSearchResponse1<UserData> result = textSearch.searchNewOwner(searchTerm, 3, admin.getId());
				admins = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void resetAndSearch() {
		loadAdmins();
	}

}

