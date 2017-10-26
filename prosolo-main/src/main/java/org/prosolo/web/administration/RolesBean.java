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
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.nodes.CapabilityManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.CapabilityData;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.page.PageUtil;
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
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private RoleData formData;
	private List<RoleData> roles;
	
	private RoleData roleToDelete;
	
	private List<CapabilityData> capabilities;
	private ResourceAccessData access;

	@PostConstruct
	public void init() {
		loadRoles();
		loadCapabilities();
	}
	
	public void loadRoles() {
		roles = new ArrayList<RoleData>();

		try {
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
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	private void loadCapabilities() {
		try {
			capabilities = new ArrayList<>();
			Map<Capability, List<Long>> caps = capabilityManager.getAllCapabilitiesWithRoleIds();
			if(caps != null){
				for (Map.Entry<Capability, List<Long>> entry : caps.entrySet()){
					capabilities.add(new CapabilityData(entry.getKey(), entry.getValue()));
				}
			}
		} catch(Exception e) {
			logger.error(e);
		}
		
	}
	
	public boolean isCreateRoleUseCase() {
		return formData != null && formData.getId() == 0;
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
			PageUtil.fireSuccessfulInfoMessage("The capability has been updated");
		}catch(Exception e){
			PageUtil.fireErrorMessage("Error updating the capability");
		}
	}
	
	public boolean isSelected(CapabilityData cd, RoleData rd) {
		if(cd.getRoleIds().contains(rd.getId())){
			return true;
		}
		return false;
	}
	
	public void prepareAddRole(){
		formData = new RoleData();
	}
	
	public void prepareEditRole(RoleData roleData){
		this.setFormData(roleData);
	}

	
//	private void prepareCapabilityList() {
//		try {
//			List<Capability> caps = roleManager.getRoleCapabilities(formData.getId());
//			selectedCapabilities = new ArrayList<Long>();
//			initialCapabilities = new ArrayList<>();
//			if(allCapabilities == null){
//				List<Capability> capabilities = capabilityManager.getAllCapabilities();
//				if (capabilities != null && !capabilities.isEmpty()) {
//					allCapabilities = new SelectItem[capabilities.size()];
//					for (int i = 0; i < capabilities.size(); i++) {
//						Capability cap = capabilities.get(i);
//						SelectItem selectItem = new SelectItem(cap.getId(), cap.getDescription());
//						allCapabilities[i] = selectItem;
//						if(editMode){
//							if(caps.contains(cap)){
//								selectedCapabilities.add(cap.getId());
//								initialCapabilities.add(cap.getId());
//							}
//						}
//					}
//				}
//			}else{
//				for(SelectItem si:allCapabilities){
//					long capId =  (long) si.getValue();
//					boolean exists = checkIfCapabilityExist(capId, caps);
//					if(exists){
//						selectedCapabilities.add(capId);
//						initialCapabilities.add(capId);
//					}
//				}
//			}
//		} catch (DbConnectionException e) {
//			logger.error(e);
//		}
//		
//	}

//	private boolean checkIfCapabilityExist(long capabilityId, List<Capability> capabilities) {
//		for(Capability c:capabilities){
//			if(c.getId() == capabilityId){
//				return true;
//			}
//		}
//		return false;
//	}

	public void validateName(FacesContext context, UIComponent component, Object value){
		Collection<Long> roleUris = roleManager.getRoleIdsForName((String) value);
		
		boolean isValid = roleUris.size() > 0 ? false : true;

		if (!isValid && !isCreateRoleUseCase()) {
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

	public void saveRole() {
		if(formData.getId() > 0) {
			updateRole();
		} else {
			saveNewRole();
		}
	}
	
	public void saveNewRole() {
		try {
			logger.debug("Creating new Role for the user "+ loggedUser.getFullName() );
			
			Role role = roleManager.createNewRole(formData.getName(), formData.getDescription(), false);
	
			logger.debug("New Role ("+role.getTitle()+") for the user "+ loggedUser.getFullName() );
			PageUtil.fireSuccessfulInfoMessage("Role \""+role.getTitle()+"\" has been created");
			
			roles.add(formData);
			formData = null;
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while saving role");
		}
	}
	
	public void updateRole(){
		logger.debug("Updating Role "+ formData.getId() +" for the user "+ loggedUser.getFullName());
		
		try {
			Role role = roleManager.updateRole(formData.getId(), formData.getName(), formData.getDescription());
			PageUtil.fireSuccessfulInfoMessage("The role has been updated");
			logger.debug("Role ("+role.getId()+") updated by the user "+ loggedUser.getFullName());
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the role");
		}
		
		formData = null;
		loadRoles();
	}
	
	public boolean isRoleUsed(RoleData roleData) {
		return !(roleData.getUserIds() == null || roleData.getUserIds().isEmpty());
	}
	
	public void delete(){
		if (roleToDelete != null) {
			try {
				roleManager.deleteRole(roleToDelete.getId());
				roles.remove(roleToDelete);
				PageUtil.fireSuccessfulInfoMessage("The role has been deleted");
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error deleting the role");
			}
			roleToDelete = null;
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

	public List<CapabilityData> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<CapabilityData> capabilities) {
		this.capabilities = capabilities;
	}
	
}
