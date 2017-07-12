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

import javax.faces.bean.ManagedBean;
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
	private UserData admin;
	private UserData newOwner = new UserData();
	private List<RoleCheckboxData> allRoles;
	private List<UserData> admins;
	private String searchTerm;
	private List<Role> adminRoles;
	private List<UserData> adminsToExclude = new ArrayList<>();

	private String organizationTitle;

	public void initPassword() {
		logger.debug("initializing");
		try {
			decodedId = idEncoder.decodeId(id);
			admin = userManager.getUserData(decodedId);
			accountData = new AccountData();
			adminsToExclude.add(admin);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	public void initAdmin() {
		init(new String[] {"Admin", "Super Admin"});
	}

	public void initOrgUser() {
		decodedOrgId = idEncoder.decodeId(orgId);
		init(new String[] {"User", "Instructor", "Manager"});
		//load organization title if 'create' use case
		if (admin.getId() <= 0) {
			if (decodedOrgId > 0) {
				organizationTitle = organizationManager.getOrganizationTitle(decodedOrgId);
				if (organizationTitle == null) {
					PageUtil.notFound();
				}
			} else {
				PageUtil.notFound();
			}
		}
	}

	public void init(String [] rolesArray) {
		try {
			decodedId = idEncoder.decodeId(id);
			// edit user
			if (decodedId > 0) {
				admin = userManager.getUserWithRoles(decodedId, decodedOrgId);
				if (admin != null) {
					accountData = new AccountData();
				} else {
					admin = new UserData();
					PageUtil.fireErrorMessage("Admin cannot be found");
				}
			}
			// new admin
			else {
				admin = new UserData();
			}
			adminRoles = roleManager.getRolesByNames(rolesArray);
			adminsToExclude.add(admin);
			prepareRoles();
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading page");
		}
	}

	private void prepareRoles() {
		try {
			allRoles = new ArrayList<>();
			if (adminRoles != null) {
				for (int i = 0; i < adminRoles.size(); i++) {
					Role r = adminRoles.get(i);
					RoleCheckboxData roleCheckboxData = new RoleCheckboxData(r.getTitle(), this.admin.hasRole(r.getId()), r.getId());
					allRoles.add(roleCheckboxData);
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

	private List<Long> getSelectedRoles(){
		return allRoles.stream()
				.filter(r -> r.isSelected())
				.map(RoleCheckboxData::getId)
				.collect(Collectors.toList());
	}

	private void createNewAdminUser() {
		try {
			User adminUser = userManager.createNewUser(decodedOrgId, this.admin.getName(), this.admin.getLastName(),
					this.admin.getEmail(),true, this.admin.getPassword(), this.admin.getPosition(),
					null, null, getSelectedRoles());

			this.admin.setId(adminUser.getId());

			logger.debug("New user (" + adminUser.getName() + " " + adminUser.getLastname() + ") for the user "
					+ loggedUser.getUserId());

			sendNewPassword();

			PageUtil.fireSuccessfulInfoMessageAcrossPages("New user is created");
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

	private void updateAdminUser() {
		try {
			User updatedUser = userManager.updateUser(
					this.admin.getId(),
					this.admin.getName(),
					this.admin.getLastName(),
					this.admin.getEmail(),
					true,
					false,
					this.admin.getPassword(),
					this.admin.getPosition(),
					getSelectedRoles(),
					adminRoles.stream().map(role -> role.getId()).collect(Collectors.toList()),
					loggedUser.getUserId());

			logger.debug("Admin user (" + updatedUser.getId() + ") updated by the user " + loggedUser.getUserId());

			PageUtil.fireSuccessfulInfoMessage("User is updated");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to update admin data");
		} catch (EventException e) {
			logger.error(e);
		}
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

	public UserData getAdmin() {
		return admin;
	}

	public void setAdmin(UserData user) {
		this.admin = user;
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

	public List<UserData> getAdmins() {
		return admins;
	}

	public void setAdmins(List<UserData> admins) {
		this.admins = admins;
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

		final User user = userManager.getUser(admin.getEmail());

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
				userManager.deleteUser(this.userToDelete.getId(), newOwner.getId());
				PageUtil.fireSuccessfulInfoMessage("User " + userToDelete.getFullName() + " is deleted.");
				userToDelete = null;
				PageUtil.redirect("/admin/admins");
			} catch (Exception ex) {
				logger.error(ex);
				PageUtil.fireErrorMessage("Error while trying to delete user");
			}
		}
	}

	public void loadAdminsForNewOwnerSearch() {
		this.admins = null;
		if (searchTerm == null || searchTerm.isEmpty()) {
			admins = null;
		} else {
			try {
				PaginatedResult<UserData> result = textSearch.searchUsers(searchTerm, 3, this.adminsToExclude,null);
				admins = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void resetAndSearch() {
		loadAdminsForNewOwnerSearch();
	}

	public void savePassChangeForAnotherAdmin() {
		if (accountData.getNewPassword().length() < 6) {
			PageUtil.fireErrorMessage("The password is too short. It has to contain more than 6 characters.");
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

	public UserData getUser() {
		return admin;
	}

	public void setUser(UserData user) {
		this.admin = user;
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

