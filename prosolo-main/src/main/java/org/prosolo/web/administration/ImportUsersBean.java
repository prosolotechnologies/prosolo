package org.prosolo.web.administration;

import com.google.common.base.CharMatcher;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.data.UserImportData;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.validators.EmailValidatorUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@ManagedBean(name = "importUsersBean")
@Component("importUsersBean")
@Scope("view")
public class ImportUsersBean implements Serializable {

	private static final long serialVersionUID = 4650744941513663908L;

	private static Logger logger = Logger.getLogger(ImportUsersBean.class.getName());

	@Inject private UserManager userManager;
	@Inject private LoggedUserBean loggedUser;

	private UploadedFile file;
	private boolean fileContentValid;
	private List<UserImportData> users;

	private List<String> usersNotImported;
	private int numberOfUsersSuccessfullyImported;
	private boolean importFinished;

	public void init() {
		resetData();
	}

	private void resetData() {
		this.file = null;
		this.fileContentValid = false;
		this.users = new ArrayList<>();
		this.usersNotImported = new ArrayList<>();
		this.numberOfUsersSuccessfullyImported = 0;
		this.importFinished = false;
	}

	public void handleFileUpload(FileUploadEvent event) {
		resetData();

		file = event.getFile();

		if(isFileTypeValid()) {
			parseAndValidateImportedData();
		}
	}

	private void parseAndValidateImportedData() {
		try (
				InputStream inputStream = file.getInputstream();
				BufferedReader bReader = new BufferedReader(new InputStreamReader(
						inputStream, "UTF-8"))) {
			String line;
			while ((line = bReader.readLine()) != null) {
				String[] user = line.split(",");
				if (user.length < 3 || user.length > 4) {
					logger.error("User data format is wrong - " + line);
					fileContentValid = false;
					return;
				} else {
					String email = CharMatcher.WHITESPACE.trimFrom(user[0]);
					if (!EmailValidatorUtil.isValid(email)) {
						fileContentValid = false;
						return;
					}
					String firstName = CharMatcher.WHITESPACE.trimFrom(user[1]);
					String lastName = CharMatcher.WHITESPACE.trimFrom(user[2]);
					String position = null;
					if (user.length == 4) {
						position = CharMatcher.WHITESPACE.trimFrom(user[3]);
					}

					users.add(new UserImportData(email, firstName, lastName, position));
				}
			}
			fileContentValid = true;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void importUsersToGroup(long organizationId, long unitId, long roleId, long groupId) {
		importUsers(organizationId, unitId, roleId, groupId);
	}

	public void importUsersToUnit(long organizationId, long unitId, long roleId) {
		importUsers(organizationId, unitId, roleId, 0);
	}

	private void importUsers(long organizationId, long unitId, long roleId, long groupId) {
		if (isFileValid()) {
			//reset old data before import
			this.usersNotImported = new ArrayList<>();
			this.numberOfUsersSuccessfullyImported = 0;
			this.importFinished = false;

			logger.info("Import users from file '" + file.getFileName() + "'");
			for (UserImportData user : users) {
				try {
					logger.info("Import user - email: " + user.getEmail()
							+ " , first name: " + user.getFirstName() + " , last name: "
							+ user.getLastName() + " , position: " + user.getPosition());

					boolean importSuccessful = userManager.createNewUserAndConnectToResources(
							organizationId, user.getFirstName(), user.getLastName(), user.getEmail(),
							null, user.getPosition(), unitId, roleId, groupId,
							PageUtil.extractLearningContextData(), loggedUser.getUserId());

					if (!importSuccessful) {
						usersNotImported.add(getUserCSV(user));
					} else {
						numberOfUsersSuccessfullyImported++;
					}
				} catch (DbConnectionException e) {
					logger.error("User not imported", e);
					usersNotImported.add(getUserCSV(user));
				} catch (EventException e) {
					logger.error(e);
				}
			}
			importFinished = true;
		}
	}

	private String getUserCSV(UserImportData user) {
		return user.getEmail() + ", " + user.getFirstName() + ", " +
			   user.getLastName() + ", "
			   + (user.getPosition() != null ? user.getPosition() : "");
	}

	public boolean isFileTypeValid() {
		return file != null ? file.getFileName().endsWith(".csv") : false;
	}

	public boolean isFileContentValid() {
		return fileContentValid;
	}

	public boolean isFileValid() {
		return isFileTypeValid() && isFileContentValid();
	}
	/*
	 * GETTERS / SETTERS
	 */
	public UploadedFile getFile() {
		return file;
	}

	public List<String> getUsersNotImported() {
		return usersNotImported;
	}

	public int getNumberOfUsersSuccessfullyImported() {
		return numberOfUsersSuccessfullyImported;
	}

	public boolean isImportFinished() {
		return importFinished;
	}
}
