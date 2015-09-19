package org.prosolo.web.administration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.primefaces.event.FileUploadEvent;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.email.EmailSenderManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "adminUsers")
@Component("adminUsers")
@Scope("view")
public class UsersBean implements Serializable {

	private static final long serialVersionUID = 138952619791500473L;

	protected static Logger logger = Logger.getLogger(UsersBean.class);

	@Autowired private UserManager userManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private EmailSenderManager emailSenderManager;
	@Autowired private UserEntityESService userEntityESService;

	private UserData formData;

	private List<UserData> users;
	private boolean editMode;
	
	private UserData userToDelete;

	// used for search
	private List<UserData> filteredUsers;
	private String keyword;

	@PostConstruct
	public void init() {
		logger.debug("initializing");
		resetFormData();
		loadUsers();
	}

	public void delete() {
		if (userToDelete != null) {
			try {
				User user = userManager.loadResource(User.class, this.userToDelete.getId());
				user.setDeleted(true);
				userManager.saveEntity(user);
				
				resetFormData();
				userEntityESService.deleteNodeFromES(user);
				loadUsers();
				
				PageUtil.fireSuccessfulInfoMessage("User " + userToDelete.getName()+" "+userToDelete.getLastName()+" is deleted.");
				
				userToDelete = null;
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
	}

	public void validatePassword(FacesContext context, UIComponent component, Object value) {
		boolean isValid;
		boolean isChangePassword = false;

		String reenterpassword = value != null ? value.toString() : "";

		if (this.editMode) {
			HtmlSelectBooleanCheckbox changePass = (HtmlSelectBooleanCheckbox) component.findComponent("changepassword");
			isChangePassword = changePass != null ? (Boolean) changePass
					.getLocalValue() : true;
		}

		String password = ((UIInput) component.findComponent("password")).getLocalValue().toString();

		if (this.editMode) {
			isValid = !isChangePassword ? true : password.equals(reenterpassword);
		} else {
			isValid = password.equals(reenterpassword);
		}

		if (isValid && (password.isEmpty() || reenterpassword.isEmpty())) {
			FacesMessage message = new FacesMessage(
					"Please enter password.");
			throw new ValidatorException(message);
		} else if (!isValid) {
			FacesMessage message = new FacesMessage(
					"Entered passwords do not match!");
			throw new ValidatorException(message);
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		PageUtil.fireSuccessfulInfoMessage(event.getFile().getFileName()
				+ " is uploaded.");
	}

	public void prepareAdd() {
		this.editMode = false;
		this.resetFormData();
	}

	public void loadUsers() {
		this.users = new ArrayList<UserData>();

		Collection<User> allUsers = userManager.getAllUsers();

		if (allUsers != null && !allUsers.isEmpty()) {
			List<User> list = new ArrayList<User>(allUsers);
			for (User user : list) {
				this.users.add(new UserData(user));
			}
		}
	}

	public void saveNewUser() {
		loggedUser.refreshUser();

		logger.debug("Creating new User for the user " + loggedUser.getUser());

		try {
			User user = userManager.createNewUser(
					formData.getName(), 
					formData.getLastName(), 
					formData.getEmail(),
					true,
					formData.getPassword(), 
					loggedUser.getUser().getOrganization(), 
					formData.getPosition());

			if (formData.isSendEmail()) {
				emailSenderManager.sendEmailAboutNewAccount(user,
						formData.getEmail());
			}

			logger.debug("New User (" + user.getName() + " "
					+ user.getLastname() + ") for the user "
					+ loggedUser.getUser());
			
			try {
				PageUtil.fireSuccessfulInfoMessage(
						ResourceBundleUtil.getMessage(
								"admin.users.created", 
								loggedUser.getLocale(), 
								user.getName() + " " + user.getLastname()));
				
				// this will display the growl message and refresh the data
				Ajax.update("usersForm");
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}

			resetFormData();
			loadUsers();
		} catch (UserAlreadyRegisteredException e) {
			logger.debug(e);
		} catch (EventException e) {
			logger.debug(e);
		} catch (FileNotFoundException e) {
			logger.debug(e);
		} catch (IOException e) {
			logger.debug(e);
		}
	}

	public void prepareEdit(UserData user) {
		this.editMode = true;
		this.setFormData(user);
	}
	
	public void updateUser() {
		loggedUser.refreshUser();
		logger.debug("Updating User " + formData.getId() + " by the user "
				+ loggedUser.getUser());

		try {
			User user = userManager.updateUser(
				this.formData.getId(),
				this.formData.getName(),
				this.formData.getLastName(),
				this.formData.getEmail(),
				true,
				this.formData.isChangePassword(),
				this.formData.getPassword(),
				this.formData.getPosition());

			logger.debug("User (" + user.getId() + ") updated by the user "
					+ loggedUser.getUser());

			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
					"admin.users.updated", 
					loggedUser.getLocale()));

			// this will display the growl message and refresh the data
			Ajax.update("usersForm");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		resetFormData();
		loadUsers();
	}

	public void resetFormData() {
		this.setFormData(new UserData());
	}

	/*
	 * GETTERS / SETTERS
	 */
	public UserData getFormData() {
		return this.formData;
	}

	public void setFormData(UserData formData) {
		this.formData = formData;
	}

	public List<UserData> getUsers() {
		return this.users;
	}

	public UserData getUserToDelete() {
		return userToDelete;
	}

	public void setUserToDelete(UserData userToDelete) {
		this.userToDelete = userToDelete;
	}

	public List<UserData> getFilteredUsers() {
		return filteredUsers;
	}

	public void setFilteredUsers(List<UserData> filteredUsers) {
		this.filteredUsers = filteredUsers;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
