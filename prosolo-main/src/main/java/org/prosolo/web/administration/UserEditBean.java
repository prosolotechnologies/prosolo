package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "userEditBean")
@Component("userEditBean")
@Scope("view")
public class UserEditBean implements Serializable {

	private static final long serialVersionUID = 7711054337337237619L;

	protected static Logger logger = Logger.getLogger(UserEditBean.class);

	@Inject
	private UserManager userManager;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private Activity1Manager activity1Manager;
	@Inject
	private Competence1Manager competence1Manager;
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
	@Autowired
	private AuthenticationService authenticationService;

	private UIInput passwordInput;

	private String id;
	private long decodedId;
	private AccountData accountData;
	private UserData userToDelete;

	private UserData user;
	private org.prosolo.services.nodes.data.UserData newOwner = new org.prosolo.services.nodes.data.UserData();
	private SelectItem[] allRoles;
	private List<org.prosolo.services.nodes.data.UserData> users;
	private String searchTerm;
	private RoleFilter filter;
	private PaginationData paginationData = new PaginationData();
	private List<Long> userToExcludeFromSearch;
	
	public void initPassword() {
		logger.debug("initializing");
		try {
			decodedId = idEncoder.decodeId(id);
			user = new UserData();
			user.setId(decodedId);
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
				User user = userManager.getUserWithRoles(decodedId);
				if (user != null) {
					this.user = new UserData(user);
					Set<Role> roles = user.getRoles();
					if (roles != null) {
						for (Role r : roles) {
							this.user.addRoleId(r.getId());
						}
					}
					accountData = new AccountData();
				} else {
					this.user = new UserData();
					PageUtil.fireErrorMessage("User cannot be found");
				}
			}
			// new user
			else {
				user = new UserData();
			}
			prepareRoles();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	private void prepareRoles() {
		try {
			List<Role> roles = roleManager.getAllRoles();
			if (roles != null) {
				allRoles = new SelectItem[roles.size()];

				for (int i = 0; i < roles.size(); i++) {
					Role r = roles.get(i);
					SelectItem selectItem = new SelectItem(r.getId(), r.getTitle());
					allRoles[i] = selectItem;
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}

	}

	public boolean isCreateUseCase() {
		return user.getId() == 0;
	}

	public void saveUser() {
		if (this.user.getId() == 0) {
			createNewUser();
		} else {
			updateUser();
		}
	}

	private void createNewUser() {
		try {
			User user = userManager.createNewUser(this.user.getName(), this.user.getLastName(), this.user.getEmail(),
					true, this.user.getPassword(), this.user.getPosition(), null, null, this.user.getRoleIds());

			this.user.setId(user.getId());

			logger.debug("New User (" + user.getName() + " " + user.getLastname() + ") for the user "
					+ loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessage("User successfully saved");

			sendNewPassword();

			// if (this.user.isSendEmail()) {
			// emailSenderManager.sendEmailAboutNewAccount(user,
			// this.user.getEmail());
			// }
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

	private void updateUser() {
		try {
			boolean shouldChangePassword = this.user.getPassword() != null && !this.user.getPassword().isEmpty();
			User user = userManager.updateUser(this.user.getId(), this.user.getName(), this.user.getLastName(),
					this.user.getEmail(), true, shouldChangePassword, this.user.getPassword(), this.user.getPosition(),
					this.user.getRoleIds(), loggedUser.getUserId());

			logger.debug("User (" + user.getId() + ") updated by the user " + loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessage("User successfully saved");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to save user data");
		} catch (EventException e) {
			logger.error(e);
		}
	}

	// public void changePassword() {
	// if (authenticationService.checkPassword(user.getPassword(),
	// user.getOldPassword())) {
	// if (user.getNewPassword().length() < 6) {
	// PageUtil.fireErrorMessage(passwordInput.getClientId(),
	// "Password is too short. It has to contain more that 6 characters.");
	// FacesContext.getCurrentInstance().validationFailed();
	// }
	//
	// try {
	// User user = userManager.changePassword(loggedUser.getUserId(),
	// accountData.getNewPassword());
	// loggedUser.getSessionData().setPassword(user.getPassword());
	//
	// PageUtil.fireSuccessfulInfoMessage(":settingsPasswordForm:settingsPasswordGrowl",
	// "Password updated!");
	// } catch (ResourceCouldNotBeLoadedException e) {
	// logger.error(e);
	// PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl",
	// "Error updating the password");
	// }
	// } else {
	// PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl",
	// "Old password is not correct.");
	// }
	// }

	/*
	 * GETTERS / SETTERS
	 */

	public UIInput getPasswordInput() {
		return passwordInput;
	}

	public void setPasswordInput(UIInput passwordInput) {
		this.passwordInput = passwordInput;
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

	public SelectItem[] getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(SelectItem[] allRoles) {
		this.allRoles = allRoles;
	}

	public UserData getUserToDelete() {
		return userToDelete;
	}

	public void setUserToDelete() {
		newOwner.setUserSet(false);
		searchTerm= "";
		users=null;
		this.userToDelete = user;
	}
	
	public RoleFilter getFilter() {
		return filter;
	}

	public void setFilter(RoleFilter filter) {
		this.filter = filter;
	}

	public org.prosolo.services.nodes.data.UserData getNewOwner() {
		return newOwner;
	}

	public void setNewOwner(org.prosolo.services.nodes.data.UserData userData) {
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
			userManager.changePassword(user.getId(), accountData.getNewPassword());
			PageUtil.fireSuccessfulInfoMessage("Password updated!");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the password");
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public AccountData getAccountData() {
		return accountData;
	}

	public List<org.prosolo.services.nodes.data.UserData> getUsers() {
		return users;
	}

	public void setUsers(List<org.prosolo.services.nodes.data.UserData> users) {
		this.users = users;
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

	@SuppressWarnings("deprecation")
	public void sendNewPassword() {

		User userNewPass = userManager.getUser(user.getEmail());
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
				userManager.deleteUser(this.userToDelete.getId(), newOwner.getId());;
				users.remove(userToDelete);
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
	
	@SuppressWarnings("deprecation")
	public void loadUsers() {
		this.users = new ArrayList<org.prosolo.services.nodes.data.UserData>();
		if (searchTerm == null && searchTerm.isEmpty()) {
			users = null;
		} else {
			try {
				TextSearchResponse1<org.prosolo.services.nodes.data.UserData> result= textSearch.searchNewOwner(searchTerm, 3 ,user.getId());
				users = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public void resetAndSearch() {
		loadUsers();
	}

}
