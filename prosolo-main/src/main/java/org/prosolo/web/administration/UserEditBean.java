package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.AccountData;
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

	@Autowired
	private UserTextSearch textSearch;
	@Inject private OrganizationManager organizationManager;

	private String orgId;
	private long decodedOrgId;
	private String id;
	private long decodedId;
	private AccountData accountData;
	private UserData userToDelete;
	private UserData user;
	private UserData newOwner = new UserData();
	private List<RoleCheckboxData> allRoles;
	private List<UserData> users;
	private String searchTerm;
	private List<Role> userRoles;
	private List<UserData> usersToExclude = new ArrayList<>();

	private String organizationTitle;

	private static final String passwordFieldId = "editUserPassword:formMain:inputPassword";

	public void initPassword() {
		logger.debug("initializing");
		try {
			decodedId = idEncoder.decodeId(id);
			user = userManager.getUserData(decodedId);
			accountData = new AccountData();
			usersToExclude.add(user);

			if (orgId != null) {
				decodedOrgId = idEncoder.decodeId(orgId);
				initOrgTitle();
			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
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
		init(new String[] {"Admin", "Super Admin"});
	}

	public void initOrgUser() {
		decodedOrgId = idEncoder.decodeId(orgId);
		initOrgTitle();
		if (organizationTitle != null) {
			init(new String[]{"User", "Instructor", "Manager", "Admin"});
		}
	}

	public void init(String [] rolesArray) {
		try {
			decodedId = idEncoder.decodeId(id);
			// edit user
			if (decodedId > 0) {
				user = userManager.getUserWithRoles(decodedId, decodedOrgId);
				if (user != null) {
					accountData = new AccountData();
				} else {
					user = new UserData();
					PageUtil.fireErrorMessage("Admin cannot be found");
				}
			}
			// new user
			else {
				user = new UserData();
			}
			userRoles = roleManager.getRolesByNames(rolesArray);
			usersToExclude.add(user);
			prepareRoles();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	private void prepareRoles() {
		try {
			allRoles = new ArrayList<>();
			if (userRoles != null) {
				for (int i = 0; i < userRoles.size(); i++) {
					Role r = userRoles.get(i);
					RoleCheckboxData roleCheckboxData = new RoleCheckboxData(r.getTitle(), this.user.hasRole(r.getId()), r.getId());
					allRoles.add(roleCheckboxData);
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
		return allRoles.stream()
				.filter(r -> r.isSelected())
				.map(RoleCheckboxData::getId)
				.collect(Collectors.toList());
	}

	private void createNewUser() {
		try {
			User adminUser = userManager.createNewUser(decodedOrgId, this.user.getName(), this.user.getLastName(),
					this.user.getEmail(),true, this.user.getPassword(), this.user.getPosition(),
					null, null, getSelectedRoles());

			this.user.setId(adminUser.getId());

			logger.debug("New user (" + adminUser.getName() + " " + adminUser.getLastname() + ") for the user "
					+ loggedUser.getUserId());

			sendNewPassword();

			PageUtil.fireSuccessfulInfoMessageAcrossPages("A new user has been created");
			if (decodedOrgId > 0) {
				PageUtil.redirect("/admin/organizations/" + orgId + "/users");
			} else {
				PageUtil.redirect("/admin/admins");
			}
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
			User updatedUser = userManager.updateUser(
					this.user.getId(),
					this.user.getName(),
					this.user.getLastName(),
					this.user.getEmail(),
					true,
					false,
					this.user.getPassword(),
					this.user.getPosition(),
					getSelectedRoles(),
					userRoles.stream().map(role -> role.getId()).collect(Collectors.toList()),
					loggedUser.getUserContext(decodedOrgId));

			logger.debug("Admin user (" + updatedUser.getId() + ") updated by the user " + loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessage("The user has been updated");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the user");
		} catch (EventException e) {
			logger.error(e);
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

	public List<RoleCheckboxData> getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(List<RoleCheckboxData> allRoles) {
		this.allRoles = allRoles;
	}

	public AccountData getAccountData() {
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

	public void setNewOwner(UserData userData) {
		newOwner.setId(userData.getId());
		newOwner.setAvatarUrl(userData.getAvatarUrl());
		newOwner.setFullName(userData.getFullName());
		newOwner.setPosition(userData.getPosition());
	}

	private void sendNewPassword() {

		final User user = userManager.getUser(this.user.getEmail());

		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) userManager.getPersistence().openSession();
				try {
					boolean resetLinkSent = passwordResetManager.initiatePasswordReset(user, user.getEmail(),
					CommonSettings.getInstance().config.appConfig.domain + "recovery", session);
					session.flush();
					if (resetLinkSent) {
						logger.info("Password instructions have been sent");
					} else {
						logger.error("Error sending password instruction");
					}
				}catch (Exception e){
					logger.error("Exception in handling mail sending", e);
				}finally {
					HibernateUtil.close(session);
				}
			}
		});
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

