package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.email.EmailSenderManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.*;

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
					String rolePosition = userRow[5];

					if (!timestamp.equals("Timestamp")) {
						User user = null;
						try {
							user = ServiceLocator
									.getInstance()
									.getService(UserManager.class)
									.createNewUser(0, firstName, lastName, emailAddress, true, "pass", rolePosition, null, null, null, false);

							emailSenderManager.sendEmailAboutNewAccount(user, emailAddress);

							noUsersCreated++;
						} catch (IllegalDataStateException e) {
							logger.error(e);
						}
					}
				}
			} catch (IOException e) {
				logger.error(e);
			}

			PageUtil.fireSuccessfulInfoMessage("The file "+event.getFile().getFileName()+" has been uploaded. Created "+noUsersCreated+" users, but there was a problem creating "+noUsersDidntCreated+" users.");
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
