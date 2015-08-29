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
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.OrganizationData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="organizations")
@Component("organizations")
@Scope("view")
public class OrganizationsBean implements Serializable {

	private static final long serialVersionUID = -26608923146401518L;

	protected static Logger logger = Logger.getLogger(OrganizationsBean.class);

	@Autowired private OrganizationManager organizationManager;
	@Autowired private LoggedUserBean loggedUser;

	private  OrganizationData formData;
	private  boolean editMode;
	private List<OrganizationData> organizations;



	@PostConstruct
	public void init() {
		resetFormData();
		loadOrganizations();
	}


	public void delete(){
		String id = PageUtil.getPostParameter("id");
		Organization organization;
		try {
			organization = organizationManager.loadResource(Organization.class, Integer.valueOf(id).intValue());
			organizationManager.delete(organization);
			resetFormData();
			loadOrganizations();
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void validateName(FacesContext context, UIComponent component, Object value){
		loggedUser.refreshUser();
		Collection<String> orgUris = organizationManager.getOrganizationUrisForName((String)value);
		boolean isValid = orgUris.size() > 0 ? false : true;

		if (!isValid && this.editMode) {
			List<String> orgUrisList = new ArrayList<String>(orgUris);
			for (String uri : orgUrisList) {
				if (uri.equals(formData.getUri())) isValid=true;
				}
		}

		if (!isValid) {
			FacesMessage message = new FacesMessage("The name: '"+ value + "' is taken!");
			throw new ValidatorException(message);
		}
	}

	public void prepareEdit(){
		String id = PageUtil.getPostParameter("id");
		this.editMode=true;
		Organization organization;
		try {
			organization = organizationManager.loadResource(Organization.class, Integer.valueOf(id).intValue());
			this.setFormData(new OrganizationData(organization));
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void prepareAdd(){
		this.editMode=false;
		this.resetFormData();
	}


	public void loadOrganizations() {
		organizations = new ArrayList<OrganizationData>();

		Collection<Organization> allOrganizations = organizationManager.getAllOrganizations();


		if (allOrganizations != null && !allOrganizations.isEmpty()) {
			List<Organization> orgList = new ArrayList<Organization>(allOrganizations);
			for (Organization organization : orgList) {
				organizations.add(new OrganizationData(organization));
			}
		}
	}


	public List<OrganizationData> getOrganizations() {
		return organizations;
	}


	public void saveNewOrganization(){
		loggedUser.refreshUser();

		logger.debug("Creating new Organization for the user "+ loggedUser.getUser());
		Organization organization = organizationManager.createNewOrganization(loggedUser.getUser(), formData.getName(), formData.getAbbreviatedName(), formData.getDescription());
		logger.debug("New Organization ("+organization.getTitle()+") for the user "+ loggedUser.getUser() );
		PageUtil.fireSuccessfulInfoMessage("Organization \""+organization.getName()+"\" created!");
		resetFormData();
		loadOrganizations();
	}

	public void updateOrganization(){

		logger.debug("Updating Organization "+ formData.getUri() +" for the user "+ loggedUser.getUser() );
		Organization organization;
		try {
			organization = organizationManager.loadResource(Organization.class, formData.getId());
			formData.updateOrganization(organization);
			this.organizationManager.saveEntity(organization);
			PageUtil.fireSuccessfulInfoMessage("Organization updated!");
			logger.debug("Organization ("+organization.getId()+") updated by the user "+ loggedUser.getUser());
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resetFormData();
		loadOrganizations();
	}



	public OrganizationData getFormData() {
		return formData;
	}



	public void setFormData(OrganizationData formData) {
		this.formData = formData;
	}


	public void resetFormData() {
		this.setFormData(new OrganizationData());
	}


}
