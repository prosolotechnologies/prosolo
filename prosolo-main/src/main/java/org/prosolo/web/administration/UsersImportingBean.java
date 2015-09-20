package org.prosolo.web.administration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.email.EmailSenderManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic 2013-10-06
 * 
 */
@ManagedBean(name = "usersImporting")
@Component("usersImporting")
@Scope("view")
public class UsersImportingBean implements Serializable {
	
	private static final long serialVersionUID = -5786790275116348611L;

	private static Logger logger = Logger.getLogger(UsersImportingBean.class.getName());
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private EmailSenderManager emailSenderManager;
	
	private UploadedFile file;

	public void handleFileUpload(FileUploadEvent event) {
		logger.debug("Importing user data from file '"+event.getFile().getFileName()+"' and creating accounts.");
		
		if (event.getFile() != null) {
			setFile(event.getFile());
			
			int noUsersCreated = 0;
			int noUsersDidntCreated = 0;
			
			try {
				InputStream inputStream = file.getInputstream();
				BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				Organization org = ServiceLocator.getInstance().getService(OrganizationManager.class).lookupDefaultOrganization();
				String line = "";

				while ((line = bReader.readLine()) != null) {

					// use comma as separator
					String[] userRow = line.split(",");

					logger.debug("Creating user account for user: timestamp= " + userRow[0]
							+ " , First name=" + userRow[1] + " , Last name="
							+ userRow[2] + " , Affiliation=" + userRow[3]
							+ " , Email address=" + userRow[4]
							+ " , Role/position=" + userRow[5]
							+ " , Level of education=" + userRow[6] + "]");
					
					String timestamp = userRow[0];
					String firstName = userRow[1];
					String lastName = userRow[2];
//					String affiliation = userRow[3];
					String emailAddress = userRow[4];
//					String fakeEmail="prosolo.2013@gmail.com"; 
					String rolePosition = userRow[5];
//					String levelOfEducation = userRow[6];
					
					if (!timestamp.equals("Timestamp")) {
						try {
							User user = ServiceLocator
									.getInstance()
									.getService(UserManager.class)
									.createNewUser(firstName, lastName, emailAddress, true, "pass", org, rolePosition);
							
							emailSenderManager.sendEmailAboutNewAccount(user, emailAddress);
							
							noUsersCreated++;
						} catch (UserAlreadyRegisteredException e) {
							logger.error(e);
							noUsersDidntCreated++;
						} catch (EventException e) {
							logger.error(e);
							noUsersDidntCreated++;
						}
					}
				}
			} catch (IOException e) {
				logger.error(e);
			}

			try {
				PageUtil.fireSuccessfulInfoMessage(
						ResourceBundleUtil.getMessage(
								"admin.users.importing.growl", 
								loggedUser.getLocale(), 
								event.getFile().getFileName(),
								noUsersCreated,
								noUsersDidntCreated));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

}
