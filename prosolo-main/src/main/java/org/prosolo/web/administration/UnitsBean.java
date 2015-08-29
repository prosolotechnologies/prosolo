package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit_User;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.administration.data.UnitData;
import org.prosolo.web.administration.data.UnitUserData;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "units")
@Component("units")
@Scope("view")
public class UnitsBean implements Serializable {

	private static final long serialVersionUID = 704530084854954172L;

	private static Logger logger = Logger.getLogger(UnitsBean.class);

	@Autowired private OrganizationManager organizationManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private RoleManager roleManager;
	@Autowired private UserManager userManager;

	@SuppressWarnings("unused")
	private boolean editMode;

	private UnitData formData;
	private List<UnitData> units;
	private Organization defaultOrganization;
	private List<String> selectedRoles;
	private SelectItem[] allRolesItems;
	private UnitUserData editedUser;
	private long parentUnitId;	
	private List<UserData> availableUsers;
	private UserData userToEdit;
	private Collection<Role> allRoles;

	@PostConstruct
	public void init() {
		this.setDefaultOrganization(organizationManager.lookupDefaultOrganization());
		this.reset();
	}

	public void restoreParentUnit(){
		try {
			OrganizationalUnit unit = organizationManager.loadResource(OrganizationalUnit.class, this.parentUnitId);
			this.setFormData(new UnitData(unit));
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public void addUnitUser(){
		String userId = PageUtil.getPostParameter("id");

		OrganizationalUnit unit;
		try {
			unit = organizationManager.loadResource(OrganizationalUnit.class, formData.getId());
			User user = organizationManager.loadResource(User.class, Integer.valueOf(userId).intValue());
			Unit_User unitUser = new Unit_User(unit,user,"enter user position here", new Date());
			organizationManager.saveEntity(unitUser);
			unit.addUnitUser(unitUser);
			organizationManager.saveEntity(unit);
			prepareManageUsers(new UnitData(unit));
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}	

	public void prepareEditUnit(UnitData unit) {
		this.parentUnitId = this.formData.getId();
		this.editMode = true;
		this.setFormData(unit);
	}

	public void prepareEditRoles(UserData userToEdit) {
		this.userToEdit = userToEdit;
		
		try {
			User user = userManager.loadResource(User.class, this.userToEdit.getId());
			this.allRoles = roleManager.getAllRoles();
			
			if (allRoles != null && !allRoles.isEmpty()) {
				List<Role> list = new ArrayList<Role>(allRoles);
				this.allRolesItems = new SelectItem[list.size()];
				this.selectedRoles = new ArrayList<String>();
				
				for (int i = 0; i < allRoles.size(); i++) {
					Role role = list.get(i);
					SelectItem selectItem = new SelectItem(role.getId(), role.getTitle());
					this.allRolesItems[i] = selectItem;
					
					if (user.getRoles().contains(role)) {
						this.selectedRoles.add(String.valueOf(role.getId()));
					}
				}
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void prepareEditUserRoles(UserData userData) {
		this.editMode = true;

		List<UnitUserData> users = this.formData.getUnitUsers();
		Collection<Role> allRoles = roleManager.getAllRoles();

		if (allRoles != null && !allRoles.isEmpty()) {
			List<Role> list = new ArrayList<Role>(allRoles);
			this.allRolesItems = new SelectItem[list.size()];
			int i = 0;
			for (Role role : list) {
				RoleData newRole = new RoleData(role);
				this.allRolesItems[i++] = new SelectItem(newRole.getId(),
						newRole.getName());
			}
		}

		for (UnitUserData user : users) {
			if (user.getId() == userData.getId()) {
				try {
					this.editedUser = new UnitUserData(organizationManager.loadResource(Unit_User.class, user.getUnitUserId()));
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				break;
			}
		}

		this.selectedRoles = new ArrayList<String>();

		for (RoleData role : this.editedUser.getRoles()) {
			if (role.isActive())
				this.selectedRoles.add(String.valueOf(role.getId()));
		}
	}

	public void prepareManageUsers(UnitData unit) {
		this.editMode = true;
		this.setFormData(unit);
		loadAvailableUsers();
	}

	public void loadAvailableUsers() {
		this.availableUsers = new ArrayList<UserData>();

		Collection<User> allUsers = userManager.getAllUsers();

		if (allUsers != null && !allUsers.isEmpty()) {
			List<User> list = new ArrayList<User>(allUsers);
			for (User user : list) {
				this.availableUsers.add(new UserData(user));
			}

			for (UnitUserData user : formData.getUnitUsers()) {
				removeUser(user);
			}
		}
	}

	private void removeUser(UnitUserData unitUser) {
		Iterator<UserData> iterator = availableUsers.iterator();
		
		while (iterator.hasNext()) {
			UserData userData = (UserData) iterator.next();
			
			if (userData.getId() == unitUser.getId()) {
				iterator.remove();
			}
		}
	}
	
	public void prepareManageSubUnits(UnitData unit) {
		this.editMode = true;
		this.setFormData(unit);
	}

	public void prepareAddSubUnit(UnitData unit) {
		this.editMode = false;
		this.resetFormData();
		this.getFormData().setParentName(unit.getName());
		this.getFormData().setParentUri(unit.getParentUri());
		this.getFormData().setParentId(unit.getParentId());
	}

	public void loadUnits() {
		this.units = new ArrayList<UnitData>();

		Collection<OrganizationalUnit> allUnits = organizationManager.getAllUnitsOfOrganization(this.defaultOrganization);

		if (allUnits != null && !allUnits.isEmpty()) {
			List<OrganizationalUnit> unitList = new ArrayList<OrganizationalUnit>(allUnits);
			
			for (OrganizationalUnit unit : unitList) {
				if (!unit.isSystem())
					this.units.add(new UnitData(unit));
			}
		}
	}

	public void saveNewSubUnit() {
		loggedUser.refreshUser();
		OrganizationalUnit parentUnit;
		try {
			parentUnit = organizationManager.loadResource(OrganizationalUnit.class, formData.getParentId());
			OrganizationalUnit unit = organizationManager
					.createNewOrganizationalUnit(this.defaultOrganization,
							parentUnit, formData.getName(),
							formData.getDescription(), false);
			this.organizationManager.saveEntity(unit);
			parentUnit.addSubUnit(unit);
			this.organizationManager.saveEntity(parentUnit);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		this.reset();
	}

	public void updateSubUnit() {
		OrganizationalUnit unit;
		try {
			unit = organizationManager.loadResource(OrganizationalUnit.class, formData.getId());
			formData.updateUnit(unit);
			this.organizationManager.saveEntity(unit);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		this.restoreParentUnit();
		loadUnits();
	}

	public void updateUserRoles() {
		if (this.selectedRoles != null) {
			try {
				User user = roleManager.updateUserRoles(userToEdit.getId(), selectedRoles);
				
				if (user.equals(loggedUser.getUser())) {
					loggedUser.refreshUser();
					loggedUser.initializeRoles(user);
				}

				PageUtil.fireSuccessfulInfoMessage(
						"unitUserRolesFormGrowl", 
						ResourceBundleUtil.getMessage(
								"admin.units.update", 
								loggedUser.getLocale(), 
								userToEdit.getName()+" "+user.getLastname()));
			} catch (ResourceCouldNotBeLoadedException e1) {
				logger.error(e1);
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}

	public void reset() {
		resetFormData();
		loadUnits();
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public List<UserData> getAvailableUsers() {
		return availableUsers;
	}

	public void setAvailableUsers(List<UserData> availableUsers) {
		this.availableUsers = availableUsers;
	}

	public UnitUserData getEditedUser() {
		return editedUser;
	}

	public void setEditedUser(UnitUserData editedUser) {
		this.editedUser = editedUser;
	}

	public List<String> getSelectedRoles() {
		return selectedRoles;
	}

	public void setSelectedRoles(List<String> selectedRoles) {
		 
		this.selectedRoles = selectedRoles;
	}

	public SelectItem[] getAllRoles() {
		return allRolesItems;
	}

	public void setAllRoles(SelectItem[] allRoles) {
		this.allRolesItems = allRoles;
	}

	public List<UnitData> getUnits() {
		return units;
	}

	public void setUnits(List<UnitData> units) {
		this.units = units;
	}

	public Organization getDefaultOrganization() {
		return defaultOrganization;
	}

	public void setDefaultOrganization(Organization defaultOrganization) {
		this.defaultOrganization = defaultOrganization;
	}

	public UnitData getFormData() {
		return formData;
	}

	public void setFormData(UnitData formData) {
		this.formData = formData;
	}

	public void resetFormData() {
		this.setFormData(new UnitData());
	}
	
	public UserData getUserToEdit() {
		return userToEdit;
	}

	public void delete(OrganizationalUnit unit) {
		/*		long id = PageUtil.getPostParameter("id");
		OrganizationalUnit unit = organizationManager.loadResourceByUri(
				OrganizationalUnit.class, uri);
		if (unit != null) {
			organizationManager.delete(unit);
			this.reset();
		}
		 */	}

	public void validateName(FacesContext context, UIComponent component,
			Object value) {
		// TODO
		/*
		 * loggedUser.refreshUser(); Collection<String> uris =
		 * organizationManager
		 * .getOrganizationalUnitUrisForName(this.defaultOrganization,
		 * (String)value); boolean isValid = uris.size() > 0 ? false : true;
		 * 
		 * if (!isValid && this.editMode) { List<String> list = new
		 * ArrayList<String>(uris); for (String uri : list) { if
		 * (uri.equals(formData.getUri())) isValid=true; } }
		 * 
		 * if (!isValid) { FacesMessage message = new
		 * FacesMessage("The name: '"+ value + "' is taken!"); throw new
		 * ValidatorException(message); }
		 */}


}
