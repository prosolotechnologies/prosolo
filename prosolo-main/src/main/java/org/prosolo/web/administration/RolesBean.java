package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CapabilityManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.CapabilityData;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="roles")
@Component("roles")
@Scope("view")
public class RolesBean implements Serializable {

	private static final long serialVersionUID = 8190788278967027394L;

	protected static Logger logger = Logger.getLogger(RolesBean.class);

	@Inject private RoleManager roleManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private CapabilityManager capabilityManager;
	@Inject private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private RoleData formData;
	private List<RoleData> roles;
	private boolean editMode;
	
	private RoleData roleToDelete;
	
	private List<Long> selectedCapabilities;
	private List<Long> initialCapabilities;
	private SelectItem[] allCapabilities;
	
	private List<CapabilityData> capabilities;
	


	@PostConstruct
	public void init() {
		resetFormData();
		loadRoles();
		loadCapabilities();
	}
	
	private void loadCapabilities() {
		try{
			capabilities = new ArrayList<>();
			Map<Capability, List<Long>> caps = capabilityManager.getAllCapabilitiesWithRoleIds();
			if(caps != null){
				for (Map.Entry<Capability, List<Long>> entry : caps.entrySet()){
					capabilities.add(new CapabilityData(entry.getKey(), entry.getValue()));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void capabilityChanged(CapabilityData cd, RoleData rd){
		List<Long> list = new ArrayList<>(cd.getRoleIds());
		if(cd.getRoleIds().contains(rd.getId())){
			list.remove(new Long(rd.getId()));	
		}else{
			list.add(new Long(rd.getId()));
		}
		try{
			capabilityManager.updateCapabilityRoles(cd.getId(), list);
			cd.setRoleIds(list);
			PageUtil.fireSuccessfulInfoMessage("growlMsg","Capability updated");
		}catch(Exception e){
			PageUtil.fireErrorMessage("Error while updating capability");
		}
	}
	
	public boolean isSelected(CapabilityData cd, RoleData rd){
		
		if(cd.getRoleIds().contains(rd.getId())){
			return true;
		}
		return false;
	}
	

	public void loadRoles() {
		roles = new ArrayList<RoleData>();

		Collection<Role> allRoles = roleManager.getAllRoles();

		if (allRoles != null && !allRoles.isEmpty()) {
			List<Role> list = new ArrayList<Role>(allRoles);
			
			for (Role role : list) {
				roles.add(new RoleData(role));
			}
			
			Map<Long, List<Long>> roleUsers = roleManager.getUsersWithRoles(list);
			for(RoleData rd:roles){
				rd.setUserIds(roleUsers.get(rd.getId()));
			}
		}
	}
	
	public void prepareAddRole(){
		this.editMode = false;
		this.resetFormData();
		prepareCapabilityList();
	}
	
	private void prepareCapabilityList() {
		try {
			List<Capability> caps = roleManager.getRoleCapabilities(formData.getId());
			selectedCapabilities = new ArrayList<Long>();
			initialCapabilities = new ArrayList<>();
			if(allCapabilities == null){
				List<Capability> capabilities = capabilityManager.getAllCapabilities();
				if (capabilities != null && !capabilities.isEmpty()) {
					allCapabilities = new SelectItem[capabilities.size()];
					for (int i = 0; i < capabilities.size(); i++) {
						Capability cap = capabilities.get(i);
						SelectItem selectItem = new SelectItem(cap.getId(), cap.getDescription());
						allCapabilities[i] = selectItem;
						if(editMode){
							if(caps.contains(cap)){
								selectedCapabilities.add(cap.getId());
								initialCapabilities.add(cap.getId());
							}
						}
					}
				}
			}else{
				for(SelectItem si:allCapabilities){
					long capId =  (long) si.getValue();
					boolean exists = checkIfCapabilityExist(capId, caps);
					if(exists){
						selectedCapabilities.add(capId);
						initialCapabilities.add(capId);
					}
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}
		
	}

	private boolean checkIfCapabilityExist(long capabilityId, List<Capability> capabilities) {
		for(Capability c:capabilities){
			if(c.getId() == capabilityId){
				return true;
			}
		}
		return false;
	}

	public void validateName(FacesContext context, UIComponent component, Object value){
		Collection<Long> roleUris = roleManager.getRoleIdsForName((String) value);
		
		boolean isValid = roleUris.size() > 0 ? false : true;

		if (!isValid && this.editMode) {
			List<Long> urisList = new ArrayList<Long>(roleUris);
			
			for (Long id : urisList) {
				if (id.equals(formData.getId()))
					isValid = true;
			}
		}

		if (!isValid) {
			FacesMessage message = new FacesMessage("The name: '" + value + "' is taken!");
			throw new ValidatorException(message);
		}
	}

	public void saveNewRole(){
		logger.debug("Creating new Role for the user "+ loggedUser.getUser() );

		loggedUser.refreshUser();
		
		Role role = roleManager.createNewRole(formData.getName(), formData.getDescription(), false, selectedCapabilities);

		logger.debug("New Role ("+role.getTitle()+") for the user "+ loggedUser.getUser() );
		PageUtil.fireSuccessfulInfoMessage("Role \""+role.getTitle()+"\" created!");
		
		resetFormData();
		loadRoles();
		loadCapabilities();
	}
	
	public void prepareEdit(RoleData roleData){
		this.editMode = true;
		this.setFormData(roleData);
		prepareCapabilityList();
	}

	public void updateRole(){
		logger.debug("Updating Role "+ formData.getId() +" for the user "+ loggedUser.getUser());
		
		try {
			Role role = roleManager.updateRole(formData.getId(), formData.getName(), formData.getDescription(), selectedCapabilities, initialCapabilities);
			PageUtil.fireSuccessfulInfoMessage("Role updated!");
			logger.debug("Role ("+role.getId()+") updated by the user "+ loggedUser.getUser());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		
		resetFormData();
		loadRoles();
		loadCapabilities();
	}
	
	public boolean isRoleUsed(RoleData roleData) {
		return roleManager.isRoleUsed(roleData.getId());
	}
	
	public void delete(){
		if (roleToDelete != null) {
			try {
				roleManager.deleteRole(roleToDelete.getId());
				resetFormData();
				loadRoles();
				loadCapabilities();
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			roleToDelete = null;
		}
	}
	
	public void resetFormData() {
		this.setFormData(new RoleData());
	}
	
	
	private UserData userToEdit;
	private SelectItem[] allRolesItems;
	private List<String> selectedRoles;
	
	public void prepareEditUserRoles(UserData userToEdit) {
		this.userToEdit = userToEdit;
		
		try {
			User user = roleManager.loadResource(User.class, this.userToEdit.getId());
			List<Role> allRoles = roleManager.getAllRoles();
			
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
	
	public void updateUserRoles() {
		if (this.selectedRoles != null) {
			try {
				User user = roleManager.updateUserRoles(userToEdit.getId(), selectedRoles);
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try{
							eventFactory.generateEvent(EventType.USER_ROLES_UPDATED, loggedUser.getUser(), user, null, 
								null, null, null, null);
						} catch(Exception e) {
							logger.error(e);
						}
								
					}
				});
				if (user.equals(loggedUser.getUser())) {
					loggedUser.refreshUser();
					//loggedUser.initializeRoles(user);
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
	
	/*
	 * GETTERS / SETTERS
	 */

	public RoleData getFormData() {
		return formData;
	}

	public void setFormData(RoleData formData) {
		this.formData = formData;
	}
	
	public List<RoleData> getRoles() {
		return this.roles;
	}

	public RoleData getRoleToDelete() {
		return roleToDelete;
	}

	public void setRoleToDelete(RoleData roleToDelete) {
		this.roleToDelete = roleToDelete;
	}

	public List<Long> getSelectedCapabilities() {
		return selectedCapabilities;
	}

	public void setSelectedCapabilities(List<Long> selectedCapabilities) {
		this.selectedCapabilities = selectedCapabilities;
	}

	public SelectItem[] getAllCapabilities() {
		return allCapabilities;
	}

	public void setAllCapabilities(SelectItem[] allCapabilities) {
		this.allCapabilities = allCapabilities;
	}

	public List<CapabilityData> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<CapabilityData> capabilities) {
		this.capabilities = capabilities;
	}

	public UserData getUserToEdit() {
		return userToEdit;
	}

	public SelectItem[] getAllRolesItems() {
		return allRolesItems;
	}

	public List<String> getSelectedRoles() {
		return selectedRoles;
	}

	public void setSelectedRoles(List<String> selectedRoles) {
		this.selectedRoles = selectedRoles;
	}
	
}
