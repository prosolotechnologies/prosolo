package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="roles")
@Component("roles")
@Scope("view")
public class RolesBean implements Serializable {

	private static final long serialVersionUID = 8190788278967027394L;

	protected static Logger logger = Logger.getLogger(RolesBean.class);

	@Autowired private RoleManager roleManager;
	@Autowired private LoggedUserBean loggedUser;

	private RoleData formData;
	private List<RoleData> roles;
	private boolean editMode;
	
	private RoleData roleToDelete;

	@PostConstruct
	public void init() {
		resetFormData();
		loadRoles();
	}
	
	public void loadRoles() {
		roles = new ArrayList<RoleData>();

		Collection<Role> allRoles = roleManager.getAllRoles();

		if (allRoles != null && !allRoles.isEmpty()) {
			List<Role> list = new ArrayList<Role>(allRoles);
			
			for (Role role : list) {
				roles.add(new RoleData(role));
			}
		}
	}
	
	public void prepareAddRole(){
		this.editMode = false;
		this.resetFormData();
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
		
		Role role = roleManager.createNewRole(formData.getName(), formData.getDescription(), false);

		logger.debug("New Role ("+role.getTitle()+") for the user "+ loggedUser.getUser() );
		PageUtil.fireSuccessfulInfoMessage("Role \""+role.getTitle()+"\" created!");
		
		resetFormData();
		loadRoles();
	}
	
	public void prepareEdit(RoleData roleData){
		this.editMode = true;
		this.setFormData(roleData);
	}

	public void updateRole(){
		logger.debug("Updating Role "+ formData.getId() +" for the user "+ loggedUser.getUser());
		
		try {
			Role role = roleManager.updateRole(formData.getId(), formData.getName(), formData.getDescription());
			
			PageUtil.fireSuccessfulInfoMessage("Role updated!");
			logger.debug("Role ("+role.getId()+") updated by the user "+ loggedUser.getUser());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		
		resetFormData();
		loadRoles();
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
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			roleToDelete = null;
		}
	}
	
	public void resetFormData() {
		this.setFormData(new RoleData());
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
	
}
